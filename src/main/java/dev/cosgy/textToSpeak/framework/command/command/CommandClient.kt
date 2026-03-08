package dev.cosgy.textToSpeak.framework.command.command

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.*

class CommandClient internal constructor(
    var prefix: String,
    var altPrefix: String,
    var ownerId: String,
    var helpWord: String,
    var serverInvite: String?,
    private val settingsManager: GuildSettingsManager<*>?,
    private val commandListener: CommandListener?,
    private val commandList: List<SlashCommand>
) : ListenerAdapter() {
    val commands: List<SlashCommand> = commandList
    val slashCommands: List<SlashCommand> = commandList
    val startTime: OffsetDateTime = OffsetDateTime.now()
    private val slashIndex: Map<String, SlashCommand> =
        commandList.associateBy { it.name.lowercase(Locale.getDefault()) }
    private val commandIndex: Map<String, SlashCommand> = buildMap {
        commandList.forEach { cmd ->
            put(cmd.name.lowercase(Locale.getDefault()), cmd)
            cmd.aliases.forEach { alias -> put(alias.lowercase(Locale.getDefault()), cmd) }
        }
    }

    val success = "✅ "
    val warning = "⚠️ "
    val error = "❌ "

    val totalGuilds: Int
        get() = jdaRef?.guilds?.size ?: 0

    private val logger = LoggerFactory.getLogger(CommandClient::class.java)
    private var jdaRef: net.dv8tion.jda.api.JDA? = null

    override fun onReady(event: ReadyEvent) {
        jdaRef = event.jda
        registerSlashCommands(event)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = slashIndex[event.name.lowercase(Locale.getDefault())] ?: return
        val target = resolveSlashTarget(command, event)
        val guildChannel = event.channel as? GuildMessageChannel
        if (!canExecute(target, event.member, event.user, guildChannel)) {
            event.reply("${warning}権限がないため実行できません。").setEphemeral(true).queue()
            return
        }

        try {
            target.execute(SlashCommandEvent(event, this))
        } catch (ex: Exception) {
            logger.error("Failed to execute slash command: ${event.fullCommandName}", ex)
            if (!event.isAcknowledged) {
                event.reply("${error}コマンド実行中にエラーが発生しました。")
                    .setEphemeral(true)
                    .queue()
            } else {
                event.hook.sendMessage("${error}コマンド実行中にエラーが発生しました。")
                    .setEphemeral(true)
                    .queue()
            }
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val command = slashIndex[event.name.lowercase(Locale.getDefault())] ?: return
        val target = if (event.subcommandName != null) {
            command.children.firstOrNull { it.name.equals(event.subcommandName, ignoreCase = true) } ?: command
        } else {
            command
        }
        target.onAutoComplete(event)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || event.isWebhookMessage) return

        val raw = event.message.contentRaw
        val activePrefix = detectPrefix(raw, event)
        if (activePrefix == null) return

        val content = raw.substring(activePrefix.length).trim()
        if (content.isEmpty()) return

        val split = content.split("\\s+", limit = 2)
        val commandName = split[0].lowercase(Locale.getDefault())
        val args = if (split.size > 1) split[1] else ""

        val command = commandIndex[commandName] ?: return

        val commandEvent = CommandEvent(event, this, args)
        val guildChannel = if (event.isFromGuild) event.channel as? GuildMessageChannel else null
        if (!canExecute(command, event.member, event.author, guildChannel, commandEvent)) {
            commandEvent.reply("${warning}権限がないため実行できません。")
            return
        }

        commandListener?.onCommand(commandEvent, command)

        try {
            command.execute(commandEvent)
        } catch (ex: Exception) {
            logger.error("Failed to execute command: ${command.name}", ex)
            commandEvent.reply("${error}コマンド実行中にエラーが発生しました。")
        }
    }

    fun <T> getSettingsFor(guild: net.dv8tion.jda.api.entities.Guild?): T {
        requireNotNull(guild) { "Guild is required for this command." }
        @Suppress("UNCHECKED_CAST")
        return (settingsManager?.getSettings(guild) as T)
    }

    private fun detectPrefix(raw: String, event: MessageReceivedEvent): String? {
        val prefixes = mutableListOf(prefix, altPrefix)
        if (event.isFromGuild && settingsManager != null) {
            val settings = settingsManager.getSettings(event.guild)
            if (settings is GuildSettingsProvider) {
                settings.getPrefixes().filterNotNull().forEach { prefixes.add(it) }
            }
        }

        return prefixes
            .filter { it.isNotBlank() }
            .distinct()
            .maxByOrNull { if (raw.startsWith(it, ignoreCase = false)) it.length else -1 }
            ?.takeIf { raw.startsWith(it) }
    }

    private fun resolveSlashTarget(command: SlashCommand, event: SlashCommandInteractionEvent): SlashCommand {
        val subName = event.subcommandName ?: return command
        return command.children.firstOrNull { it.name.equals(subName, ignoreCase = true) } ?: command
    }

    private fun canExecute(
        command: SlashCommand,
        member: Member?,
        user: User,
        guildChannel: GuildMessageChannel?,
        commandEvent: CommandEvent? = null
    ): Boolean {
        if (command.ownerCommand && user.id != ownerId) return false
        if (command.guildOnly && member == null) return false

        if (member != null && command.userPermissions.isNotEmpty() && !member.hasPermission(*command.userPermissions)) {
            return false
        }

        if (guildChannel != null && command.botPermissions.isNotEmpty()) {
            val selfMember = guildChannel.guild.selfMember
            if (!selfMember.hasPermission(guildChannel, *command.botPermissions)) {
                return false
            }
        }

        if (commandEvent != null && command.category?.predicate != null && !command.category!!.test(commandEvent)) {
            return false
        }
        return true
    }

    private fun registerSlashCommands(event: ReadyEvent) {
        val commandData = slashCommands.map { top ->
            val description = top.help.ifBlank { "No description" }.take(100)
            val slash = Commands.slash(top.name, description)
            if (top.userPermissions.isNotEmpty()) {
                slash.setDefaultPermissions(DefaultMemberPermissions.enabledFor(*top.userPermissions))
            }
            if (top.children.isEmpty()) {
                top.options.forEach { slash.addOptions(it) }
            } else {
                top.children.forEach { child ->
                    val sub = SubcommandData(child.name, child.help.ifBlank { "No description" }.take(100))
                    child.options
                        .filterNot { it.type == OptionType.SUB_COMMAND || it.type == OptionType.SUB_COMMAND_GROUP }
                        .forEach { sub.addOptions(it) }
                    slash.addSubcommands(sub)
                }
            }
            slash
        }

        event.jda.updateCommands().addCommands(commandData).queue(
            { logger.info("Registered {} slash commands", commandData.size) },
            { err -> logger.error("Failed to register slash commands", err) }
        )
    }
}
