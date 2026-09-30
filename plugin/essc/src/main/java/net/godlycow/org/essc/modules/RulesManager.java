package net.godlycow.org.essc.modules;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RulesManager {
    private final EssentialsC plugin;
    private final MiniMessage miniMessage;
    private final List<Component> rules = new ArrayList<>();
    private final File rulesFile;

    public RulesManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.miniMessage = plugin.getMiniMessage();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.rulesFile = new File(plugin.getDataFolder(), "rules.txt");
    }

    public void load() {
        rules.clear();

        try {
            if (!rulesFile.exists()) {
                if (rulesFile.createNewFile()) {
                    plugin.debug("Created rules.txt");
                    createDefaultRules();
                }
            }

            if (rulesFile.length() == 0) {
                createDefaultRules();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(rulesFile.toPath()), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    Component parsed = miniMessage.deserialize(line);
                    rules.add(parsed);
                }

                plugin.debug("Loaded " + rules.size() + " rules from rules.txt");

            }

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load rules.txt: " + e.getMessage());
        }
    }

    private void createDefaultRules() {
        List<String> defaultRules = List.of(
                "<#FFF200><b>==== Server Rules =====</b>",
                "<#FFF200>1.</#FFF200> <white>Be respectful to all players</white>",
                "<#FFF200>2.</#FFF200> <white>No griefing or stealing</white>",
                "<#FFF200>3.</#FFF200> <white>No cheating or hacked clients</white>",
                "<#FFF200>4.</#FFF200> <white>No spamming or advertising</white>",
                "<#FFF200>5.</#FFF200> <white>Have fun!</white>",
                "<#FFF200><b>======================</#FFF200>"
        );

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(rulesFile.toPath()), StandardCharsets.UTF_8))) {

            for (String line : defaultRules) {
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default rules.txt: " + e.getMessage());
        }
    }

    public List<Component> getRules() {
        return new ArrayList<>(rules);
    }

    public void reload() {
        plugin.debug("Reloading rules...");
        load();
    }
}