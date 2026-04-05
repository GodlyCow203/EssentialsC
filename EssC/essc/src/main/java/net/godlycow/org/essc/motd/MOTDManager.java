package net.godlycow.org.essc.motd;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MOTDManager implements Listener {

    private final EssentialsC plugin;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern CENTER_PATTERN = Pattern.compile("^%center%");

    private String line1;
    private String line2;
    private boolean enabled;
    private int maxLineWidth;
    private File motdFile;

    public MOTDManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        load();

        if (enabled) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    public void load() {
        enabled = plugin.getConfigManager().isMotdEnabled();
        maxLineWidth = plugin.getConfigManager().getMotdMaxLineWidth();
        String fileName = plugin.getConfigManager().getMotdFileName();

        this.motdFile = new File(plugin.getDataFolder(), fileName);

        if (!enabled) {
            plugin.debug("MOTD system is disabled in config");
            return;
        }

        if (!motdFile.exists()) {
            createDefaultMotdFile();
        }

        List<String> lines = readMotdFile();

        if (lines.isEmpty()) {
            line1 = "";
            line2 = "";
        } else {
            line1 = lines.size() > 0 ? lines.get(0) : "";
            line2 = lines.size() > 1 ? lines.get(1) : "";
        }

        plugin.debug("MOTD loaded from " + fileName + ": Line 1: " + line1 + ", Line 2: " + line2);
    }

    public void reload() {
        load();
        plugin.debug("MOTD reloaded");
    }

    public void shutdown() {
    }

    private void createDefaultMotdFile() {
        try {
            InputStream defaultMotd = plugin.getResource("motd.txt");
            if (defaultMotd != null) {
                Files.copy(defaultMotd, motdFile.toPath());
                plugin.debug("Created default motd.txt from resources");
            } else {
                FileWriter writer = new FileWriter(motdFile);
                writer.write("%center%Change this in the motd.txt file");
                writer.close();
                plugin.debug("Created default motd.txt with built-in defaults");
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create default motd.txt: " + e.getMessage());
        }
    }

    private List<String> readMotdFile() {
        List<String> lines = new ArrayList<>();

        if (!motdFile.exists()) {
            plugin.getLogger().warning("MOTD file not found: " + motdFile.getName());
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(motdFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read " + motdFile.getName() + ": " + e.getMessage());
        }

        return lines;
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        if (!enabled) {
            return;
        }

        Component motdComponent = parseMotd(line1, line2);
        event.motd(motdComponent);
    }

    public Component parseMotd(String firstLine, String secondLine) {
        Component line1Component = parseLine(firstLine);
        Component line2Component = parseLine(secondLine);

        return Component.text()
                .append(line1Component)
                .append(Component.newline())
                .append(line2Component)
                .build();
    }

    private Component parseLine(String line) {
        if (line == null || line.isEmpty()) {
            return Component.empty();
        }

        String processed = line;

        boolean shouldCenter = false;
        Matcher centerMatcher = CENTER_PATTERN.matcher(processed);
        if (centerMatcher.find()) {
            shouldCenter = true;
            processed = centerMatcher.replaceFirst("").trim();
        }

        processed = convertHexColors(processed);
        processed = convertLegacyColors(processed);

        Component component;

        try {
            component = miniMessage.deserialize(processed);
        } catch (Exception e) {
            plugin.debug("MiniMessage parsing failed, using legacy: " + e.getMessage());
            component = legacySerializer.deserialize(processed);
        }

        if (shouldCenter) {
            component = centerComponent(component);
        }

        return component;
    }

    private String convertHexColors(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb, "<#" + hex + ">");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private String convertLegacyColors(String input) {
        Pattern bukkitHexPattern = Pattern.compile("&x([&][0-9a-fA-F]){6}");
        Matcher bukkitMatcher = bukkitHexPattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (bukkitMatcher.find()) {
            String match = bukkitMatcher.group();
            String hex = match.replace("&x", "").replace("&", "");
            bukkitMatcher.appendReplacement(sb, "<#" + hex + ">");
        }
        bukkitMatcher.appendTail(sb);
        String result = sb.toString();

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
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");

        return result;
    }

    private Component centerComponent(Component component) {
        String plainText = getPlainText(component);
        int textLength = calculateVisualLength(plainText);

        if (textLength >= maxLineWidth) {
            return component;
        }

        int padding = (maxLineWidth - textLength) / 2;
        String spaces = " ".repeat(Math.max(0, padding));

        return Component.text(spaces).append(component);
    }

    private String getPlainText(Component component) {
        StringBuilder sb = new StringBuilder();
        extractText(component, sb);
        return sb.toString();
    }

    private void extractText(Component component, StringBuilder sb) {
        if (component instanceof TextComponent textComp) {
            sb.append(textComp.content());
        }

        for (Component child : component.children()) {
            extractText(child, sb);
        }
    }

    private int calculateVisualLength(String text) {
        int length = 0;
        for (char c : text.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length;
    }

    public void setLine1(String line) {
        this.line1 = line;
    }

    public void setLine2(String line) {
        this.line2 = line;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxLineWidth() {
        return maxLineWidth;
    }

    public void setMaxLineWidth(int maxLineWidth) {
        this.maxLineWidth = maxLineWidth;
    }

    public File getMotdFile() {
        return motdFile;
    }

    public void saveMotdFile() {
        try (FileWriter writer = new FileWriter(motdFile)) {
            writer.write(line1 + "\n");
            writer.write(line2);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + motdFile.getName() + ": " + e.getMessage());
        }
    }
}