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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.cosgy.textToSpeak.audio.AudioHandler
import dev.cosgy.textToSpeak.audio.QueuedTrack
import dev.cosgy.textToSpeak.utils.OtherUtil
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class Listener(private val bot: Bot) : ListenerAdapter() {
    var log: Logger = LoggerFactory.getLogger(this.javaClass)
    override fun onReady(event: ReadyEvent) {
        if (event.jda.guilds.isEmpty()) {
            val log = LoggerFactory.getLogger("TTSBot")
            log.warn("このボットはグループに入っていません！ボットをあなたのグループに追加するには、以下のリンクを使用してください。")
            log.warn(event.jda.getInviteUrl(*TextToSpeak.RECOMMENDED_PERMS))
        }
        if (bot.config.useUpdateAlerts()) {
            bot.threadpool.scheduleWithFixedDelay({
                val owner = bot.jda?.getUserById(bot.config.ownerId)
                if (owner != null) {
                    val currentVersion = OtherUtil.currentVersion
                    // 現在のバージョンがリリース版かを確認
                    if (!OtherUtil.isBetaVersion(currentVersion)) {
                        // リリースバージョンの場合
                        val latestVersion = OtherUtil.latestVersion
                        if (latestVersion != null && !currentVersion.equals(
                                latestVersion,
                                ignoreCase = true
                            ) && TextToSpeak.CHECK_UPDATE
                        ) {
                            val msg = String.format(OtherUtil.NEW_VERSION_AVAILABLE, currentVersion, latestVersion)
                            owner.openPrivateChannel().queue { pc: PrivateChannel -> pc.sendMessage(msg).queue() }
                        }
                    }else{
                        // ベータバージョンの場合
                        val latestBeta = OtherUtil.latestBetaVersion
                        if(latestBeta != null && OtherUtil.compareVersions(currentVersion, latestBeta) != 0){
                            val msg = String.format(
                                OtherUtil.NEW_BETA_VERSION_AVAILABLE, currentVersion,
                                OtherUtil.latestBetaVersion,
                                OtherUtil.latestBetaVersion
                            )
                            owner.openPrivateChannel().queue { pc: PrivateChannel -> pc.sendMessage(msg).queue() }
                        }
                    }
                }
            }, 0, 24, TimeUnit.HOURS)
        }
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val botMember = event.guild.selfMember
        val settings = bot.settingsManager.getSettings(event.guild)
        if (event.channelLeft != null) {
            if (settings.isJoinAndLeaveRead() && Objects.requireNonNull(event.guild.selfMember.voiceState)?.channel === event.channelLeft && event.channelLeft!!.members.size > 1) {
                val file: String? = try {
                    bot.voiceCreation.createVoice(
                        event.guild,
                        event.member.user,
                        "${if(settings.isReadNic()) event.member.user.effectiveName else event.member.user.name}がボイスチャンネルから退出しました。"
                    )
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
                bot.playerManager.loadItemOrdered(event.guild, file, ResultHandler(event))
            }
            if (event.channelLeft!!.members.size == 1 && event.channelLeft!!.members.contains(botMember)) {
                val handler = event.guild.audioManager.sendingHandler as AudioHandler?
                handler!!.queue.clear()
                try {
                    bot.voiceCreation.clearGuildFolder(event.guild)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        }
        if (event.channelJoined != null) {
            if (settings.isJoinAndLeaveRead() && Objects.requireNonNull(event.guild.selfMember.voiceState)?.channel === event.channelJoined) {
                val file: String? = try {
                    bot.voiceCreation.createVoice(
                        event.guild,
                        event.member.user,
                        "${if(settings.isReadNic()) event.member.user.effectiveName else event.member.user.name}がボイスチャンネルに参加しました。"
                    )
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
                bot.playerManager.loadItemOrdered(event.guild, file, ResultHandler(event))
            }
        }
    }

    override fun onShutdown(event: ShutdownEvent) {
        bot.shutdown()
    }

    private inner class ResultHandler(private val event: GuildVoiceUpdateEvent) : AudioLoadResultHandler {
        private fun loadSingle(track: AudioTrack) {
            val handler = event.guild.audioManager.sendingHandler as AudioHandler?
            handler!!.addTrack(QueuedTrack(track, event.member.user))
        }

        override fun trackLoaded(track: AudioTrack) {
            loadSingle(track)
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {}
        override fun noMatches() {}
        override fun loadFailed(throwable: FriendlyException) {}
    }
}