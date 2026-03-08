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

import club.minnced.discord.jdave.interop.JDaveSessionFactory
import com.github.lalyos.jfiglet.FigletFont
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import dev.cosgy.textToSpeak.commands.admin.*
import dev.cosgy.textToSpeak.commands.dictionary.AddWordCmd
import dev.cosgy.textToSpeak.commands.dictionary.DlWordCmd
import dev.cosgy.textToSpeak.commands.dictionary.WordListCmd
import dev.cosgy.textToSpeak.commands.general.*
import dev.cosgy.textToSpeak.commands.owner.ShutdownCmd
import dev.cosgy.textToSpeak.entities.Prompt
import dev.cosgy.textToSpeak.framework.command.command.CommandClientBuilder
import dev.cosgy.textToSpeak.framework.command.command.SlashCommand
import dev.cosgy.textToSpeak.framework.command.commons.waiter.EventWaiter
import dev.cosgy.textToSpeak.gui.GUI
import dev.cosgy.textToSpeak.listeners.CommandAudit
import dev.cosgy.textToSpeak.listeners.MessageListener
import dev.cosgy.textToSpeak.settings.SettingsManager
import dev.cosgy.textToSpeak.utils.OtherUtil
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.audio.AudioModuleConfig
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.exceptions.InvalidTokenException
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import sun.misc.Signal
import java.awt.Color
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess

object TextToSpeak {
    val RECOMMENDED_PERMS = arrayOf(
        Permission.VIEW_CHANNEL,
        Permission.MESSAGE_SEND,
        Permission.MESSAGE_HISTORY,
        Permission.MESSAGE_ADD_REACTION,
        Permission.MESSAGE_EMBED_LINKS,
        Permission.MESSAGE_ATTACH_FILES,
        Permission.MESSAGE_MANAGE,
        Permission.MESSAGE_EXT_EMOJI,
        Permission.USE_APPLICATION_COMMANDS,
        Permission.MANAGE_CHANNEL,
        Permission.VOICE_CONNECT,
        Permission.VOICE_SPEAK,
        Permission.NICKNAME_CHANGE
    )
    private val INTENTS = arrayOf(
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_VOICE_STATES,
        GatewayIntent.MESSAGE_CONTENT
    )
    var CHECK_UPDATE = true
    var COMMAND_AUDIT_ENABLED = false

