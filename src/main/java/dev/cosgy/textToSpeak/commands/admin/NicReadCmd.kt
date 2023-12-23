package dev.cosgy.textToSpeak.commands.admin

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommandEvent
import dev.cosgy.textToSpeak.Bot
import dev.cosgy.textToSpeak.commands.AdminCommand
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NicReadCmd(private val bot: Bot) : AdminCommand() {
    var log: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        name = "readnic"
        help = "ニックネーム読み上げを優先するかを設定します。。"

        options = listOf(OptionData(OptionType.BOOLEAN, "value", "ニックネームを優先するか", false))
    }

    override fun execute(event: SlashCommandEvent) {
        if (!checkAdminPermission(event.client, event)) {
            event.reply("${event.client.warning}権限がないため実行できません。").queue()
            return
        }

        val settings = bot.settingsManager.getSettings(event.guild!!)

        if (event.getOption("value") == null) {
            settings.setReadNic(!settings.isReadNic())
            event.reply("ニックネーム読み上げの優先を${if (settings.isReadNic()) "有効" else "無効"}にしました。")
                .queue()
        } else {
            val args = event.getOption("value")!!.asBoolean

            settings.setReadNic(args)

            event.reply("ニックネーム読み上げの優先を${if (args) "有効" else "無効"}にしました。").queue()
        }
    }

    override fun execute(event: CommandEvent) {
        val settings = bot.settingsManager.getSettings(event.guild)

        settings.setReadNic(!settings.isReadNic())
        event.reply("ニックネーム読み上げの優先を${if (settings.isReadNic()) "有効" else "無効"}にしました。")
    }
}