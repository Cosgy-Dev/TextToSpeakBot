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
package dev.cosgy.textToSpeak.utils

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

object FormatUtil {
    fun listOfTChannels(list: List<TextChannel>, query: String): String {
        val out = StringBuilder(" 複数のテキストチャンネルで${query}が一致しました。:")
        var i = 0
        while (i < 6 && i < list.size) {
            out.append("\n - ").append(list[i].name).append(" (<#").append(list[i].id).append(">)")
            i++
        }
        if (list.size > 6) out.append("\n**と ").append(list.size - 6).append(" など...**")
        return out.toString()
    }
}