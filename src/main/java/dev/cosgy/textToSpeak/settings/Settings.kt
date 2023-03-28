//////////////////////////////////////////////////////////////////////////////////////////
//  Copyright 2023 Cosgy Dev                                                             /
//                                                                                       /
//     Licensed under the Apache License, Version 2.0 (the "License");                   /
//     you may not use this file except in compliance with the License.                  /
//     You may obtain a copy of the License at                                           /
//                                                                                       /
//        http://www.apache.org/licenses/LICENSE-2.0                                     /
//                                                                                       /
//     Unless required by applicable law or agreed to in writing, software               /
//     distributed under the License is distributed on an "AS IS" BASIS,                 /
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.          /
//     See the License for the specific language governing permissions and               /
//     limitations under the License.                                                    /
//////////////////////////////////////////////////////////////////////////////////////////
package dev.cosgy.textToSpeak.settings

import com.jagrosh.jdautilities.command.GuildSettingsProvider
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

class Settings : GuildSettingsProvider {
    private val manager: SettingsManager
    var textId: Long = 0
    var prefix: String?
    var volume: Int
    private var readName: Boolean
    private var joinAndLeaveRead: Boolean

    constructor(manager: SettingsManager, textId: String?, prefix: String?, volume: Int, readName: Boolean, joinAndLeaveRead: Boolean) {
        this.manager = manager
        try {
            this.textId = textId!!.toLong()
        } catch (e: NumberFormatException) {
            this.textId = 0
        }
        this.prefix = prefix
        this.volume = volume
        this.readName = readName
        this.joinAndLeaveRead = joinAndLeaveRead
    }

    constructor(manager: SettingsManager, textId: Long, prefix: String?, volume: Int, readName: Boolean, joinAndLeaveRead: Boolean) {
        this.manager = manager
        this.textId = textId
        this.prefix = prefix
        this.volume = volume
        this.readName = readName
        this.joinAndLeaveRead = joinAndLeaveRead
    }

    fun getTextChannel(guild: Guild?): TextChannel? {
        return guild?.getTextChannelById(textId)
    }

    fun setTextChannel(tc: TextChannel?) {
        textId = tc?.idLong ?: 0
        manager.writeSettings()
    }

    override fun getPrefixes(): Collection<String?> {
        return if (prefix == null) emptySet() else setOf(prefix)
    }


    fun isReadName(): Boolean {
        return readName
    }

    fun setReadName(readName: Boolean) {
        this.readName = readName
        manager.writeSettings()
    }

    fun isJoinAndLeaveRead(): Boolean {
        return joinAndLeaveRead
    }

    fun setJoinAndLeaveRead(joinAndLeaveRead: Boolean) {
        this.joinAndLeaveRead = joinAndLeaveRead
        manager.writeSettings()
    }
}