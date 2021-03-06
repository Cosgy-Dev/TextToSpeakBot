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
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.commands.AdminCommand;
import dev.cosgy.TextToSpeak.settings.Settings;
import dev.cosgy.TextToSpeak.settings.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetReadNameCmd extends AdminCommand {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private Bot bot;

    public SetReadNameCmd(Bot bot) {
        this.bot = bot;
        this.name = "setreadname";
        this.help = "テキストを読み上げる際にユーザー名も読み上げるかを設定します。";
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());

        if(settings.isReadName()){
            settings.setReadName(false);
            event.reply("ユーザー名の読み上げを無効にしました。");
        }else{
            settings.setReadName(true);
            event.reply("ユーザー名の読み上げを有効にしました。");
        }
    }
}
