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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
    private final Bot bot;

    public MessageListener(Bot bot){
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        JDA jda = event.getJDA();
        long responseNumber = event.getResponseNumber();//???????????????????????????JDA?????????????????????????????????????????????

        //???????????????????????????
        User author = event.getAuthor();                //??????????????????????????????????????????

        Message message = event.getMessage();           //??????????????????????????????
        MessageChannel channel = event.getChannel();    //?????????????????????????????????MessageChannel

        String msg = message.getContentDisplay();
        //??????????????????????????????????????????????????????????????? ??????????????????????????????????????????????????????

        boolean isBot = author.isBot();
        //if(Arrays.asList(mentionedUsers).contains())
        //?????????????????????????????????????????????BOT?????????????????????????????????

        if(event.isFromType(ChannelType.TEXT)){
            if(isBot) return;
            Guild guild = event.getGuild();
            TextChannel textChannel = event.getTextChannel();
            TextChannel settingText = bot.getSettingsManager().getSettings(guild).getTextChannel(guild);

            if(!guild.getAudioManager().isConnected()){
                return;
            }

            String prefix = bot.getConfig().getPrefix().equals("@mention") ? "@" + event.getJDA().getSelfUser().getName() + " " : bot.getConfig().getPrefix();

            if(msg.startsWith(prefix)){
                return;
            }

            // URL???????????????
            msg = msg.replaceAll("(http://|https://)[\\w.\\-/:#?=&;%~+]+","?????????????????????");

            if(textChannel == settingText){

                if(bot.getSettingsManager().getSettings(guild).isReadName()){
                    msg = author.getName() + "  " + msg;
                }

                VoiceCreation vc = bot.getVoiceCreation();
                String file = vc.CreateVoice(guild,author, msg);

                bot.getPlayerManager().loadItemOrdered(event.getGuild(), file, new ResultHandler(null, event, false));

                //textChannel.sendMessage(author.getName() + "?????????"+ msg +"???????????????????????????").queue();
            }
        }
    }


    @Override
    public void onReady(ReadyEvent e){
        bot.getDictionary().Init();
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final Message m;
        private final MessageReceivedEvent event;
        private final boolean ytsearch;

        private ResultHandler(Message m, MessageReceivedEvent event, boolean ytsearch) {
            this.m = m;
            this.event = event;
            this.ytsearch = ytsearch;
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
