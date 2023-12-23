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
package dev.cosgy.textToSpeak.settings

import com.jagrosh.jdautilities.command.GuildSettingsManager
import dev.cosgy.textToSpeak.utils.OtherUtil
import net.dv8tion.jda.api.entities.Guild
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.util.function.Consumer

class SettingsManager : GuildSettingsManager<Any?> {
    private val settings: HashMap<Long, Settings> = HashMap()

    init {
        try {
            val loadedSettings = JSONObject(String(Files.readAllBytes(OtherUtil.getPath("serversettings.json"))))
            loadedSettings.keySet().forEach { id ->
                val value = loadedSettings.getJSONObject(id)
                settings[id.toLong()] = Settings(
                    this,
                    if (value.has("text_channel_id")) value.getString("text_channel_id") else null,
                    if (value.has("prefix")) value.getString("prefix") else null,
                    if (value.has("volume")) value.getInt("volume") else 50,
                    value.has("read_name") && value.getBoolean("read_name"),
                    value.has("join_and_leave_read") && value.getBoolean("join_and_leave_read"),
                    value.has("read_nic") && value.getBoolean("read_nic")
                )
            }
        } catch (e: IOException) {
            LoggerFactory.getLogger("Settings")
                .warn("サーバー設定を読み込めませんでした(まだ設定がない場合は正常です): $e")
        } catch (e: JSONException) {
            LoggerFactory.getLogger("Settings")
                .warn("サーバー設定を読み込めませんでした(まだ設定がない場合は正常です): $e")
        }
    }

    override fun getSettings(guild: Guild): Settings {
        return getSettings(guild.idLong)
    }

    fun getSettings(guildId: Long): Settings {
        return settings.computeIfAbsent(guildId) { _: Long? -> createDefaultSettings() }
    }

    /**
     * デフォルト設定のデータを作って返す。
     *
     * @return 作成されたデフォルト設定
     */
    private fun createDefaultSettings(): Settings {
        return Settings(this, 0, null, 50, readName = false, joinAndLeaveRead = false, readNic = false)
    }

    /**
     * 設定をファイルに書き込む
     */
    fun writeSettings() {
        val obj = JSONObject()
        settings.keys.forEach(Consumer { key: Long ->
            val o = JSONObject()
            val s = settings[key]
            if (s!!.textId != 0L) o.put("text_channel_id", s.textId.toString())
            if (s.prefix != null) o.put("prefix", s.prefix)
            if (s.volume != 50) o.put("volume", s.volume)
            if (s.isReadName()) o.put("read_name", s.isReadName())
            if (s.isJoinAndLeaveRead()) o.put("join_and_leave_read", s.isJoinAndLeaveRead())
            if (s.isReadNic()) o.put("read_nic", s.isReadNic())
            obj.put(key.toString(), o)
        })
        try {
            Files.write(OtherUtil.getPath("serversettings.json"), obj.toString(4).toByteArray())
        } catch (ex: IOException) {
            LoggerFactory.getLogger("Settings").warn("ファイルへの書き込みに失敗しました： $ex")
        }
    }
}