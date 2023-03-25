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

package dev.cosgy.TextToSpeak.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.audio.AudioHandler;
import dev.cosgy.TextToSpeak.audio.QueuedTrack;
import dev.cosgy.TextToSpeak.audio.VoiceCreation;
import dev.cosgy.TextToSpeak.utils.ReadChannel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

public class MessageListener extends ListenerAdapter {
    private final Bot bot;

    public MessageListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        JDA jda = event.getJDA();
        long responseNumber = event.getResponseNumber();

        //イベント固有の情報
        User author = event.getAuthor();                //メッセージを送信したユーザー

        Message message = event.getMessage();           //受信したメッセージ。
        MessageChannel channel = event.getChannel();    //メッセージが送信されたMessageChannel

        String msg = message.getContentDisplay();
        //人間が読める形式のメッセージが返されます。 クライアントに表示されるものと同様。

        boolean isBot = author.isBot();
        //if(Arrays.asList(mentionedUsers).contains())
        //メッセージを送信したユーザーがBOTであるかどうかを判断。

        if (event.isFromType(ChannelType.TEXT)) {
            if (isBot) return;
            Guild guild = event.getGuild();
            TextChannel textChannel = event.getGuildChannel().asTextChannel();
            TextChannel settingText = bot.getSettingsManager().getSettings(event.getGuild()).getTextChannel(event.getGuild());

            if (!guild.getAudioManager().isConnected()) {
                return;
            }

            String prefix = bot.getConfig().getPrefix().equals("@mention") ? "@" + event.getJDA().getSelfUser().getName() + " " : bot.getConfig().getPrefix();

            if (msg.startsWith(prefix)) {
                return;
            }

            if (textChannel != settingText) {
                if (settingText == null) {
                    settingText = event.getGuild().getTextChannelById(ReadChannel.getChannel(event.getGuild().getIdLong()));
                }
            }

            // URLを置き換え
            msg = msg.replaceAll("(http://|https://)[\\w.\\-/:#?=&;%~+]+", "ゆーあーるえる");

            if (textChannel == settingText) {

                if (bot.getSettingsManager().getSettings(guild).isReadName()) {
                    msg = author.getName() + "  " + msg;
                }

                VoiceCreation vc = bot.getVoiceCreation();
                String file = null;
                try {
                    file = vc.createVoice(guild, author, msg);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                bot.getPlayerManager().loadItemOrdered(event.getGuild(), file, new ResultHandler(null, event, false));

                //textChannel.sendMessage(author.getName() + "が、「"+ msg +"」と送信しました。").queue();
            }
        }
    }


    @Override
    public void onReady(ReadyEvent e) {
        bot.readyJDA();
    }

    private static class ResultHandler implements AudioLoadResultHandler {
        private final MessageReceivedEvent event;

        private ResultHandler(Message m, MessageReceivedEvent event, boolean ytsearch) {
            this.event = event;
        }

        private void loadSingle(AudioTrack track, AudioPlaylist playlist) {
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            handler.addTrack(new QueuedTrack(track, event.getAuthor()));
        }

        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
            int[] count = {0};
            playlist.getTracks().forEach((track) -> {
                if (!track.equals(exclude)) {
                    AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                    handler.addTrack(new QueuedTrack(track, event.getAuthor()));
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
