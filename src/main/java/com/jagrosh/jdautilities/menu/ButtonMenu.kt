package com.jagrosh.jdautilities.menu

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class ButtonMenu private constructor(
    private val text: String,
    private val choices: List<String>,
    private val action: Consumer<Emoji>?,
    private val finalAction: Consumer<Message>?
) {
    fun display(channel: MessageChannel) {
        channel.sendMessage(text).queue { msg ->
            val selected = when {
                choices.contains("✔") -> "✔"
                choices.isNotEmpty() -> choices.last()
                else -> null
            }
            if (selected != null) {
                action?.accept(Emoji.fromUnicode(selected))
            }
            finalAction?.accept(msg)
        }
    }

    class Builder {
        private var text: String = ""
        private val choices: MutableList<String> = mutableListOf()
        private var action: Consumer<Emoji>? = null
        private var finalAction: Consumer<Message>? = null

        fun setText(text: String) = apply { this.text = text }

        fun addChoices(vararg choices: String) = apply { this.choices += choices }

        fun setEventWaiter(ignore: Any?) = this

        fun setTimeout(ignore: Long, ignoreUnit: TimeUnit) = this

        fun setAction(action: Consumer<Emoji>) = apply { this.action = action }

        fun setFinalAction(finalAction: Consumer<Message>) = apply { this.finalAction = finalAction }

        fun build(): ButtonMenu = ButtonMenu(text, choices, action, finalAction)
    }
}
