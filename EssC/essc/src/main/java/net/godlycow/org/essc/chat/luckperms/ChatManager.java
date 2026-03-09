package net.godlycow.org.essc.chat.luckperms;

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
        MiniMessage miniMessage = plugin.getMiniMessage();

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


        String processedMessage = message;
        boolean hasColor = player.hasPermission("essentialsc.chat.legacycodes");
        boolean hasRgb = player.hasPermission("essentialsc.chat.rbgcodes");

        if (hasColor && hasRgb) {
            processedMessage = colorize(translateHexColorCodes(processedMessage));
        } else if (hasColor) {
            processedMessage = colorize(processedMessage);
        } else if (hasRgb) {
            processedMessage = translateHexColorCodes(processedMessage);
        }

        Component messageComponent = legacySerializer.deserialize(processedMessage);


        Component formatted = formatWithLuckPerms(player, messageComponent);
        event.setCancelled(true);
        plugin.getServer().broadcast(formatted);
        plugin.getServer().getConsoleSender().sendMessage(formatted);
    }

    private Component formatWithLuckPerms(Player player, Component messageComponent) {
        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        String group = metaData.getPrimaryGroup();

        String formatString = plugin.getConfig().getString("group-formats." + group);
        if (formatString == null) {
            formatString = plugin.getConfig().getString("chat-format", "<prefix><name><suffix>: <message>");
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            formatString = PlaceholderAPI.setPlaceholders(player, formatString);
        }

        formatString = formatString
                .replace("<prefix>", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("<suffix>", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("<name>", player.getName())
                .replace("{name}", player.getName());


        return legacySerializer.deserialize(colorize(translateHexColorCodes(formatString)))
                .replaceText(builder -> builder.matchLiteral("<message>").replacement(messageComponent))
                .replaceText(builder -> builder.matchLiteral("{message}").replacement(messageComponent));
    }

    /*
    private Component formatDefault(Player player, Component messageComponent) {
        return miniMessage.deserialize("<yellow><name><gray>: ")
                .append(messageComponent);
    }
     */

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
        return matcher.appendTail(buffer).toString();
    }
}