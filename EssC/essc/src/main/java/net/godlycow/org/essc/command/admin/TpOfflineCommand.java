package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.storage.LogoutDataManager;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TpOfflineCommand extends Command {

    private final LogoutDataManager logoutData;

    public TpOfflineCommand(EssentialsC plugin, LogoutDataManager logoutData) {
        super(plugin, "tpoffline", "essentialsc.tpoffline", true, 1, "command.usage.tpoffline");
        this.logoutData = logoutData;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String targetName = args[0];

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", targetName);
            player.sendMessage(lang.get(player, "error.player_not_found", placeholders));
            return true;
        }

        Location loc = logoutData.getLogoutLocation(target.getUniqueId());

        if (loc == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            player.sendMessage(lang.get(player, "tpoffline.no_location", placeholders));
            return true;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("world", loc.getWorld().getName());
        placeholders.put("x", String.format("%.1f", loc.getX()));
        placeholders.put("y", String.format("%.1f", loc.getY()));
        placeholders.put("z", String.format("%.1f", loc.getZ()));

        plugin.getEssScheduler().teleportAsync(player, loc).thenAccept(success -> {
            if (!success) return;
            player.sendMessage(lang.get(player, "tpoffline.success", placeholders));
            plugin.debug(player.getName() + " teleported to " + target.getName() + "'s logout location");
        });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();

            Set<String> suggestions = new HashSet<>();

            suggestions.addAll(plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList()));

            for (String key : logoutData.getConfig().getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String name = logoutData.getLastKnownName(uuid);
                    if (name != null && name.toLowerCase().startsWith(input)) {
                        suggestions.add(name);
                    }
                } catch (IllegalArgumentException ignored) {}
            }

            return suggestions.stream()
                    .sorted()
                    .limit(50)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}