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

import dev.cosgy.textToSpeak.Bot
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class AloneInVoiceHandler(private val bot: Bot) {
    private val aloneSince = ConcurrentHashMap<Long, Instant>()
    private var aloneTimeUntilStop: Long = 0
    fun init() {
        aloneTimeUntilStop = bot.config.aloneTimeUntilStop
        if (aloneTimeUntilStop > 0) bot.threadpool.scheduleWithFixedDelay({ this.check() }, 0, 5, TimeUnit.SECONDS)
    }

    private fun check() {
        val now = Instant.now().epochSecond
        aloneSince.entries.removeAll { (key, value) ->
            if (value.epochSecond > now - aloneTimeUntilStop) return@removeAll false
            val guild = bot.jda?.getGuildById(key)
            if (guild != null) {
                (guild.audioManager.sendingHandler as? AudioHandler)?.stopAndClear()
                guild.audioManager.closeAudioConnection()
            }
            true
        }
    }

    fun onVoiceUpdate(event: GuildVoiceUpdateEvent) {
        if (aloneTimeUntilStop <= 0) return
        val guild = event.entity.guild
        if (!bot.playerManager.hasHandler(guild)) return
        val alone = isAlone(guild)
        val inList = aloneSince.containsKey(guild.idLong)
        if (!alone && inList) aloneSince.remove(guild.idLong) else if (alone && !inList) aloneSince[guild.idLong] =
            Instant.now()
    }

    private fun isAlone(guild: Guild): Boolean {
        val channel = guild.audioManager.connectedChannel ?: return false
        return channel.members.none { member ->
            member.voiceState?.isDeafened == false && !member.user.isBot
        }
    }
}