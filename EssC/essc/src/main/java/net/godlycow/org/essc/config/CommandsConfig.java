package net.godlycow.org.essc.config;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class CommandsConfig {

    private final EssentialsC plugin;
    private final File file;
    private FileConfiguration config;

    public CommandsConfig(EssentialsC plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "commands.yml");
    }

    public void load() {
        plugin.saveResource("commands.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
        migrate();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
        migrate();
        plugin.debug("commands.yml reloaded");
    }

    public void migrate() {
        InputStream defaultStream = plugin.getResource("commands.yml");
        if (defaultStream == null) return;

        FileConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
        );

        boolean dirty = false;
        for (String key : defaults.getKeys(true)) {
            if (!config.contains(key)) {
                config.set(key, defaults.get(key));
                plugin.getLogger().info("[EssentialsC] Migrated missing commands.yml key: " + key);
                dirty = true;
            }
        }

        if (dirty) save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save commands.yml: " + e.getMessage());
        }
    }

    public boolean isEnabled(String command) {
        return config.getBoolean(command + ".enabled", true);
    }

    public String getPriority(String command) {
        return config.getString(command + ".priority", "normal").toLowerCase();
    }

    public List<String> getAliases(String command) {
        return config.getStringList(command + ".aliases");
    }

    public FileConfiguration getConfig() {
        return config;
    }
}