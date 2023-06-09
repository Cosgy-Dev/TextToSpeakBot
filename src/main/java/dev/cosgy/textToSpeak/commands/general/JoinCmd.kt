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
package dev.cosgy.textToSpeak.commands.general

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent
import dev.cosgy.textToSpeak.Bot
import dev.cosgy.textToSpeak.settings.Settings
import dev.cosgy.textToSpeak.utils.ReadChannel
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.exceptions.PermissionException
import java.awt.Color

class JoinCmd(private var bot: Bot) : SlashCommand() {
    init {
        name = "join"
        help = "ボイスチャンネルに参加します。"
        guildOnly = true
    }

    override fun execute(event: SlashCommandEvent) {
        event.deferReply().queue()
        val settings = event.client.getSettingsFor<Settings>(event.guild)
        val channel = settings.getTextChannel(event.guild)
        // VoiceChannel voiceChannel = settings.getVoiceChannel(event.getGuild());
        bot.playerManager.setUpHandler(event.guild!!)
        val userState = event.member!!.voiceState
        val builder = EmbedBuilder()
        builder.setColor(Color(76, 108, 179))
        builder.setTitle("VCに接続")
        if (!userState!!.inAudioChannel() || userState.isDeafened) {
            builder.setDescription(String.format("このコマンドを使用するには、%sに参加している必要があります。", "音声チャンネル"))
            event.replyEmbeds(builder.build()).queue()
            return
        }
        if (channel == null) {
            builder.addField("読み上げ対象", event.channel.name, true)
        } else {
            builder.addField("読み上げ対象", channel.name, true)
        }
        try {
            event.guild!!.audioManager.openAudioConnection(userState.channel)
            builder.addField("ボイスチャンネル", String.format("**%s**", userState.channel!!.name), false)
            builder.setDescription("ボイスチャンネルへの接続に成功しました。")
            event.hook.sendMessageEmbeds(builder.build()).queue()
            ReadChannel.setChannel(event.guild!!.idLong, event.textChannel.idLong)
        } catch (ex: PermissionException) {
            builder.appendDescription(event.client.error + String.format("**%s**に接続できません!", userState.channel!!.name))
            builder.addField(
                "ボイスチャンネル", event.client.error + String.format(
                    "**%s**に接続できません!",
                    userState.channel!!.name
                ), false
            )
            event.hook.sendMessageEmbeds(builder.build()).queue()
        }
    }

    override fun execute(event: CommandEvent) {
        val settings = event.client.getSettingsFor<Settings>(event.guild)
        val channel = settings.getTextChannel(event.guild)
        bot.playerManager.setUpHandler(event.guild)
        val userState = event.member.voiceState
        val builder = EmbedBuilder()
        builder.setColor(Color(76, 108, 179))
        builder.setTitle("VCに接続")
        if (!userState!!.inAudioChannel()) {
            builder.setDescription("このコマンドを使用するには、ボイスチャンネルに参加している必要があります。")
            event.reply(builder.build())
            return
        }
        if (channel == null) {
            builder.addField("読み上げ対象", event.channel.name, true)
        } else {
            builder.addField("読み上げ対象", channel.name, true)
        }
        try {
            event.guild.audioManager.openAudioConnection(userState.channel)
            builder.addField("ボイスチャンネル", String.format("**%s**", userState.channel!!.name), false)
            builder.setDescription("ボイスチャンネルへの接続に成功しました。")
            event.reply(builder.build())
            ReadChannel.setChannel(event.guild.idLong, event.textChannel.idLong)
        } catch (ex: PermissionException) {
            builder.setDescription("ボイスチャンネルへの接続に失敗しました。")
            event.reply(builder.build())
        }
    }
}