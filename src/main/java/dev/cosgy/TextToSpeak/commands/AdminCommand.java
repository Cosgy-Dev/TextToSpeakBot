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

package dev.cosgy.TextToSpeak.commands;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;

public abstract class AdminCommand extends SlashCommand {
    public AdminCommand() {
        this.category = new Category("管理", event ->
        {
            if (event.isOwner() || event.getMember().isOwner())
                return true;
            if (event.getGuild() == null)
                return true;
            return event.getMember().hasPermission(Permission.MANAGE_SERVER);
        });
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }

    public static boolean checkAdminPermission(CommandClient client, SlashCommandEvent event) {
        if (event.getUser().getId().equals(client.getOwnerId()) || event.getMember().isOwner())
            return true;
        if (event.getGuild() == null)
            return true;
        return event.getMember().hasPermission(Permission.MANAGE_SERVER);
    }
}
