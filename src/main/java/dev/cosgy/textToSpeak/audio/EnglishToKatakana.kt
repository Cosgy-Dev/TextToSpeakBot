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

import dev.cosgy.textToSpeak.Bot
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class EnglishToKatakana(bot: Bot) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    // 辞書ファイルの名前
    private val fileName = "bep-eng.dic"
    private val map = TreeMap<String, String>()

    init {
        logger.info("英語->カタカナ変換辞書を読み込みます。")
        val inputStream = object {}.javaClass.getResourceAsStream("/$fileName")
        val reader = BufferedReader(InputStreamReader(inputStream!!))

        reader.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(" ", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0]
                    val value = parts[1]
                    map[key] = value
                }
            }
        }

        logger.info("英語->カタカナ変換辞書の読み込みが完了しました。")
    }

    fun convert(text: String): String {
        val startTime = System.currentTimeMillis()
        val words = text.split("\\s+".toRegex()) // 空白文字で単語を区切る

        val result = StringBuilder()

        for (word in words) {
            val replacement = searchWord(word.uppercase())
            if (replacement != null) {
                result.append(replacement)
            } else {
                result.append(word)
            }
            result.append(" ")
        }

        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        logger.debug("探索処理時間: $executionTime ミリ秒")


        return result.toString().trim()
    }

    private fun searchWord(word: String): String? {
        return map[word]
    }
}