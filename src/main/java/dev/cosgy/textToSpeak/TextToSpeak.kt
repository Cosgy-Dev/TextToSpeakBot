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

package dev.cosgy.TextToSpeak;

import com.github.lalyos.jfiglet.FigletFont;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import dev.cosgy.TextToSpeak.commands.admin.GuildSettings;
import dev.cosgy.TextToSpeak.commands.admin.JLReadCmd;
import dev.cosgy.TextToSpeak.commands.admin.SetReadNameCmd;
import dev.cosgy.TextToSpeak.commands.admin.SettcCmd;
import dev.cosgy.TextToSpeak.commands.dictionary.AddWordCmd;
import dev.cosgy.TextToSpeak.commands.dictionary.DlWordCmd;
import dev.cosgy.TextToSpeak.commands.dictionary.WordListCmd;
import dev.cosgy.TextToSpeak.commands.general.*;
import dev.cosgy.TextToSpeak.commands.owner.ShutdownCmd;
import dev.cosgy.TextToSpeak.entities.Prompt;
import dev.cosgy.TextToSpeak.gui.GUI;
import dev.cosgy.TextToSpeak.listeners.CommandAudit;
import dev.cosgy.TextToSpeak.listeners.MessageListener;
import dev.cosgy.TextToSpeak.settings.SettingsManager;
import dev.cosgy.TextToSpeak.utils.OtherUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class TextToSpeak {
    public final static Permission[] RECOMMENDED_PERMS = {Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.MESSAGE_EXT_EMOJI, Permission.USE_APPLICATION_COMMANDS,
            Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE};
    public final static GatewayIntent[] INTENTS = {GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT};
    public static boolean CHECK_UPDATE = true;
    public static boolean COMMAND_AUDIT_ENABLED = false;

    /**
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        Logger log = getLogger("立ち上げ");
        try {
            System.out.println(FigletFont.convertOneLine("Yomiage Bot v" + OtherUtil.getCurrentVersion()) + "\n" + "by Cosgy Dev");
        } catch (IOException ignored) {
        }

        Prompt prompt = new Prompt("TextToSpeak Bot", "noguiモードに切り替えます。  -Dnogui=trueフラグを含めると、手動でnoguiモードで起動できます。",
                "true".equalsIgnoreCase(System.getProperty("nogui", "false")));

        // check deprecated nogui mode (new way of setting it is -Dnogui=true)
        for (String arg : args)
            if ("-nogui".equalsIgnoreCase(arg)) {
                prompt.alert(Prompt.Level.WARNING, "GUI", "-noguiフラグは廃止予定です。 "
                        + "jarの名前の前に-Dnogui = trueフラグを使用してください。 例：java -jar -Dnogui=true JMusicBot.jar");
            } else if ("-nocheckupdates".equalsIgnoreCase(arg)) {
                CHECK_UPDATE = false;
                log.info("アップデートチェックを無効にしました");
            } else if ("-auditcommands".equalsIgnoreCase(arg)) {
                COMMAND_AUDIT_ENABLED = true;
                log.info("実行されたコマンドの記録を有効にしました。");
            }

        String version = OtherUtil.checkVersion(prompt);

        if (!System.getProperty("java.vm.name").contains("64"))
            prompt.alert(Prompt.Level.WARNING, "Java Version", "サポートされていないJavaバージョンを使用しています。64ビット版のJavaを使用してください。");

        BotConfig config = new BotConfig(prompt);
        config.load();
        if (!config.isValid())
            return;

        EventWaiter waiter = new EventWaiter();
        SettingsManager settings = new SettingsManager();
        Bot bot = new Bot(waiter, config, settings);
        Bot.INSTANCE = bot;

        AboutCommand aboutCommand = new AboutCommand(Color.BLUE.brighter(),
                bot.GetLang().getString("appName") + "(v" + version + ")",
                RECOMMENDED_PERMS);
        aboutCommand.setIsAuthor(false);
        aboutCommand.setReplacementCharacter("🎶");


        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefix(config.getPrefix())
                .setAlternativePrefix(config.getAltPrefix())
                .setOwnerId(Long.toString(config.getOwnerId()))
                //.setEmojis(config.getSuccess(), config.getWarning(), config.getError())
                .setHelpWord("help")
                .setLinkedCacheSize(200)
                .setGuildSettingsManager(settings)
                .setListener(new CommandAudit());

        List<SlashCommand> slashCommandList = new ArrayList<>() {{
            add(aboutCommand);
            add(new HelpCmd(bot));
            add(new JoinCmd(bot));
            add(new ByeCmd(bot));
            add(new SettingsCmd(bot));
            add(new SetVoiceCmd(bot));
            add(new SetSpeedCmd(bot));
            add(new SetIntonationCmd(bot));
            add(new SetVoiceQualityA(bot));
            add(new SetVoiceQualityFm(bot));
            add(new AddWordCmd(bot));
            add(new WordListCmd(bot));
            add(new DlWordCmd(bot));
            add(new SettcCmd(bot));
            add(new SetReadNameCmd(bot));
            add(new JLReadCmd(bot));
            add(new GuildSettings(bot));
            add(new ShutdownCmd(bot));
        }};
        cb.addSlashCommands(slashCommandList.toArray(new SlashCommand[0]));
        cb.addCommands(slashCommandList.toArray(new SlashCommand[0]));

        boolean nogame = false;
        if (config.getStatus() != OnlineStatus.UNKNOWN)
            cb.setStatus(config.getStatus());
        if (config.getGame() == null)
            cb.setActivity(Activity.playing("/helpでヘルプを確認"));
        else if (config.getGame().getName().toLowerCase().matches("(none|なし)")) {
            cb.setActivity(null);
            nogame = true;
        } else
            cb.setActivity(config.getGame());

        if (!prompt.isNoGUI()) {
            try {
                GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            } catch (Exception e) {
                log.error("GUIを開くことができませんでした。次の要因が考えられます:\n"
                        + "サーバー上で実行している\n"
                        + "画面がない環境下で実行している\n"
                        + "このエラーを非表示にするには、 -Dnogui=true フラグを使用してGUIなしモードで実行してください。");
            }
        }

        log.info(config.getConfigLocation() + " から設定を読み込みました");

        try {
            JDA jda = JDABuilder.create(config.getToken(), Arrays.asList(INTENTS))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS, CacheFlag.STICKER)
                    .setActivity(nogame ? null : Activity.playing("準備中..."))
                    .setStatus(config.getStatus() == OnlineStatus.INVISIBLE || config.getStatus() == OnlineStatus.OFFLINE
                            ? OnlineStatus.INVISIBLE : OnlineStatus.DO_NOT_DISTURB)
                    .addEventListeners(cb.build(), waiter, new Listener(bot), new MessageListener(bot))
                    .setBulkDeleteSplittingEnabled(true)
                    .build();

            bot.setJDA(jda);
        } /*catch (LoginException ex) {
prompt.alert(Prompt.Level.ERROR, bot.GetLang().getString("appName"), ex + "\n" +
"正しい設定ファイルを編集していることを確認してください。Botトークンでのログインに失敗しました。" +
"正しいBotトークンを入力してください。(CLIENT SECRET ではありません!)\n" +
"設定ファイルの場所: " + config.getConfigLocation());
System.exit(1);
}*/ catch (IllegalArgumentException ex) {
            prompt.alert(Prompt.Level.ERROR, bot.GetLang().getString("appName"), "設定の一部が無効です:" + ex + "\n" +
                    "設定ファイルの場所: " + config.getConfigLocation());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));
    }
}