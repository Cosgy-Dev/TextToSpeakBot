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
import dev.cosgy.textToSpeak.utils.OtherUtil
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction

class Dictionary private constructor(bot: Bot) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val path: Path?
    private val create: Boolean
    private var connection: Connection
    private val guildDic: ConcurrentHashMap<Long, HashMap<String?, String?>> = ConcurrentHashMap()

    init {
        path = OtherUtil.getPath("UserData.sqlite")
        create = !path.toFile().exists()
        try {
            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection("jdbc:sqlite:UserData.sqlite")
            val statement = connection.createStatement()
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Dictionary(guild_id integer,word text,reading)")
            val guilds = bot.jda!!.guilds
            for (value in guilds) {
                val guildId = value.idLong
                val optionalHashMap = getWordsFromDatabase(guildId)
                optionalHashMap.ifPresent { hashMap: HashMap<String?, String?> -> guildDic[guildId] = hashMap }
            }
        } catch (e: SQLException) {
            logger.error("An error occurred while initializing the dictionary: ", e)
            throw IllegalStateException(e)
        } catch (e: ClassNotFoundException) {
            logger.error("An error occurred while initializing the dictionary: ", e)
            throw IllegalStateException(e)
        }
        logger.info("Dictionary initialization completed.")
    }

    /**
     * データベースとHashMapの内容を更新または新規追加します。
     *
     * @param guildId サーバーID
     * @param word    単語
     * @param reading 読み方
     */
    @Synchronized
    fun updateDictionary(guildId: Long, word: String?, reading: String?) {
        guildDic.compute(guildId) { _: Long?, v: HashMap<String?, String?>? ->
            val words: HashMap<String?, String?> = v ?: HashMap()
            words[word] = reading
            executeUpdate(guildId, word, reading)
            words
        }
    }

    /**
     * データベースに登録されている単語を削除します。
     *
     * @param guildId サーバーID
     * @param word    単語
     * @return 正常に削除できた場合は `true`、削除時に問題が発生した場合は`false`を返します。
     */
    @Synchronized
    fun deleteDictionary(guildId: Long, word: String?): Boolean {
        guildDic.compute(guildId, BiFunction { _: Long?, v: HashMap<String?, String?>? ->
            if (v == null || !v.containsKey(word)) {
                return@BiFunction null
            }
            val words = HashMap(v)
            words.remove(word)
            executeDelete(guildId, word)
            words
        })
        return true
    }

    /**
     * サーバーの辞書データを取得します。
     *
     * @param guildId サーバーID
     * @return `HashMap<String, String>`形式の変数を返します。
     */
    fun getWords(guildId: Long): HashMap<String?, String?> {
        return guildDic.getOrDefault(guildId, HashMap())
    }

    private fun getWordsFromDatabase(guildId: Long): Optional<HashMap<String?, String?>> {
        val sql = "SELECT * FROM Dictionary WHERE guild_id = ?"
        try {
            connection.prepareStatement(sql).use { ps ->
                ps.setLong(1, guildId)
                val rs = ps.executeQuery()
                val word = HashMap<String?, String?>()
                while (rs.next()) {
                    word[rs.getString(2)] = rs.getString(3)
                }
                return Optional.of(word)
            }
        } catch (throwables: SQLException) {
            logger.error("An error occurred while retrieving data from the dictionary: ", throwables)
            return Optional.empty()
        }
    }

    private fun executeUpdate(guildId: Long, word: String?, reading: String?) {
        val sql = "INSERT OR REPLACE INTO Dictionary(guild_id, word, reading) VALUES (?,?,?)"
        try {
            connection.prepareStatement(sql).use { ps ->
                ps.setLong(1, guildId)
                ps.setString(2, word)
                ps.setString(3, reading)
                ps.executeUpdate()
            }
        } catch (throwables: SQLException) {
            logger.error("An error occurred while updating the dictionary: ", throwables)
        }
    }

    private fun executeDelete(guildId: Long, word: String?) {
        val sql = "DELETE FROM Dictionary WHERE guild_id = ? AND word = ?"
        try {
            connection.prepareStatement(sql).use { ps ->
                ps.setLong(1, guildId)
                ps.setString(2, word)
                ps.executeUpdate()
            }
        } catch (throwables: SQLException) {
            logger.error("An error occurred while deleting from the dictionary: ", throwables)
        }
    }

    fun close() {
        try {
            connection.close()
        } catch (throwables: SQLException) {
            logger.error("An error occurred while closing the database connection: ", throwables)
        }
    }

    companion object {
        private var instance: Dictionary? = null
        fun getInstance(bot: Bot): Dictionary? {
            if (instance == null) {
                instance = Dictionary(bot)
            }
            return instance
        }
    }
}