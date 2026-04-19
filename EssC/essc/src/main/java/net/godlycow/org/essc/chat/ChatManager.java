package net.godlycow.org.essc.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.config.ConfigManager;
import net.godlycow.org.essc.util.LegacyColorConverter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.chat.ChatRenderer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager implements Listener {

    private final EssentialsC plugin;
    private final ConfigManager configManager;

    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character(ChatColor.COLOR_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private LuckPerms luckPerms;
    private boolean luckPermsEnabled;
    private boolean useLuckPermsFormatting;

    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();

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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (configManager.isChatSlowModeEnabled() && !player.hasPermission("essentialsc.chat.slowmode.bypass")) {
            long lastMsg = lastMessageTime.getOrDefault(player.getUniqueId(), 0L);
            long delayMs = configManager.getChatSlowModeDelay() * 1000L;
            long remaining = (lastMsg + delayMs) - System.currentTimeMillis();

            if (remaining > 0) {
                player.sendMessage(plugin.getLanguageManager().get(player, "chat.slowmode.wait",
                        Map.of("seconds", String.valueOf((remaining / 1000) + 1))));
                event.setCancelled(true);
                return;
            }
        }

        double capsThreshold = configManager.getChatCapslockThreshold();
        if (capsThreshold > 0 && capsThreshold <= 1.0 && !player.hasPermission("essentialsc.chat.caps.bypass")) {
            String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
            if (plain.length() >= 3) {
                int upperCount = 0;
                int letterCount = 0;
                for (char c : plain.toCharArray()) {
                    if (Character.isLetter(c)) {
                        letterCount++;
                        if (Character.isUpperCase(c)) upperCount++;
                    }
                }
                if (letterCount > 0) {
                    double ratio = (double) upperCount / letterCount;
                    if (ratio >= capsThreshold) {
                        event.message(Component.text(plain.toLowerCase()));
                    }
                }
            }
        }

        if (configManager.isChatMentionEnabled()) {
            Component message = applyMessageColorsToComponent(player, event.message());
            String plain = PlainTextComponentSerializer.plainText().serialize(message);

            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (online.equals(player)) continue;

                String name = online.getName();
                String nick = plugin.getNickManager() != null
                        ? plugin.getNickManager().getCachedNickname(online.getUniqueId())
                        : null;

                String cleanNick;
                if (nick != null && !nick.isEmpty()) {
                    cleanNick = PlainTextComponentSerializer.plainText().serialize(
                            plugin.getMiniMessage().deserialize(nick)
                    );
                } else {
                    cleanNick = null;
                }

                if (plain.contains(name)) {
                    online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                    String format = configManager.getChatMentionFormat().replace("<player>", name);
                    Component colored = plugin.getMiniMessage().deserialize(format);
                    message = message.replaceText(b -> b.matchLiteral(name).replacement(colored));
                    plain = PlainTextComponentSerializer.plainText().serialize(message);
                } else if (cleanNick != null && plain.contains(cleanNick)) {
                    online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                    String format = configManager.getChatMentionFormat().replace("<player>", cleanNick);
                    Component colored = plugin.getMiniMessage().deserialize(format);
                    message = message.replaceText(b -> b.matchLiteral(cleanNick).replacement(colored));
                    plain = PlainTextComponentSerializer.plainText().serialize(message);
                }
            }
            event.message(message);
        } else {
            event.message(applyMessageColorsToComponent(player, event.message()));
        }

        if (!luckPermsEnabled || !useLuckPermsFormatting) {
            lastMessageTime.put(player.getUniqueId(), System.currentTimeMillis());
            return;
        }

        event.renderer((source, sourceDisplayName, msg, viewer) ->
                formatWithLuckPerms(source, msg)
        );

        lastMessageTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private Component formatWithLuckPerms(Player player, Component messageComponent) {
        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        String primaryGroup = metaData.getPrimaryGroup();

        String format = plugin.getConfig().getString("luckperms.group-formats." + primaryGroup);
        if (format == null) {
            format = plugin.getConfig().getString("luckperms.chat-format", "<DISPLAYNAME> &7» &f<MESSAGE>");
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

            String nickForParsing = LegacyColorConverter.convertHexAmpersandToLegacy(
                    applyLegacyColors(cachedNick));
            Component nickComponent = plugin.getMiniMessage().deserialize(nickForParsing);
            if (!indicator.isEmpty()) {
                nickComponent = Component.text(indicator).append(nickComponent);
            }

            Component prefixComponent = prefix.isEmpty() ? Component.empty()
                    : legacySerializer.deserialize(applyLegacyColors(LegacyColorConverter.convertHexAmpersandToLegacy(prefix)));

            Component suffixComponent = suffix.isEmpty() ? Component.empty()
                    : legacySerializer.deserialize(applyLegacyColors(LegacyColorConverter.convertHexAmpersandToLegacy(suffix)));

            Component baseDisplayName = prefixComponent.append(nickComponent).append(suffixComponent);

            net.kyori.adventure.text.event.HoverEvent<?> hoverEvent = null;
            net.kyori.adventure.text.event.ClickEvent clickEvent = null;

            if (plugin.getConfigManager().isNickShowRealnameOnHover()) {
                String hoverFormat = plugin.getConfigManager().getNickHoverFormat();
                String hoverText = hoverFormat.replace("<realname>", player.getName())
                        .replace("<nick>", cachedNick)
                        .replace("<prefix>", prefix)
                        .replace("<suffix>", suffix);
                Component hoverComponent = plugin.getMiniMessage().deserialize(hoverText);
                hoverEvent = net.kyori.adventure.text.event.HoverEvent.showText(hoverComponent);
            }

            if (plugin.getConfigManager().isNickClickSuggestMsg()) {
                clickEvent = net.kyori.adventure.text.event.ClickEvent.suggestCommand("/msg " + player.getName() + " ");
            }

            if (hoverEvent != null || clickEvent != null) {
                net.kyori.adventure.text.TextComponent textComponent = (net.kyori.adventure.text.TextComponent) baseDisplayName;
                net.kyori.adventure.text.TextComponent.Builder builder = textComponent.toBuilder();

                if (hoverEvent != null) {
                    builder.hoverEvent(hoverEvent);
                }
                if (clickEvent != null) {
                    builder.clickEvent(clickEvent);
                }
                displayNameComponent = builder.build();
            } else {
                displayNameComponent = baseDisplayName;
            }
        } else {
            String nameFormat = LegacyColorConverter.convertHexAmpersandToLegacy(prefix)
                    + player.getName()
                    + LegacyColorConverter.convertHexAmpersandToLegacy(suffix);

            displayNameComponent = legacySerializer.deserialize(applyLegacyColors(nameFormat));
        }

        format = format
                .replace("<PREFIX>", prefix)
                .replace("<SUFFIX>", suffix)
                .replace("<USERNAME>", player.getName())
                .replace("<GROUP>", primaryGroup);

        format = LegacyColorConverter.convertHexAmpersandToLegacy(format);

        return legacySerializer.deserialize(applyLegacyColors(format))
                .replaceText(b -> b.matchLiteral("<DISPLAYNAME>").replacement(displayNameComponent))
                .replaceText(b -> b.matchLiteral("<MESSAGE>").replacement(messageComponent));
    }

    private Component applyMessageColorsToComponent(Player player, Component message) {
        String plain = PlainTextComponentSerializer.plainText().serialize(message);

        boolean legacy = player.hasPermission("essentialsc.chat.legacycodes");
        boolean rgb = player.hasPermission("essentialsc.chat.rgbcodes");

        if (rgb) {
            String converted = LegacyColorConverter.convertHexAmpersandToLegacy(plain);
            return legacySerializer.deserialize(applyLegacyColors(converted));
        }

        if (legacy) {
            return legacySerializer.deserialize(applyLegacyColors(plain));
        }

        return Component.text(plain);
    }


    private String applyLegacyColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean isLuckPermsChatEnabled() {
        return useLuckPermsFormatting;
    }

    public boolean isLuckPermsAvailable() {
        return luckPermsEnabled;
    }

    public Component formatMessage(Player player, String message) {
        Component messageComponent = applyMessageColorsToComponent(player, Component.text(message));
        if (!luckPermsEnabled || !useLuckPermsFormatting) return messageComponent;
        return formatWithLuckPerms(player, messageComponent);
    }
}