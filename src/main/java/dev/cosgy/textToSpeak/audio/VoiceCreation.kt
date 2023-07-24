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
package dev.cosgy.textToSpeak.audio

import com.ibm.icu.text.Transliterator
import dev.cosgy.textToSpeak.Bot
import dev.cosgy.textToSpeak.settings.UserSettings
import net.dv8tion.jda.api.entities.*
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.BreakIterator
import java.util.*
import java.util.regex.Pattern

class VoiceCreation( // 各種設定の値を保持するためのフィールド
    private val bot: Bot
) {
    private val dictionary: String? = bot.config.dictionary
    private val voiceDirectory: String? = bot.config.voiceDirectory
    private val winJTalkDir: String? = bot.config.winJTalkDir
    private val maxMessageCount: Int = bot.config.maxMessageCount

    @Throws(IOException::class, InterruptedException::class)
    fun createVoice(guild: Guild, user: User, message: String): String {
        // ファイル名やパスの生成に使用するIDを生成する
        val guildId = guild.id
        val fileId = UUID.randomUUID().toString()
        val fileName = "wav" + File.separator + guildId + File.separator + fileId + ".wav"

        // 必要なディレクトリを作成する
        createDirectories(guildId)

        // ユーザーの設定を取得する
        val settings = bot.userSettingsManager.getSettings(user.idLong)

        // 辞書データを取得し、メッセージを変換する
        val words = bot.dictionary?.getWords(guild.idLong)
        var dicMsg = sanitizeMessage(message)
        for ((key, value) in words!!) {
            dicMsg = dicMsg.replace(Regex.escape(key!!), value!!)
        }

        toKatakanaIfEnglishExists(dicMsg)

        val tmpFilePath = createTmpTextFile(guildId, fileId, dicMsg.replace("\n", ""))


        // コマンドを生成して実行する
        val command = getCommand(settings, tmpFilePath, fileName)
        val builder = ProcessBuilder(*command)
        builder.redirectErrorStream(true)
        logger.debug("Command: " + java.lang.String.join(" ", *command))
        val process = builder.start()
        process.waitFor()
        return fileName
    }

    // 英単語をカタカナに変換するメソッド
    private fun toKatakanaIfEnglishExists(message: String): String {
        var englishExists = false
        for (c in message) {
            if (c in 'a'..'z' || c in 'A'..'Z') {
                englishExists = true
                break
            }
        }
        return if (englishExists) {
            val latinToKatakana = Transliterator.getInstance("Latin-Katakana")
            latinToKatakana.transliterate(message)
        } else {
            message
        }
    }

    // メッセージをサニタイズするメソッド
    private fun sanitizeMessage(message: String): String {
        var sanitizedMsg = message.replace("[\\uD800-\\uDFFF]".toRegex(), " ")
        sanitizedMsg = sanitizedMsg.replace("Kosugi_kun".toRegex(), "コスギクン")
        val sentences = BreakIterator.getSentenceInstance(Locale.JAPANESE)
        sentences.setText(sanitizedMsg)
        var messageCount = 0
        var lastIndex = 0
        val builder = StringBuilder()
        while (sentences.next() != BreakIterator.DONE) {
            val sentence = sanitizedMsg.substring(lastIndex, sentences.current())
            if (maxMessageCount > 0 && sentence.length + builder.length > maxMessageCount) {
                builder.append("以下略")
                break
            }
            builder.append(sentence)
            builder.append("\n")
            messageCount++
            lastIndex = sentences.current()
        }
        return builder.toString()
    }

    // テキストファイルを作成するメソッド
    @Throws(FileNotFoundException::class, UnsupportedEncodingException::class)
    private fun createTmpTextFile(guildId: String, fileId: String, message: String): String {
        val filePath = Paths.get("tmp", guildId, "$fileId.txt").toString()
        PrintWriter(filePath, characterCode).use { it.write(message) }
        return filePath
    }

    private val characterCode: String
        // 文字コードを取得するメソッド
        get() {
            if (!IS_WINDOWS) return "UTF-8"

            return if (bot.config.isForceUTF8) "UTF-8" else "Shift-JIS"
        }

    // コマンドを生成するメソッド
    private fun getCommand(settings: UserSettings?, tmpFilePath: String, fileName: String): Array<String?> {
        val command = ArrayList<String?>()
        command.add(openJTalkExecutable)
        command.add("-x")
        command.add(dictionary)
        command.add("-m")
        command.add(getVoiceFilePath(settings!!.voiceSetting))
        command.add("-ow")
        command.add(fileName)
        command.add("-r")
        command.add(settings.speedSetting.toString())
        command.add("-jf")
        command.add(settings.intonationSetting.toString())
        command.add("-a")
        command.add(settings.voiceQualityASetting.toString())
        command.add("-fm")
        command.add(settings.voiceQualityFmSetting.toString())
        command.add(tmpFilePath)
        return command.toTypedArray()
    }

    private val openJTalkExecutable: String?
        get() = if (IS_WINDOWS) {
            winJTalkDir?.let { Paths.get(it, "open_jtalk.exe").toString() }
        } else {
            "open_jtalk"
        }

    private fun getVoiceFilePath(voice: String?): String? {
        return voiceDirectory?.let { Paths.get(it, "$voice.htsvoice").toString() }
    }

    // 必要なディレクトリを作成するメソッド

    @Throws(IOException::class)
    private fun createDirectories(guildId: String) {
        val tmpPath: Path = Paths.get("tmp", guildId)
        val wavPath: Path = Paths.get("wav", guildId)
        Files.createDirectories(tmpPath)
        Files.createDirectories(wavPath)
    }

    // ギルドに関連する一時ファイルや音声ファイルを削除するメソッド
    @Throws(IOException::class)
    fun clearGuildFolder(guild: Guild) {
        val guildId = guild.id
        val tmpPath = Paths.get("tmp" + File.separator + guildId)
        val wavPath = Paths.get("wav" + File.separator + guildId)
        if (Files.exists(tmpPath)) {
            FileUtils.cleanDirectory(tmpPath.toFile())
            logger.info("Cleared temporary files for guild: $guildId")
        }
        if (Files.exists(wavPath)) {
            FileUtils.cleanDirectory(wavPath.toFile())
            logger.info("Cleared WAV files for guild: $guildId")
        }
    }

    val voices: List<String>
        get() {
            val voiceDir = File(requireNotNull(voiceDirectory) { "voiceDirectory is null" })
            return voiceDir.listFiles { _, name -> name.endsWith(".htsvoice") }
                ?.map { file -> file.nameWithoutExtension }
                ?.toList()
                .orEmpty()
                .also { logger.debug("Available voices: {}", it) }
        }


    companion object {
        private val logger = LoggerFactory.getLogger(VoiceCreation::class.java)
        private val IS_WINDOWS = System.getProperty("os.name").lowercase(Locale.getDefault()).startsWith("win")
    }
}