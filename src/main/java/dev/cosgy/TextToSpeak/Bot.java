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

package dev.cosgy.TextToSpeak;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import dev.cosgy.TextToSpeak.audio.AudioHandler;
import dev.cosgy.TextToSpeak.audio.Dictionary;
import dev.cosgy.TextToSpeak.audio.PlayerManager;
import dev.cosgy.TextToSpeak.audio.VoiceCreation;
import dev.cosgy.TextToSpeak.gui.GUI;
import dev.cosgy.TextToSpeak.settings.SettingsManager;
import dev.cosgy.TextToSpeak.settings.UserSettingsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Bot {
    public static Bot INSTANCE;
    private final EventWaiter waiter;
    private final ScheduledExecutorService threadpool;
    private final BotConfig config;
    private final SettingsManager settings;
    private final ResourceBundle lang;
    private final PlayerManager players;
    private final VoiceCreation voiceCreation;
    private final UserSettingsManager userSettingsManager;
    private final Dictionary dictionary;

    private boolean shuttingDown = false;
    private JDA jda;
    private GUI gui;


    public Bot(EventWaiter waiter, BotConfig config, SettingsManager settings, VoiceCreation voiceCreation, UserSettingsManager userSettingsManager, Dictionary dictionary) {
        this.waiter = waiter;
        this.lang = ResourceBundle.getBundle("lang.yomiage", Locale.JAPAN);
        this.config = config;
        this.settings = settings;
        this.threadpool = Executors.newSingleThreadScheduledExecutor();
        this.players = new PlayerManager(this);
        this.players.init();
        this.voiceCreation = voiceCreation;
        voiceCreation.Init(this);
        this.userSettingsManager = userSettingsManager;
        this.dictionary = dictionary;
    }

    public JDA getJDA() {
        return jda;
    }

    public void setJDA(JDA jda) {
        this.jda = jda;
    }

    public void closeAudioConnection(long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null)
            threadpool.submit(() -> guild.getAudioManager().closeAudioConnection());
    }

    public void resetGame() {
        Activity game = config.getGame() == null || config.getGame().getName().toLowerCase().matches("(none|なし)") ? null : config.getGame();
        if (!Objects.equals(jda.getPresence().getActivity(), game))
            jda.getPresence().setActivity(game);
    }

    public void shutdown() {
        if (shuttingDown)
            return;
        shuttingDown = true;
        threadpool.shutdownNow();
        if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            jda.getGuilds().forEach(g ->
            {
                g.getAudioManager().closeAudioConnection();
                AudioHandler ah = (AudioHandler) g.getAudioManager().getSendingHandler();
                if (ah != null) {
                    ah.stopAndClear();
                    ah.getPlayer().destroy();
                }
            });
            jda.shutdown();
        }
        if (gui != null)
            gui.dispose();
        System.exit(0);
    }

    public void setGUI(GUI gui) {
        this.gui = gui;
    }

    public ResourceBundle GetLang(){
        return lang;
    }

    public SettingsManager getSettingsManager() {
        return settings;
    }

    public PlayerManager getPlayerManager() {
        return players;
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public BotConfig getConfig() {
        return config;
    }

    public ScheduledExecutorService getThreadpool() {
        return threadpool;
    }

    public VoiceCreation getVoiceCreation(){
        return voiceCreation;
    }

    public UserSettingsManager getUserSettingsManager(){
        return userSettingsManager;
    }

    public Dictionary getDictionary(){return dictionary;}
}
