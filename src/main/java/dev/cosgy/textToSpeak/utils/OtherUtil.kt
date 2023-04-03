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
package dev.cosgy.textToSpeak.utils

import dev.cosgy.textToSpeak.TextToSpeak
import dev.cosgy.textToSpeak.entities.Prompt
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object OtherUtil {
    const val NEW_VERSION_AVAILABLE = ("利用可能な新しいバージョンがあります!\n"
            + "現在のバージョン: %s\n"
            + "最新のバージョン: %s\n\n"
            + " https://github.com/Cosgy-Dev/TextToSpeakBot/releases/latest から最新バージョンをダウンロードして下さい。")
    private const val WINDOWS_INVALID_PATH = "c:\\windows\\system32\\"
    fun getPath(path: String): Path {
        // special logic to prevent trying to access system32
        var path = path
        if (path.lowercase(Locale.getDefault()).startsWith(WINDOWS_INVALID_PATH)) {
            val filename = path.substring(WINDOWS_INVALID_PATH.length)
            try {
                path = File(TextToSpeak::class.java.protectionDomain.codeSource.location.toURI()).parentFile.path + File.separator + filename
            } catch (ignored: URISyntaxException) {
            }
        }
        return Paths.get(path)
    }

    /**
     * jarからリソースを文字列としてロードします
     *
     * @param clazz クラスベースオブジェクト
     * @param name  リソースの名前
     * @return リソースの内容を含む文字列
     */
    fun loadResource(clazz: Any, name: String?): String? {
        return try {
            name?.let { clazz.javaClass.getResourceAsStream(it)?.let { readString(it) } }
        } catch (ex: Exception) {
            null
        }
    }

    @Throws(IOException::class)
    fun readString(inputStream: InputStream): String {
        val into = ByteArrayOutputStream()
        val buf = ByteArray(32768)
        var n: Int
        while (0 < inputStream.read(buf).also { n = it }) {
            into.write(buf, 0, n)
        }
        into.close()
        return into.toString("utf-8")
    }

    /**
     * URLから画像データをロード
     *
     * @param url 画像のURL
     * @return URLのinputstream
     */
    fun imageFromUrl(url: String?): InputStream? {
        if (url == null) return null
        try {
            val u = URL(url)
            val urlConnection = u.openConnection()
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36")
            return urlConnection.getInputStream()
        } catch (ignore: IOException) {
        } catch (ignore: IllegalArgumentException) {
        }
        return null
    }

    /**
     * 文字列からアクティビティを解析
     *
     * @param game the game, including the action such as 'playing' or 'watching'
     * @return the parsed activity
     */
    fun parseGame(game: String?): Activity? {
        if (game == null || game.trim { it <= ' ' }.isEmpty() || game.trim { it <= ' ' }.equals("default", ignoreCase = true)) return null
        val lower = game.lowercase(Locale.getDefault())
        if (lower.startsWith("playing")) return Activity.playing(makeNonEmpty(game.substring(7).trim { it <= ' ' }))
        if (lower.startsWith("listening to")) return Activity.listening(makeNonEmpty(game.substring(12).trim { it <= ' ' }))
        if (lower.startsWith("listening")) return Activity.listening(makeNonEmpty(game.substring(9).trim { it <= ' ' }))
        if (lower.startsWith("watching")) return Activity.watching(makeNonEmpty(game.substring(8).trim { it <= ' ' }))
        if (lower.startsWith("streaming")) {
            val parts = game.substring(9).trim { it <= ' ' }.split("\\s+".toRegex(), limit = 2).toTypedArray()
            if (parts.size == 2) {
                return Activity.streaming(makeNonEmpty(parts[1]), "https://twitch.tv/" + parts[0])
            }
        }
        return Activity.playing(game)
    }

    private fun makeNonEmpty(str: String?): String {
        return if (str.isNullOrEmpty()) "\u200B" else str
    }

    fun parseStatus(status: String?): OnlineStatus {
        if (status == null || status.trim { it <= ' ' }.isEmpty()) return OnlineStatus.ONLINE
        val st = OnlineStatus.fromKey(status)
        return st ?: OnlineStatus.ONLINE
    }

    fun checkVersion(prompt: Prompt): String {
        // Get current version number
        val version = currentVersion

        // Check for new version
        val latestVersion = latestVersion
        if (latestVersion != null && latestVersion != version && TextToSpeak.CHECK_UPDATE) {
            prompt.alert(Prompt.Level.WARNING, "Version", String.format(NEW_VERSION_AVAILABLE, version, latestVersion))
        }

        // Return the current version
        return version
    }

    val currentVersion: String
        get() = if (TextToSpeak::class.java.getPackage() != null && TextToSpeak::class.java.getPackage().implementationVersion != null) TextToSpeak::class.java.getPackage().implementationVersion else "不明"
    val latestVersion: String?
        get() {
            try {
                val client = OkHttpClient()

                val request = Request.Builder()
                        .url("https://api.github.com/repos/Cosgy-Dev/TextToSpeakBot/releases/latest")
                        .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (body != null) {
                    try {
                        val obj = JSONObject(body)
                        return obj.getString("tag_name")
                    } catch (e: JSONException) {
                        LoggerFactory.getLogger("Settings").warn("タグ名を解析できませんでした: $e")
                    }
                } else return null
            } catch (ex: IOException) {
                return null
            } catch (ex: JSONException) {
                return null
            } catch (ex: NullPointerException) {
                return null
            }
            return null
        }
}