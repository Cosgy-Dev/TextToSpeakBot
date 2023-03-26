//////////////////////////////////////////////////////////////////////////////////////////
//  Copyright 2023 Cosgy Dev                                                             /
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

package dev.cosgy.TextToSpeak.commands.general;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.settings.UserSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetVoiceCmd extends SlashCommand {
    protected Bot bot;
    String[] voices;

    public SetVoiceCmd(Bot bot) {
        this.bot = bot;
        this.name = "setvoice";
        this.help = "声の種類を変更することができます。";
        this.guildOnly = false;
        this.category = new Category("設定");

        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "name", "声データの名前", true, true));

        this.options = options;
        voices = bot.getVoiceCreation().getVoices().toArray(new String[0]);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (event.getOption("name") == null) {
            EmbedBuilder ebuilder = new EmbedBuilder()
                    .setTitle("setvoiceコマンド")
                    .addField("声データ一覧：", voices.toString(), false)
                    .addField("使用方法：", name + " <声データの名前>", false);
            event.replyEmbeds(ebuilder.build()).queue();
            return;
        }
        String viceName = event.getOption("name").getAsString();

        if (isValidVoice(viceName)) {
            UserSettings settings = bot.getUserSettingsManager().getSettings(event.getUser().getIdLong());
            settings.setVoice(viceName);
            event.reply("声データを`" + viceName + "`に設定しました。").queue();
        } else {
            event.reply("有効な声データを選択して下さい。").queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        ArrayList<String> voices = bot.getVoiceCreation().getVoices();
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            EmbedBuilder ebuilder = new EmbedBuilder()
                    .setTitle("setvoiceコマンド")
                    .addField("声データ一覧：", voices.toString(), false)
                    .addField("使用方法：", name + " <声データの名前>", false);
            event.reply(ebuilder.build());
            return;
        }
        String args = event.getArgs();
        if (voices.contains(args)) {
            UserSettings settings = bot.getUserSettingsManager().getSettings(event.getAuthor().getIdLong());
            settings.setVoice(args);
            event.reply("声データを`" + args + "`に設定しました。");
        } else {
            event.reply("有効な声データを選択して下さい。");
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("setvoice") && event.getFocusedOption().getName().equals("name")) {
            List<Command.Choice> options = Stream.of(voices)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(word -> new Command.Choice(word, word)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
        super.onAutoComplete(event);
    }

    /**
     * ユーザーが入力した声の名前が有効かを確認
     *
     * @param voice ユーザーが入力した声の名前
     * @return 名前が有効の場合は true 無効な場合は false
     */
    private boolean isValidVoice(String voice) {
        for (String v : voices) {
            if (Objects.equals(v, voice)) {
                return true;
            }
        }
        return false;
    }
}
