package net.godlycow.org.essc.fly;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlyManager implements Listener {
    private final EssentialsC plugin;
    private final Set<UUID> flyingPlayers;
    private final File dataFile;

    public FlyManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "flying_players.json");
        this.flyingPlayers = loadData();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.isFlying() || player.getAllowFlight()) {
            if (flyingPlayers.add(player.getUniqueId())) {
                saveData();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            if (player.hasPermission("essentialsc.fly.disable-on-join")) {
                player.setFlying(false);
                player.setAllowFlight(false);
                flyingPlayers.remove(player.getUniqueId());
                saveData();
                plugin.debug("Disabled fly for " + player.getName() + " (disable-on-join permission)");
            } else if (flyingPlayers.contains(player.getUniqueId())) {
                player.setAllowFlight(true);
                player.setFlying(true);
                flyingPlayers.remove(player.getUniqueId());
                saveData();
                plugin.debug("Restored fly mode for " + player.getName());
            }
        }, 2L);
    }


    public boolean isFlying(Player player) {
        return player.getAllowFlight() && player.isFlying();
    }

    public void setFlying(Player player, boolean flying) {
        player.setAllowFlight(flying);
        player.setFlying(flying);
    }

    public void toggleFlying(Player player) {
        setFlying(player, !isFlying(player));
    }

    public boolean hasPersistentFly(UUID uuid) {
        return flyingPlayers.contains(uuid);
    }

    public void setPersistentFly(UUID uuid, boolean persistent) {
        if (persistent) {
            flyingPlayers.add(uuid);
        } else {
            flyingPlayers.remove(uuid);
        }
        saveData();
    }

    public Set<UUID> getPersistentFlyPlayers() {
        return Collections.unmodifiableSet(new HashSet<>(flyingPlayers));
    }


    private Set<UUID> loadData() {
        Set<UUID> data = new HashSet<>();
        if (!dataFile.exists()) return data;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    data.add(UUID.fromString(line.trim()));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load flying players data");
        }
        return data;
    }

    private void saveData() {
        try {
            if (!dataFile.exists()) dataFile.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8))) {
                for (UUID uuid : flyingPlayers) {
                    writer.write(uuid.toString());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save flying players data");
        }
    }
}