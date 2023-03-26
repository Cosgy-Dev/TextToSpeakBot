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

package dev.cosgy.TextToSpeak.settings;

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
                e.printStackTrace();
            }
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:UserData.sqlite");
            Statement statement = connection.createStatement();
            String sql = "create table settings ( id integer not null constraint settings_pk primary key, voice TEXT, speed real, intonation real, voiceQualityA  real, voiceQualityFm real)";
            if (create) {
                statement.execute(sql);
            }

            ResultSet rs = statement.executeQuery("select * from settings");
            while (rs.next()) {
                settings.put(rs.getLong(1), new UserSettings(this, rs.getLong(1), rs.getString(2), rs.getFloat(3), rs.getFloat(4), rs.getFloat(5), rs.getFloat(6)));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public UserSettings getSettings(long userId) {
        return settings.computeIfAbsent(userId, id -> createDefaultSettings(userId));
    }

    /**
     * デフォルト設定のデータを作って返す。
     *
     * @return 作成されたデフォルト設定
     */
    private UserSettings createDefaultSettings(Long userId) {
        // [スピード:0.0-] [抑揚:0.0-] [声質a:0.0-1.0] [声質fm:0.0-]
        return new UserSettings(this, userId, "mei_normal", 1.0f, 1.0f, 0.5f, 2.0f);
    }

    protected void saveSetting(Long userId) {
        String sql = "REPLACE INTO settings VALUES (?,?,?,?,?,?)";
        UserSettings settings = this.settings.get(userId);
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setString(2, settings.getVoice());
            ps.setFloat(3, settings.getSpeed());
            ps.setFloat(4, settings.getIntonation());
            ps.setFloat(5, settings.getVoiceQualityA());
            ps.setFloat(6, settings.getVoiceQualityFm());

            logger.debug(ps.toString());
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
