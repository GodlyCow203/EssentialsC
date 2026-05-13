package net.godlycow.org.essc.modules;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.util.LegacyColorConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MOTDManager implements Listener {

    private final EssentialsC plugin;
    private final MiniMessage miniMessage;

    private static final Pattern CENTER_PATTERN = Pattern.compile("^%center%");

    private String line1;
    private String line2;
    private boolean enabled;
    private int maxLineWidth;
    private File motdFile;

    public MOTDManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        load();
        if (enabled) Bukkit.getPluginManager().registerEvents(this, plugin);
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

        if (!motdFile.exists()) createDefaultMotdFile();

        List<String> lines = readMotdFile();
        line1 = lines.size() > 0 ? lines.get(0) : "";
        line2 = lines.size() > 1 ? lines.get(1) : "";

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
                try (FileWriter writer = new FileWriter(motdFile)) {
                    writer.write("%center%Change this in the motd.txt file");
                }
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
            while ((line = reader.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read " + motdFile.getName() + ": " + e.getMessage());
        }
        return lines;
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        if (!enabled) return;
        event.motd(parseMotd(line1, line2));
    }

    public Component parseMotd(String firstLine, String secondLine) {
        return Component.text()
                .append(parseLine(firstLine))
                .append(Component.newline())
                .append(parseLine(secondLine))
                .build();
    }

    private Component parseLine(String line) {
        if (line == null || line.isEmpty()) return Component.empty();

        String processed = line;

        boolean shouldCenter = false;
        Matcher centerMatcher = CENTER_PATTERN.matcher(processed);
        if (centerMatcher.find()) {
            shouldCenter = true;
            processed = centerMatcher.replaceFirst("").trim();
        }

        processed = LegacyColorConverter.toMiniMessage(processed);

        Component component;
        try {
            component = miniMessage.deserialize(processed);
        } catch (Exception e) {
            plugin.debug("MiniMessage parsing failed, using legacy: " + e.getMessage());
            component = LegacyColorConverter.fromLegacyAmpersand(processed);
        }

        return shouldCenter ? centerComponent(component) : component;
    }

    private Component centerComponent(Component component) {
        String plainText = getPlainText(component);
        int textLength = calculateVisualLength(plainText);
        if (textLength >= maxLineWidth) return component;
        int padding = (maxLineWidth - textLength) / 2;
        return Component.text(" ".repeat(Math.max(0, padding))).append(component);
    }

    private String getPlainText(Component component) {
        StringBuilder sb = new StringBuilder();
        extractText(component, sb);
        return sb.toString();
    }

    private void extractText(Component component, StringBuilder sb) {
        if (component instanceof TextComponent tc) sb.append(tc.content());
        for (Component child : component.children()) extractText(child, sb);
    }

    private int calculateVisualLength(String text) {
        int length = 0;
        for (char c : text.toCharArray()) {
            length += (c >= '\u4e00' && c <= '\u9fff') ? 2 : 1;
        }
        return length;
    }

    public void saveMotdFile() {
        try (FileWriter writer = new FileWriter(motdFile)) {
            writer.write(line1 + "\n");
            writer.write(line2);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + motdFile.getName() + ": " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean e) {
        this.enabled = e;
    }
}