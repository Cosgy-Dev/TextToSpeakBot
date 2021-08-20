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
import dev.cosgy.TextToSpeak.audio.AudioHandler;
import dev.cosgy.TextToSpeak.slashCommands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class ByeCmd extends SlashCommand {
    protected final Bot bot;

    public ByeCmd(Bot bot) {
        this.bot = bot;
        this.name = "bye";
        this.help = "ボイスチャンネルから退出します。";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        //event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
        //InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages without having permissions in the channel and also allows ephemeral messages
        //hook.setEphemeral(true); // All messages here will now be ephemeral implicitly

        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        handler.stopAndClear();
        bot.getVoiceCreation().ClearGuildFolder(event.getGuild());
        event.getGuild().getAudioManager().closeAudioConnection();
        event.reply("ボイスチャンネルから切断しました。").queue();
    }
}
