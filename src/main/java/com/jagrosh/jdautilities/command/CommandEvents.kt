package com.jagrosh.jdautilities.command

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionMapping

class CommandEvent(
    private val event: MessageReceivedEvent,
    val client: CommandClient,
    val args: String
) {
    val jda: JDA get() = event.jda
    val guild get() = event.guild
    val member: Member get() = event.member!!
    val author: User get() = event.author
    val selfUser: User get() = event.jda.selfUser
    val selfMember: Member get() = event.guild.selfMember
    val message get() = event.message
    val channel: MessageChannelUnion get() = event.channel
    val textChannel: TextChannel get() = event.channel.asTextChannel()
    val isOwner: Boolean get() = author.id == client.ownerId

    fun isFromType(type: ChannelType): Boolean = event.isFromType(type)

    fun reply(content: String) {
        channel.sendMessage(content).queue()
    }

    fun reply(embed: MessageEmbed) {
        channel.sendMessageEmbeds(embed).queue()
    }
}

class SlashCommandEvent(
    private val event: SlashCommandInteractionEvent,
    val client: CommandClient
) {
    val jda: JDA get() = event.jda
    val guild get() = event.guild
    val member: Member? get() = event.member
    val user: User get() = event.user
    val channel: MessageChannelUnion get() = event.channel
    val textChannel: TextChannel get() = event.channel.asTextChannel()
    val messageChannel: MessageChannelUnion get() = event.channel
    val hook: InteractionHook get() = event.hook

    fun getOption(name: String): OptionMapping? = event.getOption(name)

    fun isFromType(type: ChannelType): Boolean = event.isFromType(type)

    fun deferReply() = event.deferReply()

    fun reply(content: String) = event.reply(content)

    fun replyEmbeds(embed: MessageEmbed) = event.replyEmbeds(embed)

    fun asRawEvent(): SlashCommandInteractionEvent = event
}
