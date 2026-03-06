package dev.cosgy.textToSpeak.interactions

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.ConcurrentHashMap

class ButtonRouter : ListenerAdapter() {
    private data class Route(
        val userId: Long?,
        val expiresAtMillis: Long,
        val oneShot: Boolean,
        val handler: (ButtonInteractionEvent) -> Unit
    )

    private val routes = ConcurrentHashMap<String, Route>()

    fun register(
        customId: String,
        userId: Long?,
        ttlMillis: Long,
        oneShot: Boolean = true,
        handler: (ButtonInteractionEvent) -> Unit
    ) {
        val expiresAt = System.currentTimeMillis() + ttlMillis
        routes[customId] = Route(userId, expiresAt, oneShot, handler)
    }

    fun unregister(customId: String) {
        routes.remove(customId)
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val route = routes[event.componentId] ?: return

        if (System.currentTimeMillis() > route.expiresAtMillis) {
            routes.remove(event.componentId)
            event.reply("この操作は期限切れです。もう一度コマンドを実行してください。")
                .setEphemeral(true)
                .queue()
            return
        }

        if (route.userId != null && route.userId != event.user.idLong) {
            event.reply("このボタンはコマンド実行者のみ操作できます。")
                .setEphemeral(true)
                .queue()
            return
        }

        if (route.oneShot) {
            routes.remove(event.componentId)
        }

        route.handler.invoke(event)
    }
}
