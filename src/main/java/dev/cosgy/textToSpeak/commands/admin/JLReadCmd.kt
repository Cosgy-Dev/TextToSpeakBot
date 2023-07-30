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
package dev.cosgy.textToSpeak.commands.admin

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommandEvent
import dev.cosgy.textToSpeak.Bot
import dev.cosgy.textToSpeak.commands.AdminCommand
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JLReadCmd(private val bot: Bot) : AdminCommand() {
    var log: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        name = "jlread"
        help = "ボイスチャンネルにユーザーが参加または退出した時にユーザー名を読み上げるか否かを設定します。"

        options = listOf(OptionData(OptionType.BOOLEAN, "value", "機能を有効にするか否か", false))
    }

    override fun execute(event: SlashCommandEvent) {
        if (!checkAdminPermission(event.client, event)) {
            event.reply(event.client.warning + "権限がないため実行できません。").queue()
            return
        }
        val settings = bot.settingsManager.getSettings(event.guild!!)

        if (event.getOption("value") == null) {
            settings.setJoinAndLeaveRead(!settings.isJoinAndLeaveRead())
            event.reply("ボイスチャンネルにユーザーが参加、退出した際の読み上げを${if (settings.isJoinAndLeaveRead()) "有効" else "無効"}にしました。").queue()
        } else {
            val args = event.getOption("value")!!.asBoolean
            settings.setJoinAndLeaveRead(args)
            event.reply("ボイスチャンネルにユーザーが参加、退出した際の読み上げを${if (args) "有効" else "無効"}にしました。").queue()
        }
    }

    override fun execute(event: CommandEvent) {
        val settings = bot.settingsManager.getSettings(event.guild)

        settings.setJoinAndLeaveRead(!settings.isJoinAndLeaveRead())
        event.reply("ボイスチャンネルにユーザーが参加、退出した際の読み上げを${if (settings.isJoinAndLeaveRead()) "有効" else "無効"}にしました。")
    }
}