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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import dev.cosgy.textToSpeak.queue.FairQueue
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import java.nio.ByteBuffer

class AudioHandler(guild: Guild, val player: AudioPlayer) : AudioEventAdapter(), AudioSendHandler {
    val queue = FairQueue<QueuedTrack?>()
    val votes: Set<String> = HashSet()
    private val guildId: Long
    private val stringGuildId: String
    private var lastFrame: AudioFrame? = null

    init {
        guildId = guild.idLong
        stringGuildId = guild.id
    }

    /**
     * 再生キューに追加
     */
    fun addTrack(qtrack: QueuedTrack): Int {
        return if (player.playingTrack == null) {
            player.playTrack(qtrack.track)
            -1
        } else queue.add(qtrack)
    }

    fun stopAndClear() {
        queue.clear()
        player.stopTrack()
    }

    fun isVoiceListening(jda: JDA): Boolean {
        return guild(jda)!!.selfMember.voiceState!!.inAudioChannel() && player.playingTrack != null
    }

    val requester: Long
        get() = if (player.playingTrack == null || player.playingTrack.getUserData(Long::class.java) == null) 0 else player.playingTrack.getUserData(Long::class.java)

    // Audio Events
    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (!queue.isEmpty) {
            val qt = queue.pull()
            player.playTrack(qt!!.track)
        }
    }

    // Audio Send Handler methods
    override fun canProvide(): Boolean {
        lastFrame = player.provide()
        return lastFrame != null
    }

    override fun provide20MsAudio(): ByteBuffer? {
        return ByteBuffer.wrap(lastFrame!!.data)
    }

    override fun isOpus(): Boolean {
        return true
    }

    // Private methods
    private fun guild(jda: JDA): Guild? {
        return jda.getGuildById(guildId)
    }
}