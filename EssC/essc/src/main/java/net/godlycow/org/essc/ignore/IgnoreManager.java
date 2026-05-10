package net.godlycow.org.essc.ignore;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IgnoreManager {
    private final EssentialsC plugin;
    private final File ignoreFile;
    private FileConfiguration ignoreConfig;
    private final Map<UUID, Set<UUID>> ignoreCache = new HashMap<>();
    private final Map<UUID, String> lastKnownNames = new HashMap<>();

    public IgnoreManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.ignoreFile = new File(plugin.getDataFolder(), "ignores.yml");
        load();
    }

    private void load() {
        if (!ignoreFile.exists()) {
            try {
                ignoreFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create ignores.yml");
            }
        }
        ignoreConfig = YamlConfiguration.loadConfiguration(ignoreFile);

        for (String key : ignoreConfig.getKeys(false)) {
            try {
                UUID playerUuid = UUID.fromString(key);
                Set<UUID> ignored = new HashSet<>();
                List<String> list = ignoreConfig.getStringList(key + ".ignored");
                for (String uuidStr : list) {
                    try {
                        ignored.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException Ignored) {}
                }
                ignoreCache.put(playerUuid, ignored);
                String lastName = ignoreConfig.getString(key + ".lastName");
                if (lastName != null) {
                    lastKnownNames.put(playerUuid, lastName);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void save() {
        for (Map.Entry<UUID, Set<UUID>> entry : ignoreCache.entrySet()) {
            String key = entry.getKey().toString();
            List<String> list = new ArrayList<>();
            for (UUID uuid : entry.getValue()) {
                list.add(uuid.toString());
            }
            ignoreConfig.set(key + ".ignored", list);
        }
        for (Map.Entry<UUID, String> entry : lastKnownNames.entrySet()) {
            ignoreConfig.set(entry.getKey().toString() + ".lastName", entry.getValue());
        }
        try {
            ignoreConfig.save(ignoreFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save ignores.yml");
        }
    }

    public void ignore(UUID player, UUID target, String targetName) {
        ignoreCache.computeIfAbsent(player, k -> new HashSet<>()).add(target);
        lastKnownNames.put(target, targetName);
        save();
    }

    public void unignore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreCache.get(player);
        if (ignored != null) {
            ignored.remove(target);
            if (ignored.isEmpty()) {
                ignoreCache.remove(player);
            }
            save();
        }
    }

    public boolean isIgnoring(UUID player, UUID target) {
        Set<UUID> ignored = ignoreCache.get(player);
        return ignored != null && ignored.contains(target);
    }

    public Set<UUID> getIgnored(UUID player) {
        return ignoreCache.getOrDefault(player, Collections.emptySet());
    }

    public String getLastKnownName(UUID uuid) {
        return lastKnownNames.get(uuid);
    }

    public void shutdown() {
        save();
        ignoreCache.clear();
        lastKnownNames.clear();
        plugin.debug("Shutting down the Ignore Manager");
    }
}