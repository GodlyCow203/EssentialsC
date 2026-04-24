package net.godlycow.org.essc.kit;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.impl.kit.KitImpl;
import net.godlycow.org.essc.api.kit.event.KitCooldownExpireEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KitCooldowns {
    private final EssentialsC plugin;
    private final KitData data;
    private final Set<String> expiredNotifications = ConcurrentHashMap.newKeySet();

    public KitCooldowns(EssentialsC plugin, KitData data) {
        this.plugin = plugin;
        this.data = data;
    }

    public long getRemainingSeconds(Player player, Kit kit) {
        PlayerKitData claimData = data.getKitData(player.getUniqueId(), kit.getName());
        if (claimData == null || claimData.lastClaimed == 0) {
            return 0;
        }

        long cooldownEnd = claimData.lastClaimed + (kit.getCooldown() * 1000L);
        long remaining = cooldownEnd - System.currentTimeMillis();
        long secondsRemaining = Math.max(0, remaining / 1000L);

        if (secondsRemaining == 0 && kit.getCooldown() > 0) {
            String notificationKey = player.getUniqueId().toString() + ":" + kit.getName();
            if (!expiredNotifications.contains(notificationKey)) {
                expiredNotifications.add(notificationKey);

                KitImpl apiKit = new KitImpl(kit);
                KitCooldownExpireEvent expireEvent = new KitCooldownExpireEvent(player, apiKit, claimData.lastClaimed);
                Bukkit.getPluginManager().callEvent(expireEvent);
            }
        }

        return secondsRemaining;
    }

    public void clearNotification(Player player, Kit kit) {
        String notificationKey = player.getUniqueId().toString() + ":" + kit.getName();
        expiredNotifications.remove(notificationKey);
    }

    public void clearAllNotifications() {
        expiredNotifications.clear();
    }
}