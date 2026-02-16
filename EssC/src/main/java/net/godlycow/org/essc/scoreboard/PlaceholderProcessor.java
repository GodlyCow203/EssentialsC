package net.godlycow.org.essc.scoreboard;

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

        processed = convertAmpersandToMiniMessage(processed);

        if (papiEnabled) {
            try {
                processed = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, processed);
            } catch (Exception e) {
                Bukkit.getLogger().warning("PAPI placeholder failed for " + player.getName() + ": " + e.getMessage());
            }
        }

        processed = convertSectionToMiniMessage(processed);

        processed = HEX_PATTERN.matcher(processed)
                .replaceAll("<#$1>");


        processed = processed
                .replace("%player_name%", player.getName())
                .replace("%player_displayname%", player.getDisplayName())
                .replace("%server_online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%server_max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%world%", player.getWorld().getName())
                .replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                .replace("%z%", String.valueOf(player.getLocation().getBlockZ()))
                .replace("%ping%", String.valueOf(player.getPing()))
                .replace("%level%", String.valueOf(player.getLevel()));

        return processed;
    }


    private String convertAmpersandToMiniMessage(String text) {
        if (text == null) return "";

        return text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>")
                .replace("&&", "&");
    }

    private String convertSectionToMiniMessage(String text) {
        if (text == null) return "";

        return text
                .replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§k", "<obfuscated>")
                .replace("§l", "<bold>")
                .replace("§m", "<strikethrough>")
                .replace("§n", "<underlined>")
                .replace("§o", "<italic>")
                .replace("§r", "<reset>");
    }
}