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

import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent
import com.jagrosh.jdautilities.menu.ButtonMenu
import dev.cosgy.textToSpeak.Bot
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.awt.Color
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AddWordCmd(private val bot: Bot) : SlashCommand() {
    init {
        name = "wdad"
        help = "辞書に単語を追加します。辞書に単語が存在している場合は上書きされます。"
        this.category = Category("辞書")
        val options: MutableList<OptionData> = ArrayList()
        options.add(OptionData(OptionType.STRING, "word", "単語", true))
        options.add(OptionData(OptionType.STRING, "reading", "読み方（カタカナ）", true))
        this.options = options
    }

    private fun handleCommand(event: SlashCommandEvent, word: String, reading: String) {
        val guildId = event.guild!!.idLong
        val dictionary = bot.dictionary
        val isWordExist = dictionary!!.getWords(guildId).containsKey(word)
        event.deferReply().queue()
        if (isWordExist) {
            val no = "❌"
            val ok = "✔"
            ButtonMenu.Builder()
                    .setText("単語が既に存在します。上書きしますか？")
                    .addChoices(no, ok)
                    .setEventWaiter(bot.waiter)
                    .setTimeout(30, TimeUnit.SECONDS)
                    .setAction { re: Emoji ->
                        if (re.name == ok) {
                            dictionary.updateDictionary(guildId, word, reading)
                            sendSuccessMessage(event)
                        } else {

                            event.hook.sendMessage("辞書登録をキャンセルしました。").queue()
                        }
                    }.setFinalAction { m: Message ->
                        try {
                            m.clearReactions().queue()
                            m.delete().queue()
                        } catch (ignore: PermissionException) {
                        }
                    }.build().display(event.messageChannel)
        } else {
            dictionary.updateDictionary(guildId, word, reading)
            sendSuccessMessage(event)
        }
    }

    private fun sendSuccessMessage(event: SlashCommandEvent) {
        val builder = EmbedBuilder()
                .setColor(SUCCESS_COLOR)
                .setTitle("単語を追加しました。")
                .addField("単語", "```" + event.getOption("word")!!.asString + "```", false)
                .addField("読み", "```" + event.getOption("reading")!!.asString + "```", false)
        event.hook.sendMessageEmbeds(builder.build()).queue()
    }

    override fun execute(event: SlashCommandEvent) {
        val word = event.getOption("word")!!.asString
        val reading = event.getOption("reading")!!.asString
        if (!isKatakana(reading)) {
            event.reply("読み方はすべてカタカナで入力して下さい。").setEphemeral(true).queue()
            return
        }
        handleCommand(event, word, reading)
    }

    companion object {
        private val SUCCESS_COLOR = Color(0, 163, 129)
        private val ERROR_COLOR = Color.RED
        private const val INVALID_ARGS_MESSAGE = "コマンドが無効です。単語と読み方の２つを入力して実行して下さい。"
        private const val USAGE_MESSAGE = "使用方法: /addword <単語> <読み方>"
        private const val KATAKANA_REGEX = "^[ァ-ヶー]*$"
        private fun isKatakana(str: String): Boolean {
            return Pattern.matches(KATAKANA_REGEX, str)
        }
    }
}