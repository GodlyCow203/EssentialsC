package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerListCommand extends Command {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern BUKKIT_HEX_PATTERN = Pattern.compile("&x([&0-9a-fA-F]){12}");
    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fA-Fk-orK-OR])");

    public PlayerListCommand(EssentialsC plugin) {
        super(plugin, "playerlist", "essentialsc.playerlist", false, 0, "command.usage.playerlist");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int total = onlinePlayers.size();
        int max = Bukkit.getMaxPlayers();

        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("online", String.valueOf(total));
        headerPlaceholders.put("max", String.valueOf(max));
        sender.sendMessage(lang.get(sender, "playerlist.header", headerPlaceholders));

        if (total == 0) {
            sender.sendMessage(lang.get(sender, "playerlist.empty"));
            sender.sendMessage(lang.get(sender, "playerlist.footer", headerPlaceholders));
            return true;
        }

        List<String> playerDisplays = new ArrayList<>();
        boolean useLuckPerms = plugin.getConfigManager().isPlayerListLuckPermsEnabled()
                && plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null;

        LuckPerms luckPerms = null;
        if (useLuckPerms) {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckPerms = provider.getProvider();
            }
        }

        for (Player player : onlinePlayers) {
            String display;
            if (useLuckPerms && luckPerms != null) {
                User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    String prefix = user.getCachedData().getMetaData().getPrefix();
                    String suffix = user.getCachedData().getMetaData().getSuffix();
                    String name = player.getName();

                    StringBuilder sb = new StringBuilder();
                    if (prefix != null) {
                        sb.append(convertToMiniMessage(prefix));
                    }
                    sb.append(name);
                    if (suffix != null) {
                        sb.append(convertToMiniMessage(suffix));
                    }
                    display = sb.toString();
                } else {
                    display = player.getName();
                }
            } else {
                display = player.getName();
            }
            playerDisplays.add(display);
        }

        Collections.sort(playerDisplays, String.CASE_INSENSITIVE_ORDER);

        int perLine = plugin.getConfigManager().getPlayerListPerLine();
        StringBuilder line = new StringBuilder();
        int count = 0;

        for (String displayName : playerDisplays) {
            if (count > 0) line.append("<gray>, </gray>");
            line.append(displayName);
            count++;

            if (count >= perLine) {
                sender.sendMessage(plugin.getMiniMessage().deserialize(line.toString()));
                line = new StringBuilder();
                count = 0;
            }
        }

        if (count > 0) {
            sender.sendMessage(plugin.getMiniMessage().deserialize(line.toString()));
        }

        sender.sendMessage(lang.get(sender, "playerlist.footer", headerPlaceholders));

        return true;
    }

    private String convertToMiniMessage(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        Matcher hexMatcher = HEX_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (hexMatcher.find()) {
            String color = hexMatcher.group(1);
            hexMatcher.appendReplacement(sb, "<#" + color + ">");
        }
        hexMatcher.appendTail(sb);
        result = sb.toString();

        result = convertBukkitHex(result);

        result = result.replace("&0", "<black>")
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
                .replace("&r", "<reset>");

        result = result.replace("&A", "<green>")
                .replace("&B", "<aqua>")
                .replace("&C", "<red>")
                .replace("&D", "<light_purple>")
                .replace("&E", "<yellow>")
                .replace("&F", "<white>")
                .replace("&K", "<obfuscated>")
                .replace("&L", "<bold>")
                .replace("&M", "<strikethrough>")
                .replace("&N", "<underlined>")
                .replace("&O", "<italic>")
                .replace("&R", "<reset>");

        return result;
    }

    private String convertBukkitHex(String text) {
        if (text == null || !text.contains("&x")) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();
        int i = 0;

        while (i < chars.length) {
            if (i + 13 < chars.length &&
                    chars[i] == '&' && chars[i + 1] == 'x' &&
                    chars[i + 2] == '&' && chars[i + 4] == '&' &&
                    chars[i + 6] == '&' && chars[i + 8] == '&' &&
                    chars[i + 10] == '&' && chars[i + 12] == '&') {

                String hex = "" + chars[i + 3] + chars[i + 5] + chars[i + 7] +
                        chars[i + 9] + chars[i + 11] + chars[i + 13];
                result.append("<#").append(hex).append(">");
                i += 14;
            } else {
                result.append(chars[i]);
                i++;
            }
        }

        return result.toString();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}