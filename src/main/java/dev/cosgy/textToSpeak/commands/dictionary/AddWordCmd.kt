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

import dev.cosgy.textToSpeak.framework.command.command.CommandEvent
import dev.cosgy.textToSpeak.framework.command.command.SlashCommand
import dev.cosgy.textToSpeak.framework.command.command.SlashCommandEvent
import dev.cosgy.textToSpeak.Bot
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.awt.Color
import java.util.UUID
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

    private fun handleSlashConfirm(event: SlashCommandEvent, word: String, reading: String) {
        val guildId = event.guild!!.idLong
        val dictionary = bot.dictionary ?: return
        val token = UUID.randomUUID().toString().substring(0, 8)
        val confirmId = "wdad:$token:ok"
        val cancelId = "wdad:$token:cancel"

        event.reply("単語が既に存在します。上書きしますか？")
            .addComponents(ActionRow.of(Button.success(confirmId, "上書き"), Button.danger(cancelId, "キャンセル")))
            .queue()

        bot.buttonRouter.register(confirmId, event.user.idLong, 30_000L) { interaction ->
            dictionary.updateDictionary(guildId, word, reading)
            val builder = successEmbed(word, reading)
            interaction.editMessageEmbeds(builder.build()).setComponents().queue()
        }
        bot.buttonRouter.register(cancelId, event.user.idLong, 30_000L) { interaction ->
            interaction.editMessage("辞書登録をキャンセルしました。")
                .setComponents()
                .queue()
        }
    }

    private fun handleTextConfirm(event: CommandEvent, word: String, reading: String) {
        val guildId = event.guild.idLong
        val dictionary = bot.dictionary ?: return
        val token = UUID.randomUUID().toString().substring(0, 8)
        val confirmId = "wdad:$token:ok"
        val cancelId = "wdad:$token:cancel"

        event.channel.sendMessage("単語が既に存在します。上書きしますか？")
            .setComponents(ActionRow.of(Button.success(confirmId, "上書き"), Button.danger(cancelId, "キャンセル")))
            .queue()

        bot.buttonRouter.register(confirmId, event.author.idLong, 30_000L) { interaction ->
            dictionary.updateDictionary(guildId, word, reading)
            val builder = successEmbed(word, reading)
            interaction.editMessageEmbeds(builder.build()).setComponents().queue()
        }
        bot.buttonRouter.register(cancelId, event.author.idLong, 30_000L) { interaction ->
            interaction.editMessage("辞書登録をキャンセルしました。")
                .setComponents()
                .queue()
        }
    }

    private fun successEmbed(word: String, reading: String): EmbedBuilder {
        return EmbedBuilder()
            .setColor(SUCCESS_COLOR)
            .setTitle("単語を追加しました。")
            .addField("単語", "```${replaceEmoji(word)}```", false)
            .addField("読み", "```${reading}```", false)
    }

    override fun execute(event: SlashCommandEvent) {
        val word = event.getOption("word")!!.asString
        val reading = event.getOption("reading")!!.asString
        if (!isKatakana(reading)) {
            event.reply("読み方はすべてカタカナで入力して下さい。").setEphemeral(true).queue()
            return
        }

        val normalizedWord = replaceEmoji(word)
        val guildId = event.guild!!.idLong
        val dictionary = bot.dictionary ?: return
        val isWordExist = dictionary.getWords(guildId).containsKey(normalizedWord)

        if (isWordExist) {
            handleSlashConfirm(event, normalizedWord, reading)
        } else {
            dictionary.updateDictionary(guildId, normalizedWord, reading)
            event.replyEmbeds(successEmbed(normalizedWord, reading).build()).queue()
        }
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex(), 2).toTypedArray()
        if (args.size < 2) {
            event.reply("コマンドが無効です。単語と読み方の２つを入力して実行して下さい。")
            return
        }

        val word = args[0]
        val reading = args[1]
        if (!isKatakana(reading)) {
            event.reply("読み方はすべてカタカナで入力して下さい。")
            return
        }

        val normalizedWord = replaceEmoji(word)
        val guildId = event.guild.idLong
        val dictionary = bot.dictionary ?: return
        val isWordExist = dictionary.getWords(guildId).containsKey(normalizedWord)

        if (isWordExist) {
            handleTextConfirm(event, normalizedWord, reading)
        } else {
            dictionary.updateDictionary(guildId, normalizedWord, reading)
            event.reply(successEmbed(normalizedWord, reading).build())
        }
    }

    companion object {
        private val SUCCESS_COLOR = Color(0, 163, 129)
        private const val KATAKANA_REGEX = "^[ァ-ヶー]*$"

        private fun isKatakana(str: String): Boolean {
            return Pattern.matches(KATAKANA_REGEX, str)
        }

        private val EMOJI_REGEX = """<(:[a-z0-9_]+:)\\d+>""".toRegex()
        private fun replaceEmoji(str: String): String {
            return EMOJI_REGEX.replace(str, "$1")
        }
    }
}
