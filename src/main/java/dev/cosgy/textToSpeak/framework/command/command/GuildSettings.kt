package dev.cosgy.textToSpeak.framework.command.command

import net.dv8tion.jda.api.entities.Guild

interface GuildSettingsProvider {
    fun getPrefixes(): Collection<String?>
}

interface GuildSettingsManager<T> {
    fun getSettings(guild: Guild): T
}
