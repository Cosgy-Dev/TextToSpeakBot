package dev.cosgy.textToSpeak.framework.command.menu

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import java.awt.Color
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class Paginator private constructor(
    private val text: String,
    private val items: List<String>,
    private val itemsPerPage: Int,
    private val color: Color?,
    private val finalAction: Consumer<Message>?
) {
    fun paginate(channel: MessageChannel, page: Int) {
        if (items.isEmpty()) {
            channel.sendMessage("単語が登録されていません。" ).queue()
            return
        }

        val safePage = page.coerceAtLeast(1)
        val pageCount = (items.size + itemsPerPage - 1) / itemsPerPage
        val currentPage = safePage.coerceAtMost(pageCount)
        val from = (currentPage - 1) * itemsPerPage
        val to = minOf(items.size, from + itemsPerPage)

        val body = items.subList(from, to)
            .mapIndexed { index, item -> "${from + index + 1}. $item" }
            .joinToString("\n")

        val embed = EmbedBuilder()
            .setTitle(text)
            .setDescription(body)
            .setFooter("Page $currentPage/$pageCount")
            .apply { if (color != null) setColor(color) }
            .build()

        channel.sendMessageEmbeds(embed).queue { msg -> finalAction?.accept(msg) }
    }

    class Builder {
        private var text: String = ""
        private var items: List<String> = emptyList()
        private var itemsPerPage: Int = 10
        private var color: Color? = null
        private var finalAction: Consumer<Message>? = null

        fun setColumns(ignore: Int) = this

        fun setFinalAction(action: Consumer<Message>) = apply { this.finalAction = action }

        fun setItemsPerPage(itemsPerPage: Int) = apply { this.itemsPerPage = itemsPerPage.coerceAtLeast(1) }

        fun waitOnSinglePage(ignore: Boolean) = this

        fun useNumberedItems(ignore: Boolean) = this

        fun showPageNumbers(ignore: Boolean) = this

        fun wrapPageEnds(ignore: Boolean) = this

        fun setEventWaiter(ignore: Any?) = this

        fun setTimeout(ignore: Long, ignoreUnit: TimeUnit) = this

        fun setText(text: String) = apply { this.text = text }

        fun setItems(vararg items: String) = apply { this.items = items.toList() }

        fun setUsers(vararg users: User) = this

        fun setColor(color: Color?) = apply { this.color = color }

        fun build(): Paginator = Paginator(text, items, itemsPerPage, color, finalAction)
    }
}
