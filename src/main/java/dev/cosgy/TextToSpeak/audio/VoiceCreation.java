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

package dev.cosgy.TextToSpeak.audio;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.settings.UserSettings;
import jdk.nashorn.internal.runtime.Debug;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class VoiceCreation {
    private Bot bot;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("win");

    String dic = "/var/lib/mecab/dic/open-jtalk/naist-jdic";
    String vDic = "/usr/share/hts-voice";
    ArrayList<String> voices = new ArrayList<>();
    String testVoice = "/usr/share/hts-voice/mei_normal.htsvoice";

    public void Init(Bot bot){
        this.bot = bot;
        FilenameFilter filter = (file, str) -> {
            // 拡張子を指定する
            return str.endsWith("htsvoice");
        };
        vDic = bot.getConfig().getVoiceDirectory();
        dic = bot.getConfig().getDictionary();

        File dir = new File(vDic);
        File[] list = dir.listFiles(filter);
        for (File file : list) {
            voices.add(file.getName().replace(".htsvoice", ""));
        }

        logger.debug("声データ："+ voices.toString());
    }

    public String CreateVoice(Guild guild , User user, String message) {
        if(!tmpFolderExists()){
            createTmpFolder();
            createGuildTmpFolder(guild);
            logger.info("tmpフォルダが存在しなかったため作成しました。");
        }else if(!guildTmpFolderExists(guild)){
            createGuildTmpFolder(guild);
        }

        if(!wavFolderExists()){
            createWavFolder();
            createGuildWavFolder(guild);
            logger.info("wavフォルダが存在しなかったため作成しました。");
        }else if(!guildWavFolderExists(guild)){
            createGuildWavFolder(guild);
        }

        UserSettings settings = bot.getUserSettingsManager().getSettings(user.getIdLong());
        Process p = null;
        UUID fileId = UUID.randomUUID();
        String fileName = "wav" + File.separator + guild.getId() + File.separator + fileId + ".wav";

        File file = new File(vDic+ File.separator+ settings.getVoice() + ".htsvoice");
        logger.debug("読み込む声データ:"+ file.toString());

        HashMap<String, String> words = bot.getDictionary().GetWords(guild.getIdLong());
        String dicMsg =message;
        dicMsg = dicMsg.replaceAll("Kosugi_kun", "コスギクン");

        try {
            for (String key : words.keySet()) {
                dicMsg = dicMsg.replaceAll(key, words.get(key));
            }
        }catch (NullPointerException ignored){
            logger.debug("辞書データがなかったため処理をスキップします。");
        }
        String[] Command;
        if(IS_WINDOWS){
            File dir = new File(bot.getConfig().getWinJTalkDir()+ File.separator + "open_jtalk.exe");
            Command = new String[]{dir.toString(), "-x", dic, "-m", file.toString(), "-ow", fileName, "-r", String.valueOf(settings.getSpeed()), "-jf", String.valueOf(settings.getIntonation()), "-a", String.valueOf(settings.getVoiceQualityA()), "-fm", String.valueOf(settings.getVoiceQualityFm()), CreateTmpText(guild,fileId, dicMsg.replaceAll("[\r\n]", " "))};
        }else{
            Command = new String[]{"open_jtalk", "-x", dic, "-m", file.toString(), "-ow", fileName, "-r", String.valueOf(settings.getSpeed()), "-jf", String.valueOf(settings.getIntonation()), "-a", String.valueOf(settings.getVoiceQualityA()), "-fm", String.valueOf(settings.getVoiceQualityFm()), CreateTmpText(guild ,fileId, dicMsg.replaceAll("[\r\n]", " "))};
        }


        Runtime runtime = Runtime.getRuntime(); // ランタイムオブジェクトを取得する
        try {
            p = runtime.exec(Command); // 指定したコマンドを実行する
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Objects.requireNonNull(p).waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return fileName;
    }

    private String CreateTmpText(Guild guild ,UUID id, String message) {
        String tmp_dir = "tmp" + File.separator+ guild.getId() + File.separator + id + ".txt";
        String characterCode = IS_WINDOWS ? "Shift-JIS" : "UTF-8";
        try (PrintWriter writer = new PrintWriter(tmp_dir, characterCode)) {
            writer.write(message);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return tmp_dir;
    }

    public void ClearGuildFolder(Guild guild){
        File tmp = new File("tmp" + File.separator + guild.getId());
        File wav = new File("wav" + File.separator + guild.getId());

        try {
            FileUtils.cleanDirectory(tmp);
            FileUtils.cleanDirectory(wav);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getVoices(){
        return voices;
    }

    public void createTmpFolder() {
        try {
            Files.createDirectory(Paths.get("tmp"));
        } catch (IOException ignore) {
        }
    }

    public boolean tmpFolderExists() {
        return Files.exists(Paths.get("tmp"));
    }

    public void createWavFolder() {
        try {
            Files.createDirectory(Paths.get("wav"));
        } catch (IOException ignore) {
        }
    }

    public boolean wavFolderExists() {
        return Files.exists(Paths.get("wav"));
    }

    public boolean guildWavFolderExists(Guild guild){
        return Files.exists(Paths.get("wav"+ File.separator + guild.getId()));
    }

    public void createGuildWavFolder(Guild guild){
        try {
            Files.createDirectory(Paths.get("wav"+File.separator + guild.getId()));
        } catch (IOException ignore) {
        }
    }

    public boolean guildTmpFolderExists(Guild guild){
        return Files.exists(Paths.get("tmp"+ File.separator + guild.getId()));
    }

    public void createGuildTmpFolder(Guild guild){
        try {
            Files.createDirectory(Paths.get("tmp"+File.separator + guild.getId()));
        } catch (IOException ignore) {
        }
    }
}