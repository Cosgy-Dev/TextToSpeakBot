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
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.audio.Dictionary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AddWordCmd extends SlashCommand {
        private final Bot bot;
        private static final Logger logger = LoggerFactory.getLogger(AddWordCmd.class);
        private static final Color SUCCESS_COLOR = new Color(0, 163, 129);
        private static final Color ERROR_COLOR = Color.RED;
        private static final String INVALID_ARGS_MESSAGE = "コマンドが無効です。単語と読み方の２つを入力して実行して下さい。";
        private static final String USAGE_MESSAGE = "使用方法: /addWord <単語> <読み方>";
        private static final String KATAKANA_REGEX = "^[ァ-ヶー]*$";

        public AddWordCmd(Bot bot) {
                this.bot = bot;
                this.name = "addWord";
                this.help = "辞書に単語を追加します。辞書に単語が存在している場合は上書きされます。";
                this.category = new Category("辞書");

                List<OptionData> options = new ArrayList<>();
                options.add(new OptionData(OptionType.STRING, "word", "単語", true));
                options.add(new OptionData(OptionType.STRING, "reading", "読み方（カタカナ）", true));
                this.options = options;
        }

        private static boolean isKatakana(String str) {
                return Pattern.matches(KATAKANA_REGEX, str);
        }

        private void handleCommand(SlashCommandEvent event, String word, String reading) {
                long guildId = event.getGuild().getIdLong();
                Dictionary dictionary = bot.getDictionary();
                boolean isWordExist = dictionary.GetWords(guildId).containsKey(word);

                if (isWordExist) {
                        EmbedBuilder builder = new EmbedBuilder()
                    .setColor(ERROR_COLOR)
                    .setTitle("単語が既に存在します。上書きしますか？")
                    .setDescription("既に辞書に登録されている単語を上書きする場合は、このメッセージにリアクションしてください。");
                        event.replyEmbeds(builder.build()).queue((message) -> {
                                message.addReaction("\u2705").queue();
                                bot.getReactionManager().subscribeAction(message.getIdLong(), (userId) -> {
                                        dictionary.UpdateDictionary(guildId, word, reading);
                                        sendSuccessMessage(even);
                                });
                        });
                } else {
                        dictionary.UpdateDictionary(guildId, word, reading);
                        sendSuccessMessage(event);
                }
        }

        private void sendSuccessMessage(SlashCommandEvent event) {
                EmbedBuilder builder = new EmbedBuilder()
            .setColor(SUCCESS_COLOR)
            .setTitle("単語を追加しました。")
            .addField("単語", "```" + event.getOption("word").getAsString() + "```", false)
            .addField("読み", "```" + event.getOption("reading").getAsString() + "```", false);
                event.replyEmbeds(builder.build()).queue();
        }

        @Override
        protected void execute(SlashCommandEvent event) {
                String word = event.getOption("word").getAsString();
                String reading = event.getOption("reading").getAsString();

                if (!isKatakana(reading)) {
                        event.reply("読み方はすべてカタカナで入力して下さい。").setEphemeral(true).queue();
                        return;
                }

                handleCommand(event, word, reading);
        }

        @Override
protected void execute(CommandEvent event) {
                String[] parts = event.getArgs().split("\\s+", 2);

                if (parts.length < 2) {
                        EmbedBuilder builder = new EmbedBuilder()
                .setColor(ERROR_COLOR)
                .setTitle("AddWordコマンド")
                .addField("エラー", INVALID_ARGS_MESSAGE, false)
                .addField("使用方法:", USAGE_MESSAGE, false);
                        event.reply(builder.build());
                        return;
                }

                String word = parts[0];
                String reading = parts[1];

                if (!isKatakana(reading)) {
                        event.reply("読み方はすべてカタカナで入力して下さい。");
                        return;
                }

                handleCommand(event, word, reading);
        }
}