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

package dev.cosgy.TextToSpeak.listeners;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import dev.cosgy.TextToSpeak.TextToSpeak;
import net.dv8tion.jda.api.entities.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandAudit implements CommandListener {
    @Override
    public void onCommand(CommandEvent event, Command command) {
        if (TextToSpeak.COMMAND_AUDIT_ENABLED) {
            Logger logger = LoggerFactory.getLogger("CommandAudit");
            String textFormat = event.isFromType(ChannelType.PRIVATE) ? "%s%s で %s#%s (%s) がコマンド %s を実行しました" : "%s の #%s で %s#%s (%s) がコマンド %s を実行しました";

            logger.info(String.format(textFormat,
                    event.isFromType(ChannelType.PRIVATE) ? "DM" : event.getGuild().getName(),
                    event.isFromType(ChannelType.PRIVATE) ? "" : event.getTextChannel().getName(),
                    event.getAuthor().getName(), event.getAuthor().getDiscriminator(), event.getAuthor().getId(),
                    event.getMessage().getContentDisplay()));
        }
    }
}
