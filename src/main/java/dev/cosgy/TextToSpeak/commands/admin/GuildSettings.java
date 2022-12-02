//////////////////////////////////////////////////////////////////////////////////////////
//  Copyright 2021 Cosgy Dev                                                             /
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

package dev.cosgy.TextToSpeak.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.commands.AdminCommand;
import dev.cosgy.TextToSpeak.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Objects;

public class GuildSettings extends AdminCommand {
    private final Bot bot;
    Logger log = LoggerFactory.getLogger(this.getClass());

    public GuildSettings(Bot bot) {
        this.bot = bot;
        this.name = "gsettings";
        this.help = "ギルドの現在の設定を確認できます。";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if(!checkAdminPermission(client, event)){
            event.reply(client.getWarning()+"権限がないため実行できません。").queue();
            return;
        }

        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());

        String text = "null";
        if(settings.getTextChannel(event.getGuild()) != null){
            text = settings.getTextChannel(event.getGuild()).getName();
        }

        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(Color.orange)
                .setTitle(event.getGuild().getName() + "の設定")
                .addField("ユーザー名読み上げ：", String.valueOf(Objects.requireNonNull(settings).isReadName()), false)
                .addField("参加、退出時の読み上げ：", String.valueOf(settings.isJoinAndLeaveRead()), false)
                //.addField("接頭語：", settings.getPrefix(), false)
                .addField("読み上げるチャンネル：", text, false)
                .addField("読み上げの主音量：", String.valueOf(settings.getVolume()), false);
        event.replyEmbeds(ebuilder.build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());
        String text = "null";
        if(settings.getTextChannel(event.getGuild()) != null){
            text = settings.getTextChannel(event.getGuild()).getName();
        }

        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(Color.orange)
                .setTitle(event.getGuild().getName() + "の設定")
                .addField("ユーザー名読み上げ：", String.valueOf(Objects.requireNonNull(settings).isReadName()), false)
                .addField("参加、退出時の読み上げ：", String.valueOf(settings.isJoinAndLeaveRead()), false)
                //.addField("接頭語：", settings.getPrefix(), false)
                .addField("読み上げるチャンネル：", text, false)
                .addField("読み上げの主音量：", String.valueOf(settings.getVolume()), false);
        event.reply(ebuilder.build());
    }
}
