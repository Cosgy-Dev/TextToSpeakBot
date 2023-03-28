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
package dev.cosgy.textToSpeak.commands

import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent
import net.dv8tion.jda.api.Permission
import java.util.function.Predicate

abstract class AdminCommand : SlashCommand() {
    init {
        this.category = Category("管理", Predicate { event: CommandEvent ->
            if (event.isOwner || event.member.isOwner) return@Predicate true
            if (event.guild == null) return@Predicate true
            event.member.hasPermission(Permission.MANAGE_SERVER)
        })
        guildOnly = true
        userPermissions = arrayOf(Permission.ADMINISTRATOR)
    }

    companion object {
        fun checkAdminPermission(client: CommandClient, event: SlashCommandEvent): Boolean {
            if (event.user.id == client.ownerId || event.member!!.isOwner) return true
            return if (event.guild == null) true else event.member!!.hasPermission(Permission.MANAGE_SERVER)
        }
    }
}