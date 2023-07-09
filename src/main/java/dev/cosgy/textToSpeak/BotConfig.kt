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
package dev.cosgy.textToSpeak

import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import dev.cosgy.textToSpeak.entities.Prompt
import dev.cosgy.textToSpeak.utils.OtherUtil
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import org.apache.commons.io.FileUtils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.system.exitProcess

class BotConfig(private val prompt: Prompt) {
    private var path: Path? = null
    var token: String? = null
        private set
    var prefix: String? = null
        private set
    private var altprefix: String? = null
    var dictionary: String? = null
        private set
    var voiceDirectory: String? = null
        private set
    var winJTalkDir: String? = null
        private set
    var ownerId: Long = 0
        private set
    var aloneTimeUntilStop: Long = 0
        private set
    var maxMessageCount = 0
        private set
    var status: OnlineStatus? = null
        private set
    var game: Activity? = null
        private set
    private var updatealerts = false
    private var dBots = false
    var helpToDm = false
        private set
    var isValid = false
        private set

    fun load() {
        isValid = false
        try {
            path = OtherUtil.getPath(System.getProperty("config.file", System.getProperty("config", "config.txt")))
            if (path!!.toFile().exists()) {
                if (System.getProperty("config.file") == null) System.setProperty(
                    "config.file",
                    System.getProperty("config", "config.txt")
                )
                ConfigFactory.invalidateCaches()
            }

            // load in the config file, plus the default values
            //Config = ConfigFactory.parseFile(path.toFile()).withFallback(ConfigFactory.load());
            val config = ConfigFactory.load()
            token = config.getString("token")
            prefix = config.getString("prefix")
            altprefix = config.getString("altprefix")
            ownerId = if (config.getAnyRef("owner") is String) 0L else config.getLong("owner")
            game = OtherUtil.parseGame(config.getString("game"))
            status = OtherUtil.parseStatus(config.getString("status"))
            updatealerts = config.getBoolean("updatealerts")
            dictionary = config.getString("dictionary")
            voiceDirectory = config.getString("voiceDirectory")
            aloneTimeUntilStop = config.getLong("alonetimeuntilstop")
            maxMessageCount = config.getInt("maxmessagecount")
            winJTalkDir = config.getString("winjtalkdir")
            helpToDm = config.getBoolean("helptodm")
            dBots = ownerId == 334091398263341056
            var write = false

            // validate bot token
            if (token == null || token!!.isEmpty() || token!!.matches("(BOT_TOKEN_HERE|Botトークンをここに貼り付け)".toRegex())) {
                token = prompt.prompt(
                    """
    BOTトークンを入力してください。
    BOTトークン: 
    """.trimIndent()
                )
                write = if (token == null) {
                    prompt.alert(
                        Prompt.Level.WARNING, CONTEXT, """
     トークンが入力されていません！終了します。
     
     設定ファイルの場所: ${path!!.toAbsolutePath()}
     """.trimIndent()
                    )
                    return
                } else {
                    true
                }
            }

            // validate bot owner
            if (ownerId <= 0) {
                ownerId = try {
                    prompt.prompt(
                        """
                        所有者のユーザーIDが設定されていない、または有効なIDではありません。
                        BOTの所有者のユーザーIDを入力してください。
                        所有者のユーザーID: 
                        """.trimIndent()
                    )!!.toLong()
                } catch (ex: NumberFormatException) {
                    0
                } catch (ex: NullPointerException) {
                    0
                }
                if (ownerId <= 0) {
                    prompt.alert(
                        Prompt.Level.ERROR, CONTEXT,
                        """
                        無効なユーザーIDです！終了します。
                        設定ファイルの場所: ${path!!.toAbsolutePath()}
                        """.trimIndent()
                    )
                    exitProcess(0)
                } else {
                    write = true
                }
            }
            if (write) {
                val original = OtherUtil.loadResource(this, "/reference.conf")
                val mod: String =
                    original?.substring(original.indexOf(START_TOKEN) + START_TOKEN.length, original.indexOf(END_TOKEN))
                        ?.replace("BOT_TOKEN_HERE", token!!)?.replace("Botトークンをここに貼り付け", token!!)
                        ?.replace("0 // OWNER ID", ownerId.toString())
                        ?.replace("所有者IDをここに貼り付け", ownerId.toString())?.trim { it <= ' ' }
                        ?: """
                            token = $token
                            owner = $ownerId
                        """.trimIndent()
                FileUtils.writeStringToFile(path!!.toFile(), mod, StandardCharsets.UTF_8)
            }

            // if we get through the whole config, it's good to go
            isValid = true
        } catch (ex: ConfigException) {
            prompt.alert(
                Prompt.Level.ERROR, CONTEXT, """
                $ex: ${ex.message}
     
                設定ファイルの場所: ${path!!.toAbsolutePath()}
            """.trimIndent()
            )
        } catch (ex: IOException) {
            prompt.alert(
                Prompt.Level.ERROR, CONTEXT, """
                $ex: ${ex.message}
     
                設定ファイルの場所: ${path!!.toAbsolutePath()}
            """.trimIndent()
            )
        }
    }

    val configLocation: String
        get() = path!!.toFile().absolutePath
    val altPrefix: String?
        get() = if ("NONE".equals(altprefix, ignoreCase = true)) null else altprefix

    fun useUpdateAlerts(): Boolean {
        return updatealerts
    }

    companion object {
        private const val CONTEXT = "Config"
        private const val START_TOKEN = "/// START OF YOMIAGEBOT CONFIG ///"
        private const val END_TOKEN = "/// END OF YOMIAGEBOT CONFIG ///"
    }
}