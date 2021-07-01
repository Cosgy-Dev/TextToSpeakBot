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

import com.jagrosh.jdautilities.command.Command;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.settings.UserSettings;
import dev.cosgy.TextToSpeak.slashCommands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public class SetVoiceCmd extends SlashCommand {
    protected Bot bot;

    public SetVoiceCmd(Bot bot){
        this.bot = bot;
        this.name = "setvoice";
        this.help = "声の種類を変更することができます。";

        ArrayList<String> voices = bot.getVoiceCreation().getVoices();

        List<SubcommandData> list = new ArrayList<>();

        for (String voice : voices){
            list.add(new SubcommandData(voice, "声データ"));
        }

        this.subCommandData = list.toArray(new SubcommandData[list.size()]);
    }


    @Override
    protected void execute(SlashCommandEvent event) {
        ArrayList<String> voices = bot.getVoiceCreation().getVoices();

        String args = event.getSubcommandName();

        if(voices.contains(args)){
            UserSettings settings = bot.getUserSettingsManager().getSettings(event.getUser().getIdLong());
            settings.setVoice(args);
            event.reply("声データを`"+ args + "`に設定しました。").queue();
        }else{
            event.reply("有効な声データを選択して下さい。").queue();
        }
    }
}
