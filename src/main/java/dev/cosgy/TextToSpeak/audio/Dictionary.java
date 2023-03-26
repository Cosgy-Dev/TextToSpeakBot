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

import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Dictionary {
    private static Dictionary instance;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Bot bot;
    private final Path path;
    private final boolean create;
    private final Connection connection;
    private final ConcurrentHashMap<Long, HashMap<String, String>> guildDic;

    private Dictionary(Bot bot) {
        this.bot = bot;
        this.guildDic = new ConcurrentHashMap<>();
        this.path = OtherUtil.getPath("UserData.sqlite");
        this.create = !path.toFile().exists();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:UserData.sqlite");
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Dictionary(guild_id integer,word text,reading)");

            List<Guild> guilds = bot.getJDA().getGuilds();
            for (Guild value : guilds) {
                long guildId = value.getIdLong();
                Optional<HashMap<String, String>> optionalHashMap = getWordsFromDatabase(guildId);
                optionalHashMap.ifPresent(hashMap -> guildDic.put(guildId, hashMap));
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("An error occurred while initializing the dictionary: ", e);
            throw new IllegalStateException(e);
        }
        logger.info("Dictionary initialization completed.");
    }

    public static Dictionary getInstance(Bot bot) {
        if (instance == null) {
            instance = new Dictionary(bot);
        }
        return instance;
    }

    /**
     * データベースとHashMapの内容を更新または新規追加します。
     *
     * @param guildId サーバーID
     * @param word    単語
     * @param reading 読み方
     */
    public synchronized void updateDictionary(Long guildId, String word, String reading) {
        guildDic.compute(guildId, (k, v) -> {
            HashMap<String, String> words = v != null ? v : new HashMap<>();
            words.put(word, reading);
            executeUpdate(guildId, word, reading);
            return words;
        });
    }

    /**
     * データベースに登録されている単語を削除します。
     *
     * @param guildId サーバーID
     * @param word    単語
     * @return 正常に削除できた場合は {@code true}、削除時に問題が発生した場合は{@code false}を返します。
     */
    public synchronized boolean deleteDictionary(Long guildId, String word) {
        guildDic.compute(guildId, (k, v) -> {
            if (v == null || !v.containsKey(word)) {
                return null;
            }
            HashMap<String, String> words = new HashMap<>(v);
            words.remove(word);
            executeDelete(guildId, word);
            return words;
        });
        return true;
    }

    /**
     * サーバーの辞書データを取得します。
     *
     * @param guildId サーバーID
     * @return {@code HashMap<String, String>}形式の変数を返します。
     */
    public HashMap<String, String> getWords(Long guildId) {
        return guildDic.getOrDefault(guildId, new HashMap<>());
    }

    private Optional<HashMap<String, String>> getWordsFromDatabase(Long guildId) {
        String sql = "SELECT * FROM Dictionary WHERE guild_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, guildId);
            ResultSet rs = ps.executeQuery();
            HashMap<String, String> word = new HashMap<>();
            while (rs.next()) {
                word.put(rs.getString(2), rs.getString(3));
            }
            return Optional.of(word);
        } catch (SQLException throwables) {
            logger.error("An error occurred while retrieving data from the dictionary: ", throwables);
            return Optional.empty();
        }
    }

    private void executeUpdate(Long guildId, String word, String reading) {
        String sql = "INSERT INTO Dictionary VALUES (?,?,?) ON CONFLICT (guild_id, word) DO UPDATE SET reading = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, guildId);
            ps.setString(2, word);
            ps.setString(3, reading);
            ps.setString(4, reading);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            logger.error("An error occurred while updating the dictionary: ", throwables);
        }
    }

    private void executeDelete(Long guildId, String word) {
        String sql = "DELETE FROM Dictionary WHERE guild_id = ? AND word = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, guildId);
            ps.setString(2, word);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            logger.error("An error occurred while deleting from the dictionary: ", throwables);
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            logger.error("An error occurred while closing the database connection: ", throwables);
        }
    }
}

