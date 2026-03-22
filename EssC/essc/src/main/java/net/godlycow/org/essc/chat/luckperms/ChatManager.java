package net.godlycow.org.essc.chat.luckperms;

import github.scarsz.discordsrv.DiscordSRV;
import me.clip.placeholderapi.PlaceholderAPI;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager implements Listener {

    private final EssentialsC plugin;
    private final ConfigManager configManager;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_MM = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character(ChatColor.COLOR_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private LuckPerms luckPerms;
    private boolean luckPermsEnabled;
    private boolean useLuckPermsFormatting;

    public ChatManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();

        reload();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void reload() {
        this.useLuckPermsFormatting = configManager.isLuckPermsChatEnabled();
        if (useLuckPermsFormatting && plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            var provider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                this.luckPerms = provider.getProvider();
                this.luckPermsEnabled = true;
            } else {
                try {
                    this.luckPerms = LuckPermsProvider.get();
                    this.luckPermsEnabled = true;
                } catch (IllegalStateException e) {
                    this.luckPermsEnabled = false;
                    plugin.getLogger().warning("LuckPerms API not loaded yet!");
                }
            }
        } else {
            this.luckPermsEnabled = false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!luckPermsEnabled || !useLuckPermsFormatting) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (player.hasPermission("essentialsc.chat.legacycodes") && player.hasPermission("essentialsc.chat.rbgcodes")) {
            message = colorize(translateHexColorCodes(message));
        } else if (player.hasPermission("essentialsc.chat.legacycodes")) {
            message = colorize(message);
        } else if (player.hasPermission("essentialsc.chat.rbgcodes")) {
            message = translateHexColorCodes(message);
        }

        Component messageComponent = legacySerializer.deserialize(message);
        Component formatted = formatWithLuckPerms(player, messageComponent);

        event.setCancelled(true);
        plugin.getServer().broadcast(formatted);

        if (plugin.getConfigManager().isDiscordSRVEnabled()
                && plugin.getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
            try {
                String cachedNick = plugin.getNickManager() != null
                        ? plugin.getNickManager().getCachedNickname(player.getUniqueId())
                        : null;
                Component originalDisplayName = player.displayName();
                if (cachedNick != null && !cachedNick.isEmpty()) {
                    String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                            .serialize(plugin.getMiniMessage().deserialize(cachedNick));
                    player.displayName(Component.text(plain + "(" + player.getName() + ")"));
                }
                String channel = DiscordSRV.getPlugin().getOptionalChannel(player.getWorld().getName());
                DiscordSRV.getPlugin().processChatMessage(player, event.getMessage(), channel, false);
                player.displayName(originalDisplayName);
            } catch (Exception e) {
                plugin.debug("Failed to relay chat message to DiscordSRV: " + e.getMessage());
            }
        }
    }

    private Component formatWithLuckPerms(Player player, Component messageComponent) {
        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        String primaryGroup = metaData.getPrimaryGroup();

        String format = plugin.getConfig().getString("luckperms.group-formats." + primaryGroup);
        if (format == null) {
            format = plugin.getConfig().getString("luckperms.chat-format", "<DISPLAYNAME> &7\u00bb &f<MESSAGE>");
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        String prefix = metaData.getPrefix() != null ? metaData.getPrefix() : "";
        String suffix = metaData.getSuffix() != null ? metaData.getSuffix() : "";

        Component displayNameComponent;
        String cachedNick = plugin.getNickManager() != null
                ? plugin.getNickManager().getCachedNickname(player.getUniqueId())
                : null;

        if (cachedNick != null && !cachedNick.isEmpty()) {
            String indicator = plugin.getConfigManager().getNickIndicator();
            String nickLegacy = colorize(translateHexColorCodes(
                    legacySerializer.serialize(plugin.getMiniMessage().deserialize(cachedNick))
            ));
            Component nickComponent = indicator.isEmpty()
                    ? legacySerializer.deserialize(nickLegacy)
                    : Component.text(indicator).append(legacySerializer.deserialize(nickLegacy));
            Component prefixComponent = prefix.isEmpty() ? Component.empty() : legacySerializer.deserialize(colorize(translateHexColorCodes(prefix)));
            Component suffixComponent = suffix.isEmpty() ? Component.empty() : legacySerializer.deserialize(colorize(translateHexColorCodes(suffix)));
            displayNameComponent = prefixComponent.append(nickComponent).append(suffixComponent);
        } else {
            String rawDisplayName = colorize(translateHexColorCodes(prefix + player.getName() + suffix));
            displayNameComponent = legacySerializer.deserialize(rawDisplayName);
        }

        format = format
                .replace("<PREFIX>", prefix)
                .replace("<SUFFIX>", suffix)
                .replace("<USERNAME>", player.getName())
                .replace("<GROUP>", primaryGroup);

        String colorized = colorize(translateHexColorCodes(format));

        return legacySerializer.deserialize(colorized)
                .replaceText(b -> b.matchLiteral("<DISPLAYNAME>").replacement(displayNameComponent))
                .replaceText(b -> b.matchLiteral("<MESSAGE>").replacement(messageComponent));
    }

    public boolean isLuckPermsChatEnabled() {
        return useLuckPermsFormatting;
    }

    public boolean isLuckPermsAvailable() {
        return luckPermsEnabled;
    }

    public boolean canUseColorCodes(Player player) {
        return player.hasPermission("essentialsc.chat.legacycodes");
    }

    public boolean canUseRgbCodes(Player player) {
        return player.hasPermission("essentialsc.chat.rbgcodes");
    }

    public Component formatMessage(Player player, String message) {
        if (player.hasPermission("essentialsc.chat.legacycodes") && player.hasPermission("essentialsc.chat.rbgcodes")) {
            message = colorize(translateHexColorCodes(message));
        } else if (player.hasPermission("essentialsc.chat.legacycodes")) {
            message = colorize(message);
        } else if (player.hasPermission("essentialsc.chat.rbgcodes")) {
            message = translateHexColorCodes(message);
        }

        Component messageComponent = legacySerializer.deserialize(message);

        if (!luckPermsEnabled || !useLuckPermsFormatting) {
            return messageComponent;
        }

        return formatWithLuckPerms(player, messageComponent);
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String translateHexColorCodes(String message) {
        final char colorChar = ChatColor.COLOR_CHAR;

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 32);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }
        String result = matcher.appendTail(buffer).toString();

        Matcher mmMatcher = HEX_PATTERN_MM.matcher(result);
        StringBuffer mmBuffer = new StringBuffer(result.length() + 32);
        while (mmMatcher.find()) {
            String group = mmMatcher.group(1);
            mmMatcher.appendReplacement(mmBuffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }
        return mmMatcher.appendTail(mmBuffer).toString();
    }
}