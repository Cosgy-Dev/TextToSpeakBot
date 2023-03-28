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

package dev.cosgy.TextToSpeak.settings;

import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Collection;
import java.util.Collections;

public class Settings implements GuildSettingsProvider {
    private final SettingsManager manager;
    protected long textId;
    private String prefix;
    private int volume;
    private boolean readName;
    private boolean joinAndLeaveRead;


    public Settings(SettingsManager manager, String textId, String prefix, int volume, boolean readName, boolean joinAndLeaveRead) {
        this.manager = manager;
        try {
            this.textId = Long.parseLong(textId);
        } catch (NumberFormatException e) {
            this.textId = 0;
        }
        this.prefix = prefix;
        this.volume = volume;
        this.readName = readName;
        this.joinAndLeaveRead = joinAndLeaveRead;
    }

    public Settings(SettingsManager manager, long textId, String prefix, int volume, boolean readName, boolean joinAndLeaveRead) {
        this.manager = manager;
        this.textId = textId;
        this.prefix = prefix;
        this.volume = volume;
        this.readName = readName;
        this.joinAndLeaveRead = joinAndLeaveRead;
    }


    public TextChannel getTextChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(textId);
    }

    public void setTextChannel(TextChannel tc) {
        this.textId = tc == null ? 0 : tc.getIdLong();
        this.manager.writeSettings();
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
        this.manager.writeSettings();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.manager.writeSettings();
    }

    @Override
    public Collection<String> getPrefixes() {
        return prefix == null ? Collections.EMPTY_SET : Collections.singleton(prefix);
    }

    public boolean isReadName() {
        return readName;
    }

    public void setReadName(boolean readName) {
        this.readName = readName;
        this.manager.writeSettings();
    }

    public boolean isJoinAndLeaveRead() {
        return joinAndLeaveRead;
    }

    public void setJoinAndLeaveRead(boolean joinAndLeaveRead) {
        this.joinAndLeaveRead = joinAndLeaveRead;
        this.manager.writeSettings();
    }
}
