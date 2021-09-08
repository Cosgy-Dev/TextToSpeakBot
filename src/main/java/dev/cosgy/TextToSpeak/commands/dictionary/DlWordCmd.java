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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.cosgy.TextToSpeak.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class DlWordCmd extends SlashCommand {
    private final Bot bot;
    Logger log = getLogger(this.getClass());

    public DlWordCmd(Bot bot) {
        this.bot = bot;
        this.name = "dlwd";
        this.help = "辞書に登録されている単語を削除します。";
        this.category = new Category("辞書");
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "word", "単語", true));

        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        HashMap<String, String> words = bot.getDictionary().GetWords(event.getGuild().getIdLong());

        String args = event.getOption("word").getAsString();

        try {
            if (!words.containsKey(args)) {
                event.reply(args + "は、辞書に登録されていません。").queue();
                return;
            }
        } catch (NullPointerException e) {
            return;
        }

        boolean result = bot.getDictionary().DeleteDictionary(event.getGuild().getIdLong(), args);

        if (result) {
            event.reply("単語を削除しました。").queue();
        } else {
            event.reply("削除中に問題が発生しました。").queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            EmbedBuilder ebuilder = new EmbedBuilder()
                    .setTitle("dlwordコマンド")
                    .addField("使用方法:", name + " <単語>", false)
                    .addField("説明:", help, false);
            event.reply(ebuilder.build());
            return;
        }

        HashMap<String, String> words = bot.getDictionary().GetWords(event.getGuild().getIdLong());

        String args = event.getArgs();

        try {
            if (!words.containsKey(args)) {
                event.reply(args + "は、辞書に登録されていません。");
                return;
            }
        } catch (NullPointerException e) {
            return;
        }

        boolean result = bot.getDictionary().DeleteDictionary(event.getGuild().getIdLong(), args);

        if (result) {
            event.reply("単語を削除しました。");
        } else {
            event.reply("削除中に問題が発生しました。");
        }
    }
}
