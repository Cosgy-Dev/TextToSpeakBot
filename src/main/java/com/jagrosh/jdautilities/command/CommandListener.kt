package com.jagrosh.jdautilities.command

interface CommandListener {
    fun onCommand(event: CommandEvent, command: Command)
}
