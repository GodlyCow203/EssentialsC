package net.godlycow.org.essc.modules.home;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HomeNotificationManager {

    public enum NotificationType {
        DELETED,
        RENAMED,
        RELOCATED
    }

    private final EssentialsC plugin;
    private final Map<UUID, Boolean> notificationsCache = new ConcurrentHashMap<>();

    public HomeNotificationManager(EssentialsC plugin) {
        this.plugin = plugin;
    }

    public void deliverPending(Player player) {
        plugin.getHomeManager().getDatabase().async(conn -> {
            List<PendingNotification> pending = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, type, home_name, extra, admin_name FROM home_notifications WHERE target_uuid = ? ORDER BY created_at ASC")) {
                stmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    pending.add(new PendingNotification(
                            rs.getInt("id"),
                            NotificationType.valueOf(rs.getString("type")),
                            rs.getString("home_name"),
                            rs.getString("extra"),
                            rs.getString("admin_name")
                    ));
                }
            }
            return pending;
        }).thenAccept(pending -> {
            if (pending.isEmpty()) return;
            player.getScheduler().run(plugin, task -> {
                if (!player.isOnline()) return;
                for (PendingNotification n : pending) {
                    player.sendMessage(buildMessage(player, n.type(), n.homeName(), n.extra(), n.adminName()));
                }
            }, null);
            clearPending(player.getUniqueId());
        });

        loadNotificationsEnabled(player.getUniqueId());
    }

    private void clearPending(UUID uuid) {
        plugin.getHomeManager().getDatabase().async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM home_notifications WHERE target_uuid = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            }
            return null;
        });
    }

    private void loadNotificationsEnabled(UUID uuid) {
        plugin.getHomeManager().getDatabase().async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT enabled FROM home_notification_settings WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getInt("enabled") == 1;
            }
            return true;
        }).thenAccept(enabled -> notificationsCache.put(uuid, enabled));
    }

    public boolean isNotificationsEnabled(UUID uuid) {
        return notificationsCache.getOrDefault(uuid, true);
    }

    public void setNotificationsEnabled(UUID uuid, boolean enabled) {
        notificationsCache.put(uuid, enabled);
        plugin.getHomeManager().getDatabase().async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO home_notification_settings (uuid, enabled) VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET enabled = excluded.enabled
            """)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, enabled ? 1 : 0);
                stmt.executeUpdate();
            }
            return null;
        });
    }

    private Component buildMessage(Player player, NotificationType type,
                                   String homeName, String extra, String adminName) {
        return switch (type) {
            case DELETED -> plugin.getLanguageManager().get(player,
                    "home.notification.admin.deleted",
                    Map.of("name", homeName, "admin", adminName));
            case RENAMED -> plugin.getLanguageManager().get(player,
                    "home.notification.admin.renamed",
                    Map.of("old", homeName, "new", extra != null ? extra : "?", "admin", adminName));
            case RELOCATED -> plugin.getLanguageManager().get(player,
                    "home.notification.admin.relocated",
                    Map.of("name", homeName, "admin", adminName));
        };
    }

    private record PendingNotification(int id, NotificationType type, String homeName, String extra, String adminName) {

    }
}