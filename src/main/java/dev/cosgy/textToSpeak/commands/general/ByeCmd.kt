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
import dev.cosgy.textToSpeak.audio.AudioHandler
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.io.IOException

class ByeCmd(private val bot: Bot) : SlashCommand() {
    init {
        name = "bye"
        help = "ボイスチャンネルから退出します。"
        guildOnly = true
    }

    override fun execute(event: SlashCommandEvent) {
        val handler = event.guild!!.audioManager.sendingHandler as AudioHandler?
        handler!!.stopAndClear()
        try {
            bot.voiceCreation.clearGuildFolder(event.guild!!)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        event.guild!!.audioManager.closeAudioConnection()
        val builder = EmbedBuilder()
        builder.setColor(Color(180, 76, 151))
        builder.setTitle("VCから切断")
        builder.setDescription("ボイスチャンネルから切断しました。")
        event.replyEmbeds(builder.build()).queue()
    }

    override fun execute(event: CommandEvent) {
        val handler = event.guild.audioManager.sendingHandler as AudioHandler?
        handler!!.stopAndClear()
        try {
            bot.voiceCreation.clearGuildFolder(event.guild)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        event.guild.audioManager.closeAudioConnection()
        val builder = EmbedBuilder()
        builder.setColor(Color(180, 76, 151))
        builder.setTitle("VCから切断")
        builder.setDescription("ボイスチャンネルから切断しました。")
        event.reply(builder.build())
    }
}