    /**
     * @param args コマンドライン引数
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger("Startup")
        try {
            println("${FigletFont.convertOneLine("TextToSpeak Bot v${OtherUtil.currentVersion}")}\nby Cosgy Dev")
        } catch (ignored: IOException) {
        }
        val prompt = Prompt(
            "TextToSpeak Bot",
            "noguiモードに切り替えます。  -Dnogui=trueフラグを含めると、手動でnoguiモードで起動できます。",
            "true".equals(System.getProperty("nogui", "false"), ignoreCase = true)
        )

        // check deprecated nogui mode (new way of setting it is -Dnogui=true)
        for (arg in args) if ("-nogui".equals(arg, ignoreCase = true)) {
            prompt.alert(
                Prompt.Level.WARNING, "GUI", "-noguiフラグは廃止予定です。 "
                        + "jarの名前の前に-Dnogui = trueフラグを使用してください。 例：java -jar -Dnogui=true JMusicBot.jar"
            )
        } else if ("-nocheckupdates".equals(arg, ignoreCase = true)) {
            CHECK_UPDATE = false
            log.info("アップデートチェックを無効にしました")
        } else if ("-auditcommands".equals(arg, ignoreCase = true)) {
            COMMAND_AUDIT_ENABLED = true
            log.info("実行されたコマンドの記録を有効にしました。")
        }
        val version = OtherUtil.checkVersion(prompt)
        if (!System.getProperty("java.vm.name").contains("64")) prompt.alert(
            Prompt.Level.WARNING,
            "Java Version",
            "サポートされていないJavaバージョンを使用しています。64ビット版のJavaを使用してください。"
        )
        val config = BotConfig(prompt)
        config.load()
        if (!config.isValid) return
        val waiter = EventWaiter()
        val settings = SettingsManager()
        val bot = Bot(waiter, config, settings)
        Bot.INSTANCE = bot
        val aboutCommand = AboutCommand(
            Color.BLUE.brighter(),
            "TextToSpeak Bot by Cosgy Dev(V${version})",
            *RECOMMENDED_PERMS
        )
        aboutCommand.setIsAuthor(false)
        aboutCommand.setReplacementCharacter("🎶")
        val cb = CommandClientBuilder()
            .setPrefix(config.prefix)
            .setAlternativePrefix(config.altPrefix)
            .setOwnerId(config.ownerId.toString())
            .setHelpWord("help")
            .useHelpBuilder(false)
            .setLinkedCacheSize(200)
            .setGuildSettingsManager(settings)
            .setListener(CommandAudit())
        val slashCommandList: ArrayList<SlashCommand?> = object : ArrayList<SlashCommand?>() {
            init {
                add(aboutCommand)
                add(HelpCmd(bot))
                add(JoinCmd(bot))
                add(ByeCmd(bot))
                add(SettingsCmd(bot))
                add(SetVoiceCmd(bot))
                add(SetSpeedCmd(bot))
                add(SetIntonationCmd(bot))
                add(SetVoiceQualityA(bot))
                add(SetVoiceQualityFm(bot))
                add(AddWordCmd(bot))
                add(WordListCmd(bot))
                add(DlWordCmd(bot))
                add(SettcCmd(bot))
                add(TranslateCmd(bot))
                add(SetReadNameCmd(bot))
                add(NicReadCmd(bot))
                add(JLReadCmd(bot))
                add(GuildSettings(bot))
                add(ShutdownCmd(bot))
            }
        }
        cb.addSlashCommands(*slashCommandList.toTypedArray())
        cb.addCommands(*slashCommandList.toTypedArray())
        var nogame = false
        if (config.status != OnlineStatus.UNKNOWN) cb.setStatus(config.status)
        if (config.game == null) cb.setActivity(Activity.playing("/helpでヘルプを確認")) else if (config.game!!.name.lowercase(
                Locale.getDefault()
            ).matches("(none|なし)".toRegex())
        ) {
            cb.setActivity(null)
            nogame = true
        } else cb.setActivity(config.game)
        if (!prompt.isNoGUI) {
            try {
                val gui = GUI(bot)
                bot.setGUI(gui)
                gui.init()
            } catch (e: Exception) {
                log.error(
                    """
                    GUIを開くことができませんでした。次の要因が考えられます:
                    ・サーバー上で実行している。
                    ・GUIがない環境下で実行している。
                    このエラーを非表示にするには、 -Dnogui=true フラグを使用してGUIなしモードで実行してください。
                    """.trimIndent()
                )
            }
        }
        log.info("${config.configLocation}から設定を読み込みました")
        try {
            val jda = JDABuilder.create(config.token, listOf(*INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .disableCache(
                    CacheFlag.ACTIVITY,
                    CacheFlag.CLIENT_STATUS,
                    CacheFlag.EMOJI,
                    CacheFlag.ONLINE_STATUS,
                    CacheFlag.STICKER
                )
                .setActivity(if (nogame) null else Activity.playing("準備中..."))
                .setStatus(if (config.status == OnlineStatus.INVISIBLE || config.status == OnlineStatus.OFFLINE) OnlineStatus.INVISIBLE else OnlineStatus.DO_NOT_DISTURB)
                .addEventListeners(cb.build(), waiter, bot.buttonRouter, Listener(bot), MessageListener(bot))
                .setBulkDeleteSplittingEnabled(true)
                .setAudioModuleConfig(
                    AudioModuleConfig()
                        .withDaveSessionFactory(JDaveSessionFactory())
                        .withAudioSendFactory(NativeAudioSendFactory())
                )
                .build()
            bot.jda = jda
        } catch (ex: InvalidTokenException) {
            prompt.alert(
                Prompt.Level.ERROR, "TextToSpeak Bot",
                """
                ボットトークンでのログインに失敗しました。
                正しいボットトークンが設定されていることを確認してください。(CLIENT SECRET ではありません!)
                設定ファイルの場所：${config.configLocation}
                """.trimIndent()
            )
            exitProcess(1)
        } catch (ex: IllegalArgumentException) {
            prompt.alert(
                Prompt.Level.ERROR, "TextToSpeak Bot",
                """
                設定の一部が無効です：$ex
                設定ファイルの場所：${config.configLocation}
                """.trimIndent()
            )
            exitProcess(1)
        }

        Signal.handle(Signal("INT")) { _ ->
            println("プログラムを終了しています...")
            bot.shutdown()
        }
    }
}
