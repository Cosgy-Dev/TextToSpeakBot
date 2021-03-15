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

public class SetIntonationCmd extends Command {
    protected Bot bot;

    public SetIntonationCmd(Bot bot){
        this.bot = bot;
        this.name = "setinto";
        this.help = "抑揚の設定を変更します。";
        this.guildOnly = false;
        this.category = new Category("設定");
    }
    @Override
    protected void execute(CommandEvent event){
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            EmbedBuilder ebuilder = new EmbedBuilder()
                    .setTitle("setintoコマンド")
                    .addField("使用方法:", name+" <数値(0.0~)>", false)
                    .addField("説明:","抑揚の設定を変更します。抑揚は、0.0以上の数値で設定して下さい。",false);
            event.reply(ebuilder.build());
            return;
        }
        String args = event.getArgs();
        boolean result;
        float value = 0.0f;
        try {
            value = Float.parseFloat(args);
            result = true;
        }
        catch (NumberFormatException e) {
            result = false;
        }
        if(!result){
            event.reply("数値を設定して下さい。");
            return;
        }
        if(!(0.1f <= value && value <= 100.0f)){
            event.reply("有効な数値を設定して下さい。0.1~100.0");
            return;
        }
        UserSettings settings = bot.getUserSettingsManager().getSettings(event.getAuthor().getIdLong());
        settings.setIntonation(value);
        event.reply("抑揚を"+value+"に設定しました。");
    }
}
