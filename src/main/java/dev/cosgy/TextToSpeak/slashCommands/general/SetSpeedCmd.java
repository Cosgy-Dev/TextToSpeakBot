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

package dev.cosgy.TextToSpeak.slashCommands.general;

import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.settings.UserSettings;
import dev.cosgy.TextToSpeak.slashCommands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.math.BigDecimal;

public class SetSpeedCmd extends SlashCommand {
    protected Bot bot;

    public SetSpeedCmd(Bot bot) {
        this.bot = bot;
        this.name = "setspeed";
        this.help = "読み上げ速度の設定を変更します。";
        this.optionData = new OptionData[]{new OptionData(OptionType.STRING, "数値", "0.0以上の数値で設定して下さい。", true)};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String args = event.getOption("数値").getAsString();
        boolean result;
        BigDecimal bd = null;
        try {
            bd = new BigDecimal(args);
            result = true;
        } catch (NumberFormatException e) {
            result = false;
        }
        if (!result) {
            event.reply("数値を設定して下さい。").queue();
            return;
        }

        BigDecimal min = new BigDecimal("0.0");
        BigDecimal max = new BigDecimal("100.0");

        if (!(min.compareTo(bd) < 0 && max.compareTo(bd) > 0)) {
            event.reply("有効な数値を設定して下さい。0.1~100.0").queue();
            return;
        }
        UserSettings settings = bot.getUserSettingsManager().getSettings(event.getUser().getIdLong());
        settings.setSpeed(bd.floatValue());
        event.reply("速度を" + bd + "に設定しました。").queue();
    }
}
