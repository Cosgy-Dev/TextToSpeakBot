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
package dev.cosgy.textToSpeak.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.cosgy.textToSpeak.queue.Queueable
import net.dv8tion.jda.api.entities.User

class QueuedTrack(val track: AudioTrack, owner: Long) : Queueable {

    constructor(track: AudioTrack, owner: User) : this(track, owner.idLong)

    init {
        track.userData = owner
    }

    override val identifier: Long
        get() = track.getUserData(Long::class.java)
}