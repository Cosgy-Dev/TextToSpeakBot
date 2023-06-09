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

class SetIntonationCmd(private val bot: Bot) : SlashCommand() {
    init {
        name = "setinto"
        help = "F0系列内変動の重みの設定を変更します。"
        guildOnly = false
        category = Category("設定")
        options = listOf(OptionData(OptionType.STRING, "value", "0.1~100.0", true))
    }

    override fun execute(event: SlashCommandEvent) {
        val bd = event.getOption("value")?.asString?.toBigDecimalOrNull()

        if (bd == null || bd < BigDecimal.ZERO || bd > BigDecimal("100.0")) {
            event.reply("有効な数値を設定してください。0.1~100.0").queue()
            return
        }

        val settings = bot.userSettingsManager.getSettings(event.user.idLong)
        settings.intonationSetting = bd.toFloat()
        event.reply("F0系列内変動の重みを $bd に設定しました。").queue()
    }

    override fun execute(event: CommandEvent) {
        if (event.args.isEmpty()) {
            val builder = EmbedBuilder()
                .setTitle("setintoコマンド")
                .addField("使用方法:", "$name <数値(0.0~)>", false)
                .addField("説明:", "F0系列内変動の重みを変更します。F0系列内変動の重みは、0.0以上の数値で設定して下さい。", false)
            event.reply(builder.build())
            return
        }

        val bd = event.args.toBigDecimalOrNull()

        if (bd == null || bd < BigDecimal.ZERO || bd > BigDecimal("100.0")) {
            event.reply("有効な数値を設定してください。0.1~100.0")
            return
        }

        val settings = bot.userSettingsManager.getSettings(event.author.idLong)
        settings.intonationSetting = bd.toFloat()
        event.reply("F0系列内変動の重みを $bd に設定しました。")
    }
}
