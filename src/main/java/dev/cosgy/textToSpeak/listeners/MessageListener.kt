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
package dev.cosgy.textToSpeak.listeners

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.cosgy.textToSpeak.Bot
import dev.cosgy.textToSpeak.audio.AudioHandler
import dev.cosgy.textToSpeak.audio.QueuedTrack
import dev.cosgy.textToSpeak.utils.ReadChannel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.IOException

class MessageListener(private val bot: Bot) : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val startTime = System.currentTimeMillis()
        event.jda
        event.responseNumber

        //イベント固有の情報
        val author = event.author //メッセージを送信したユーザー
        val message = event.message //受信したメッセージ。
        event.channel //メッセージが送信されたMessageChannel
        var msg = message.contentDisplay
        //人間が読める形式のメッセージが返されます。 クライアントに表示されるものと同様。
        val isBot = author.isBot
        //if(Arrays.asList(mentionedUsers).contains())
        //メッセージを送信したユーザーがBOTであるかどうかを判断。
        if (event.isFromType(ChannelType.TEXT)) {
            if (isBot) return
            val guild = event.guild
            val textChannel = event.guildChannel.asTextChannel()
            var settingText = bot.settingsManager.getSettings(event.guild).getTextChannel(event.guild)
            if (!guild.audioManager.isConnected) {
                return
            }
            val prefix = if (bot.config.prefix == "@mention") "@" + event.jda.selfUser.name + " " else bot.config.prefix
            if (prefix?.let { msg.startsWith(it) } == true) {
                return
            }
            if (textChannel !== settingText) {
                if (settingText == null) {
                    settingText = event.guild.getTextChannelById(ReadChannel.getChannel(event.guild.idLong)!!)
                }
            }

            // URLを置き換え
            msg = msg.replace("(http://|https://)[\\w.\\-/:#?=&;%~+]+".toRegex(), "ゆーあーるえる")
            message.getStickers().forEach { sticker -> msg += " " + sticker.getName() }
            if (textChannel === settingText) {
                val settings = bot.settingsManager.getSettings(guild)
                if (settings.isReadName()) {

                    var nic = event.member?.nickname

                    nic = nic ?: author.effectiveName

                    msg = "${if(settings.isReadNic()) nic else author.effectiveName}  " + msg
                }
                val vc = bot.voiceCreation
                val file: String? = try {
                    vc.createVoice(guild, author, msg)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
                bot.playerManager.loadItemOrdered(event.guild, file, ResultHandler(event))

                //textChannel.sendMessage(author.getName() + "が、「"+ msg +"」と送信しました。").queue();
            }
        }

        // 終了時刻を記録
        val endTime = System.currentTimeMillis()

        // 実行時間を計算
        val executionTime = endTime - startTime
    }

    override fun onReady(e: ReadyEvent) {
        bot.readyJDA()
    }

    private class ResultHandler(private val event: MessageReceivedEvent) : AudioLoadResultHandler {
        private fun loadSingle(track: AudioTrack) {
            val handler = event.guild.audioManager.sendingHandler as AudioHandler?
            handler!!.addTrack(QueuedTrack(track, event.author))
        }

        override fun trackLoaded(track: AudioTrack) {
            loadSingle(track)
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {}
        override fun noMatches() {}
        override fun loadFailed(throwable: FriendlyException) {}
    }
}