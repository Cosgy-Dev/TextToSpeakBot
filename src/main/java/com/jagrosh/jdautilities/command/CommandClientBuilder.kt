package com.jagrosh.jdautilities.command

class CommandClientBuilder {
    private var prefix: String = "/"
    private var altPrefix: String = "/"
    private var ownerId: String = ""
    private var helpWord: String = "help"
    private var serverInvite: String? = null
    private var settingsManager: GuildSettingsManager<*>? = null
    private var commandListener: CommandListener? = null
    private val slashCommands: MutableList<SlashCommand> = mutableListOf()

    fun setPrefix(prefix: String) = apply { this.prefix = prefix }

    fun setAlternativePrefix(altPrefix: String) = apply { this.altPrefix = altPrefix }

    fun setOwnerId(ownerId: String) = apply { this.ownerId = ownerId }

    fun setHelpWord(helpWord: String) = apply { this.helpWord = helpWord }

    fun setServerInvite(serverInvite: String?) = apply { this.serverInvite = serverInvite }

    fun useHelpBuilder(ignore: Boolean) = apply { }

    fun setLinkedCacheSize(ignore: Int) = apply { }

    fun setStatus(ignore: net.dv8tion.jda.api.OnlineStatus) = apply { }

    fun setGuildSettingsManager(settingsManager: GuildSettingsManager<*>) = apply {
        this.settingsManager = settingsManager
    }

    fun setListener(listener: CommandListener) = apply {
        this.commandListener = listener
    }

    fun addSlashCommands(vararg commands: SlashCommand?) = apply {
        commands.filterNotNull().forEach { addUnique(it) }
    }

    fun addCommands(vararg commands: SlashCommand?) = apply {
        commands.filterNotNull().forEach { addUnique(it) }
    }

    fun build(): CommandClient {
        return CommandClient(
            prefix = prefix,
            altPrefix = altPrefix,
            ownerId = ownerId,
            helpWord = helpWord,
            serverInvite = serverInvite,
            settingsManager = settingsManager,
            commandListener = commandListener,
            commandList = slashCommands
        )
    }

    private fun addUnique(command: SlashCommand) {
        if (slashCommands.none { it.name.equals(command.name, ignoreCase = true) }) {
            slashCommands += command
        }
    }
}
