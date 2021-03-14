////////////////////////////////////////////////////////////////////////////////
//  Copyright 2021 Cosgy Dev                                                   /
//                                                                             /
//     Licensed under the Apache License, Version 2.0 (the "License");         /
//     you may not use this file except in compliance with the License.        /
//     You may obtain a copy of the License at                                 /
//                                                                             /
//        http://www.apache.org/licenses/LICENSE-2.0                           /
//                                                                             /
//     Unless required by applicable law or agreed to in writing, software     /
//     distributed under the License is distributed on an "AS IS" BASIS,       /
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied./
//     See the License for the specific language governing permissions and     /
//     limitations under the License.                                          /
////////////////////////////////////////////////////////////////////////////////

package dev.cosgy.TextToSpeak.settings;

public class UserSettings {
    private final UserSettingsManager manager;
    private Long userId;
    private String voice;
    private float speed;
    private float intonation;
    private float voiceQualityA;
    private float voiceQualityFm;

    public UserSettings(UserSettingsManager manager, Long userId,String voice, float speed, float intonation, float voiceQualityA, float voiceQualityFm){
        this.manager = manager;
        this.userId = userId;
        this.voice = voice;
        this.speed = speed;
        this.intonation = intonation;
        this.voiceQualityA = voiceQualityA;
        this.voiceQualityFm = voiceQualityFm;
    }


    // getter
    public Long getUserId(){
        return userId;
    }

    public String getVoice(){
        return voice;
    }

    public float getSpeed(){
        return speed;
    }

    public float getIntonation(){
        return  intonation;
    }

    public float getVoiceQualityA(){
        return voiceQualityA;
    }

    public float getVoiceQualityFm(){
        return voiceQualityFm;
    }

    // TODO: 書き込みの処理を入れてない。DBなのでバッチ処理をした方が良いかもしれない。
    // setter
    public void setVoice(String voice){
        this.voice = voice;
        this.manager.saveSetting(userId);
    }

    public void setSpeed(float speed){
        this.speed = speed;
        this.manager.saveSetting(userId);
    }

    public void setIntonation(float intonation){
        this.intonation = intonation;
        this.manager.saveSetting(userId);
    }

    public void setVoiceQualityA(float voiceQualityA){
        this.voiceQualityA = voiceQualityA;
        this.manager.saveSetting(userId);
    }

    public void setVoiceQualityFm(float voiceQualityFm){
        this.voiceQualityFm = voiceQualityFm;
        this.manager.saveSetting(userId);
    }
}
