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
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class AloneInVoiceHandler(private val bot: Bot) {
    private val aloneSince = HashMap<Long, Instant>()
    private var aloneTimeUntilStop: Long = 0
    fun init() {
        aloneTimeUntilStop = bot.config.aloneTimeUntilStop
        if (aloneTimeUntilStop > 0) bot.threadpool.scheduleWithFixedDelay({ this.check() }, 0, 5, TimeUnit.SECONDS)
    }

    private fun check() {
        val toRemove: MutableSet<Long> = HashSet()
        for ((key, value) in aloneSince) {
            if (value.epochSecond > Instant.now().epochSecond - aloneTimeUntilStop) continue
            val guild = bot.jda?.getGuildById(key)
            if (guild == null) {
                toRemove.add(key)
                continue
            }
            (guild.audioManager.sendingHandler as AudioHandler?)!!.stopAndClear()
            guild.audioManager.closeAudioConnection()
            toRemove.add(key)
        }
        toRemove.forEach(Consumer { key: Long -> aloneSince.remove(key) })
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
        return if (guild.audioManager.connectedChannel == null) false else guild.audioManager.connectedChannel!!.members.stream()
            .noneMatch { x: Member ->
                (!x.voiceState!!.isDeafened
                        && !x.user.isBot)
            }
    }
}