package dev.cosgy.textToSpeak.framework.command.command

interface CommandListener {
    fun onCommand(event: CommandEvent, command: Command)
}
