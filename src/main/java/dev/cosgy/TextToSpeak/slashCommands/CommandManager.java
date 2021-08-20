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

package dev.cosgy.TextToSpeak.slashCommands;

import dev.cosgy.TextToSpeak.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class CommandManager extends ListenerAdapter {
    private final Bot bot;
    protected List<SlashCommand> commands;
    HashMap<String, SlashCommand> commandsMap = new HashMap<>();
    Logger log = getLogger(this.getClass());

    public CommandManager(Bot bot, List<SlashCommand> commands) {
        this.bot = bot;
        this.commands = commands;
    }

    @Override
    public void onReady(ReadyEvent event) {
        log.info("コマンド作成中");
        JDA jda = bot.getJDA();
        CommandListUpdateAction update = jda.updateCommands();
        for (SlashCommand cmd : commands) {
            if (cmd.getOptionData().length == 0 && cmd.getSubCommandData().length == 0) {
                update.addCommands(new CommandData(cmd.getName(), cmd.getHelp())).queue();
            } else if (cmd.getSubCommandData().length != 0) {
                update.addCommands(new CommandData(cmd.getName(), cmd.getHelp())
                        .addSubcommands(cmd.getSubCommandData())).queue();
            } else {
                update.addCommands(new CommandData(cmd.getName(), cmd.getHelp())
                        .addOptions(cmd.optionData)).queue();
            }
            commandsMap.put(cmd.getName(), cmd);
        }
        update.queue();
        bot.setJDA(jda);
        log.info("コマンド作成完了");
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        log.debug("コマンド実行");
        if (!commandsMap.containsKey(event.getName())) return;
        log.debug("コマンド実行2");
        SlashCommand sc = commandsMap.get(event.getName());
        sc.execute(event);
    }
}
