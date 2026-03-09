package net.godlycow.org.essc.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardConfig {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final boolean enabled;
    private final int updateInterval;
    private final boolean persistent;
    private final Component title;
    private final String titleRaw;
    private List<String> lines;

    public ScoreboardConfig(ConfigurationSection config) {
        this.enabled = config.getBoolean("enabled", true);
        this.updateInterval = Math.max(1, config.getInt("update-interval", 20));
        this.persistent = config.getBoolean("persistent", true);

        this.titleRaw = translateColorCodes(config.getString("title", "<gold><bold>MyServer</bold></gold>"));
        this.title = MINI_MESSAGE.deserialize(titleRaw);

        this.lines = config.getStringList("lines").stream()
                .map(this::translateColorCodes)
                .collect(Collectors.toList());
        if (this.lines.isEmpty()) {
            this.lines = List.of(
                    "",
                    "",
                    "",
                    ""
            );
        }
    }

    private String translateColorCodes(String text) {
        if (text == null || text.isEmpty()) return text;

        String result = text;

        result = result.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>");

        result = result.replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");

        return result;
    }

    public boolean isEnabled() { return enabled; }
    public int getUpdateInterval() { return updateInterval; }
    public boolean isPersistent() { return persistent; }
    public Component getTitle() { return title; }
    public String getTitleRaw() { return titleRaw; }
    public List<String> getLines() { return lines; }
    public int getLineCount() { return lines.size(); }
}