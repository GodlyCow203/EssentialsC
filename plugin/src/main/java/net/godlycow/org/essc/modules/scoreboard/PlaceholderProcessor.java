package net.godlycow.org.essc.modules.scoreboard;

import net.godlycow.org.essc.util.LegacyColorConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class PlaceholderProcessor {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private final boolean papiEnabled;

    public PlaceholderProcessor() {
        this.papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public Component process(Player player, Component base) {
        return base;
    }

    public String processString(Player player, String text) {
        if (text == null || text.isEmpty()) return "";

        String processed = text;
        if (papiEnabled) {
            try {
                processed = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, processed);
            } catch (Exception e) {
                Bukkit.getLogger().warning("PAPI failed for " + player.getName() + ": " + e.getMessage());
            }
        }

        processed = processed
                .replace("%player_name%",        player.getName())
                .replace("%player_displayname%", player.getDisplayName())
                .replace("%server_online%",      String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%server_max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%world%",              player.getWorld().getName())
                .replace("%x%",                  String.valueOf(player.getLocation().getBlockX()))
                .replace("%y%",                  String.valueOf(player.getLocation().getBlockY()))
                .replace("%z%",                  String.valueOf(player.getLocation().getBlockZ()))
                .replace("%ping%",               String.valueOf(player.getPing()))
                .replace("%level%",              String.valueOf(player.getLevel()));

        return LegacyColorConverter.toMiniMessage(processed);
    }
}