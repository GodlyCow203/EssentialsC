package net.godlycow.org.essc.punishment;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PunishmentManager {
    private final EssentialsC plugin;
    private final File banFile;
    private final File muteFile;
    private FileConfiguration banConfig;
    private FileConfiguration muteConfig;
    private NetworkPunishmentHook networkHook = null;

    public PunishmentManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.banFile = new File(plugin.getDataFolder(), "bans.yml");
        this.muteFile = new File(plugin.getDataFolder(), "mutes.yml");
        loadFiles();
    }

    public void setNetworkHook(NetworkPunishmentHook hook) {
        this.networkHook = hook;
        plugin.getLogger().info("[PunishmentManager] Network punishment hook registered.");
    }

    public void clearNetworkHook() {
        this.networkHook = null;
    }

    private void loadFiles() {
        if (!banFile.exists()) {
            try { banFile.createNewFile(); }
            catch (IOException e) { plugin.getLogger().severe("Failed to create bans.yml"); }
        }
        if (!muteFile.exists()) {
            try { muteFile.createNewFile(); }
            catch (IOException e) { plugin.getLogger().severe("Failed to create mutes.yml"); }
        }
        banConfig  = YamlConfiguration.loadConfiguration(banFile);
        muteConfig = YamlConfiguration.loadConfiguration(muteFile);
    }

    public void saveBans() {
        try { banConfig.save(banFile); }
        catch (IOException e) { plugin.getLogger().severe("Failed to save bans.yml"); }
    }

    public void saveMutes() {
        try { muteConfig.save(muteFile); }
        catch (IOException e) { plugin.getLogger().severe("Failed to save mutes.yml"); }
    }


    public void banPlayer(UUID uuid, String name, String reason, String banner, long expires) {
        String path = "players." + uuid;
        banConfig.set(path + ".name",    name);
        banConfig.set(path + ".reason",  reason);
        banConfig.set(path + ".banner",  banner);
        banConfig.set(path + ".time",    System.currentTimeMillis());
        banConfig.set(path + ".expires", expires);
        saveBans();
        plugin.debug("Banned " + name + " (" + uuid + ") by " + banner + " until " + expires);

        if (networkHook != null) networkHook.onBan(uuid, name, reason, banner, expires);
    }

    public void unbanPlayer(UUID uuid) {
        banConfig.set("players." + uuid, null);
        saveBans();
        plugin.debug("Unbanned " + uuid);

        if (networkHook != null) networkHook.onUnban(uuid);
    }

    public boolean isBanned(UUID uuid) {
        String path = "players." + uuid;
        if (!banConfig.contains(path)) return false;
        long expires = banConfig.getLong(path + ".expires");
        if (expires > 0 && expires < System.currentTimeMillis()) {
            unbanPlayer(uuid);
            return false;
        }
        return true;
    }

    public BanEntry getBanEntry(UUID uuid) {
        if (!isBanned(uuid)) return null;
        String path = "players." + uuid;
        return new BanEntry(
                uuid,
                banConfig.getString(path + ".name"),
                banConfig.getString(path + ".reason"),
                banConfig.getString(path + ".banner"),
                banConfig.getLong(path + ".time"),
                banConfig.getLong(path + ".expires")
        );
    }

    public List<BanEntry> getActiveBans() {
        return getAllBans().stream().filter(this::isBanActive).collect(Collectors.toList());
    }

    public List<BanEntry> getAllBans() {
        List<BanEntry> bans = new ArrayList<>();
        if (!banConfig.contains("players")) return bans;
        for (String key : banConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String path = "players." + key;
                bans.add(new BanEntry(uuid,
                        banConfig.getString(path + ".name"),
                        banConfig.getString(path + ".reason"),
                        banConfig.getString(path + ".banner"),
                        banConfig.getLong(path + ".time"),
                        banConfig.getLong(path + ".expires")));
            } catch (IllegalArgumentException ignored) {}
        }
        return bans;
    }

    private boolean isBanActive(BanEntry e) {
        return e.expires() <= 0 || e.expires() > System.currentTimeMillis();
    }


    public void banIp(String ip, String reason, String banner, long expires) {
        String safeIp = ip.replace('.', '_');
        String path   = "ips." + safeIp;
        banConfig.set(path + ".ip",      ip);
        banConfig.set(path + ".reason",  reason);
        banConfig.set(path + ".banner",  banner);
        banConfig.set(path + ".time",    System.currentTimeMillis());
        banConfig.set(path + ".expires", expires);
        saveBans();
        plugin.debug("IP Banned " + ip + " by " + banner + " until " + expires);

        if (networkHook != null) networkHook.onIpBan(ip, reason, banner, expires);
    }

    public void unbanIp(String ip) {
        banConfig.set("ips." + ip.replace('.', '_'), null);
        saveBans();
        plugin.debug("Unbanned IP " + ip);

        if (networkHook != null) networkHook.onIpUnban(ip);
    }

    public boolean isIpBanned(String ip) {
        String path = "ips." + ip.replace('.', '_');
        if (!banConfig.contains(path)) return false;
        long expires = banConfig.getLong(path + ".expires");
        if (expires > 0 && expires < System.currentTimeMillis()) {
            unbanIp(ip);
            return false;
        }
        return true;
    }

    public IpBanEntry getIpBanEntry(String ip) {
        if (!isIpBanned(ip)) return null;
        String path = "ips." + ip.replace('.', '_');
        return new IpBanEntry(ip,
                banConfig.getString(path + ".reason"),
                banConfig.getString(path + ".banner"),
                banConfig.getLong(path + ".time"),
                banConfig.getLong(path + ".expires"));
    }

    public List<IpBanEntry> getActiveIpBans() {
        return getAllIpBans().stream().filter(this::isIpBanActive).collect(Collectors.toList());
    }

    public List<IpBanEntry> getAllIpBans() {
        List<IpBanEntry> bans = new ArrayList<>();
        if (!banConfig.contains("ips")) return bans;
        for (String key : banConfig.getConfigurationSection("ips").getKeys(false)) {
            String path = "ips." + key;
            String originalIp = banConfig.getString(path + ".ip", key.replace('_', '.'));
            bans.add(new IpBanEntry(originalIp,
                    banConfig.getString(path + ".reason"),
                    banConfig.getString(path + ".banner"),
                    banConfig.getLong(path + ".time"),
                    banConfig.getLong(path + ".expires")));
        }
        return bans;
    }

    private boolean isIpBanActive(IpBanEntry e) {
        return e.expires() <= 0 || e.expires() > System.currentTimeMillis();
    }


    public void mutePlayer(UUID uuid, String name, String reason, String muter, long expires) {
        String path = uuid.toString();
        muteConfig.set(path + ".name",    name);
        muteConfig.set(path + ".reason",  reason);
        muteConfig.set(path + ".muter",   muter);
        muteConfig.set(path + ".time",    System.currentTimeMillis());
        muteConfig.set(path + ".expires", expires);
        saveMutes();
        plugin.debug("Muted " + name + " by " + muter + " until " + expires);

        if (networkHook != null) networkHook.onMute(uuid, name, reason, muter, expires);
    }

    public void unmutePlayer(UUID uuid) {
        muteConfig.set(uuid.toString(), null);
        saveMutes();
        plugin.debug("Unmuted " + uuid);

        if (networkHook != null) networkHook.onUnmute(uuid);
    }

    public boolean isMuted(UUID uuid) {
        if (!muteConfig.contains(uuid.toString())) return false;
        long expires = muteConfig.getLong(uuid + ".expires");
        if (expires > 0 && expires < System.currentTimeMillis()) {
            unmutePlayer(uuid);
            return false;
        }
        return true;
    }

    public MuteEntry getMuteEntry(UUID uuid) {
        if (!isMuted(uuid)) return null;
        String path = uuid.toString();
        return new MuteEntry(uuid,
                muteConfig.getString(path + ".name"),
                muteConfig.getString(path + ".reason"),
                muteConfig.getString(path + ".muter"),
                muteConfig.getLong(path + ".time"),
                muteConfig.getLong(path + ".expires"));
    }

    public List<MuteEntry> getAllMutes() {
        List<MuteEntry> mutes = new ArrayList<>();
        for (String key : muteConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                MuteEntry entry = getMuteEntry(uuid);
                if (entry != null) mutes.add(entry);
            } catch (IllegalArgumentException ignored) {}
        }
        return mutes;
    }


    public record BanEntry(UUID uuid, String name, String reason, String banner, long time, long expires) {}
    public record IpBanEntry(String ip, String reason, String banner, long time, long expires) {}
    public record MuteEntry(UUID uuid, String name, String reason, String muter, long time, long expires) {}
}