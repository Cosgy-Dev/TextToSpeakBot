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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class Dictionary {
    private Bot bot;
    private Path path = null;
    private boolean create = false;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Connection connection;
    private Statement statement;

    /**
     * Long サーバーID
     * String 1　元の単語
     * String 2　単語の読み
     */
    private HashMap<Long, HashMap<String, String>> guildDic;

    public void Init(Bot bot){
        this.bot = bot;
        this.guildDic = new HashMap<>();

        path = OtherUtil.getPath("UserData.sqlite");
        if(!path.toFile().exists()){
            create = true;
            String original = OtherUtil.loadResource(this, "UserData.sqlite");
            try {
                FileUtils.writeStringToFile(path.toFile(), original, StandardCharsets.UTF_8);
                logger.info("データベースファイルが存在しなかったためファイルを作成しました。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:UserData.sqlite");
            statement = connection.createStatement();
            String SQL = "CREATE TABLE IF NOT EXISTS Dictionary(guild_id integer,word text,reading text)";
            statement.execute(SQL);

            List<Guild> guilds = bot.getJDA().getGuilds();

            for (Guild value : guilds) {
                long guildId = value.getIdLong();
                PreparedStatement ps = connection.prepareStatement("select * from Dictionary where guild_id = ?");
                ps.setLong(1, guildId);
                ResultSet rs = ps.executeQuery();
                HashMap<String, String> word = new HashMap<>();
                while (rs.next()) {
                    word.put(rs.getString(2), rs.getString(3));
                }
                guildDic.put(guildId, word);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void UpdateDictionary(Long guildId, String word, String reading) {
        HashMap<String, String> words;
        words = bot.getDictionary().GetWords(guildId);
        try{
            words.put(word, reading);
        }catch (NullPointerException e){
            words = new HashMap<>();
            words.put(word, reading);
        }

        guildDic.put(guildId, words);

        String sql = "REPLACE INTO Dictionary VALUES (?,?,?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, guildId);
            ps.setString(2, word);
            ps.setString(3, reading);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public HashMap<String, String> GetWords(Long guildId){
        return guildDic.get(guildId);
    }
}
