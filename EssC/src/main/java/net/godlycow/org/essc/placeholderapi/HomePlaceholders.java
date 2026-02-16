package net.godlycow.org.essc.placeholderapi;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.home.Home;
import org.bukkit.entity.Player;

import java.util.*;

public class HomePlaceholders {

    private final EssentialsC plugin;


    private final Map<UUID, List<Home>> homeCache = new HashMap<>();
    private final Map<UUID, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_DURATION_MS = 5000;


    public HomePlaceholders(EssentialsC plugin) {
        this.plugin = plugin;
    }

    public String onRequest(Player player, String identifier) {
        if (!identifier.startsWith("home_")) {
            return null;
        }


        refreshCacheIfNeeded(player);

        List<Home> homes = homeCache.getOrDefault(player.getUniqueId(), Collections.emptyList());

        return switch (identifier.toLowerCase()) {
            case "home_count" -> String.valueOf(homes.size());
            case "home_max" -> String.valueOf(plugin.getHomeManager().getMaxHomes(player));
            case "home_remaining" -> getRemainingHomes(player, homes.size());
            case "home_list" -> homes.isEmpty() ? "None" : String.join(", ", homes.stream().map(Home::getName).toList());
            default -> {

                if (identifier.toLowerCase().startsWith("home_name_")) {
                    yield getHomeNameAtIndex(homes, identifier.substring("home_name_".length()));
                }
                yield null;
            }
        };
    }

    private void refreshCacheIfNeeded(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastUpdate = cacheTimestamps.get(uuid);

        if (lastUpdate == null || (now - lastUpdate) > CACHE_DURATION_MS) {

            cacheTimestamps.put(uuid, now);
            plugin.getHomeManager().getHomes(uuid).thenAccept(homes -> {
                homeCache.put(uuid, homes);
                cacheTimestamps.put(uuid, System.currentTimeMillis());
            });
        }
    }

    private String getRemainingHomes(Player player, int currentCount) {
        int max = plugin.getHomeManager().getMaxHomes(player);
        if (max == Integer.MAX_VALUE) {
            return "∞";
        }
        return String.valueOf(Math.max(0, max - currentCount));
    }

    private String getHomeNameAtIndex(List<Home> homes, String indexStr) {
        try {
            int index = Integer.parseInt(indexStr);
            if (index >= 0 && index < homes.size()) {
                return homes.get(index).getName();
            }
            return "";
        } catch (NumberFormatException e) {
            return "";
        }
    }

    public void clearCache(UUID uuid) {
        homeCache.remove(uuid);
        cacheTimestamps.remove(uuid);
    }

    public static List<String> getPlaceholderList() {
        List<String> list = new ArrayList<>();

        list.add("%essc_home_count% - Returns the number of homes the player has set");
        list.add("%essc_home_max% - Returns the maximum number of homes the player can set");
        list.add("%essc_home_remaining% - Returns the remaining number of homes the player can set");
        list.add("%essc_home_list% - Returns a comma-separated list of home names");
        list.add("%essc_home_name_<index>% - Returns the home name at the specified index (0-based)");

        return list;
    }
}