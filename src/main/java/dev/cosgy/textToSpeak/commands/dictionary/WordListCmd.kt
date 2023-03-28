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

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent
import com.jagrosh.jdautilities.menu.Paginator
import dev.cosgy.textToSpeak.Bot
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.interactions.InteractionHook
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

class WordListCmd(private val bot: Bot) : SlashCommand() {
    private val builder: Paginator.Builder

    init {
        name = "wdls"
        help = "辞書に登録してある単語をリストアップします。"
        this.category = Category("辞書")
        botPermissions = arrayOf(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS)
        builder = Paginator.Builder()
                .setColumns(1)
                .setFinalAction { m: Message ->
                    try {
                        m.clearReactions().queue()
                    } catch (ignore: PermissionException) {
                    }
                }
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.waiter)
                .setTimeout(1, TimeUnit.MINUTES)
    }

    override fun execute(event: SlashCommandEvent) {
        event.reply("単語一覧を表示します。").queue { m: InteractionHook ->
            val wordList = bot.dictionary?.getWords(event.guild!!.idLong)
                    ?.entries?.stream()
                    ?.map { (key, value): Map.Entry<String?, String?> -> "$key-$value" }
                    ?.collect(Collectors.toList())
            if (wordList != null) {
                if (wordList.isEmpty()) {
                    m.editOriginal("単語が登録されていません。").queue()
                    return@queue
                }
            }
            m.deleteOriginal().queue()
            builder.setText("単語一覧")
                    .setItems(*wordList!!.toTypedArray())
                    .setUsers(event.user)
                    .setColor(event.guild!!.selfMember.color)
            builder.build().paginate(event.channel, 1)
        }
    }

    override fun execute(event: CommandEvent) {
        var pagenum = 1
        try {
            pagenum = event.args.toInt()
        } catch (ignore: NumberFormatException) {
        }
        val wordList = bot.dictionary?.getWords(event.guild.idLong)
                ?.entries?.stream()
                ?.map { (key, value): Map.Entry<String?, String?> -> "$key-$value" }
                ?.collect(Collectors.toList())
        if (wordList != null) {
            if (wordList.isEmpty()) {
                event.reply("単語が登録されていません。")
                return
            }
        }
        builder.setText("単語一覧")
                .setItems(*wordList!!.toTypedArray())
                .setUsers(event.author)
                .setColor(event.selfMember.color)
        builder.build().paginate(event.channel, pagenum)
    }
}