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

package dev.cosgy.TextToSpeak.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.commands.OwnerCommand;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ShutdownCmd extends OwnerCommand {
    private final Bot bot;

    public ShutdownCmd(Bot bot) {
        this.bot = bot;
        this.name = "shutdown";
        this.help = "一時ファイルを削除してボットを停止します。";
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply("一時ファイルを削除しています。").queue(m -> {
            File tmp = new File("tmp");
            File wav = new File("wav");

            try {
                FileUtils.cleanDirectory(tmp);
                FileUtils.cleanDirectory(wav);
            } catch (IOException e) {
                e.printStackTrace();
            }
            m.editOriginal("一時ファイルを削除しました。").queue();
            event.reply(event.getClient().getWarning() + "シャットダウンしています...").queue();
            bot.shutdown();
        });
    }

    @Override
    protected void execute(CommandEvent event) {

        event.reply("一時ファイルを削除しています。", m -> {
            File tmp = new File("tmp");
            File wav = new File("wav");

            try {
                FileUtils.cleanDirectory(tmp);
                FileUtils.cleanDirectory(wav);
            } catch (IOException e) {
                e.printStackTrace();
            }
            m.editMessage("一時ファイルを削除しました。").queue();
            event.replyWarning("シャットダウンしています...");
            bot.shutdown();
        });
    }
}
