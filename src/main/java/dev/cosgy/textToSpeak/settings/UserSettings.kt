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

class UserSettings(
    private val manager: UserSettingsManager,
    private val userId: Long,
    private var voice: String,
    private var speed: Float,
    private var intonation: Float,
    private var voiceQualityA: Float,
    private var voiceQualityFm: Float
) {
    var voiceSetting: String
        get() = voice
        set(value) {
            voice = value
            manager.saveSetting(userId)
        }

    var speedSetting: Float
        get() = speed
        set(value) {
            speed = value
            manager.saveSetting(userId)
        }

    var intonationSetting: Float
        get() = intonation
        set(value) {
            intonation = value
            manager.saveSetting(userId)
        }

    var voiceQualityASetting: Float
        get() = voiceQualityA
        set(value) {
            voiceQualityA = value
            manager.saveSetting(userId)
        }

    var voiceQualityFmSetting: Float
        get() = voiceQualityFm
        set(value) {
            voiceQualityFm = value
            manager.saveSetting(userId)
        }
}
