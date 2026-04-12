package net.godlycow.org.essc.chat.luckperms;

import github.scarsz.discordsrv.DiscordSRV;
import me.clip.placeholderapi.PlaceholderAPI;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.config.ConfigManager;
import net.godlycow.org.essc.util.LegacyColorConverter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.chat.ChatRenderer;

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
        if (!luckPermsEnabled || !useLuckPermsFormatting) return;

        Player player = event.getPlayer();

        event.renderer(new ChatRenderer() {
            @Override
            public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
                Component coloredMessage = applyMessageColorsToComponent(source, message);
                return formatWithLuckPerms(source, coloredMessage);
            }
        });

        if (plugin.getConfigManager().isDiscordSRVEnabled()
                && plugin.getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
            try {
                String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

                String cachedNick = plugin.getNickManager() != null
                        ? plugin.getNickManager().getCachedNickname(player.getUniqueId())
                        : null;

                Component originalDisplayName = player.displayName();

                if (cachedNick != null && !cachedNick.isEmpty()) {
                    String plain = PlainTextComponentSerializer.plainText()
                            .serialize(plugin.getMiniMessage().deserialize(cachedNick));
                    player.displayName(Component.text(plain + "(" + player.getName() + ")"));
                }

                String channel = DiscordSRV.getPlugin().getOptionalChannel(player.getWorld().getName());
                DiscordSRV.getPlugin().processChatMessage(player, plainMessage, channel, false);

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
            String nickLegacy = LegacyColorConverter.convertHexAmpersandToLegacy(cachedNick);
            nickLegacy = applyLegacyColors(nickLegacy);

            Component nickComponent = indicator.isEmpty()
                    ? legacySerializer.deserialize(nickLegacy)
                    : Component.text(indicator).append(legacySerializer.deserialize(nickLegacy));

            Component prefixComponent = prefix.isEmpty() ? Component.empty()
                    : legacySerializer.deserialize(applyLegacyColors(LegacyColorConverter.convertHexAmpersandToLegacy(prefix)));

            Component suffixComponent = suffix.isEmpty() ? Component.empty()
                    : legacySerializer.deserialize(applyLegacyColors(LegacyColorConverter.convertHexAmpersandToLegacy(suffix)));

            displayNameComponent = prefixComponent.append(nickComponent).append(suffixComponent);
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
        boolean rgb = player.hasPermission("essentialsc.chat.rbgcodes");

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

    public boolean isLuckPermsChatEnabled() { return useLuckPermsFormatting; }
    public boolean isLuckPermsAvailable() { return luckPermsEnabled; }
    public boolean canUseColorCodes(Player p) { return p.hasPermission("essentialsc.chat.legacycodes"); }
    public boolean canUseRgbCodes(Player p) { return p.hasPermission("essentialsc.chat.rbgcodes"); }

    public Component formatMessage(Player player, String message) {
        Component messageComponent = applyMessageColorsToComponent(player, Component.text(message));
        if (!luckPermsEnabled || !useLuckPermsFormatting) return messageComponent;
        return formatWithLuckPerms(player, messageComponent);
    }
}
