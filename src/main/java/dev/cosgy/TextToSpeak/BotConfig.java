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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import dev.cosgy.TextToSpeak.entities.Prompt;
import dev.cosgy.TextToSpeak.utils.OtherUtil;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class BotConfig {
    private final static String CONTEXT = "Config";
    private final static String START_TOKEN = "/// START OF YOMIAGEBOT CONFIG ///";
    private final static String END_TOKEN = "/// END OF YOMIAGEBOT CONFIG ///";
    private final Prompt prompt;
    private Path path = null;

    private String token, prefix, altprefix, dictionary, voiceDirectory, winjtalkdir;
    private long owner, aloneTimeUntilStop;
    private OnlineStatus status;
    private Activity game;
    private boolean updatealerts, dbots;


    private boolean valid = false;

    public BotConfig(Prompt prompt) {
        this.prompt = prompt;
    }

    public void load() {
        valid = false;

        try {
            path = OtherUtil.getPath(System.getProperty("config.file", System.getProperty("config", "config.txt")));
            if (path.toFile().exists()) {
                if (System.getProperty("config.file") == null)
                    System.setProperty("config.file", System.getProperty("config", "config.txt"));
                ConfigFactory.invalidateCaches();
            }

            // load in the config file, plus the default values
            //Config config = ConfigFactory.parseFile(path.toFile()).withFallback(ConfigFactory.load());
            Config config = ConfigFactory.load();


            token = config.getString("token");
            prefix = config.getString("prefix");
            altprefix = config.getString("altprefix");
            owner = (config.getAnyRef("owner") instanceof String ? 0L : config.getLong("owner"));
            game = OtherUtil.parseGame(config.getString("game"));
            status = OtherUtil.parseStatus(config.getString("status"));
            updatealerts = config.getBoolean("updatealerts");
            dictionary = config.getString("dictionary");
            voiceDirectory = config.getString("voiceDirectory");
            aloneTimeUntilStop = config.getLong("alonetimeuntilstop");
            winjtalkdir = config.getString("winjtalkdir");
            dbots = owner == 334091398263341056L;


            boolean write = false;

            // validate bot token
            if (token == null || token.isEmpty() || token.matches("(BOT_TOKEN_HERE|Botトークンをここに貼り付け)")) {
                token = prompt.prompt("BOTトークンを入力してください。"
                        + "\nBOTトークン: ");
                if (token == null) {
                    prompt.alert(Prompt.Level.WARNING, CONTEXT, "トークンが入力されていません！終了します。\n\n設定ファイルの場所: " + path.toAbsolutePath().toString());
                    return;
                } else {
                    write = true;
                }
            }

            // validate bot owner
            if (owner <= 0) {
                try {
                    owner = Long.parseLong(prompt.prompt("所有者のユーザーIDが設定されていない、または有効なIDではありません。"
                            + "\nBOTの所有者のユーザーIDを入力してください。"
                            + "\n所有者のユーザーID: "));
                } catch (NumberFormatException | NullPointerException ex) {
                    owner = 0;
                }
                if (owner <= 0) {
                    prompt.alert(Prompt.Level.ERROR, CONTEXT, "無効なユーザーIDです！終了します。\n\n設定ファイルの場所: " + path.toAbsolutePath().toString());
                    System.exit(0);
                } else {
                    write = true;
                }
            }

            if (write) {
                String original = OtherUtil.loadResource(this, "/reference.conf");
                String mod;
                if (original == null) {
                    mod = ("token = " + token + "\r\nowner = " + owner);
                } else {
                    mod = original.substring(original.indexOf(START_TOKEN) + START_TOKEN.length(), original.indexOf(END_TOKEN))
                            .replace("BOT_TOKEN_HERE", token).replace("Botトークンをここに貼り付け", token)
                            .replace("0 // OWNER ID", Long.toString(owner)).replace("所有者IDをここに貼り付け", Long.toString(owner))
                            .trim();
                }

                FileUtils.writeStringToFile(path.toFile(), mod, StandardCharsets.UTF_8);
            }

            // if we get through the whole config, it's good to go
            valid = true;
        } catch (ConfigException |
                IOException ex) {
            prompt.alert(Prompt.Level.ERROR, CONTEXT, ex + ": " + ex.getMessage() + "\n\n設定ファイルの場所: " + path.toAbsolutePath().toString());
        }
    }

    public boolean isValid() {
        return valid;
    }

    public String getConfigLocation() {
        return path.toFile().getAbsolutePath();
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAltPrefix() {
        return "NONE".equalsIgnoreCase(altprefix) ? null : altprefix;
    }

    public String getToken() {
        return token;
    }

    public long getOwnerId() {
        return owner;
    }

    public Activity getGame() {
        return game;
    }

    public OnlineStatus getStatus() {
        return status;
    }

    public boolean useUpdateAlerts() {
        return updatealerts;
    }

    public String getDictionary(){
        return dictionary;
    }

    public String getVoiceDirectory(){
        return voiceDirectory;
    }

    public String getWinJTalkDir(){
        return winjtalkdir;
    }

    public long getAloneTimeUntilStop()
    {
        return aloneTimeUntilStop;
    }

    public boolean getDBots() {
        return dbots;
    }
}
