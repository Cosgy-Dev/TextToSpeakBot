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

import dev.cosgy.TextToSpeak.audio.AudioHandler;
import dev.cosgy.TextToSpeak.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class Listener extends ListenerAdapter {
    private final Bot bot;

    public Listener(Bot bot) {
        this.bot = bot;
    }
    Logger log = getLogger(this.getClass());

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
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event)
    {
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        Member botMember = event.getGuild().getSelfMember();
        if (event.getChannelLeft().getMembers().size() == 1 && event.getChannelLeft().getMembers().contains(botMember)){
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            handler.getQueue().clear();
            bot.getVoiceCreation().ClearGuildFolder(event.getGuild());
        }
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        log.info("シャットダウンします。");
        bot.shutdown();
    }
}
