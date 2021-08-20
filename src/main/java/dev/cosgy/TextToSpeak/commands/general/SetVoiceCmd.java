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
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.settings.UserSettings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;

public class SetVoiceCmd extends Command {
    protected Bot bot;

    public SetVoiceCmd(Bot bot) {
        this.bot = bot;
        this.name = "setvoice";
        this.help = "声の種類を変更することができます。";
        this.guildOnly = false;
        this.category = new Category("設定");
    }

    @Override
    protected void execute(CommandEvent event) {
        ArrayList<String> voices = bot.getVoiceCreation().getVoices();
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            EmbedBuilder ebuilder = new EmbedBuilder()
                    .setTitle("setvoiceコマンド")
                    .addField("声データ一覧：", voices.toString(), false)
                    .addField("使用方法：", name + " <声データの名前>", false);
            event.reply(ebuilder.build());
            return;
        }
        String args = event.getArgs();
        if (voices.contains(args)) {
            UserSettings settings = bot.getUserSettingsManager().getSettings(event.getAuthor().getIdLong());
            settings.setVoice(args);
            event.reply("声データを`" + args + "`に設定しました。");
        } else {
            event.reply("有効な声データを選択して下さい。");
        }
    }
}
