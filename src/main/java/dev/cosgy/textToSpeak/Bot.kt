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

import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import dev.cosgy.textToSpeak.audio.*
import dev.cosgy.textToSpeak.audio.Dictionary
import dev.cosgy.textToSpeak.gui.GUI
import dev.cosgy.textToSpeak.settings.SettingsManager
import dev.cosgy.textToSpeak.settings.UserSettingsManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.system.exitProcess

class Bot(val waiter: EventWaiter, val config: BotConfig, val settingsManager: SettingsManager) {
    val threadpool: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val lang: ResourceBundle = ResourceBundle.getBundle("lang.yomiage", Locale.JAPAN)
    val playerManager: PlayerManager = PlayerManager(this)
    val voiceCreation: VoiceCreation
    val userSettingsManager: UserSettingsManager
    private val aloneInVoiceHandler: AloneInVoiceHandler
    var log: Logger = LoggerFactory.getLogger(this.javaClass)
    var dictionary: Dictionary? = null
        private set
    private var shuttingDown = false
    var jda: JDA? = null
    private var gui: GUI? = null

    init {
        playerManager.init()
        voiceCreation = VoiceCreation(this)
        userSettingsManager = UserSettingsManager()
        aloneInVoiceHandler = AloneInVoiceHandler(this)
        aloneInVoiceHandler.init()
    }

    fun readyJDA() {
        dictionary = Dictionary.getInstance(this)
    }

    fun closeAudioConnection(guildId: Long) {
        val guild = jda!!.getGuildById(guildId)
        if (guild != null) threadpool.submit { guild.audioManager.closeAudioConnection() }
    }

    fun resetGame() {
        val game = if (config.game == null || config.game!!.name.lowercase(Locale.getDefault())
                .matches("(none|なし)".toRegex())
        ) null else config.game
        if (jda!!.presence.activity != game) jda!!.presence.activity = game
    }

    fun shutdown() {
        if (shuttingDown) return
        shuttingDown = true
        if (jda!!.status != JDA.Status.SHUTTING_DOWN) {
            jda!!.guilds.forEach(Consumer { g: Guild ->
                val am = g.audioManager
                if (am.isConnected) {
                    am.closeAudioConnection()
                    val ah = am.sendingHandler as AudioHandler?
                    if (ah != null) {
                        ah.stopAndClear()
                        ah.player.destroy()
                    }
                }
            })

            // Wait for any remaining tasks to complete before shutting down the thread pool
            threadpool.shutdown()
            try {
                if (!threadpool.awaitTermination(10, TimeUnit.SECONDS)) {
                    threadpool.shutdownNow()
                    if (!threadpool.awaitTermination(10, TimeUnit.SECONDS)) {
                        log.warn("Thread pool did not terminate")
                    }
                }
            } catch (e: InterruptedException) {
                threadpool.shutdownNow()
                Thread.currentThread().interrupt()
                log.warn("Thread pool shutdown was interrupted")
            }
        }

        // Shutdown JDA
        jda?.shutdown()
        try {
            if (!jda!!.awaitShutdown(10, TimeUnit.SECONDS)) {
                log.warn("JDA did not shutdown properly")
            }
        } catch (e: InterruptedException) {
            jda!!.shutdownNow()
            Thread.currentThread().interrupt()
            log.warn("JDA shutdown was interrupted")
        }

        dictionary?.close()
        // Delete temporary files
        try {
            FileUtils.cleanDirectory(File("tmp"))
            FileUtils.cleanDirectory(File("wav"))
            log.info("Deleted temporary files")
        } catch (e: IOException) {
            log.warn("Failed to delete temporary files")
        } catch (e: IllegalArgumentException) {
            log.warn("One or more directory paths were invalid.")
        }

        if (gui != null) gui!!.dispose()
        exitProcess(0)
    }


    fun setGUI(gui: GUI?) {
        this.gui = gui
    }

    fun GetLang(): ResourceBundle {
        return lang
    }

    companion object {
        var INSTANCE: Bot? = null
    }
}