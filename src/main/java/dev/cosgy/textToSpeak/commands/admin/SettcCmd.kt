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
import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent
import com.jagrosh.jdautilities.commons.utils.FinderUtil
import dev.cosgy.textToSpeak.Bot
import dev.cosgy.textToSpeak.commands.AdminCommand
import dev.cosgy.textToSpeak.settings.Settings
import dev.cosgy.textToSpeak.utils.FormatUtil
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class SettcCmd(bot: Bot?) : AdminCommand() {
    var log: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        name = "settc"
        help = "読み上げをするチャンネルを設定します。読み上げするチャンネルを設定していない場合は、joinコマンドを最後に実行したチャンネルが読み上げ対象になります。"
        arguments = "<チャンネル名|NONE|なし>"
        children = arrayOf<SlashCommand>(Set(), None())
    }

    override fun execute(event: SlashCommandEvent) {}
    override fun execute(event: CommandEvent) {
        if (event.args.isEmpty()) {
            event.reply("${event.client.error}チャンネルまたはNONEを含めてください。")
            return
        }
        val s = event.client.getSettingsFor<Settings>(event.guild)
        if (event.args.lowercase(Locale.getDefault()).matches("(none|なし)".toRegex())) {
            s.setTextChannel(null)
            event.reply("${event.client.success}読み上げをするチャンネルの設定を無効にしました。")
        } else {
            val list = FinderUtil.findTextChannels(event.args, event.guild)
            if (list.isEmpty()) event.reply("${event.client.warning}一致するチャンネルが見つかりませんでした ${event.args}") else if (list.size > 1) event.reply(
                event.client.warning + FormatUtil.listOfTChannels(list, event.args)
            ) else {
                s.setTextChannel(list[0])
                log.info("読み上げを行うチャンネルを設定しました。")
                event.reply("${event.client.success}読み上げるチャンネルを<#${list[0].id}>に設定しました。")
            }
        }
    }

    private class Set : AdminCommand() {
        init {
            name = "set"
            help = "読み上げるチャンネルを設定"
            val options: MutableList<OptionData> = ArrayList()
            options.add(OptionData(OptionType.CHANNEL, "channel", "テキストチャンネル", true))
            this.options = options
        }

        override fun execute(event: SlashCommandEvent) {
            val s = event.client.getSettingsFor<Settings>(event.guild)
            if (event.getOption("channel")!!.channelType != ChannelType.TEXT) {
                event.reply("${event.client.error}テキストチャンネルを設定して下さい。").queue()
                return
            }
            val channelId = event.getOption("channel")!!.asLong
            val tc = event.guild!!.getTextChannelById(channelId)
            s.setTextChannel(tc)
            event.reply("${event.client.success}読み上げるチャンネルを<#${tc!!.id}>に設定しました。").queue()
        }
    }

    private class None : AdminCommand() {
        init {
            name = "none"
            help = "読み上げるチャンネル設定をリセットします。"
        }

        override fun execute(event: SlashCommandEvent) {
            if (!checkAdminPermission(event.client, event)) {
                event.reply("${event.client.warning}権限がないため実行できません。").queue()
                return
            }
            val s = event.client.getSettingsFor<Settings>(event.guild)
            s.setTextChannel(null)
            event.reply("${event.client.success}読み上げるチャンネル設定をリセットしました。").queue()
        }

        override fun execute(event: CommandEvent) {
            val s = event.client.getSettingsFor<Settings>(event.guild)
            s.setTextChannel(null)
            event.reply("${event.client.success}読み上げるチャンネル設定をリセットしました。")
        }
    }
}