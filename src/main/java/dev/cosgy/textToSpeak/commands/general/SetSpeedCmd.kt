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

class SetSpeedCmd(private val bot: Bot) : SlashCommand() {
    init {
        name = "setspeed"
        help = "読み上げ速度の設定を変更します。"
        guildOnly = false
        category = Category("設定")
        options = mutableListOf(OptionData(OptionType.STRING, "value", "0.1~100.0", true))
    }

    override fun execute(event: SlashCommandEvent) {
        val args = event.getOption("value")?.asString
        val bd = try {
            BigDecimal(args)
        } catch (e: NumberFormatException) {
            event.reply("数値を設定して下さい。").queue()
            return
        }
        val min = BigDecimal.ZERO
        val max = BigDecimal("100.0")
        if (!(min < bd && max > bd)) {
            event.reply("有効な数値を設定して下さい。0.1~100.0").queue()
            return
        }
        val settings = bot.userSettingsManager.getSettings(event.user.idLong)
        settings.speedSetting = bd.toFloat()
        event.reply("速度を $bd に設定しました。").queue()
    }

    override fun execute(event: CommandEvent) {
        val args = event.args

        if (args == null) {
            help(event)
            return
        }

        val bd = try {
            BigDecimal(args)
        } catch (e: NumberFormatException) {
            event.reply("数値を設定して下さい。")
            return
        }
        val min = BigDecimal.ZERO
        val max = BigDecimal("100.0")
        if (!(min < bd && max > bd)) {
            event.reply("有効な数値を設定して下さい。0.1~100.0")
            return
        }
        val settings = bot.userSettingsManager.getSettings(event.author.idLong)
        settings.speedSetting = bd.toFloat()
        event.reply("速度を $bd に設定しました。")
    }

    fun help(event: CommandEvent?) {
        val builder = EmbedBuilder()
            .setTitle("setspeedコマンド")
            .addField("使用方法:", "$name <数値(0.0~)>", false)
            .addField("説明:", "読み上げの速度を設定します。読み上げ速度は、0.0以上の数値で設定して下さい。", false)
        event?.reply(builder.build())
    }
}
