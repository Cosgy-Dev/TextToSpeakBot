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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import dev.cosgy.TextToSpeak.Bot;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.exceptions.PermissionException;

public class JoinCmd extends Command {
    protected Bot bot;

    public JoinCmd(Bot bot){
        this.bot = bot;
        this.name = "join";
        this.help = "ボイスチャンネルに参加します。";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event){
        //Settings settings = event.getClient().getSettingsFor(event.getGuild());
        //TextChannel channel = settings.getTextChannel(event.getGuild());
        bot.getPlayerManager().setUpHandler(event.getGuild());

        GuildVoiceState userState = event.getMember().getVoiceState();

        if(!userState.inVoiceChannel()){
            event.reply("このコマンドを使用するには、ボイスチャンネルに参加している必要があります。");
            return;
        }

        try {
            event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
            event.reply(String.format("**%s**に接続しました。", userState.getChannel().getName()));
        } catch (PermissionException ex) {
            event.reply(event.getClient().getError() + String.format("**%s**に接続できません!", userState.getChannel().getName()));
        }
    }
}
