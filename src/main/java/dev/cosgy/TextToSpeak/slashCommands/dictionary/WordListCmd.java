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

import com.jagrosh.jdautilities.menu.Paginator;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.slashCommands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class WordListCmd extends SlashCommand {
    private final Bot bot;
    private final Paginator.Builder builder;
    Logger log = getLogger(this.getClass());

    public WordListCmd(Bot bot) {
        this.bot = bot;
        this.name = "wdls";
        this.help = "辞書に、登録してある単語をリストアップします。";
        this.optionData = new OptionData[]{new OptionData(OptionType.INTEGER, "ページ番号", "リストのページ番号", false)};
        builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignore) {
                    }
                })
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);
    }


    @Override
    protected void execute(SlashCommandEvent event) {
        int pagenum = 1;
        try {
            pagenum = Integer.parseInt(event.getOption("ページ番号").getAsString());
        } catch (NumberFormatException ignore) {
        }
        List<String> list = new ArrayList<>();
        try {
            HashMap<String, String> words = bot.getDictionary().GetWords(event.getGuild().getIdLong());

            for (String key : words.keySet()) {
                list.add(key + "-" + words.get(key));
            }
        } catch (NullPointerException ignored) {
            return;
        }
        String[] wordList = new String[list.size()];

        if (list.size() == 0) {
            event.reply("単語が登録されていません。");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            wordList[i] = list.get(i);
        }
        builder.setText("単語一覧")
                .setItems(wordList)
                .setUsers(event.getUser())
                .setColor(event.getMember().getColor());
        builder.build().paginate(event.getChannel(), pagenum);
    }
}
