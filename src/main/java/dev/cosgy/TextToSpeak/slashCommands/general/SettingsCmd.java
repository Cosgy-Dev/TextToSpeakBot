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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;

public class SettingsCmd extends SlashCommand {
    protected Bot bot;

    public SettingsCmd(Bot bot) {
        this.bot = bot;
        this.name = "settings";
        this.help = "現在の設定を確認します。";
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        UserSettings settings = bot.getUserSettingsManager().getSettings(event.getUser().getIdLong());

        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(Color.orange)
                .setTitle(event.getUser().getName() + "の設定")
                .addField("声：", settings.getVoice(), false)
                .addField("速度：", String.valueOf(settings.getSpeed()), false)
                .addField("抑揚：", String.valueOf(settings.getIntonation()), false)
                .addField("声質a：", String.valueOf(settings.getVoiceQualityA()), false)
                .addField("声質fm：", String.valueOf(settings.getVoiceQualityFm()), false);
        event.replyEmbeds(ebuilder.build()).queue();
    }
}
