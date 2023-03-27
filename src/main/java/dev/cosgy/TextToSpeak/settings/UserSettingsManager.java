package dev.cosgy.TextToSpeak.settings;

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

import dev.cosgy.TextToSpeak.utils.OtherUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;

public class UserSettingsManager {
    private final HashMap<Long, UserSettings> settings;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Connection connection;

    public UserSettingsManager() {
        this.settings = new HashMap<>();
        Path path = OtherUtil.getPath("UserData.sqlite");
        boolean create = false;
        if (!path.toFile().exists()) {
            create = true;
            String original = OtherUtil.loadResource(this, "UserData.sqlite");
            try {
                FileUtils.writeStringToFile(path.toFile(), original, StandardCharsets.UTF_8);
                logger.info("データベースファイルが存在しなかったためファイルを作成しました。");
            } catch (IOException e) {
                logger.error("データベースファイルを作成できませんでした。", e);
            }
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:UserData.sqlite");
            Statement statement = connection.createStatement();
            String sql = "create table if not exists settings ( id integer not null constraint settings_pk primary key, voice TEXT, speed real, intonation real, voiceQualityA  real, voiceQualityFm real)";
            statement.execute(sql);

            ResultSet rs = statement.executeQuery("select * from settings");
            while (rs.next()) {
                settings.put(rs.getLong(1), new UserSettings(this, rs.getLong(1), rs.getString(2), rs.getFloat(3), rs.getFloat(4), rs.getFloat(5), rs.getFloat(6)));
            }
        } catch (SQLException throwables) {
            logger.error("データベースに接続できませんでした。", throwables);
        }
    }

    public UserSettings getSettings(long userId) {
        return settings.computeIfAbsent(userId, this::createDefaultSettings);
    }

    private UserSettings createDefaultSettings(Long userId) {
        return new UserSettings(this, userId, "mei_normal", 1.0f, 1.0f, 0.5f, 2.0f);
    }

    protected void saveSetting(Long userId) {
        String sql = "REPLACE INTO settings (id, voice, speed, intonation, voiceQualityA, voiceQualityFm) VALUES (?,?,?,?,?,?)";
        UserSettings settings = this.settings.get(userId);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, settings.getVoice());
            ps.setFloat(3, settings.getSpeed());
            ps.setFloat(4, settings.getIntonation());
            ps.setFloat(5, settings.getVoiceQualityA());
            ps.setFloat(6, settings.getVoiceQualityFm());

            logger.debug(ps.toString());
            ps.executeUpdate();
        } catch (SQLException throwables) {
            logger.error("設定を保存できませんでした。", throwables);
        }
    }

    public void closeConnection() {
        try {
            connection.close();
            logger.info("データベース接続を終了しました。");
        } catch (SQLException e) {
            logger.error("データベース接続を終了できませんでした。", e);
        }
    }
}
