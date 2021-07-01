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

package dev.cosgy.TextToSpeak.slashCommands.dictionary;

import com.jagrosh.jdautilities.command.Command;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.audio.Dictionary;
import dev.cosgy.TextToSpeak.slashCommands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;

import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class AddWordCmd extends SlashCommand {
    private final Bot bot;
    Logger log = getLogger(this.getClass());

    public AddWordCmd(Bot bot){
        this.bot = bot;
        this.name = "addwd";
        this.help = "辞書に、単語を追加します。辞書に単語が存在している場合は上書きされます。";
        this.optionData = new OptionData[]{new OptionData(OptionType.STRING, "単語", "読み方を設定する単語", true),
                new OptionData(OptionType.STRING, "読み方", "読み方をカタカナで入力してください。", true)};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String word = event.getOption("単語").getAsString();
        String reading = event.getOption("読み方").getAsString();

        if(!isFullKana(reading)){
            event.reply("読み方はすべてカタカナで入力して下さい。").queue();
            return;
        }

        log.debug("単語追加:"+word +"-"+reading);

        Dictionary dic = bot.getDictionary();
        dic.UpdateDictionary(event.getGuild().getIdLong(), word,reading);
        event.reply("これから"+ bot.getJDA().getSelfUser().getName() + "は、`"+ word + "`を`"+ reading +"`と読みます。").queue();

    }

    public static boolean isFullKana(String str) {
        return Pattern.matches("^[ァ-ヶー]*$", str);
    }
}
