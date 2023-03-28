package dev.cosgy.textToSpeak.settings

import dev.cosgy.textToSpeak.utils.OtherUtil
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

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
class UserSettingsManager {
    private val settings: HashMap<Long, UserSettings>
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var connection: Connection? = null

    init {
        settings = HashMap()
        val path = OtherUtil.getPath("UserData.sqlite")
        var create = false
        if (!path!!.toFile().exists()) {
            create = true
            val original = OtherUtil.loadResource(this, "UserData.sqlite")
            try {
                FileUtils.writeStringToFile(path.toFile(), original, StandardCharsets.UTF_8)
                logger.info("データベースファイルが存在しなかったためファイルを作成しました。")
            } catch (e: IOException) {
                logger.error("データベースファイルを作成できませんでした。", e)
            }
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:UserData.sqlite")
            val statement = connection!!.createStatement()
            val sql = "create table if not exists settings ( id integer not null constraint settings_pk primary key, voice TEXT, speed real, intonation real, voiceQualityA  real, voiceQualityFm real)"
            statement.execute(sql)
            val rs = statement.executeQuery("select * from settings")
            while (rs.next()) {
                settings[rs.getLong(1)] = UserSettings(this, rs.getLong(1), rs.getString(2), rs.getFloat(3), rs.getFloat(4), rs.getFloat(5), rs.getFloat(6))
            }
        } catch (throwables: SQLException) {
            logger.error("データベースに接続できませんでした。", throwables)
        }
    }

    fun getSettings(userId: Long): UserSettings {
        return settings.computeIfAbsent(userId) { userId: Long -> createDefaultSettings(userId) }
    }

    private fun createDefaultSettings(userId: Long): UserSettings {
        return UserSettings(this, userId, "mei_normal", 1.0f, 1.0f, 0.5f, 2.0f)
    }

    fun saveSetting(userId: Long) {
        val sql = "REPLACE INTO settings (id, voice, speed, intonation, voiceQualityA, voiceQualityFm) VALUES (?,?,?,?,?,?)"
        val settings = settings[userId]
        try {
            connection!!.prepareStatement(sql).use { ps ->
                ps.setLong(1, userId)
                ps.setString(2, settings!!.voice)
                ps.setFloat(3, settings.speed)
                ps.setFloat(4, settings.intonation)
                ps.setFloat(5, settings.voiceQualityA)
                ps.setFloat(6, settings.voiceQualityFm)
                logger.debug(ps.toString())
                ps.executeUpdate()
            }
        } catch (throwables: SQLException) {
            logger.error("設定を保存できませんでした。", throwables)
        }
    }

    fun closeConnection() {
        try {
            connection!!.close()
            logger.info("データベース接続を終了しました。")
        } catch (e: SQLException) {
            logger.error("データベース接続を終了できませんでした。", e)
        }
    }
}