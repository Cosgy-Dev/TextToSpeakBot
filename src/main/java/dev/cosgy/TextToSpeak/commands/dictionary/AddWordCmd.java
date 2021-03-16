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

package dev.cosgy.TextToSpeak.commands.dictionary;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.TextToSpeak;
import dev.cosgy.TextToSpeak.audio.Dictionary;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;

import java.awt.*;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class AddWordCmd extends Command {
    private final Bot bot;
    Logger log = getLogger(this.getClass());

    public AddWordCmd(Bot bot){
        this.bot = bot;
        this.name = "addword";
        this.help = "辞書に、単語を追加します。辞書に単語が存在している場合は上書きされます。";
        this.category = new Category("辞書");
    }

    @Override
    protected void execute(CommandEvent event){
        String[] parts = event.getArgs().split("\\s+", 2);
        String word = null;
        String reading = null;

        if(parts.length < 2){
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Color.red)
                    .setTitle("AddWordコマンド")
                    .addField("コマンドが無効です。", "単語と読み方の２つを入力して実行して下さい。", false)
                    .addField("使用方法:",name +" <単語> <読み方>", false);

            event.reply(builder.build());
            return;
        }

        word = parts[0];
        reading = parts[1];

        if(!isFullKana(reading)){
            event.reply("読み方はすべてカタカナで入力して下さい。");
            return;
        }

        log.debug("単語追加:"+word +"-"+reading);

        Dictionary dic = bot.getDictionary();
        dic.UpdateDictionary(event.getGuild().getIdLong(), word,reading);
        event.reply("これから"+ bot.getJDA().getSelfUser().getName() + "は、`"+ word + "`を`"+ reading +"`と読みます。");
    }

    public static boolean isFullKana(String str) {
        return Pattern.matches("^[ァ-ヶー]*$", str);
    }
}
