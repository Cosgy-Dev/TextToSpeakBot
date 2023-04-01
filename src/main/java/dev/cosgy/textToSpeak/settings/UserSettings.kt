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

class UserSettings(private val manager: UserSettingsManager, // getter
                   private val userId: Long, var voice: String, var speed: Float, var intonation: Float, var voiceQualityA: Float, var voiceQualityFm: Float) {

    fun setVoice(voice: String) {
        this.voice = voice
        manager.saveSetting(userId)
    }

    fun setSpeed(speed:Float){
        this.speed = speed
        manager.saveSetting(userId)
    }

    fun setIntonation(intonation: Float){
        this.intonation = intonation
        manager.saveSetting(userId)
    }

    fun setVoiceQualityA(voiceQualityA: Float){
        this.voiceQualityA = voiceQualityA
        manager.saveSetting(userId)
    }

    fun setVoiceQualityFm(voiceQualityFm: Float){
        this.voiceQualityFm = voiceQualityFm
        manager.saveSetting(userId)
    }

}