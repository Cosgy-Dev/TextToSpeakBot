//////////////////////////////////////////////////////////////////////////////////////////
//  Copyright 2023 Cosgy Dev                                                             /
//                                                                                       /
//     Licensed under the Apache License, Version 2.0 (the "License");                   /
//     you may not use this file except in compliance with the License.                  /
//     You may obtain a copy of the License at                                           /
//                                                                                       /
//        http://www.apache.org/licenses/LICENSE-2.0                                     /
//                                                                                       /
//     Unless required by applicable law or agreed to in writing, software               /
//     distributed under the License is distributed on an "AS IS" BASIS,                 /
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.          /
//     See the License for the specific language governing permissions and               /
//     limitations under the License.                                                    /
//////////////////////////////////////////////////////////////////////////////////////////

package dev.cosgy.TextToSpeak.utils;

import dev.cosgy.TextToSpeak.TextToSpeak;
import dev.cosgy.TextToSpeak.entities.Prompt;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OtherUtil {
    public final static String NEW_VERSION_AVAILABLE = "利用可能な新しいバージョンがあります!\n"
            + "現在のバージョン: %s\n"
            + "最新のバージョン: %s\n\n"
            + " https://github.com/Cosgy-Dev/TextToSpeakBot/releases/latest から最新バージョンをダウンロードして下さい。";
    private final static String WINDOWS_INVALID_PATH = "c:\\windows\\system32\\";

    public static Path getPath(String path) {
        // special logic to prevent trying to access system32
        if (path.toLowerCase().startsWith(WINDOWS_INVALID_PATH)) {
            String filename = path.substring(WINDOWS_INVALID_PATH.length());
            try {
                path = new File(TextToSpeak.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath() + File.separator + filename;
            } catch (URISyntaxException ignored) {
            }
        }
        return Paths.get(path);
    }

    /**
     * jarからリソースを文字列としてロードします
     *
     * @param clazz クラスベースオブジェクト
     * @param name  リソースの名前
     * @return リソースの内容を含む文字列
     */
    public static String loadResource(Object clazz, String name) {
        try {
            return readString(clazz.getClass().getResourceAsStream(name));
        } catch (Exception ex) {
            return null;
        }
    }

    public static String readString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream into = new ByteArrayOutputStream();
        byte[] buf = new byte[32768];
        for (int n; 0 < (n = inputStream.read(buf)); ) {
            into.write(buf, 0, n);
        }
        into.close();
        return into.toString("utf-8");
    }

    /**
     * URLから画像データをロード
     *
     * @param url 画像のURL
     * @return URLのinputstream
     */
    public static InputStream imageFromUrl(String url) {
        if (url == null)
            return null;
        try {
            URL u = new URL(url);
            URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36");
            return urlConnection.getInputStream();
        } catch (IOException | IllegalArgumentException ignore) {
        }
        return null;
    }

    /**
     * 文字列からアクティビティを解析
     *
     * @param game the game, including the action such as 'playing' or 'watching'
     * @return the parsed activity
     */
    public static Activity parseGame(String game) {
        if (game == null || game.trim().isEmpty() || game.trim().equalsIgnoreCase("default"))
            return null;
        String lower = game.toLowerCase();
        if (lower.startsWith("playing"))
            return Activity.playing(makeNonEmpty(game.substring(7).trim()));
        if (lower.startsWith("listening to"))
            return Activity.listening(makeNonEmpty(game.substring(12).trim()));
        if (lower.startsWith("listening"))
            return Activity.listening(makeNonEmpty(game.substring(9).trim()));
        if (lower.startsWith("watching"))
            return Activity.watching(makeNonEmpty(game.substring(8).trim()));
        if (lower.startsWith("streaming")) {
            String[] parts = game.substring(9).trim().split("\\s+", 2);
            if (parts.length == 2) {
                return Activity.streaming(makeNonEmpty(parts[1]), "https://twitch.tv/" + parts[0]);
            }
        }
        return Activity.playing(game);
    }

    public static String makeNonEmpty(String str) {
        return str == null || str.isEmpty() ? "\u200B" : str;
    }

    public static OnlineStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty())
            return OnlineStatus.ONLINE;
        OnlineStatus st = OnlineStatus.fromKey(status);
        return st == null ? OnlineStatus.ONLINE : st;
    }

    public static String checkVersion(Prompt prompt) {
        // Get current version number
        String version = getCurrentVersion();

        // Check for new version
        String latestVersion = getLatestVersion();

        if (latestVersion != null && !latestVersion.equals(version) && TextToSpeak.CHECK_UPDATE) {
            prompt.alert(Prompt.Level.WARNING, "Version", String.format(NEW_VERSION_AVAILABLE, version, latestVersion));
        }

        // Return the current version
        return version;
    }

    public static String getCurrentVersion() {
        if (TextToSpeak.class.getPackage() != null && TextToSpeak.class.getPackage().getImplementationVersion() != null)
            return TextToSpeak.class.getPackage().getImplementationVersion();
        else
            return "不明";
    }

    public static String getLatestVersion() {
        try {
            Response response = new OkHttpClient.Builder().build()
                    .newCall(new Request.Builder().get().url("https://api.github.com/repos/Cosgy-Dev/TextToSpeakBot/releases/latest").build())
                    .execute();
            ResponseBody body = response.body();
            if (body != null) {
                try (Reader reader = body.charStream()) {
                    JSONObject obj = new JSONObject(new JSONTokener(reader));
                    return obj.getString("tag_name");
                } finally {
                    response.close();
                }
            } else
                return null;
        } catch (IOException | JSONException | NullPointerException ex) {
            return null;
        }
    }
}
