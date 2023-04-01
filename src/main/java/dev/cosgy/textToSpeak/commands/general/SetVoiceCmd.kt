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
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

class SetVoiceCmd(private var bot: Bot) : SlashCommand() {
    private var voices: Array<String?>

    init {
        name = "setvoice"
        help = "声の種類を変更することができます。"
        guildOnly = false
        this.category = Category("設定")
        val options: MutableList<OptionData> = ArrayList()
        options.add(OptionData(OptionType.STRING, "name", "声データの名前", true, true))
        this.options = options
        voices = bot.voiceCreation.voices.toTypedArray()
    }

    override fun execute(event: SlashCommandEvent) {
        if (event.getOption("name") == null) {
            val builder = EmbedBuilder()
                    .setTitle("setvoiceコマンド")
                    .addField("声データ一覧：", voices.contentToString(), false)
                    .addField("使用方法：", "$name <声データの名前>", false)
            event.replyEmbeds(builder.build()).queue()
            return
        }
        val viceName = event.getOption("name")!!.asString
        if (isValidVoice(viceName)) {
            val settings = bot.userSettingsManager.getSettings(event.user.idLong)
            settings.setVoice(viceName)
            event.reply("声データを`$viceName`に設定しました。").queue()
        } else {
            event.reply("有効な声データを選択して下さい。").queue()
        }
    }

    override fun execute(event: CommandEvent) {
        val voices = bot.voiceCreation.voices
        if (event.args.isEmpty() && event.message.attachments.isEmpty()) {
            val builder = EmbedBuilder()
                    .setTitle("setvoiceコマンド")
                    .addField("声データ一覧：", voices.toString(), false)
                    .addField("使用方法：", "$name <声データの名前>", false)
            event.reply(builder.build())
            return
        }
        val args = event.args
        if (voices.contains(args)) {
            val settings = bot.userSettingsManager.getSettings(event.author.idLong)
            settings.setVoice(args)
            event.reply("声データを`$args`に設定しました。")
        } else {
            event.reply("有効な声データを選択して下さい。")
        }
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.name == "setvoice" && event.focusedOption.name == "name") {
            val options = Stream.of(*voices)
                    .filter { word: String? -> word!!.startsWith(event.focusedOption.value) } // only display words that start with the user's current input
                    .map { word: String? -> Command.Choice(word!!, word) } // map the words to choices
                    .collect(Collectors.toList())
            event.replyChoices(options).queue()
        }
        super.onAutoComplete(event)
    }

    /**
     * ユーザーが入力した声の名前が有効かを確認
     *
     * @param voice ユーザーが入力した声の名前
     * @return 名前が有効の場合は true 無効な場合は false
     */
    private fun isValidVoice(voice: String): Boolean {
        for (v in voices) {
            if (v == voice) {
                return true
            }
        }
        return false
    }
}