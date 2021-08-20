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

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public abstract class SlashCommand {
    /**
     * {@code /<コマンド名>} 形式でコマンドを使用します。
     */
    protected String name = "null";

    /**
     * スラッシュコマンド画面とヘルプコマンドのコマンド説明欄に表示されるテキスト
     */
    protected String help = "no help available";

    /**
     * スラッシュコマンドのオプション設定
     */
    protected OptionData[] optionData = new OptionData[0];

    protected SubcommandData[] subCommandData = new SubcommandData[0];

    protected abstract void execute(SlashCommandEvent event);

    public final void run(SlashCommandEvent event) {
        // availability check
        if (event.getChannelType() == ChannelType.TEXT) {

        }
    }

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public OptionData[] getOptionData() {
        return optionData;
    }

    public SubcommandData[] getSubCommandData() {
        return subCommandData;
    }
}
