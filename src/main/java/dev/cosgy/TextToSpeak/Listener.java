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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.cosgy.TextToSpeak.audio.AudioHandler;
import dev.cosgy.TextToSpeak.audio.QueuedTrack;
import dev.cosgy.TextToSpeak.settings.Settings;
import dev.cosgy.TextToSpeak.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class Listener extends ListenerAdapter {
    private final Bot bot;
    Logger log = getLogger(this.getClass());

    public Listener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getGuilds().isEmpty()) {
            Logger log = LoggerFactory.getLogger("YomiageBot");
            log.warn("このボットはグループに入っていません！ボットをあなたのグループに追加するには、以下のリンクを使用してください。");
            log.warn(event.getJDA().getInviteUrl(TextToSpeak.RECOMMENDED_PERMS));
        }
        if (bot.getConfig().useUpdateAlerts()) {
            bot.getThreadpool().scheduleWithFixedDelay(() ->
            {
                User owner = bot.getJDA().getUserById(bot.getConfig().getOwnerId());
                if (owner != null) {
                    String currentVersion = OtherUtil.getCurrentVersion();
                    String latestVersion = OtherUtil.getLatestVersion();
                    if (latestVersion != null && !currentVersion.equalsIgnoreCase(latestVersion) && TextToSpeak.CHECK_UPDATE) {
                        String msg = String.format(OtherUtil.NEW_VERSION_AVAILABLE, currentVersion, latestVersion);
                        owner.openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
                    }
                }
            }, 0, 24, TimeUnit.HOURS);
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        Member botMember = event.getGuild().getSelfMember();

        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());

        if (event.getChannelLeft() != null) {
            if (settings.isJoinAndLeaveRead() && Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel() == event.getChannelLeft() && event.getChannelLeft().getMembers().size() > 1) {
                String file = null;
                try {
                    file = bot.getVoiceCreation().createVoice(event.getGuild(), event.getMember().getUser(), event.getMember().getUser().getName() + "がボイスチャンネルから退出しました。");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), file, new Listener.ResultHandler(null, event));
            }

            if (event.getChannelLeft().getMembers().size() == 1 && event.getChannelLeft().getMembers().contains(botMember)) {
                AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                handler.getQueue().clear();
                try {
                    bot.getVoiceCreation().clearGuildFolder(event.getGuild());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (event.getChannelJoined() != null) {
            if (settings.isJoinAndLeaveRead() && Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel() == event.getChannelJoined()) {
                String file = null;
                try {
                    file = bot.getVoiceCreation().createVoice(event.getGuild(), event.getMember().getUser(), event.getMember().getUser().getName() + "がボイスチャンネルに参加しました。");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), file, new Listener.ResultHandler(null, event));
            }
        }
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        bot.shutdown();
    }


    private class ResultHandler implements AudioLoadResultHandler {
        private final Message m;
        private final GuildVoiceUpdateEvent event;

        private ResultHandler(Message m, GuildVoiceUpdateEvent event) {
            this.m = m;
            this.event = event;
        }

        private void loadSingle(AudioTrack track, AudioPlaylist playlist) {
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            handler.addTrack(new QueuedTrack(track, event.getMember().getUser()));
        }

        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
            int[] count = {0};
            playlist.getTracks().forEach((track) -> {
                if (!track.equals(exclude)) {
                    AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                    handler.addTrack(new QueuedTrack(track, event.getMember().getUser()));
                    count[0]++;
                }
            });
            return count[0];
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track, null);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {

        }

        @Override
        public void noMatches() {
        }

        @Override
        public void loadFailed(FriendlyException throwable) {

        }
    }
}