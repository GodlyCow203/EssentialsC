package net.godlycow.org.essc.integration.bedrock;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class BedrockUtil {

    private final EssentialsC plugin;
    private final FloodgateHook floodgateHook;

    public BedrockUtil(EssentialsC plugin, FloodgateHook floodgateHook) {
        this.plugin = plugin;
        this.floodgateHook = floodgateHook;
    }

    public boolean isAvailable() {
        return floodgateHook.isAvailable();
    }

    public String getPrefix() {
        return floodgateHook.isAvailable() ? floodgateHook.getPrefix() : "";
    }

    //exact match only
    public Player resolvePlayer(String name) {
        if (name == null || name.isBlank()) return null;

        Player exact = plugin.getServer().getPlayerExact(name);
        if (exact != null) return exact;

        String prefixed = prefixed(name);
        if (prefixed != null) {
            Player bedrock = plugin.getServer().getPlayerExact(prefixed);
            if (bedrock != null) {
                plugin.debug("BedrockUtil: resolved \"" + name + "\" to Bedrock player \"" + prefixed + "\"");
                return bedrock;
            }
        }

        return null;
    }

    //exact, then partial match
    public Player matchPlayer(String name) {
        if (name == null ||name.isBlank())
            return null;

        Player exact = resolvePlayer(name);
        if (exact != null)
            return exact;


        Player partial = plugin.getServer().getPlayer(name);
        if (partial != null)
            return partial;


        String prefixed = prefixed(name);
        if (prefixed != null) {
            Player bedrock = plugin.getServer().getPlayer(prefixed);
            if (bedrock != null)
            {
                plugin.debug("BedrockUtil: matched \"" + name + "\" to Bedrock player \"" + bedrock.getName() + "\"");
                return bedrock;
            }
        }

        return null;
    }

    public OfflinePlayer resolveOfflinePlayer(String name) {
        if (name == null || name.isBlank()) return null;

        Player online = resolvePlayer(name);
        if (online != null) return online;

        OfflinePlayer found = findKnownOffline(name);
        if (found != null) return found;

        String prefixed = prefixed(name);
        if (prefixed != null) {
            OfflinePlayer bedrockFound = findKnownOffline(prefixed);
            if (bedrockFound != null) {
                plugin.debug("BedrockUtil: resolved offline \"" + name + "\" to Bedrock player \"" + prefixed + "\"");
                return bedrockFound;
            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer resolved = resolveOfflinePlayer(name);
        return resolved
                != null ? resolved : plugin.getServer().getOfflinePlayer(name);
    }

    public boolean isBedrockPlayer(Player player) {
        return player != null && floodgateHook.isBedrockPlayer(player);
    }

    public boolean isBedrockPlayer(UUID uuid) {
        return floodgateHook.isBedrockId(uuid);
    }

    public FloodgateHook getHook() {
        return floodgateHook;
    }

    private String prefixed(String name) {
        if (!floodgateHook.isAvailable()) return null;
        String prefix = floodgateHook.getPrefix();
        if (prefix.isEmpty() || name.startsWith(prefix)) return null;
        return prefix + name;
    }

    private OfflinePlayer findKnownOffline(String name) {
        try {
            OfflinePlayer cached = plugin.getServer().getOfflinePlayerIfCached(name);
            if (cached != null && cached.hasPlayedBefore())
                return cached;
        } catch (NoSuchMethodError ignored) {
        }

        return Arrays.stream(plugin.getServer().getOfflinePlayers())
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name))
                .filter(OfflinePlayer::hasPlayedBefore)
                .findFirst()
                .orElse(null);
    }
}
