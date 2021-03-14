////////////////////////////////////////////////////////////////////////////////
//  Copyright 2021 Cosgy Dev                                                   /
//                                                                             /
//     Licensed under the Apache License, Version 2.0 (the "License");         /
//     you may not use this file except in compliance with the License.        /
//     You may obtain a copy of the License at                                 /
//                                                                             /
//        http://www.apache.org/licenses/LICENSE-2.0                           /
//                                                                             /
//     Unless required by applicable law or agreed to in writing, software     /
//     distributed under the License is distributed on an "AS IS" BASIS,       /
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied./
//     See the License for the specific language governing permissions and     /
//     limitations under the License.                                          /
////////////////////////////////////////////////////////////////////////////////

package dev.cosgy.TextToSpeak.settings;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import dev.cosgy.TextToSpeak.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class SettingsManager implements GuildSettingsManager {
    private final HashMap<Long, Settings> settings;

    public SettingsManager() {
        this.settings = new HashMap<>();
        try {
            JSONObject loadedSettings = new JSONObject(new String(Files.readAllBytes(OtherUtil.getPath("serversettings.json"))));
            loadedSettings.keySet().forEach((id) -> {
                JSONObject o = loadedSettings.getJSONObject(id);
                settings.put(Long.parseLong(id), new Settings(this,
                        o.has("text_channel_id") ? o.getString("text_channel_id") : null,
                        o.has("prefix") ? o.getString("prefix") : null,
                        o.has("volume") ? o.getInt("volume") : 50
                        ));
            });
        } catch (IOException | JSONException e) {
            LoggerFactory.getLogger("Settings").warn("サーバー設定を読み込めませんでした(まだ設定がない場合は正常です): " + e);
        }
    }

    @Override
    public Settings getSettings(Guild guild) {
        return getSettings(guild.getIdLong());
    }

    public Settings getSettings(long guildId) {
        return settings.computeIfAbsent(guildId, id -> createDefaultSettings());
    }

    /**
     * デフォルト設定のデータを作って返す。
     * @return 作成されたデフォルト設定
     */
    private Settings createDefaultSettings() {
        return new Settings(this, 0, null, 50);
    }

    /**
     * 設定をファイルに書き込む
     */
    protected void writeSettings() {
        JSONObject obj = new JSONObject();
        settings.keySet().forEach(key -> {
            JSONObject o = new JSONObject();
            Settings s = settings.get(key);
            if (s.textId != 0)
                o.put("text_channel_id", Long.toString(s.textId));
            if (s.getPrefix() != null)
                o.put("prefix", s.getPrefix());
            if (s.getVolume() != 50)
                o.put("volume", s.getVolume());
            obj.put(Long.toString(key), o);
        });
        try {
            Files.write(OtherUtil.getPath("serversettings.json"), obj.toString(4).getBytes());
        } catch (IOException ex) {
            LoggerFactory.getLogger("Settings").warn("ファイルへの書き込みに失敗しました： " + ex);
        }
    }
}
