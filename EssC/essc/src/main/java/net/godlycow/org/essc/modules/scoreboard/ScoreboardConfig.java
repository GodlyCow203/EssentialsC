package net.godlycow.org.essc.modules.scoreboard;

import net.godlycow.org.essc.util.LegacyColorConverter;
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

        this.titleRaw = LegacyColorConverter.toMiniMessage(config.getString("title", "<gold><bold>MyServer</bold></gold>"));
        this.title = MINI_MESSAGE.deserialize(titleRaw);

        List<String> rawLines = config.getStringList("lines");
        if (rawLines.isEmpty()) {
            rawLines = List.of("", "", "", "");
        }

        List<String> processedLines = new ArrayList<>(rawLines.size());
        for (String line : rawLines) {
            processedLines.add(LegacyColorConverter.toMiniMessage(line));
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

    public boolean isEnabled() {
        return enabled;
    }
    public int getUpdateInterval() {
        return updateInterval;
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