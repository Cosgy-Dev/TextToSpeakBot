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
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

class GuildSettings(private val bot: Bot) : AdminCommand() {
    var log: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        name = "gsettings"
        help = "ギルドの現在の設定を確認できます。"
    }

    override fun execute(event: SlashCommandEvent) {
        if (!checkAdminPermission(event.client, event)) {
            event.reply(event.client.warning + "権限がないため実行できません。").queue()
            return
        }
        val settings = bot.settingsManager.getSettings(event.guild!!)
        var text = "null"
        if (settings.getTextChannel(event.guild) != null) {
            text = settings.getTextChannel(event.guild)!!.name
        }
        val builder = EmbedBuilder()
            .setColor(Color.orange)
            .setTitle(event.guild!!.name + "の設定")
            .addField("ユーザー名読み上げ：", if (settings.isReadName()) "有効" else "無効", false)
            .addField(
                "参加、退出時の読み上げ：",
                if (settings.isJoinAndLeaveRead()) "有効" else "無効",
                false
            )
            .addField("読み上げるチャンネル：", text, false)
            .addField("読み上げの主音量：", settings.volume.toString(), false)
        event.replyEmbeds(builder.build()).queue()
    }

    override fun execute(event: CommandEvent) {
        val settings = bot.settingsManager.getSettings(event.guild)
        var text = "null"
        if (settings.getTextChannel(event.guild) != null) {
            text = settings.getTextChannel(event.guild)!!.name
        }
        val builder = EmbedBuilder()
            .setColor(Color.orange)
            .setTitle(event.guild.name + "の設定")
            .addField("ユーザー名読み上げ：", if (settings.isReadName()) "有効" else "無効", false)
            .addField(
                "参加、退出時の読み上げ：",
                if (settings.isJoinAndLeaveRead()) "有効" else "無効",
                false
            )
            .addField(
                "ニックネーム優先：",
                if (settings.isReadNic()) "有効" else "無効",
                false
            )
            .addField("読み上げるチャンネル：", text, false)
            .addField("読み上げの主音量：", settings.volume.toString(), false)
        event.reply(builder.build())
    }
}