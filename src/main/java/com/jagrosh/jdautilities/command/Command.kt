package com.jagrosh.jdautilities.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.function.Predicate

open class Command {
    open var name: String = ""
    open var help: String = ""
    open var arguments: String? = null
    open var guildOnly: Boolean = false
    open var ownerCommand: Boolean = false
    open var hidden: Boolean = false
    open var aliases: Array<String> = emptyArray()
    open var options: List<OptionData> = emptyList()
    open var children: Array<SlashCommand> = emptyArray()
    open var category: Category? = null
    open var userPermissions: Array<Permission> = emptyArray()
    open var botPermissions: Array<Permission> = emptyArray()

    open val isHidden: Boolean
        get() = hidden

    open val isOwnerCommand: Boolean
        get() = ownerCommand

    open fun execute(event: CommandEvent) {}

    open fun execute(event: SlashCommandEvent) {}

    open fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}

    class Category(
        val name: String,
        val predicate: Predicate<CommandEvent>? = null
    ) {
        fun test(event: CommandEvent): Boolean = predicate?.test(event) ?: true
    }
}

open class SlashCommand : Command()
