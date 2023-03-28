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

import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.settings.UserSettings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoiceCreation {
    private static final Logger logger = LoggerFactory.getLogger(VoiceCreation.class);
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("win");

    // 各種設定の値を保持するためのフィールド
    private final Bot bot;
    private final String dictionary;
    private final String voiceDirectory;
    private final String winJTalkDir;
    private final int maxMessageCount;

    // 初期化処理を行うメソッド
    public VoiceCreation(Bot bot) {
        this.bot = bot;
        this.dictionary = bot.getConfig().getDictionary();
        this.voiceDirectory = bot.getConfig().getVoiceDirectory();
        this.winJTalkDir = bot.getConfig().getWinJTalkDir();
        this.maxMessageCount = bot.getConfig().getMaxMessageCount();
    }

    public String createVoice(Guild guild, User user, String message) throws IOException, InterruptedException {
        // ファイル名やパスの生成に使用するIDを生成する
        String guildId = guild.getId();
        String fileId = UUID.randomUUID().toString();
        String fileName = "wav" + File.separator + guildId + File.separator + fileId + ".wav";

        // 必要なディレクトリを作成する
        createDirectories(guildId);

        // ユーザーの設定を取得する
        UserSettings settings = bot.getUserSettingsManager().getSettings(user.getIdLong());

        // 辞書データを取得し、メッセージを変換する
        HashMap<String, String> words = bot.getDictionary().getWords(guild.getIdLong());
        String dicMsg = sanitizeMessage(message);
        for (Map.Entry<String, String> entry : words.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            dicMsg = dicMsg.replaceAll(key, value);
        }
        String tmpFilePath = createTmpTextFile(guildId, fileId, dicMsg);


        // コマンドを生成して実行する
        String[] command = getCommand(settings, tmpFilePath, fileName);
        ProcessBuilder builder = new ProcessBuilder(command);

        builder.redirectErrorStream(true);
        logger.debug("Command: " + String.join(" ", command));
        Process process = builder.start();
        process.waitFor();

        return fileName;
    }

    // メッセージをサニタイズするメソッド
    private String sanitizeMessage(String message) {
        String sanitizedMsg = message.replaceAll("[\\uD800-\\uDFFF]", " ");
        sanitizedMsg = sanitizedMsg.replaceAll("Kosugi_kun", "コスギクン");
        return sanitizedMsg;
    }

    // テキストファイルを作成するメソッド
    private String createTmpTextFile(String guildId, String fileId, String message) throws FileNotFoundException, UnsupportedEncodingException {
        String filePath = "tmp" + File.separator + guildId + File.separator + fileId + ".txt";
        try (PrintWriter writer = new PrintWriter(filePath, getCharacterCode())) {
            writer.write(message);
        }
        return filePath;
    }

    // 文字コードを取得するメソッド
    private String getCharacterCode() {
        return IS_WINDOWS ? "Shift-JIS" : "UTF-8";
    }

    // コマンドを生成するメソッド
    private String[] getCommand(UserSettings settings, String tmpFilePath, String fileName) {
        ArrayList<String> command = new ArrayList<>();
        command.add(getOpenJTalkExecutable());
        command.add("-x");
        command.add(dictionary);
        command.add("-m");
        command.add(getVoiceFilePath(settings.getVoice()));
        command.add("-ow");
        command.add(fileName);
        command.add("-r");
        command.add(String.valueOf(settings.getSpeed()));
        command.add("-jf");
        command.add(String.valueOf(settings.getIntonation()));
        command.add("-a");
        command.add(String.valueOf(settings.getVoiceQualityA()));
        command.add("-fm");
        command.add(String.valueOf(settings.getVoiceQualityFm()));
        command.add(tmpFilePath);

        return command.toArray(new String[0]);
    }

    private String getOpenJTalkExecutable() {
        if (IS_WINDOWS) {
            return Paths.get(winJTalkDir, "open_jtalk.exe").toString();
        } else {
            return "open_jtalk";
        }
    }

    private String getVoiceFilePath(String voice) {
        return Paths.get(voiceDirectory, voice + ".htsvoice").toString();
    }

    // 必要なディレクトリを作成するメソッド
    private void createDirectories(String guildId) throws IOException {
        createDirectory("tmp");
        createDirectory("tmp" + File.separator + guildId);
        createDirectory("wav");
        createDirectory("wav" + File.separator + guildId);
    }

    // ディレクトリを作成するメゾット
    private void createDirectory(String directory) throws IOException {
        Path path = Paths.get(directory);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
            logger.info("Created directory: " + directory);
        }
    }

    // ギルドに関連する一時ファイルや音声ファイルを削除するメソッド
    public void clearGuildFolder(Guild guild) throws IOException {
        String guildId = guild.getId();
        Path tmpPath = Paths.get("tmp" + File.separator + guildId);
        Path wavPath = Paths.get("wav" + File.separator + guildId);
        if (Files.exists(tmpPath)) {
            FileUtils.cleanDirectory(tmpPath.toFile());
            logger.info("Cleared temporary files for guild: " + guildId);
        }

        if (Files.exists(wavPath)) {
            FileUtils.cleanDirectory(wavPath.toFile());
            logger.info("Cleared WAV files for guild: " + guildId);
        }
    }

    // 利用可能な音声名を取得するメソッド
    public ArrayList<String> getVoices() {
        FilenameFilter filter = (file, str) -> str.endsWith("htsvoice");
        File dir = new File(voiceDirectory);
        File[] list = dir.listFiles(filter);
        ArrayList<String> voices = new ArrayList<>();

        for (File file : list) {
            voices.add(file.getName().replace(".htsvoice", ""));
        }

        logger.debug("Available voices: " + voices.toString());
        return voices;
    }
}
