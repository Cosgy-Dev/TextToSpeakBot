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

import java.math.BigDecimal;

public class SetVoiceQualityA extends Command {
    protected Bot bot;
    public SetVoiceQualityA(Bot bot){
        this.bot = bot;
        this.name = "setqa";
        this.help = "声質aの設定を変更します。";
        this.guildOnly = false;
        this.category = new Category("設定");
    }

    @Override
    protected void execute(CommandEvent event){
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            EmbedBuilder ebuilder = new EmbedBuilder()
                    .setTitle("setqaコマンド")
                    .addField("使用方法:", name+" <数値(0.0~1.0)>", false)
                    .addField("説明:","読み上げの速度を設定します。読み上げ速度は、0.0以上1.0以下の数値で設定して下さい。",false);
            event.reply(ebuilder.build());
            return;
        }
        String args = event.getArgs();
        boolean result;
        float value = 0.0f;
        BigDecimal bd = null;
        try {
            //value = Float.parseFloat(args);
            bd = new BigDecimal(args);
            result = true;
        }
        catch (NumberFormatException e) {
            result = false;
        }
        if(!result){
            event.reply("数値を設定して下さい。");
            return;
        }

        BigDecimal min = new BigDecimal("0.0");
        BigDecimal max = new BigDecimal("1.0");

        //if(!(0.1f <= value && value <= 1.0f)){
        if(!(min.compareTo(bd) < 0 && max.compareTo(bd) > 0)){
            event.reply("有効な数値を設定して下さい。0.1~1.0");
            return;
        }
        UserSettings settings = bot.getUserSettingsManager().getSettings(event.getAuthor().getIdLong());
        settings.setVoiceQualityA(bd.floatValue());
        event.reply("声質aを"+bd+"に設定しました。");
    }
}
