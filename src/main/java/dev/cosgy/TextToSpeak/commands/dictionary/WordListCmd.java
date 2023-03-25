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
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import dev.cosgy.TextToSpeak.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.List;
import java.util.stream.Collectors;

public class WordListCmd extends SlashCommand {
    private final Bot bot;
    private final Paginator.Builder builder;

    public WordListCmd(Bot bot) {
        this.bot = bot;
        this.name = "wdls";
        this.help = "辞書に登録してある単語をリストアップします。";
        this.category = new Category("辞書");
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
        builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignore) {}
                })
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1);

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply("単語一覧を表示します。").queue(m -> {
            List<String> wordList = bot.getDictionary().GetWords(event.getGuild().getIdLong())
                    .entrySet().stream()
                    .map(entry -> entry.getKey() + "-" + entry.getValue())
                    .collect(Collectors.toList());

            if (wordList.isEmpty()) {
                m.editOriginal("単語が登録されていません。").queue();
                return;
            }

            m.deleteOriginal().queue();
            builder.setText("単語一覧")
                    .setItems(wordList.toArray(new String[0]))
                    .setUsers(event.getUser())
                    .setColor(event.getGuild().getSelfMember().getColor());
            builder.build().paginate(event.getChannel());
        });
    }

    @Override
    protected void execute(CommandEvent event) {
        int pagenum = 1;
        try {
            pagenum = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException ignore) {}

        List<String> wordList = bot.getDictionary().GetWords(event.getGuild().getIdLong())
                .entrySet().stream()
                .map(entry -> entry.getKey() + "-" + entry.getValue())
                .collect(Collectors.toList());

        if (wordList.isEmpty()) {
            event.reply("単語が登録されていません。");
            return;
        }

        builder.setText("単語一覧")
                .setItems(wordList.toArray(new String[0]))
                .setUsers(event.getAuthor())
                .setColor(event.getSelfMember().getColor());
        builder.build().paginate(event.getChannel(), pagenum);
    }
}
