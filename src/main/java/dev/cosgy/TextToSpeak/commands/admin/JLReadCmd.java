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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JLReadCmd extends AdminCommand {
    private final Bot bot;
    Logger log = LoggerFactory.getLogger(this.getClass());

    public JLReadCmd(Bot bot) {
        this.bot = bot;
        this.name = "jlread";
        this.help = "ボイスチャンネルにユーザーが参加または退出した時にユーザー名を読み上げるか否かを設定します。";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if(!checkAdminPermission(event.getClient(), event)){
            event.reply(event.getClient().getWarning()+"権限がないため実行できません。").queue();
            return;
        }
        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());

        if (settings.isJoinAndLeaveRead()) {
            settings.setJoinAndLeaveRead(false);
            event.reply("ボイスチャンネルにユーザーが参加、退出した際の読み上げを無効にしました。").queue();
        } else {
            settings.setJoinAndLeaveRead(true);
            event.reply("ボイスチャンネルにユーザーが参加、退出した際の読み上げを有効にしました。").queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());

        if (settings.isJoinAndLeaveRead()) {
            settings.setJoinAndLeaveRead(false);
            event.reply("ボイスチャンネルにユーザーが参加、退出した際の読み上げを無効にしました。");
        } else {
            settings.setJoinAndLeaveRead(true);
            event.reply("ボイスチャンネルにユーザーが参加、退出した際の読み上げを有効にしました。");
        }
    }
}
