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
import java.awt.Color

class SettingsCmd(private var bot: Bot) : SlashCommand() {
    init {
        guildOnly = false
        name = "settings"
        help = "現在の設定を確認します。"
        this.category = Category("設定")
    }

    override fun execute(event: SlashCommandEvent) {
        val settings = bot.userSettingsManager.getSettings(event.user.idLong)
        val builder = EmbedBuilder()
                .setColor(Color.orange)
                .setTitle(event.user.name + "の設定")
                .addField("声：", settings.voiceSetting, false)
                .addField("読み上げ速度：", settings.speedSetting.toString(), false)
                .addField("F0系列内変動の重み：", settings.intonationSetting.toString(), false)
                .addField("オールパス値：", settings.voiceQualityASetting.toString(), false)
                .addField("追加ハーフトーン：", settings.voiceQualityFmSetting.toString(), false)
        event.replyEmbeds(builder.build()).queue()
    }

    override fun execute(event: CommandEvent) {
        val settings = bot.userSettingsManager.getSettings(event.author.idLong)
        val builder = EmbedBuilder()
                .setColor(Color.orange)
                .setTitle(event.author.name + "の設定")
                .addField("声：", settings.voiceSetting, false)
                .addField("速度：", settings.speedSetting.toString(), false)
                .addField("抑揚：", settings.intonationSetting.toString(), false)
                .addField("声質a：", settings.voiceQualityASetting.toString(), false)
                .addField("声質fm：", settings.voiceQualityFmSetting.toString(), false)
        event.reply(builder.build())
    }
}