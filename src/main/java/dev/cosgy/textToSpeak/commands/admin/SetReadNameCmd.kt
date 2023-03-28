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
import org.slf4j.LoggerFactory

class SetReadNameCmd(private val bot: Bot) : AdminCommand() {
    var log = LoggerFactory.getLogger(this.javaClass)

    init {
        name = "setreadname"
        help = "テキストを読み上げる際にユーザー名も読み上げるかを設定します。"
    }

    override fun execute(event: SlashCommandEvent) {
        if (!checkAdminPermission(event.client, event)) {
            event.reply(event.client.warning + "権限がないため実行できません。").queue()
            return
        }
        val settings = bot.settingsManager.getSettings(event.guild!!)
        if (settings!!.isReadName()) {
            settings.setReadName(false)
            event.reply("ユーザー名の読み上げを無効にしました。").queue()
        } else {
            settings.setReadName(true)
            event.reply("ユーザー名の読み上げを有効にしました。").queue()
        }
    }

    override fun execute(event: CommandEvent) {
        val settings = bot.settingsManager.getSettings(event.guild)
        if (settings!!.isReadName()) {
            settings.setReadName(false)
            event.reply("ユーザー名の読み上げを無効にしました。")
        } else {
            settings.setReadName(true)
            event.reply("ユーザー名の読み上げを有効にしました。")
        }
    }
}