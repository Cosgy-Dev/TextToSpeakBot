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

import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo
import com.jagrosh.jdautilities.doc.standard.CommandInfo
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.*

/**
 * @author Kosugi_kun
 */
@CommandInfo(name = ["About"], description = "ボットに関する情報を表示します")
class AboutCommand(private val color: Color, private val description: String, vararg perms: Permission) :
    SlashCommand() {
    private val perms: Array<out Permission>
    private var isAuthor = true
    private var replacementIcon = "+"
    private var oauthLink: String? = null

    init {
        name = "about"
        help = "ボットに関する情報を表示します"
        guildOnly = false
        this.perms = perms
        botPermissions = arrayOf(Permission.MESSAGE_EMBED_LINKS)
    }

    fun setIsAuthor(value: Boolean) {
        isAuthor = value
    }

    fun setReplacementCharacter(value: String) {
        replacementIcon = value
    }

    override fun execute(event: SlashCommandEvent) {
        getOauthLink(event.jda)
        val builder = EmbedBuilder()
        builder.setColor(if (event.isFromType(ChannelType.TEXT)) event.guild!!.selfMember.color else color)
        builder.setAuthor(event.jda.selfUser.name + "について!", null, event.jda.selfUser.avatarUrl)
        val cosgyOwner = "Cosgy Devが運営、開発をしています。"
        val author =
            if (event.jda.getUserById(event.client.ownerId) == null) "<@" + event.client.ownerId + ">" else Objects.requireNonNull(
                event.jda.getUserById(event.client.ownerId)
            )?.name
        val descr = StringBuilder().append("こんにちは！ **").append(event.jda.selfUser.name).append("**です。 ")
            .append(description).append("は、")
            .append(JDAUtilitiesInfo.AUTHOR + "の[コマンド拡張](" + JDAUtilitiesInfo.GITHUB + ") (")
            .append(JDAUtilitiesInfo.VERSION).append(")と[JDAライブラリ](https://github.com/DV8FromTheWorld/JDA) (")
            .append(JDAInfo.VERSION).append(")を使用しており、").append(if (isAuthor) cosgyOwner else author + "が所有しています。")
            .append(event.jda.selfUser.name)
            .append("についての質問などは[Cosgy Dev公式チャンネル](https://discord.gg/RBpkHxf)へお願いします。")
            .append("\nこのボットの使用方法は`").append(event.client.textualPrefix).append(event.client.helpWord)
            .append("`で確認することができます。")
        getMessage(builder, descr, event.jda, event.client)
        event.replyEmbeds(builder.build()).queue()
    }

    private fun getMessage(builder: EmbedBuilder, descr: StringBuilder, jda: JDA, client: CommandClient) {
        builder.setDescription(descr)
        if (jda.shardInfo.shardTotal == 1) {
            builder.addField(
                "ステータス", """${jda.guilds.size} サーバー
                    |1シャード""".trimMargin(), true
            )
            builder.addField("ユーザー", """${jda.users.size} ユニーク
                |${jda.guilds.stream().mapToInt { g: Guild -> g.members.size }.sum()} 合計""".trimMargin(), true
            )
            builder.addField(
                "チャンネル", """${jda.textChannels.size} テキスト
                    |${jda.voiceChannels.size} ボイス""".trimMargin(), true
            )
        } else {
            builder.addField(
                "ステータス", """${client.totalGuilds} サーバー
                    |シャード ${jda.shardInfo.shardId + 1}/${jda.shardInfo.shardTotal}""".trimMargin(), true
            )
            builder.addField(
                "", """${jda.users.size} ユーザーのシャード
                    |${jda.guilds.size} サーバー""".trimMargin(), true
            )
            builder.addField(
                "", """${jda.textChannels.size} テキストチャンネル
                    |${jda.voiceChannels.size} ボイスチャンネル""".trimMargin(), true
            )
        }
        builder.setFooter("再起動が行われた時間")
        builder.setTimestamp(client.startTime)
    }

    override fun execute(event: CommandEvent) {
        getOauthLink(event.jda)
        val builder = EmbedBuilder()
        builder.setColor(if (event.isFromType(ChannelType.TEXT)) event.guild.selfMember.color else color)
        builder.setAuthor(event.selfUser.name + "について!", null, event.selfUser.avatarUrl)
        val cosgyOwner = "Cosgy Devが運営、開発をしています。"
        val author =
            if (event.jda.getUserById(event.client.ownerId) == null) "<@" + event.client.ownerId + ">" else Objects.requireNonNull(
                event.jda.getUserById(event.client.ownerId)
            )?.name
        val descr = StringBuilder().append("こんにちは！ **").append(event.selfUser.name).append("**です。 ")
            .append(description).append("は、")
            .append(JDAUtilitiesInfo.AUTHOR + "の[コマンド拡張](" + JDAUtilitiesInfo.GITHUB + ") (")
            .append(JDAUtilitiesInfo.VERSION).append(")と[JDAライブラリ](https://github.com/DV8FromTheWorld/JDA) (")
            .append(JDAInfo.VERSION).append(")を使用しており、").append(if (isAuthor) cosgyOwner else author + "が所有しています。")
            .append(event.selfUser.name).append("についての質問などは[Cosgy Dev公式チャンネル](https://discord.gg/RBpkHxf)へお願いします。")
            .append("\nこのボットの使用方法は`").append(event.client.textualPrefix).append(event.client.helpWord)
            .append("`で確認することができます。")
        getMessage(builder, descr, event.jda, event.client)
        event.reply(builder.build())
    }

    private fun getOauthLink(jda: JDA) {
        if (oauthLink == null) {
            oauthLink = try {
                val info = jda.retrieveApplicationInfo().complete()
                if (info.isBotPublic) info.getInviteUrl(0L, *perms) else ""
            } catch (e: Exception) {
                val log = LoggerFactory.getLogger("OAuth2")
                log.error("招待リンクを生成できませんでした ", e)
                ""
            }
        }
    }
}