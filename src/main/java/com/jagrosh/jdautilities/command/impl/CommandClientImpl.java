/*
 * Copyright 2018-2020 Cosgy Dev
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.jagrosh.jdautilities.command.impl;

import com.jagrosh.jdautilities.command.*;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.commons.utils.FixedSizeCache;
import com.jagrosh.jdautilities.commons.utils.SafeIdUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An implementation of {@link com.jagrosh.jdautilities.command.CommandClient CommandClient} to be used by a bot.
 *
 * <p>This is a listener usable with {@link net.dv8tion.jda.api.JDA JDA}, as it implements
 * {@link net.dv8tion.jda.api.hooks.EventListener EventListener} in order to catch and use different kinds of
 * {@link net.dv8tion.jda.api.events.Event Event}s. The primary usage of this is where the CommandClient implementation
 * takes {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent}s, and automatically
 * processes arguments, and provide them to a {@link com.jagrosh.jdautilities.command.Command Command} for
 * running and execution.
 *
 * @author John Grosh (jagrosh)
 */
public class CommandClientImpl implements CommandClient, EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(CommandClient.class);
    private static final int INDEX_LIMIT = 20;
    private static final String DEFAULT_PREFIX = "@mention";

    private final OffsetDateTime start;
    private final Activity game;
    private final OnlineStatus status;
    private final String ownerId;
    private final String[] coOwnerIds;
    private final String prefix;
    private final String altprefix;
    private final String serverInvite;
    private final HashMap<String, Integer> commandIndex;
    private final ArrayList<Command> commands;
    private final String success;
    private final String warning;
    private final String error;
    private final String botsKey, carbonKey;
    private final HashMap<String, OffsetDateTime> cooldowns;
    private final HashMap<String, Integer> uses;
    private final FixedSizeCache<Long, Set<Message>> linkMap;
    private final boolean useHelp;
    private final boolean shutdownAutomatically;
    private final Consumer<CommandEvent> helpConsumer;
    private final String helpWord;
    private final ScheduledExecutorService executor;
    private final AnnotatedModuleCompiler compiler;
    private final GuildSettingsManager manager;
    private final boolean helpToDm;

    private String textPrefix;
    private CommandListener listener = null;
    private int totalGuilds;

    /**
     *　変数が多いのでメモ書きします。
     * @param ownerId オーナーID
     * @param coOwnerIds 共同者オーナーID
     * @param prefix コマンドの接頭語
     * @param altprefix　サブの接頭語
     * @param game ゲームステータス情報
     * @param status オンライン情報
     * @param serverInvite サーバーへの招待リンク
     * @param success
     * @param warning
     * @param error
     * @param carbonKey carbonのキー
     * @param botsKey botsのキー
     * @param commands コマンド一覧
     * @param useHelp ヘルプを使うか否か
     * @param shutdownAutomatically 自動シャットダウン
     * @param helpConsumer ヘルプコマンドの内容
     * @param helpWord ヘルプコマンドに使うコマンド名
     * @param executor
     * @param linkedCacheSize
     * @param compiler
     * @param manager
     * @param helpToDm ヘルプコマンドをDMに送信するかしないかの設定
     */
    public CommandClientImpl(String ownerId, String[] coOwnerIds, String prefix, String altprefix, Activity game, OnlineStatus status, String serverInvite,
                             String success, String warning, String error, String carbonKey, String botsKey, ArrayList<Command> commands,
                             boolean useHelp, boolean shutdownAutomatically, Consumer<CommandEvent> helpConsumer, String helpWord, ScheduledExecutorService executor,
                             int linkedCacheSize, AnnotatedModuleCompiler compiler, GuildSettingsManager manager, boolean helpToDm) {
        Checks.check(ownerId != null, "所有者IDがnull、または設定されていません！ 所有者として登録するには、ユーザーIDを入力してください！");

        if (!SafeIdUtil.checkId(ownerId))
            LOG.warn(String.format("指定された所有者ID(%s)は安全ではないことが判明しました！ IDが非負の長さであることを確認してください！", ownerId));

        if (coOwnerIds != null) {
            for (String coOwnerId : coOwnerIds) {
                if (!SafeIdUtil.checkId(coOwnerId))
                    LOG.warn(String.format("提供された共同所有者ID(%s)は安全でないことが判明しました！ IDが非負の長さであることを確認してください！", coOwnerId));
            }
        }

        this.start = OffsetDateTime.now();

        this.ownerId = ownerId;
        this.coOwnerIds = coOwnerIds;
        this.prefix = prefix == null || prefix.isEmpty() ? DEFAULT_PREFIX : prefix;
        this.altprefix = altprefix == null || altprefix.isEmpty() ? null : altprefix;
        this.textPrefix = prefix;
        this.game = game;
        this.status = status;
        this.serverInvite = serverInvite;
        this.success = success == null ? "" : success;
        this.warning = warning == null ? "" : warning;
        this.error = error == null ? "" : error;
        this.carbonKey = carbonKey;
        this.botsKey = botsKey;
        this.commandIndex = new HashMap<>();
        this.commands = new ArrayList<>();
        this.cooldowns = new HashMap<>();
        this.uses = new HashMap<>();
        this.linkMap = linkedCacheSize > 0 ? new FixedSizeCache<>(linkedCacheSize) : null;
        this.useHelp = useHelp;
        this.shutdownAutomatically = shutdownAutomatically;
        this.helpWord = helpWord == null ? "help" : helpWord;
        this.executor = executor == null ? Executors.newSingleThreadScheduledExecutor() : executor;
        this.compiler = compiler;
        this.manager = manager;
        this.helpToDm = helpToDm;
        this.helpConsumer = helpConsumer == null ? (event) -> {
            StringBuilder builder = new StringBuilder("**" + event.getSelfUser().getName() + "** コマンド一覧:\n");
            Category category = null;
            for (Command command : commands) {
                if (!command.isHidden() && (!command.isOwnerCommand() || event.isOwner())) {
                    if (!Objects.equals(category, command.getCategory())) {
                        category = command.getCategory();
                        builder.append("\n\n  __").append(category == null ? "カテゴリーなし" : category.getName()).append("__:\n");
                    }
                    builder.append("\n`").append(textPrefix).append(prefix == null ? " " : "").append(command.getName())
                            .append(command.getArguments() == null ? "`" : " " + command.getArguments() + "`")
                            .append(" - ").append(command.getHelp());
                }
            }
            User owner = event.getJDA().getUserById(ownerId);
            if (owner != null) {
                //builder.append("\n\nほかのコマンドや使い方は **").append(owner.getName()).append("**#").append(owner.getDiscriminator() + " までお知らせ下さい");
                if (serverInvite != null)
                    builder.append(" 公式サーバーに参加することもできます: ").append(serverInvite);
            }
            if(this.helpToDm) {
                event.replyInDm(builder.toString(), unused ->
                {
                    if (event.isFromType(ChannelType.TEXT))
                        event.reactSuccess();
                }, t -> event.replyWarning("ヘルプを送信できませんでした: フレンド以外/サーバー内のメンバーからDMを受信しない設定になっていませんか？"));
            }else{
                event.reply(builder.toString());
            }
        } : helpConsumer;

        // Load commands
        for (Command command : commands) {
            addCommand(command);
        }
    }

    private static String[] splitOnPrefixLength(String rawContent, int length) {
        return Arrays.copyOf(rawContent.substring(length).trim().split("\\s+", 2), 2);
    }

    @Override
    public CommandListener getListener() {
        return listener;
    }

    @Override
    public void setListener(CommandListener listener) {
        this.listener = listener;
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public OffsetDateTime getStartTime() {
        return start;
    }

    @Override
    public OffsetDateTime getCooldown(String name) {
        return cooldowns.get(name);
    }

    @Override
    public int getRemainingCooldown(String name) {
        if (cooldowns.containsKey(name)) {
            int time = (int) OffsetDateTime.now().until(cooldowns.get(name), ChronoUnit.SECONDS);
            if (time <= 0) {
                cooldowns.remove(name);
                return 0;
            }
            return time;
        }
        return 0;
    }

    @Override
    public void applyCooldown(String name, int seconds) {
        cooldowns.put(name, OffsetDateTime.now().plusSeconds(seconds));
    }

    @Override
    public void cleanCooldowns() {
        OffsetDateTime now = OffsetDateTime.now();
        cooldowns.keySet().stream().filter((str) -> (cooldowns.get(str).isBefore(now)))
                .collect(Collectors.toList()).forEach(cooldowns::remove);
    }

    @Override
    public int getCommandUses(Command command) {
        return getCommandUses(command.getName());
    }

    @Override
    public int getCommandUses(String name) {
        return uses.getOrDefault(name, 0);
    }

    @Override
    public void addCommand(Command command) {
        addCommand(command, commands.size());
    }

    @Override
    public void addCommand(Command command, int index) {
        if (index > commands.size() || index < 0)
            throw new ArrayIndexOutOfBoundsException("指定されたインデックスが無効です: [" + index + "/" + commands.size() + "]");
        String name = command.getName();
        synchronized (commandIndex) {
            if (commandIndex.containsKey(name))
                throw new IllegalArgumentException("追加されたコマンドには、すでにインデックスが作成された名前またはエイリアスがあります。: \"" + name + "\"!");
            for (String alias : command.getAliases()) {
                if (commandIndex.containsKey(alias))
                    throw new IllegalArgumentException("追加されたコマンドには、すでにインデックスが作成された名前またはエイリアスがあります。: \"" + alias + "\"!");
                commandIndex.put(alias, index);
            }
            commandIndex.put(name, index);
            if (index < commands.size()) {
                commandIndex.keySet().stream().filter(key -> commandIndex.get(key) > index).collect(Collectors.toList())
                        .forEach(key -> commandIndex.put(key, commandIndex.get(key) + 1));
            }
        }
        commands.add(index, command);
    }

    @Override
    public void removeCommand(String name) {
        if (!commandIndex.containsKey(name))
            throw new IllegalArgumentException("指定された名前にはインデックスが付けられていません: \"" + name + "\"!");
        int targetIndex = commandIndex.remove(name);
        if (commandIndex.containsValue(targetIndex)) {
            commandIndex.keySet().stream().filter(key -> commandIndex.get(key) == targetIndex)
                    .collect(Collectors.toList()).forEach(commandIndex::remove);
        }
        commandIndex.keySet().stream().filter(key -> commandIndex.get(key) > targetIndex).collect(Collectors.toList())
                .forEach(key -> commandIndex.put(key, commandIndex.get(key) - 1));
        commands.remove(targetIndex);
    }

    @Override
    public void addAnnotatedModule(Object module) {
        compiler.compile(module).forEach(this::addCommand);
    }

    @Override
    public void addAnnotatedModule(Object module, Function<Command, Integer> mapFunction) {
        compiler.compile(module).forEach(command -> addCommand(command, mapFunction.apply(command)));
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public long getOwnerIdLong() {
        return Long.parseLong(ownerId);
    }

    @Override
    public String[] getCoOwnerIds() {
        return coOwnerIds;
    }

    @Override
    public long[] getCoOwnerIdsLong() {
        // Thought about using java.util.Arrays#setAll(T[], IntFunction<T>)
        // here, but as it turns out it's actually the same thing as this but
        // it throws an error if null. Go figure.
        if (coOwnerIds == null)
            return null;
        long[] ids = new long[coOwnerIds.length];
        for (int i = 0; i < ids.length; i++)
            ids[i] = Long.parseLong(coOwnerIds[i]);
        return ids;
    }

    @Override
    public String getSuccess() {
        return success;
    }

    @Override
    public String getWarning() {
        return warning;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public ScheduledExecutorService getScheduleExecutor() {
        return executor;
    }

    @Override
    public String getServerInvite() {
        return serverInvite;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getAltPrefix() {
        return altprefix;
    }

    @Override
    public String getTextualPrefix() {
        return textPrefix;
    }

    @Override
    public int getTotalGuilds() {
        return totalGuilds;
    }

    @Override
    public String getHelpWord() {
        return helpWord;
    }

    @Override
    public boolean usesLinkedDeletion() {
        return linkMap != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> S getSettingsFor(Guild guild) {
        if (manager == null)
            return null;
        return (S) manager.getSettings(guild);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends GuildSettingsManager> M getSettingsManager() {
        return (M) manager;
    }

    @Override
    public void shutdown() {
        GuildSettingsManager<?> manager = getSettingsManager();
        if (manager != null)
            manager.shutdown();
        executor.shutdown();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent)
            onMessageReceived((MessageReceivedEvent) event);

        else if (event instanceof GuildMessageDeleteEvent && usesLinkedDeletion())
            onMessageDelete((GuildMessageDeleteEvent) event);

        else if (event instanceof GuildJoinEvent) {
            if (((GuildJoinEvent) event).getGuild().getSelfMember().getTimeJoined()
                    .plusMinutes(10).isAfter(OffsetDateTime.now()))
                sendStats(event.getJDA());
        } else if (event instanceof GuildLeaveEvent)
            sendStats(event.getJDA());
        else if (event instanceof ReadyEvent)
            onReady((ReadyEvent) event);
        else if (event instanceof ShutdownEvent) {
            if (shutdownAutomatically)
                shutdown();
        }
    }

    private void onReady(ReadyEvent event) {
        if (!event.getJDA().getSelfUser().isBot()) {
            LOG.error("JDA-Utilitiesはクライアントアカウントをサポートしていません。");
            event.getJDA().shutdown();
            return;
        }
        textPrefix = prefix.equals(DEFAULT_PREFIX) ? "@" + event.getJDA().getSelfUser().getName() + " " : prefix;
        event.getJDA().getPresence().setPresence(status == null ? OnlineStatus.ONLINE : status,
                game == null ? null : "default".equals(game.getName()) ? Activity.playing(textPrefix + helpWord + "でヘルプを確認") : game);

        // Start SettingsManager if necessary
        GuildSettingsManager<?> manager = getSettingsManager();
        if (manager != null)
            manager.init();

        sendStats(event.getJDA());
    }

    private void onMessageReceived(MessageReceivedEvent event) {
        // Return if it's a bot
        if (event.getAuthor().isBot())
            return;

        String[] parts = null;
        String rawContent = event.getMessage().getContentRaw();

        GuildSettingsProvider settings = event.isFromType(ChannelType.TEXT) ? provideSettings(event.getGuild()) : null;

        // Check for prefix or alternate prefix (@mention cases)
        if (prefix.equals(DEFAULT_PREFIX) || (altprefix != null && altprefix.equals(DEFAULT_PREFIX))) {
            if (rawContent.startsWith("<@" + event.getJDA().getSelfUser().getId() + ">") ||
                    rawContent.startsWith("<@!" + event.getJDA().getSelfUser().getId() + ">")) {
                parts = splitOnPrefixLength(rawContent, rawContent.indexOf(">") + 1);
            }
        }
        // Check for prefix
        if (parts == null && rawContent.toLowerCase().startsWith(prefix.toLowerCase()))
            parts = splitOnPrefixLength(rawContent, prefix.length());
        // Check for alternate prefix
        if (parts == null && altprefix != null && rawContent.toLowerCase().startsWith(altprefix.toLowerCase()))
            parts = splitOnPrefixLength(rawContent, altprefix.length());
        // Check for guild specific prefixes
        if (parts == null && settings != null) {
            Collection<String> prefixes = settings.getPrefixes();
            if (prefixes != null) {
                for (String prefix : prefixes) {
                    if (parts == null && rawContent.toLowerCase().startsWith(prefix.toLowerCase()))
                        parts = splitOnPrefixLength(rawContent, prefix.length());
                }
            }
        }

        if (parts != null) //starts with valid prefix
        {
            if (useHelp && parts[0].equalsIgnoreCase(helpWord)) {
                CommandEvent cevent = new CommandEvent(event, parts[1] == null ? "" : parts[1], this);
                if (listener != null)
                    listener.onCommand(cevent, null);
                helpConsumer.accept(cevent); // Fire help consumer
                if (listener != null)
                    listener.onCompletedCommand(cevent, null);
                return; // Help Consumer is done
            } else if (event.isFromType(ChannelType.PRIVATE) || event.getTextChannel().canTalk()) {
                String name = parts[0];
                String args = parts[1] == null ? "" : parts[1];
                final Command command; // this will be null if it's not a command
                if (commands.size() < INDEX_LIMIT + 1)
                    command = commands.stream().filter(cmd -> cmd.isCommandFor(name)).findAny().orElse(null);
                else {
                    synchronized (commandIndex) {
                        int i = commandIndex.getOrDefault(name.toLowerCase(), -1);
                        command = i != -1 ? commands.get(i) : null;
                    }
                }

                if (command != null) {
                    CommandEvent cevent = new CommandEvent(event, args, this);

                    if (listener != null)
                        listener.onCommand(cevent, command);
                    uses.put(command.getName(), uses.getOrDefault(command.getName(), 0) + 1);
                    command.run(cevent);
                    return; // Command is done
                }
            }
        }

        if (listener != null)
            listener.onNonCommandMessage(event);
    }

    private void sendStats(JDA jda) {
        OkHttpClient client = jda.getHttpClient();

        if (carbonKey != null) {
            FormBody.Builder bodyBuilder = new FormBody.Builder()
                    .add("key", carbonKey)
                    .add("servercount", Integer.toString(jda.getGuilds().size()));

            jda.getShardInfo();
            bodyBuilder.add("shard_id", Integer.toString(jda.getShardInfo().getShardId()))
                    .add("shard_count", Integer.toString(jda.getShardInfo().getShardTotal()));

            Request.Builder builder = new Request.Builder()
                    .post(bodyBuilder.build())
                    .url("https://www.carbonitex.net/discord/data/botdata.php");

            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    LOG.info("carbonitex.netに情報を送信しました");
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    LOG.error("carbonitex.netへの情報の送信に失敗しました ", e);
                }
            });
        }

        if (botsKey != null) {
            JSONObject body = new JSONObject().put("guildCount", jda.getGuilds().size());
            jda.getShardInfo();
            body.put("shardId", jda.getShardInfo().getShardId())
                    .put("shardCount", jda.getShardInfo().getShardTotal());

            Request.Builder builder = new Request.Builder()
                    .post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
                    .url("https://discord.bots.gg/api/v1/bots/" + jda.getSelfUser().getId() + "/stats")
                    .header("Authorization", botsKey)
                    .header("Content-Type", "application/json");

            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        LOG.info("discord.bots.ggに情報を送信しました");
                        try (Reader reader = response.body().charStream()) {
                            totalGuilds = new JSONObject(new JSONTokener(reader)).getInt("guildCount");
                        } catch (Exception ex) {
                            LOG.error("discord.bots.ggからボットシャード情報を取得できませんでした ", ex);
                        }
                    } else
                        LOG.error("discord.bots.ggに情報を送信できませんでした: " + response.body().string());
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    LOG.error("discord.bots.ggに情報を送信できませんでした ", e);
                }
            });
        } else if (jda.getShardManager() != null) {
            totalGuilds = (int) jda.getShardManager().getGuildCache().size();
        } else {
            totalGuilds = (int) jda.getGuildCache().size();
        }
    }

    private void onMessageDelete(GuildMessageDeleteEvent event) {
        // We don't need to cover whether or not this client usesLinkedDeletion() because
        // that is checked in onEvent(Event) before this is even called.
        synchronized (linkMap) {
            if (linkMap.contains(event.getMessageIdLong())) {
                Set<Message> messages = linkMap.get(event.getMessageIdLong());
                if (messages.size() > 1 && event.getGuild().getSelfMember()
                        .hasPermission(event.getChannel(), Permission.MESSAGE_MANAGE))
                    event.getChannel().deleteMessages(messages).queue(unused -> {
                    }, ignored -> {
                    });
                else if (messages.size() > 0)
                    messages.forEach(m -> m.delete().queue(unused -> {
                    }, ignored -> {
                    }));
            }
        }
    }

    private GuildSettingsProvider provideSettings(Guild guild) {
        Object settings = getSettingsFor(guild);
        if (settings instanceof GuildSettingsProvider)
            return (GuildSettingsProvider) settings;
        else
            return null;
    }

    /**
     * <b>DO NOT USE THIS!</b>
     *
     * <p>This is a method necessary for linking a bot's response messages
     * to their corresponding call message ID.
     * <br><b>Using this anywhere in your code can and will break your bot.</b>
     *
     * @param callId  The ID of the call Message
     * @param message The Message to link to the ID
     */
    public void linkIds(long callId, Message message) {
        // We don't use linked deletion, so we don't do anything.
        if (!usesLinkedDeletion())
            return;

        synchronized (linkMap) {
            Set<Message> stored = linkMap.get(callId);
            if (stored != null)
                stored.add(message);
            else {
                stored = new HashSet<>();
                stored.add(message);
                linkMap.add(callId, stored);
            }
        }
    }
}
