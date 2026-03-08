package dev.cosgy.textToSpeak.framework.command.commons.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.util.*

object FinderUtil {
    @JvmStatic
    fun findTextChannels(query: String, guild: Guild): List<TextChannel> {
        val normalized = query.trim().lowercase(Locale.getDefault()).removePrefix("#")
        val channels = guild.textChannels
        val exact = channels.filter { it.name.lowercase(Locale.getDefault()) == normalized }
        if (exact.isNotEmpty()) return exact
        return channels.filter { it.name.lowercase(Locale.getDefault()).contains(normalized) }
    }
}
