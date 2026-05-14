package net.godlycow.org.essc.modules.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.plugin.config.EssConfig;
import net.godlycow.org.essc.util.LegacyColorConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFormatter {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");

    private final EssentialsC plugin;
    private final EssConfig config;
    private final MiniMessage miniMessage;

    private LuckPerms luckPerms;
    private boolean luckPermsEnabled;

    public ChatFormatter(EssentialsC plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.miniMessage = plugin.getMiniMessage();
    }

    public void reload(LuckPerms luckPerms, boolean luckPermsEnabled) {
        this.luckPerms = luckPerms;
        this.luckPermsEnabled = luckPermsEnabled;
    }

    public Component formatMessage(Player player, String raw) {
        raw = applyFormatPermissions(player, raw);
        Component message = buildComponent(player, raw);
        message = processLinks(player, message, raw);
        return message;
    }

    private static final Pattern BOLD_PATTERN =
            Pattern.compile("(?i)[&§]l|</?(?:bold|b)>");
    private static final Pattern ITALIC_PATTERN =
            Pattern.compile("(?i)[&§]o|</?(?:italic|i|em)>");
    private static final Pattern UNDERLINE_PATTERN =
            Pattern.compile("(?i)[&§]n|</?(?:underlined|underline|u)>");
    private static final Pattern STRIKETHROUGH_PATTERN =
            Pattern.compile("(?i)[&§]m|</?(?:strikethrough|st)>");
    private static final Pattern OBFUSCATED_PATTERN =
            Pattern.compile("(?i)[&§]k|</?(?:obfuscated|obf)>");

    private String applyFormatPermissions(Player player, String input) {
        if (!player.hasPermission("essentialsc.chat.color")) {
            return stripAllColors(input);
        }

        if (!player.hasPermission("essentialsc.chat.format.bold")) {
            input = BOLD_PATTERN.matcher(input).replaceAll("");
        }
        if (!player.hasPermission("essentialsc.chat.format.italic")) {
            input = ITALIC_PATTERN.matcher(input).replaceAll("");
        }
        if (!player.hasPermission("essentialsc.chat.format.underline")) {
            input = UNDERLINE_PATTERN.matcher(input).replaceAll("");
        }
        if (!player.hasPermission("essentialsc.chat.format.strikethrough")) {
            input = STRIKETHROUGH_PATTERN.matcher(input).replaceAll("");
        }
        if (!player.hasPermission("essentialsc.chat.format.obfuscated")) {
            input = OBFUSCATED_PATTERN.matcher(input).replaceAll("");
        }

        return input;
    }

    private String stripAllColors(String input) {
        input = input.replaceAll("(?i)[&§][0-9a-fk-or]", "");
        input = input.replaceAll("&#[A-Fa-f0-9]{6}", "");
        input = input.replaceAll("<[^>]+>", "");
        return input;
    }

    private Component processLinks(Player player, Component message, String raw) {
        Matcher m = URL_PATTERN.matcher(raw);
        if (!m.find()) return message;

        java.util.List<String> whitelist = config.getChatLinkWhitelist();
        Component removalComponent = miniMessage.deserialize(config.getChatLinkRemovalMessage());

        m.reset();
        while (m.find()) {
            String url = m.group();
            if (player.hasPermission("essentialsc.chat.links")) {
                Component linked = Component.text(url)
                        .clickEvent(ClickEvent.openUrl(url))
                        .color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                        .decorate(net.kyori.adventure.text.format.TextDecoration.UNDERLINED);
                message = message.replaceText(b -> b.matchLiteral(url).replacement(linked));
            } else {
                boolean whitelisted = whitelist.stream().anyMatch(url::contains);
                if (!whitelisted) {
                    message = message.replaceText(b -> b.matchLiteral(url).replacement(removalComponent));
                }
            }
        }

        return message;
    }

    private Component buildComponent(Player player, String raw) {
        boolean canMiniMessage = player.hasPermission("essentialsc.chat.minimessage");
        boolean canRgb = player.hasPermission("essentialsc.chat.rgbcodes");
        boolean canLegacy = player.hasPermission("essentialsc.chat.legacycodes");

        if (!player.hasPermission("essentialsc.chat.color")) {
            return Component.text(raw);
        }

        if (canMiniMessage) {
            if (canRgb || canLegacy) {
                return miniMessage.deserialize(LegacyColorConverter.toMiniMessage(raw));
            }
            return miniMessage.deserialize(raw);
        }

        if (canRgb && canLegacy) {
            return miniMessage.deserialize(LegacyColorConverter.toMiniMessage(raw));
        }

        if (canRgb) {
            return miniMessage.deserialize(
                    LegacyColorConverter.convertHexAmpersand(
                            LegacyColorConverter.convertHexBukkit(raw)));
        }

        if (canLegacy) {
            return miniMessage.deserialize(LegacyColorConverter.convertAmpersand(raw));
        }

        return Component.text(raw);
    }

    public boolean isLuckPermsEnabled() {
        return luckPermsEnabled;
    }

    public Component buildChatLine(Player player, Component message) {
        CachedMetaData meta = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);

        String format = plugin.getConfig().getString(
                "luckperms.group-formats." + meta.getPrimaryGroup());
        if (format == null) {
            format = plugin.getConfig().getString(
                    "luckperms.chat-format", "<DISPLAYNAME> <gray>»</gray> <white><MESSAGE></white>");
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        String prefix = meta.getPrefix() != null ? meta.getPrefix() : "";
        String suffix = meta.getSuffix() != null ? meta.getSuffix() : "";

        format = format
                .replace("<PREFIX>", miniMessage.escapeTags(LegacyColorConverter.toMiniMessage(prefix)))
                .replace("<SUFFIX>", miniMessage.escapeTags(LegacyColorConverter.toMiniMessage(suffix)))
                .replace("<USERNAME>", miniMessage.escapeTags(player.getName()))
                .replace("<GROUP>", miniMessage.escapeTags(meta.getPrimaryGroup()));

        Component displayName = buildDisplayName(player, meta);

        return miniMessage.deserialize(LegacyColorConverter.toMiniMessage(format))
                .replaceText(b -> b.matchLiteral("<DISPLAYNAME>").replacement(displayName))
                .replaceText(b -> b.matchLiteral("<MESSAGE>").replacement(message));
    }

    private Component buildDisplayName(Player player, CachedMetaData meta) {
        String prefix = meta.getPrefix() != null ? meta.getPrefix() : "";
        String suffix = meta.getSuffix() != null ? meta.getSuffix() : "";

        String cachedNick = plugin.getNickManager() != null
                ? plugin.getNickManager().getCachedNickname(player.getUniqueId())
                : null;

        Component nameComponent;
        if (cachedNick != null && !cachedNick.isEmpty()) {
            Component nickComponent = miniMessage.deserialize(cachedNick);
            String indicator = config.getNickIndicator();
            if (!indicator.isEmpty()) {
                nickComponent = Component.text(indicator).append(nickComponent);
            }
            nameComponent = nickComponent;
        } else {
            nameComponent = Component.text(player.getName());
        }

        Component prefixComponent = prefix.isEmpty() ? Component.empty()
                : miniMessage.deserialize(LegacyColorConverter.toMiniMessage(prefix));
        Component suffixComponent = suffix.isEmpty() ? Component.empty()
                : miniMessage.deserialize(LegacyColorConverter.toMiniMessage(suffix));

        Component displayName = prefixComponent.append(nameComponent).append(suffixComponent);

        if (config.isNickShowRealnameOnHover() && cachedNick != null && !cachedNick.isEmpty()) {
            String plainNick = PlainTextComponentSerializer.plainText().serialize(
                    miniMessage.deserialize(cachedNick));
            String hoverText = config.getNickHoverFormat()
                    .replace("<realname>", miniMessage.escapeTags(player.getName()))
                    .replace("<nick>", miniMessage.escapeTags(plainNick))
                    .replace("<prefix>", miniMessage.escapeTags(prefix))
                    .replace("<suffix>", miniMessage.escapeTags(suffix));
            displayName = displayName.hoverEvent(
                    HoverEvent.showText(miniMessage.deserialize(hoverText)));
        }

        if (config.isNickClickSuggestMsg()) {
            displayName = displayName.clickEvent(
                    ClickEvent.suggestCommand("/msg " + player.getName() + " "));
        }

        return displayName;
    }
}