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
import dev.cosgy.textToSpeak.audio.VoiceCreation
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.*

class HelpCmd(var bot: Bot) : SlashCommand() {
    init {
        name = "help"
        help = "コマンド一覧を表示します。"
    }

    override fun execute(event: SlashCommandEvent) {
        val eBuilder = EmbedBuilder()
        eBuilder.setTitle("**" + event.jda.selfUser.name + "** コマンド一覧")
        eBuilder.setColor(Color(245, 229, 107))
        val builder = StringBuilder()
        var category: Category? = null
        val commands = event.client.slashCommands
        for (command in commands) {
            if (!command.isHidden && (!command.isOwnerCommand || event.member!!.isOwner)) {
                if (category != command.category) {
                    category = command.category
                    builder.append("\n\n  __").append(if (category == null) "カテゴリなし" else category.name)
                        .append("__:\n")
                }
                builder.append("\n`").append("/").append(command.name)
                    .append(if (command.arguments == null) "`" else " " + command.arguments + "`")
                    .append(" - ").append(command.help)
            }
        }
        if (event.client.serverInvite != null) builder.append("\n\nさらにヘルプが必要な場合は、公式サーバーに参加することもできます: ")
            .append(event.client.serverInvite)
        eBuilder.setDescription(builder)
        if (bot.config.helpToDm) {
            event.user.openPrivateChannel()
                .flatMap { channel: PrivateChannel -> channel.sendMessageEmbeds(eBuilder.build()) }.queue()
        } else {
            event.replyEmbeds(eBuilder.build()).queue()
        }
    }

    public override fun execute(event: CommandEvent) {
        val eBuilder = EmbedBuilder()
        eBuilder.setTitle("**" + event.jda.selfUser.name + "** コマンド一覧")
        eBuilder.setColor(Color(245, 229, 107))
        val builder = StringBuilder()
        var category: Category? = null
        val commands = event.client.commands
        for (command in commands) {
            if (!command.isHidden && (!command.isOwnerCommand || event.isOwner)) {
                if (category != command.category) {
                    category = command.category
                    builder.append("\n\n  __").append(if (category == null) "カテゴリなし" else category.name)
                        .append("__:\n")
                }
                builder.append("\n`").append("/").append(command.name)
                    .append(if (command.arguments == null) "`" else " " + command.arguments + "`")
                    .append(" - ").append(command.help)
            }
        }
        if (event.client.serverInvite != null) builder.append("\n\nさらにヘルプが必要な場合は、公式サーバーに参加することもできます: ")
            .append(event.client.serverInvite)
        eBuilder.setDescription(builder)
        if (bot.config.helpToDm) {
            event.author.openPrivateChannel()
                .flatMap { channel: PrivateChannel -> channel.sendMessageEmbeds(eBuilder.build()) }.queue()
        } else {
            event.reply(eBuilder.build())
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(VoiceCreation::class.java)
    }
}