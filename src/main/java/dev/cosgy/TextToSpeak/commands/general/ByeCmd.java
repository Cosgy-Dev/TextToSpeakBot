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

package dev.cosgy.TextToSpeak.commands.general;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.audio.AudioHandler;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class ByeCmd extends SlashCommand {
    protected final Bot bot;

    public ByeCmd(Bot bot) {
        this.bot = bot;
        this.name = "bye";
        this.help = "ボイスチャンネルから退出します。";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        handler.stopAndClear();
        bot.getVoiceCreation().ClearGuildFolder(event.getGuild());
        event.getGuild().getAudioManager().closeAudioConnection();
        event.reply("ボイスチャンネルから切断しました。").queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        handler.stopAndClear();
        bot.getVoiceCreation().ClearGuildFolder(event.getGuild());
        event.getGuild().getAudioManager().closeAudioConnection();
        event.reply("ボイスチャンネルから切断しました。");
    }
}
