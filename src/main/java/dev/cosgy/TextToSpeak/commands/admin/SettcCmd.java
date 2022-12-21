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

package dev.cosgy.TextToSpeak.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.commands.AdminCommand;
import dev.cosgy.TextToSpeak.settings.Settings;
import dev.cosgy.TextToSpeak.utils.FormatUtil;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SettcCmd extends AdminCommand {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public SettcCmd(Bot bot) {
        this.name = "settc";
        this.help = "読み上げをするチャンネルを設定します。読み上げするチャンネルを設定していない場合は、joinコマンドを最後に実行したチャンネルが読み上げ対象になります。";
        this.arguments = "<チャンネル名|NONE|なし>";

        this.children = new SlashCommand[]{new Set(), new None()};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + "チャンネルまたはNONEを含めてください。");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().toLowerCase().matches("(none|なし)")) {
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess() + "読み上げをするチャンネルの設定を無効にしました。");
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty())
                event.reply(event.getClient().getWarning() + "一致するチャンネルが見つかりませんでした \"" + event.getArgs() + "\"");
            else if (list.size() > 1)
                event.reply(event.getClient().getWarning() + FormatUtil.listOfTChannels(list, event.getArgs()));
            else {
                s.setTextChannel(list.get(0));
                log.info("読み上げを行うチャンネルを設定しました。");
                event.reply(event.getClient().getSuccess() + "読み上げるチャンネルを<#" + list.get(0).getId() + ">に設定しました。");
            }
        }
    }

    private static class Set extends AdminCommand {
        public Set() {
            this.name = "set";
            this.help = "読み上げるチャンネルを設定";

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.CHANNEL, "channel", "テキストチャンネル", true));

            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Settings s = event.getClient().getSettingsFor(event.getGuild());


            if (event.getOption("channel").getChannelType() != ChannelType.TEXT) {
                event.reply(event.getClient().getError() + "テキストチャンネルを設定して下さい。").queue();
                return;
            }
            Long channelId = event.getOption("channel").getAsLong();
            TextChannel tc = event.getGuild().getTextChannelById(channelId);

            s.setTextChannel(tc);
            event.reply(event.getClient().getSuccess() + "読み上げるチャンネルを<#" + tc.getId() + ">に設定しました。").queue();

        }
    }

    private static class None extends AdminCommand {
        public None() {
            this.name = "none";
            this.help = "読み上げるチャンネル設定をリセットします。";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (!checkAdminPermission(event.getClient(), event)) {
                event.reply(event.getClient().getWarning() + "権限がないため実行できません。").queue();
                return;
            }
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess() + "読み上げるチャンネル設定をリセットしました。").queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess() + "読み上げるチャンネル設定をリセットしました。");
        }
    }

}
