package net.godlycow.org.essc.data;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class LogoutDataManager implements Listener {

    private final EssentialsC plugin;
    private final File dataFile;
    private FileConfiguration config;

    public LogoutDataManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "logout.yml");
        load();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void load() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create logout.yml");
            }
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void save() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save logout.yml");
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Location loc = event.getPlayer().getLocation();

        String path = uuid.toString();
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
        config.set(path + ".name", event.getPlayer().getName());
        config.set(path + ".time", System.currentTimeMillis());

        save();

        if (plugin.getUserManager() != null) {
            plugin.getUserManager().getLocationManager().setLogoutLocation(uuid, loc);
        }

        plugin.debug("Saved logout location for " + event.getPlayer().getName());
    }

    public Location getLogoutLocation(UUID uuid) {
        String path = uuid.toString();
        if (!config.contains(path)) {
            return null;
        }

        String worldName = config.getString(path + ".world");
        if (worldName == null || Bukkit.getWorld(worldName) == null) {
            return null;
        }

        return new Location(
                Bukkit.getWorld(worldName),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }

    public String getLastKnownName(UUID uuid) {
        return config.getString(uuid.toString() + ".name");
    }

}