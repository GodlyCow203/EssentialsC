package net.godlycow.org.essc.expansion.mysql.punishment;

import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.concurrent.TimeUnit;

public class NetworkPunishmentListener implements Listener {

    private final MySQLDatabaseExpansion plugin;
    private final NetworkPunishmentSyncManager syncManager;

    public NetworkPunishmentListener(MySQLDatabaseExpansion plugin, NetworkPunishmentSyncManager syncManager) {
        this.plugin      = plugin;
        this.syncManager = syncManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        var ban = syncManager.checkBan(event.getUniqueId());
        if (ban != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, buildBanScreen(ban));
            plugin.getLogger().info("[NetworkPunishments] Denied join for banned player " + event.getName());
            return;
        }

        String ip = event.getAddress().getHostAddress();
        var ipBan = syncManager.checkIpBan(ip);
        if (ipBan != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, buildIpBanScreen(ipBan, ip));
            plugin.getLogger().info("[NetworkPunishments] Denied join for IP-banned address " + ip + " (" + event.getName() + ")");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        var mute = syncManager.checkMute(event.getPlayer().getUniqueId());
        if (mute != null) {
            event.setCancelled(true);
            String duration = mute.expires() > 0 ? formatRemaining(mute.expires()) : "permanently";
            event.getPlayer().sendMessage(Component.text(
                    "§cYou are muted network-wide (" + duration + "): " + mute.reason()
            ));
            plugin.getLogger().fine("[NetworkPunishments] Blocked chat from muted player " + event.getPlayer().getName());
        }
    }


    private Component buildBanScreen(NetworkPunishmentDatabase.NetworkPunishment ban) {
        String duration = ban.expires() <= 0 ? "Permanent" : formatRemaining(ban.expires());
        return MiniMessage.miniMessage().deserialize(
                "<red><bold>You are banned from this network.</bold></red>\n\n" +
                        "<gray>Reason: <white>" + ban.reason() + "\n" +
                        "<gray>Banned by: <white>" + ban.punisher() + "\n" +
                        "<gray>Duration: <white>" + duration
        );
    }

    private Component buildIpBanScreen(NetworkPunishmentDatabase.NetworkPunishment ban, String ip) {
        String duration = ban.expires() <= 0 ? "Permanent" : formatRemaining(ban.expires());
        return MiniMessage.miniMessage().deserialize(
                "<red><bold>Your IP address is banned from this network.</bold></red>\n\n" +
                        "<gray>Reason: <white>" + ban.reason() + "\n" +
                        "<gray>Banned by: <white>" + ban.punisher() + "\n" +
                        "<gray>Duration: <white>" + duration
        );
    }

    private String formatRemaining(long expires) {
        long diff = expires - System.currentTimeMillis();
        if (diff <= 0) return "Expired";
        long days  = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long mins  = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        if (days  > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + mins + "m";
        return mins + "m";
    }
}