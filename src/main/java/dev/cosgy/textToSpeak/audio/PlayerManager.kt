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

package dev.cosgy.TextToSpeak.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.cosgy.TextToSpeak.Bot;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerManager extends DefaultAudioPlayerManager {
    private final Bot bot;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PlayerManager(Bot bot) {
        this.bot = bot;
    }

    public void init() {
        AudioSourceManagers.registerLocalSource(this);

        if (getConfiguration().getOpusEncodingQuality() != 10) {
            logger.debug("OpusEncodingQuality は、" + getConfiguration().getOpusEncodingQuality() + "(< 10)" + ", 品質を10に設定します。");
            getConfiguration().setOpusEncodingQuality(10);
        }

        if (getConfiguration().getResamplingQuality() != AudioConfiguration.ResamplingQuality.HIGH) {
            logger.debug("ResamplingQuality は " + getConfiguration().getResamplingQuality().name() + "(HIGHではない), 品質をHIGHに設定します。");
            getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        }
    }

    public Bot getBot() {
        return bot;
    }

    public boolean hasHandler(Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null;
    }

    public AudioHandler setUpHandler(Guild guild) {
        AudioHandler handler;
        if (guild.getAudioManager().getSendingHandler() == null) {
            AudioPlayer player = createPlayer();
            player.setVolume(bot.getSettingsManager().getSettings(guild).getVolume());
            handler = new AudioHandler(this, guild, player);
            player.addListener(handler);
            guild.getAudioManager().setSendingHandler(handler);
        } else
            handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        return handler;
    }
}
