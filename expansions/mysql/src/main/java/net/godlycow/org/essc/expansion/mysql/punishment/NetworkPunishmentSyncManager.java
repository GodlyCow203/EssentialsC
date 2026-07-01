package net.godlycow.org.essc.expansion.mysql.punishment;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import net.godlycow.org.essc.expansion.mysql.config.SyncConfig;
import net.godlycow.org.essc.expansion.mysql.database.SyncDatabase;
import net.godlycow.org.essc.modules.punishment.NetworkPunishmentHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NetworkPunishmentSyncManager implements NetworkPunishmentHook {

    private final MySQLDatabaseExpansion plugin;
    private final EssentialsC essc;
    private final SyncConfig config;
    private final NetworkPunishmentDatabase db;

    private BukkitTask pollTask;
    private long lastPollTime = 0;

    private final Set<Long> appliedIds = ConcurrentHashMap.newKeySet();

    public NetworkPunishmentSyncManager(MySQLDatabaseExpansion plugin,
                                        EssentialsC essc,
                                        SyncConfig config,
                                        SyncDatabase syncDb) throws SQLException {
        this.plugin = plugin;
        this.essc   = essc;
        this.config = config;
        this.db     = new NetworkPunishmentDatabase(syncDb, plugin.getLogger());
        this.db.createTable();
    }

    public void start() {
        lastPollTime = System.currentTimeMillis();
        pollTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::pollCycle, 40L, 40L);
        plugin.getLogger().info("[NetworkPunishments] Poll loop started (2s interval).");
    }

    public void shutdown() {
        if (pollTask != null) pollTask.cancel();
        essc.getPunishmentManager().clearNetworkHook();
    }


    @Override
    public void onBan(UUID uuid, String name, String reason, String banner, long expires) {
        db.insertPunishment(NetworkPunishmentDatabase.PunishType.BAN,
                        uuid.toString(), name, reason, banner, config.getServerId(), expires)
                .exceptionally(ex -> { log("Failed to push ban for " + name, ex); return null; })
                .thenAccept(id -> { if (id != null) appliedIds.add(id); });
    }

    @Override
    public void onUnban(UUID uuid) {
        db.deactivatePunishment(NetworkPunishmentDatabase.PunishType.BAN, uuid.toString())
                .exceptionally(ex -> { log("Failed to push unban for " + uuid, ex); return null; });
    }

    @Override
    public void onIpBan(String ip, String reason, String banner, long expires) {
        db.insertPunishment(NetworkPunishmentDatabase.PunishType.IP_BAN,
                        ip, null, reason, banner, config.getServerId(), expires)
                .exceptionally(ex -> { log("Failed to push IP ban for " + ip, ex); return null; })
                .thenAccept(id -> { if (id != null) appliedIds.add(id); });
    }

    @Override
    public void onIpUnban(String ip) {
        db.deactivatePunishment(NetworkPunishmentDatabase.PunishType.IP_BAN, ip)
                .exceptionally(ex -> { log("Failed to push IP unban for " + ip, ex); return null; });
    }

    @Override
    public void onMute(UUID uuid, String name, String reason, String muter, long expires) {
        db.insertPunishment(NetworkPunishmentDatabase.PunishType.MUTE,
                        uuid.toString(), name, reason, muter, config.getServerId(), expires)
                .exceptionally(ex -> { log("Failed to push mute for " + name, ex); return null; })
                .thenAccept(id -> { if (id != null) appliedIds.add(id); });
    }

    @Override
    public void onUnmute(UUID uuid) {
        db.deactivatePunishment(NetworkPunishmentDatabase.PunishType.MUTE, uuid.toString())
                .exceptionally(ex -> { log("Failed to push unmute for " + uuid, ex); return null; });
    }


    private void pollCycle() {
        try {
            long since = lastPollTime;
            lastPollTime = System.currentTimeMillis();

            List<NetworkPunishmentDatabase.NetworkPunishment> updates =
                    db.fetchUpdatedSince(since, config.getServerId()).join();

            for (var p : updates) {
                if (appliedIds.contains(p.id())) continue;
                appliedIds.add(p.id());

                switch (p.type()) {
                    case "BAN"    -> handleRemoteBan(p);
                    case "IP_BAN" -> handleRemoteIpBan(p);
                    case "MUTE"   -> handleRemoteMute(p);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[NetworkPunishments] Poll cycle error", e);
        }
    }

    private void handleRemoteBan(NetworkPunishmentDatabase.NetworkPunishment p) {
        if (!p.active()) return;

        UUID uuid;
        try { uuid = UUID.fromString(p.target()); }
        catch (IllegalArgumentException e) { return; }

        essc.getPunishmentManager().banPlayer(uuid, p.targetName(), p.reason(), p.punisher(), p.expires());

        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            Component msg = buildBanKick(p.reason(), p.punisher(), p.expires());
            Bukkit.getScheduler().runTask(plugin, () -> online.kick(msg));
            plugin.getLogger().info("[NetworkPunishments] Kicked " + online.getName()
                    + " (network ban from " + p.serverId() + ")");
        }
    }

    private void handleRemoteIpBan(NetworkPunishmentDatabase.NetworkPunishment p) {
        if (!p.active()) return;

        String ip = p.target();
        essc.getPunishmentManager().banIp(ip, p.reason(), p.punisher(), p.expires());

        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getAddress() != null
                    && pl.getAddress().getAddress().getHostAddress().equals(ip)) {
                Component msg = buildBanKick(p.reason(), p.punisher(), p.expires());
                Bukkit.getScheduler().runTask(plugin, () -> pl.kick(msg));
                plugin.getLogger().info("[NetworkPunishments] Kicked " + pl.getName()
                        + " (network IP ban from " + p.serverId() + ")");
            }
        }
    }

    private void handleRemoteMute(NetworkPunishmentDatabase.NetworkPunishment p) {
        UUID uuid;
        try { uuid = UUID.fromString(p.target()); }
        catch (IllegalArgumentException e) { return; }

        if (p.active()) {
            essc.getPunishmentManager().mutePlayer(
                    uuid, p.targetName(), p.reason(), p.punisher(), p.expires());

            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                online.sendMessage(Component.text(
                        "§cYou have been muted network-wide by " + p.punisher() + ": " + p.reason()));
            }
            plugin.getLogger().info("[NetworkPunishments] Applied network mute for "
                    + p.targetName() + " (from " + p.serverId() + ")");
        } else {
            essc.getPunishmentManager().unmutePlayer(uuid);
            plugin.getLogger().info("[NetworkPunishments] Removed network mute for " + p.targetName());
        }
    }


    public NetworkPunishmentDatabase.NetworkPunishment checkBan(UUID uuid) {
        try { return db.getActiveBan(uuid).get(3, TimeUnit.SECONDS); }
        catch (Exception e) { log("Ban lookup failed for " + uuid, e); return null; }
    }

    public NetworkPunishmentDatabase.NetworkPunishment checkIpBan(String ip) {
        try { return db.getActiveIpBan(ip).get(3, TimeUnit.SECONDS); }
        catch (Exception e) { log("IP ban lookup failed for " + ip, e); return null; }
    }

    public NetworkPunishmentDatabase.NetworkPunishment checkMute(UUID uuid) {
        try { return db.getActiveMute(uuid).get(3, TimeUnit.SECONDS); }
        catch (Exception e) { log("Mute lookup failed for " + uuid, e); return null; }
    }

    private Component buildBanKick(String reason, String punisher, long expires) {
        String duration = expires <= 0 ? "Permanent" : formatRemaining(expires);
        return MiniMessage.miniMessage().deserialize(
                "<red><bold>You are banned from this network.</bold></red>\n\n" +
                        "<gray>Reason: <white>" + reason + "\n" +
                        "<gray>By: <white>" + punisher + "\n" +
                        "<gray>Duration: <white>" + duration
        );
    }

    private String formatRemaining(long expires) {
        long diff  = expires - System.currentTimeMillis();
        if (diff <= 0) return "Expired";
        long days  = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long mins  = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        if (days  > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + mins + "m";
        return mins + "m";
    }

    private void log(String msg, Throwable t) {
        plugin.getLogger().log(Level.WARNING, "[NetworkPunishments] " + msg, t);
    }
}