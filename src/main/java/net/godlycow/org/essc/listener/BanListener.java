package net.godlycow.org.essc.listener;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.punishment.PunishmentManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BanListener implements Listener {

    private final EssentialsC plugin;
    private final PunishmentManager punishmentManager;

    public BanListener(EssentialsC plugin, PunishmentManager punishmentManager) {
        this.plugin = plugin;
        this.punishmentManager = punishmentManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (punishmentManager.isBanned(event.getUniqueId())) {
            PunishmentManager.BanEntry banEntry = punishmentManager.getBanEntry(event.getUniqueId());

            String kickMessage;
            if (banEntry != null) {
                Map<String, String> placeholders = Map.of(
                        "reason", banEntry.reason(),
                        "banner", banEntry.banner(),
                        "duration", formatDuration(banEntry.expires())
                );
                kickMessage = String.valueOf(plugin.getLanguageManager().get(null, "ban.screen_message", placeholders));
            } else {
                kickMessage = "You are banned from this server.";
            }

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage);
            plugin.debug("Prevented banned player " + event.getName() + " from joining");
        }
    }

    private String formatDuration(long expires) {
        if (expires <= 0) return "Permanent";

        long remaining = expires - System.currentTimeMillis();
        if (remaining <= 0) return "Expired";

        long days = remaining / (1000 * 60 * 60 * 24);
        long hours = (remaining / (1000 * 60 * 60)) % 24;
        long minutes = (remaining / (1000 * 60)) % 60;

        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }
}