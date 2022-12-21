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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.settings.Settings;
import dev.cosgy.TextToSpeak.utils.ReadChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.awt.*;
import java.util.Objects;

public class JoinCmd extends SlashCommand {
    protected Bot bot;

    public JoinCmd(Bot bot) {
        this.bot = bot;
        this.name = "join";
        this.help = "ボイスチャンネルに参加します。";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel channel = settings.getTextChannel(event.getGuild());
        // VoiceChannel voiceChannel = settings.getVoiceChannel(event.getGuild());
        bot.getPlayerManager().setUpHandler(event.getGuild());

        GuildVoiceState userState = event.getMember().getVoiceState();
        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(new Color(76, 108, 179));
        builder.setTitle("VCに接続");

        if (!userState.inAudioChannel() || userState.isDeafened()) {
            builder.setDescription(String.format("このコマンドを使用するには、%sに参加している必要があります。", "音声チャンネル"));
            event.replyEmbeds(builder.build()).queue();
            return;
        }

        builder.addField("読み上げ対象", "<#" + Objects.requireNonNullElseGet(channel, event::getChannel).getId() + ">", false);

        try {
            event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
            builder.addField("ボイスチャンネル", String.format("**%s**", userState.getChannel().getName()), false);
            builder.setDescription("ボイスチャンネルへの接続に成功しました。");

            event.getHook().sendMessageEmbeds(builder.build()).queue();
            ReadChannel.setChannel(event.getGuild().getIdLong(), event.getTextChannel().getIdLong());
        } catch (PermissionException ex) {
            builder.appendDescription(event.getClient().getError() + String.format("**%s**に接続できません!", userState.getChannel().getName()));
            builder.addField("ボイスチャンネル", event.getClient().getError() + String.format("**%s**に接続できません!",
                    userState.getChannel().getName()), false);
            event.getHook().sendMessageEmbeds(builder.build()).queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel channel = settings.getTextChannel(event.getGuild());
        bot.getPlayerManager().setUpHandler(event.getGuild());

        GuildVoiceState userState = event.getMember().getVoiceState();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(76, 108, 179));
        builder.setTitle("VCに接続");

        if (!userState.inAudioChannel()) {
            builder.setDescription("このコマンドを使用するには、ボイスチャンネルに参加している必要があります。");
            event.reply(builder.build());
            return;
        }
        if (channel == null) {
            builder.addField("読み上げ対象", event.getChannel().getName(), true);
        } else {
            builder.addField("読み上げ対象", channel.getName(), true);
        }
        try {
            event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
            builder.addField("ボイスチャンネル", String.format("**%s**", userState.getChannel().getName()), false);
            builder.setDescription("ボイスチャンネルへの接続に成功しました。");


            event.reply(builder.build());
            ReadChannel.setChannel(event.getGuild().getIdLong(), event.getTextChannel().getIdLong());
        } catch (PermissionException ex) {
            builder.setDescription("ボイスチャンネルへの接続に失敗しました。");
            event.reply(builder.build());
        }
    }
}
