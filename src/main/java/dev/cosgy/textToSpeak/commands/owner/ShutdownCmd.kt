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
package dev.cosgy.textToSpeak.commands.owner

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommandEvent
import dev.cosgy.textToSpeak.Bot
import dev.cosgy.textToSpeak.commands.OwnerCommand

class ShutdownCmd(private val bot: Bot) : OwnerCommand() {
    init {
        name = "shutdown"
        help = "一時ファイルを削除してボットを停止します。"
        guildOnly = false
    }

    override fun execute(event: SlashCommandEvent) {
        event.reply(event.client.warning + "シャットダウンしています...").queue()
        bot.shutdown()
    }

    override fun execute(event: CommandEvent) {
        event.reply(event.client.warning + "シャットダウンしています...")
        bot.shutdown()
    }
}