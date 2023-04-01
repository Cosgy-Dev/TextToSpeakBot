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
package dev.cosgy.textToSpeak.audio

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dev.cosgy.textToSpeak.Bot
import net.dv8tion.jda.api.entities.Guild
import org.slf4j.LoggerFactory

class PlayerManager(val bot: Bot) : DefaultAudioPlayerManager() {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    fun init() {
        AudioSourceManagers.registerLocalSource(this)
        if (configuration.opusEncodingQuality != 10) {
            logger.debug("OpusEncodingQuality は、" + configuration.opusEncodingQuality + "(< 10)" + ", 品質を10に設定します。")
            configuration.opusEncodingQuality = 10
        }
        if (configuration.resamplingQuality != AudioConfiguration.ResamplingQuality.HIGH) {
            logger.debug("ResamplingQuality は " + configuration.resamplingQuality.name + "(HIGHではない), 品質をHIGHに設定します。")
            configuration.resamplingQuality = AudioConfiguration.ResamplingQuality.HIGH
        }
    }

    fun hasHandler(guild: Guild): Boolean {
        return guild.audioManager.sendingHandler != null
    }

    fun setUpHandler(guild: Guild): AudioHandler? {
        val handler: AudioHandler?
        if (guild.audioManager.sendingHandler == null) {
            val player = createPlayer()
            player.volume = bot.settingsManager.getSettings(guild)!!.volume
            handler = AudioHandler(guild, player)
            player.addListener(handler)
            guild.audioManager.sendingHandler = handler
        } else handler = guild.audioManager.sendingHandler as AudioHandler?
        return handler
    }
}