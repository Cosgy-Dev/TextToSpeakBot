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
import dev.cosgy.textToSpeak.Bot
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class DlWordCmd(private val bot: Bot) : SlashCommand() {

    init {
        name = "wddl"
        help = "辞書に登録されている単語を削除します。"
        this.category = Category("辞書")
        val options: MutableList<OptionData> = ArrayList()
        options.add(OptionData(OptionType.STRING, "word", "単語", true))
        this.options = options
    }

    override fun execute(event: SlashCommandEvent) {
        val words = bot.dictionary?.getWords(event.guild!!.idLong)
        val args = event.getOption("word")!!.asString
        if (!words!!.containsKey(args)) {
            event.reply(args + "は、辞書に登録されていません。").queue()
            return
        }
        val result = bot.dictionary?.deleteDictionary(event.guild!!.idLong, args)
        if (result == true) {
            event.reply(String.format("単語(%s)を削除しました。", args)).queue()
        } else {
            event.reply("削除中に問題が発生しました。").setEphemeral(true).queue()
        }
    }

    override fun execute(event: CommandEvent) {
        if (event.args.isEmpty() && event.message.attachments.isEmpty()) {
            val builder = EmbedBuilder()
                .setTitle("dlwordコマンド")
                .addField("使用方法:", "$name <単語>", false)
                .addField("説明:", help, false)
            event.reply(builder.build())
            return
        }
        val words = bot.dictionary?.getWords(event.guild.idLong)
        val args = event.args
        if (!words!!.containsKey(args)) {
            event.reply(args + "は、辞書に登録されていません。")
            return
        }
        val result = bot.dictionary?.deleteDictionary(event.guild.idLong, args)
        if (result == true) {
            event.reply(String.format("単語(%s)を削除しました。", args))
        } else {
            event.reply("削除中に問題が発生しました。")
        }
    }
}