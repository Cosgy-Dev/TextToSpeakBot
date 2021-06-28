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

package dev.cosgy.TextToSpeak.slashCommands.general;

import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.slashCommands.SlashCommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class JoinCmd extends SlashCommand {
    protected Bot bot;

    public JoinCmd(Bot bot){
        this.name = "join";
        this.help = "ボイスチャンネルに参加します。";
        //this.optionData = new OptionData[]{new OptionData(OptionType.CHANNEL, "ボイスチャンネル", "ボットを参加させるボイスチャンネル", true)};
        this.bot = bot;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
        InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages without having permissions in the channel and also allows ephemeral messages
        hook.setEphemeral(true); // All messages here will now be ephemeral implicitly


        bot.getPlayerManager().setUpHandler(event.getGuildChannel().getGuild());

        GuildVoiceState userState = event.getMember().getVoiceState();


        if(!userState.inVoiceChannel()){
            hook.sendMessage("このコマンドを使用するには、ボイスチャンネルに参加している必要があります。").queue();
            return;
        }

        try {
            event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());

            hook.sendMessage(String.format("**%s**に接続しました。", userState.getChannel().getName())).queue();
        } catch (PermissionException ex) {
            hook.sendMessage(String.format("**%s**に接続できません!", userState.getChannel().getName())).queue();
        }
    }
}
