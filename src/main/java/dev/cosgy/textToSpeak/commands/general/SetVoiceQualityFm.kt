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
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.math.BigDecimal

class SetVoiceQualityFm(private var bot: Bot) : SlashCommand() {
    init {
        name = "setqfm"
        help = "追加ハーフトーンの設定を変更します。"
        guildOnly = false
        this.category = Category("設定")
        val options: MutableList<OptionData> = ArrayList()
        options.add(OptionData(OptionType.STRING, "value", "0.1~100.0", true))
        this.options = options
    }

    override fun execute(event: SlashCommandEvent) {
        val args = event.getOption("value")!!.asString
        var result: Boolean
        var bd: BigDecimal? = null
        try {
            //value = Float.parseFloat(args);
            bd = BigDecimal(args)
            result = true
        } catch (e: NumberFormatException) {
            result = false
        }
        if (!result) {
            event.reply("数値を設定して下さい。").queue()
            return
        }
        val min = BigDecimal("0.0")
        val max = BigDecimal("100.0")

        //if(!(0.1f <= value && value <= 100.0f)){
        if (!(min < bd && max > bd)) {
            event.reply("有効な数値を設定して下さい。0.1~100.0").queue()
            return
        }
        val settings = bot.userSettingsManager.getSettings(event.user.idLong)
        bd?.let { settings.voiceQualityFmSetting = it.toFloat() }
        event.reply("追加ハーフトーンを${bd}に設定しました。").queue()
    }

    override fun execute(event: CommandEvent) {
        if (event.args.isEmpty() && event.message.attachments.isEmpty()) {
            val builder = EmbedBuilder()
                .setTitle("setqfmコマンド")
                .addField("使用方法:", "$name <数値(0.0~)>", false)
                .addField("説明:", "追加ハーフトーンの設定を変更します。", false)
            event.reply(builder.build())
            return
        }
        val args = event.args
        var result: Boolean
        var bd: BigDecimal? = null
        try {
            //value = Float.parseFloat(args);
            bd = BigDecimal(args)
            result = true
        } catch (e: NumberFormatException) {
            result = false
        }
        if (!result) {
            event.reply("数値を設定して下さい。")
            return
        }
        val min = BigDecimal("0.0")
        val max = BigDecimal("100.0")

        //if(!(0.1f <= value && value <= 100.0f)){
        if (!(min < bd && max > bd)) {
            event.reply("有効な数値を設定して下さい。0.1~100.0")
            return
        }
        val settings = bot.userSettingsManager.getSettings(event.author.idLong)
        bd?.let { settings.voiceQualityFmSetting = it.toFloat() }
        event.reply("追加ハーフトーンを${bd}に設定しました。")
    }
}