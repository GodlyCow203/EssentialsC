package net.godlycow.org.essc.vanish;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager implements Listener {
    private final EssentialsC plugin;
    private final Set<UUID> vanishedPlayers;

    private boolean hideFromTab;
    private boolean giveNightVision;
    private boolean preventMobTarget;
    private boolean disableCollisions;

    public VanishManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
        loadConfig();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void loadConfig() {
        this.hideFromTab = plugin.getConfigManager().isVanishHideFromTab();
        this.giveNightVision = plugin.getConfigManager().isVanishNightVision();
        this.preventMobTarget = plugin.getConfigManager().isVanishPreventMobTarget();
        this.disableCollisions = plugin.getConfigManager().isVanishDisableCollisions();
        plugin.debug("Vanish config loaded - hideFromTab: " + hideFromTab + ", nightVision: " + giveNightVision);
    }

    public void vanish(Player player) {
        vanishedPlayers.add(player.getUniqueId());

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.hidePlayer(plugin, player);
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));

        if (giveNightVision) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        }

        if (hideFromTab) {
            player.playerListName(null);
        }

        if (preventMobTarget) {
            player.setAffectsSpawning(false);
        }

        if (disableCollisions) {
            player.setCollidable(false);
        }

        plugin.debug(player.getName() + " is now vanished");
    }

    public void unvanish(Player player) {
        vanishedPlayers.remove(player.getUniqueId());

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.showPlayer(plugin, player);
            }
        }

        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        if (giveNightVision) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }

        if (hideFromTab) {
            player.playerListName(Component.text(player.getName()));
        }

        if (preventMobTarget) {
            player.setAffectsSpawning(true);
        }

        if (disableCollisions) {
            player.setCollidable(true);
        }

        plugin.debug(player.getName() + " is no longer vanished");
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();

        if (vanishedPlayers.contains(joining.getUniqueId())) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!joining.isOnline()) return;

                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    if (!online.equals(joining)) {
                        online.hidePlayer(plugin, joining);
                    }
                }

                joining.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                if (giveNightVision) {
                    joining.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
                }
                if (hideFromTab) {
                    joining.playerListName(null);
                }
                if (preventMobTarget) {
                    joining.setAffectsSpawning(false);
                }
                if (disableCollisions) {
                    joining.setCollidable(false);
                }
            }, 2L);
        }

        for (UUID vanishedId : vanishedPlayers) {
            Player vanished = plugin.getServer().getPlayer(vanishedId);
            if (vanished != null && !vanished.equals(joining)) {
                joining.hidePlayer(plugin, vanished);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    }
}
