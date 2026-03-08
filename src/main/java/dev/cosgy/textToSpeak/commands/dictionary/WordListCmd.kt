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
package dev.cosgy.textToSpeak.commands.dictionary

import dev.cosgy.textToSpeak.Bot
import dev.cosgy.textToSpeak.framework.command.command.CommandEvent
import dev.cosgy.textToSpeak.framework.command.command.SlashCommand
import dev.cosgy.textToSpeak.framework.command.command.SlashCommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import java.awt.Color
import java.util.*
import java.util.stream.Collectors

class WordListCmd(private val bot: Bot) : SlashCommand() {
    init {
        name = "wdls"
        help = "辞書に登録してある単語をリストアップします。"
        this.category = Category("辞書")
        botPermissions = arrayOf(Permission.MESSAGE_EMBED_LINKS)
    }

    override fun execute(event: SlashCommandEvent) {
        val wordList = bot.dictionary?.getWords(event.guild!!.idLong)
            ?.entries?.stream()
            ?.map { (key, value): Map.Entry<String?, String?> -> "$key-$value" }
            ?.collect(Collectors.toList())
            ?: emptyList()

        if (wordList.isEmpty()) {
            event.reply("単語が登録されていません。")
                .setEphemeral(true)
                .queue()
            return
        }

        sendPaged(
            userId = event.user.idLong,
            items = wordList,
            color = event.guild!!.selfMember.color ?: Color(76, 108, 179),
            initialPage = 1,
            send = { embed, row ->
                event.replyEmbeds(embed).addComponents(ActionRow.of(row)).queue()
            }
        )
    }

    override fun execute(event: CommandEvent) {
        val pageNum = event.args.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val wordList = bot.dictionary?.getWords(event.guild.idLong)
            ?.entries?.stream()
            ?.map { (key, value): Map.Entry<String?, String?> -> "$key-$value" }
            ?.collect(Collectors.toList())
            ?: emptyList()

        if (wordList.isEmpty()) {
            event.reply("単語が登録されていません。")
            return
        }

        sendPaged(
            userId = event.author.idLong,
            items = wordList,
            color = event.selfMember.color ?: Color(76, 108, 179),
            initialPage = pageNum,
            send = { embed, row ->
                event.channel.sendMessageEmbeds(embed).setComponents(ActionRow.of(row)).queue()
            }
        )
    }

    private fun sendPaged(
        userId: Long,
        items: List<String>,
        color: Color,
        initialPage: Int,
        send: (net.dv8tion.jda.api.entities.MessageEmbed, List<Button>) -> Unit
    ) {
        val token = UUID.randomUUID().toString().substring(0, 8)
        val prevId = "wdls:$token:prev"
        val nextId = "wdls:$token:next"
        val closeId = "wdls:$token:close"

        val perPage = 10
        val totalPages = ((items.size + perPage - 1) / perPage).coerceAtLeast(1)
        var currentPage = initialPage.coerceIn(1, totalPages)

        fun buildEmbed(page: Int): net.dv8tion.jda.api.entities.MessageEmbed {
            val from = (page - 1) * perPage
            val to = minOf(items.size, from + perPage)
            val body = items.subList(from, to)
                .mapIndexed { index, item -> "${from + index + 1}. $item" }
                .joinToString("\n")

            return EmbedBuilder()
                .setTitle("単語一覧")
                .setDescription(body)
                .setColor(color)
                .setFooter("Page $page/$totalPages")
                .build()
        }

        fun buttons(page: Int): List<Button> {
            return listOf(
                Button.secondary(prevId, "前へ").withDisabled(page <= 1),
                Button.secondary(nextId, "次へ").withDisabled(page >= totalPages),
                Button.danger(closeId, "閉じる")
            )
        }

        send(buildEmbed(currentPage), buttons(currentPage))

        val ttl = 120_000L
        bot.buttonRouter.register(prevId, userId, ttl, oneShot = false) { interaction ->
            currentPage = (currentPage - 1).coerceAtLeast(1)
            interaction.editMessageEmbeds(buildEmbed(currentPage))
                .setComponents(ActionRow.of(buttons(currentPage)))
                .queue()
        }
        bot.buttonRouter.register(nextId, userId, ttl, oneShot = false) { interaction ->
            currentPage = (currentPage + 1).coerceAtMost(totalPages)
            interaction.editMessageEmbeds(buildEmbed(currentPage))
                .setComponents(ActionRow.of(buttons(currentPage)))
                .queue()
        }
        bot.buttonRouter.register(closeId, userId, ttl) { interaction ->
            interaction.editMessage("一覧を閉じました。")
                .setComponents()
                .queue()
            bot.buttonRouter.unregister(prevId)
            bot.buttonRouter.unregister(nextId)
        }
    }
}
