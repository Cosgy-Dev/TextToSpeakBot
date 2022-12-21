////////////////////////////////////////////////////////////////////////////////
//  Copyright 2021 Cosgy Dev                                                   /
//                                                                             /
//     Licensed under the Apache License, Version 2.0 (the "License");         /
//     you may not use this file except in compliance with the License.        /
//     You may obtain a copy of the License at                                 /
//                                                                             /
//        http://www.apache.org/licenses/LICENSE-2.0                           /
//                                                                             /
//     Unless required by applicable law or agreed to in writing, software     /
//     distributed under the License is distributed on an "AS IS" BASIS,       /
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied./
//     See the License for the specific language governing permissions and     /
//     limitations under the License.                                          /
////////////////////////////////////////////////////////////////////////////////

package dev.cosgy.TextToSpeak.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Objects;

/**
 * @author Kosugi_kun
 */
@CommandInfo(
        name = "About",
        description = "ボットに関する情報を表示します"
)

public class AboutCommand extends SlashCommand {
    private final Color color;
    private final String description;
    private final Permission[] perms;
    private boolean IS_AUTHOR = true;
    private String REPLACEMENT_ICON = "+";
    private String oauthLink;

    public AboutCommand(Color color, String description, Permission... perms) {
        this.color = color;
        this.description = description;
        this.name = "about";
        this.help = "ボットに関する情報を表示します";
        this.guildOnly = false;
        this.perms = perms;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    public void setIsAuthor(boolean value) {
        this.IS_AUTHOR = value;
    }

    public void setReplacementCharacter(String value) {
        this.REPLACEMENT_ICON = value;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (oauthLink == null) {
            try {
                ApplicationInfo info = event.getJDA().retrieveApplicationInfo().complete();
                oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, perms) : "";
            } catch (Exception e) {
                Logger log = LoggerFactory.getLogger("OAuth2");
                log.error("招待リンクを生成できませんでした ", e);
                oauthLink = "";
            }
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColor() : color);
        builder.setAuthor("" + event.getJDA().getSelfUser().getName() + "について!", null, event.getJDA().getSelfUser().getAvatarUrl());
        String CosgyOwner = "Cosgy Devが運営、開発をしています。";
        String author = event.getJDA().getUserById(event.getClient().getOwnerId()) == null ? "<@" + event.getClient().getOwnerId() + ">"
                : Objects.requireNonNull(event.getJDA().getUserById(event.getClient().getOwnerId())).getName();
        StringBuilder descr = new StringBuilder().append("こんにちは！ **").append(event.getJDA().getSelfUser().getName()).append("**です。 ")
                .append(description).append("は、").append(JDAUtilitiesInfo.AUTHOR + "の[コマンド拡張](" + JDAUtilitiesInfo.GITHUB + ") (")
                .append(JDAUtilitiesInfo.VERSION).append(")と[JDAライブラリ](https://github.com/DV8FromTheWorld/JDA) (")
                .append(JDAInfo.VERSION).append(")を使用しており、").append((IS_AUTHOR ? CosgyOwner : author + "が所有しています。"))
                .append(event.getJDA().getSelfUser().getName()).append("についての質問などは[Cosgy Dev公式チャンネル](https://discord.gg/RBpkHxf)へお願いします。")
                .append("\nこのボットの使用方法は`").append(event.getClient().getTextualPrefix()).append(event.getClient().getHelpWord())
                .append("`で確認することができます。");
        builder.setDescription(descr);

        if (event.getJDA().getShardInfo().getShardTotal() == 1) {
            builder.addField("ステータス", event.getJDA().getGuilds().size() + " サーバー\n1 シャード", true);
            builder.addField("ユーザー", event.getJDA().getUsers().size() + " ユニーク\n" + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum() + " 合計", true);
            builder.addField("チャンネル", event.getJDA().getTextChannels().size() + " テキスト\n" + event.getJDA().getVoiceChannels().size() + " ボイス", true);
        } else {
            builder.addField("ステータス", (event.getClient()).getTotalGuilds() + " サーバー\nシャード " + (event.getJDA().getShardInfo().getShardId() + 1)
                    + "/" + event.getJDA().getShardInfo().getShardTotal(), true);
            builder.addField("", event.getJDA().getUsers().size() + " ユーザーのシャード\n" + event.getJDA().getGuilds().size() + " サーバー", true);
            builder.addField("", event.getJDA().getTextChannels().size() + " テキストチャンネル\n" + event.getJDA().getVoiceChannels().size() + " ボイスチャンネル", true);
        }
        builder.setFooter("再起動が行われた時間", "https://www.cosgy.dev/wp-content/uploads/2020/03/restart.jpg");
        builder.setTimestamp(event.getClient().getStartTime());
        event.replyEmbeds(builder.build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (oauthLink == null) {
            try {
                ApplicationInfo info = event.getJDA().retrieveApplicationInfo().complete();
                oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, perms) : "";
            } catch (Exception e) {
                Logger log = LoggerFactory.getLogger("OAuth2");
                log.error("招待リンクを生成できませんでした ", e);
                oauthLink = "";
            }
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColor() : color);
        builder.setAuthor("" + event.getSelfUser().getName() + "について!", null, event.getSelfUser().getAvatarUrl());
        String CosgyOwner = "Cosgy Devが運営、開発をしています。";
        String author = event.getJDA().getUserById(event.getClient().getOwnerId()) == null ? "<@" + event.getClient().getOwnerId() + ">"
                : Objects.requireNonNull(event.getJDA().getUserById(event.getClient().getOwnerId())).getName();
        StringBuilder descr = new StringBuilder().append("こんにちは！ **").append(event.getSelfUser().getName()).append("**です。 ")
                .append(description).append("は、").append(JDAUtilitiesInfo.AUTHOR + "の[コマンド拡張](" + JDAUtilitiesInfo.GITHUB + ") (")
                .append(JDAUtilitiesInfo.VERSION).append(")と[JDAライブラリ](https://github.com/DV8FromTheWorld/JDA) (")
                .append(JDAInfo.VERSION).append(")を使用しており、").append((IS_AUTHOR ? CosgyOwner : author + "が所有しています。"))
                .append(event.getSelfUser().getName()).append("についての質問などは[Cosgy Dev公式チャンネル](https://discord.gg/RBpkHxf)へお願いします。")
                .append("\nこのボットの使用方法は`").append(event.getClient().getTextualPrefix()).append(event.getClient().getHelpWord())
                .append("`で確認することができます。");
        builder.setDescription(descr);

        if (event.getJDA().getShardInfo().getShardTotal() == 1) {
            builder.addField("ステータス", event.getJDA().getGuilds().size() + " サーバー\n1 シャード", true);
            builder.addField("ユーザー", event.getJDA().getUsers().size() + " ユニーク\n" + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum() + " 合計", true);
            builder.addField("チャンネル", event.getJDA().getTextChannels().size() + " テキスト\n" + event.getJDA().getVoiceChannels().size() + " ボイス", true);
        } else {
            builder.addField("ステータス", (event.getClient()).getTotalGuilds() + " サーバー\nシャード " + (event.getJDA().getShardInfo().getShardId() + 1)
                    + "/" + event.getJDA().getShardInfo().getShardTotal(), true);
            builder.addField("", event.getJDA().getUsers().size() + " ユーザーのシャード\n" + event.getJDA().getGuilds().size() + " サーバー", true);
            builder.addField("", event.getJDA().getTextChannels().size() + " テキストチャンネル\n" + event.getJDA().getVoiceChannels().size() + " ボイスチャンネル", true);
        }
        builder.setFooter("再起動が行われた時間", "https://www.cosgy.dev/wp-content/uploads/2020/03/restart.jpg");
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());
    }
}
