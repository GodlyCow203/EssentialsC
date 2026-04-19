package net.godlycow.org.essc.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreboardConfig {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final boolean enabled;
    private final int updateInterval;
    private final boolean persistent;
    private final Component title;
    private final String titleRaw;
    private final List<String> lines;
    private final List<String[]> lineTemplates;

    public ScoreboardConfig(ConfigurationSection config) {
        this.enabled = config.getBoolean("enabled", true);
        this.updateInterval = Math.max(1, config.getInt("update-interval", 20));
        this.persistent = config.getBoolean("persistent", true);

        this.titleRaw = translateColorCodes(config.getString("title", "<gold><bold>MyServer</bold></gold>"));
        this.title = MINI_MESSAGE.deserialize(titleRaw);

        List<String> rawLines = config.getStringList("lines");
        if (rawLines.isEmpty()) {
            rawLines = List.of("", "", "", "");
        }

        List<String> processedLines = new ArrayList<>(rawLines.size());
        for (String line : rawLines) {
            processedLines.add(translateColorCodes(line));
        }
        this.lines = Collections.unmodifiableList(processedLines);

        this.lineTemplates = new ArrayList<>(processedLines.size());
        for (String line : processedLines) {
            lineTemplates.add(splitTemplate(line));
        }
    }

    private String[] splitTemplate(String line) {
        if (line == null) return new String[]{""};
        return line.split("(?=%)", -1);
    }

    private String translateColorCodes(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder sb = new StringBuilder(text.length() + 16);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                String replacement = switch (code) {
                    case '0' -> "<black>";
                    case '1' -> "<dark_blue>";
                    case '2' -> "<dark_green>";
                    case '3' -> "<dark_aqua>";
                    case '4' -> "<dark_red>";
                    case '5' -> "<dark_purple>";
                    case '6' -> "<gold>";
                    case '7' -> "<gray>";
                    case '8' -> "<dark_gray>";
                    case '9' -> "<blue>";
                    case 'a' -> "<green>";
                    case 'b' -> "<aqua>";
                    case 'c' -> "<red>";
                    case 'd' -> "<light_purple>";
                    case 'e' -> "<yellow>";
                    case 'f' -> "<white>";
                    case 'k' -> "<obfuscated>";
                    case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>";
                    case 'n' -> "<underlined>";
                    case 'o' -> "<italic>";
                    case 'r' -> "<reset>";
                    default -> null;
                };

                if (replacement != null) {
                    sb.append(replacement);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }

        return sb.toString();
    }

    public boolean isEnabled() {
        return enabled;
    }
    public int getUpdateInterval() {
        return updateInterval;
    }
    public boolean isPersistent() {
        return persistent;
    }
    public Component getTitle() {
        return title;
    }
    public String getTitleRaw() {
        return titleRaw;
    }
    public List<String> getLines() {
        return lines;
    }
    public int getLineCount() {
        return lines.size();
    }
}