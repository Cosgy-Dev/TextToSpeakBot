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
package dev.cosgy.textToSpeak.commands.general

import com.google.gson.JsonParser
import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent
import dev.cosgy.textToSpeak.Bot
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TranslateCmd(bot: Bot) : SlashCommand() {
    override fun execute(event: SlashCommandEvent) {
        if(deeplApiKey.isNullOrEmpty()){
            event.reply("翻訳機能が有効になっていません。\n" +
                    "この機能を利用する場合はボット管理者が翻訳機能を有効にする必要があります。").setEphemeral(true)

            return
        }

        val text = event.getOption("text")!!.asString
        val sourceLang = event.getOption("source_language")!!.asString
        val targetLang = event.getOption("target_language")!!.asString
        var translation: String? = null
        translation = try {
            translateText(text, sourceLang, targetLang)
        } catch (e: IOException) {
            event.reply("翻訳に失敗しました。").setEphemeral(true).queue()
            return
        } catch (e: InterruptedException) {
            event.reply("翻訳に失敗しました。").setEphemeral(true).queue()
            return
        }
        val sourceLangName = LANGUAGES.getOrDefault(sourceLang, "Unknown")
        val targetLangName = LANGUAGES.getOrDefault(targetLang, "Unknown")
        val eBuilder = EmbedBuilder().setTitle("翻訳結果")
            .addField(String.format("%s (%s)", sourceLangName, sourceLang), "```$text```", false)
            .addField(String.format("%s (%s)", targetLangName, targetLang), "```$translation```", false)
        event.replyEmbeds(eBuilder.build()).setEphemeral(false).queue()
    }

    init {
        name = "translate"
        help = "入力されたテキストを任意の言語に翻訳します。"
        guildOnly = false
        deeplApiKey = bot.config.deeplApiKey
        this.category = Category("便利機能")
        val options: MutableList<OptionData> = ArrayList()
        val sourceLanguageOption = OptionData(OptionType.STRING, "source_language", "翻訳前の言語を選択してください。", true)
        sourceLanguageOption.addChoice("自動検出", "auto")
        sourceLanguageOption.addChoice("英語", "en")
        sourceLanguageOption.addChoice("日本語", "ja")
        sourceLanguageOption.addChoice("中国語", "zh")
        sourceLanguageOption.addChoice("ドイツ語", "de")
        sourceLanguageOption.addChoice("スペイン語", "es")
        sourceLanguageOption.addChoice("フランス語", "fr")
        sourceLanguageOption.addChoice("イタリア語", "it")
        sourceLanguageOption.addChoice("オランダ語", "nl")
        sourceLanguageOption.addChoice("ポーランド語", "pl")
        sourceLanguageOption.addChoice("ポルトガル語", "pt")
        sourceLanguageOption.addChoice("ロシア語", "ru")
        options.add(sourceLanguageOption)
        // 翻訳後の言語を選択するオプション
        val targetLanguageOption = OptionData(OptionType.STRING, "target_language", "翻訳後の言語を選択してください。", true)
        targetLanguageOption.addChoice("英語", "en")
        targetLanguageOption.addChoice("日本語", "ja")
        targetLanguageOption.addChoice("中国語", "zh")
        targetLanguageOption.addChoice("ドイツ語", "de")
        targetLanguageOption.addChoice("スペイン語", "es")
        targetLanguageOption.addChoice("フランス語", "fr")
        targetLanguageOption.addChoice("イタリア語", "it")
        targetLanguageOption.addChoice("オランダ語", "nl")
        targetLanguageOption.addChoice("ポーランド語", "pl")
        targetLanguageOption.addChoice("ポルトガル語", "pt")
        targetLanguageOption.addChoice("ロシア語", "ru")
        options.add(targetLanguageOption)
        val text = OptionData(OptionType.STRING, "text", "翻訳するテキストを入力してください。", true)
        options.add(text)
        this.options = options
    }

    companion object {
        var deeplApiKey:String? = ""
        @Throws(IOException::class, InterruptedException::class)
        private fun translateText(text: String, sourceLang: String, targetLang: String): String {
            val url = "https://api-free.deepl.com/v2/translate"
            val textParam = "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
            val sourceLangParam = "source_lang=$sourceLang"
            val targetLangParam = "target_lang=$targetLang"
            var request = java.lang.String.join("&", textParam, targetLangParam)
            if (sourceLang != "auto") {
                request += "&$sourceLangParam"
            }
            val client = HttpClient.newHttpClient()
            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "DeepL-Auth-Key $deeplApiKey")
                .POST(HttpRequest.BodyPublishers.ofString(request))
                .build()
            val response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                throw IOException("Translation API returned status code " + response.statusCode())
            }
            val jsonElement = JsonParser.parseString(response.body())
            val translations = jsonElement.asJsonObject.getAsJsonArray("translations")
            val translationObject = translations[0].asJsonObject
            return translationObject["text"].asString
        }

        private val LANGUAGES = mapOf(
            "auto" to "自動検出",
            "de"   to "ドイツ語",
            "en"   to "英語",
            "es"   to "スペイン語",
            "fr"   to "フランス語",
            "it"   to "イタリア語",
            "ja"   to "日本語",
            "nl"   to "オランダ語",
            "pl"   to "ポーランド後",
            "pt"   to "ポルトガル語",
            "ru"   to "ロシア語",
            "zh"   to "中国語"
        )
    }
}
