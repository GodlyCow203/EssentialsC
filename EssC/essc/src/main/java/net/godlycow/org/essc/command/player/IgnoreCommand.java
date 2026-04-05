package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class IgnoreCommand extends Command {

    public IgnoreCommand(EssentialsC plugin) {
        super(plugin, "ignore", "essentialsc.ignore", true, 0, "command.usage.ignore");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            Set<UUID> ignored = plugin.getIgnoreManager().getIgnored(player.getUniqueId());
            if (ignored.isEmpty()) {
                player.sendMessage(lang.get(player, "ignore.list_empty"));
                return true;
            }

            player.sendMessage(lang.get(player, "ignore.list_header"));
            for (UUID uuid : ignored) {
                Player ignoredPlayer = plugin.getServer().getPlayer(uuid);
                String name = ignoredPlayer != null ? ignoredPlayer.getName() : plugin.getIgnoreManager().getLastKnownName(uuid);
                if (name == null) name = "Unknown";

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", name);
                player.sendMessage(lang.get(player, "ignore.list_entry", placeholders));
            }
            return true;
        }

        String targetName = args[0];
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", targetName);
            player.sendMessage(lang.get(player, "error.player_not_found", placeholders));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(lang.get(player, "ignore.cannot_ignore_self"));
            return true;
        }

        if (target.hasPermission("essentialsc.ignore.exempt")) {
            player.sendMessage(lang.get(player, "ignore.exempt"));
            plugin.debug("Denied: " + player.getName() + " cannot ignore " + target.getName());
            return true;
        }

        boolean isIgnoring = plugin.getIgnoreManager().isIgnoring(player.getUniqueId(), target.getUniqueId());

        if (isIgnoring) {
            plugin.getIgnoreManager().unignore(player.getUniqueId(), target.getUniqueId());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            player.sendMessage(lang.get(player, "ignore.unignored", placeholders));
            plugin.debug(player.getName() + " unignored " + target.getName());
        } else {
            plugin.getIgnoreManager().ignore(player.getUniqueId(), target.getUniqueId(), target.getName());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            player.sendMessage(lang.get(player, "ignore.ignored", placeholders));
            plugin.debug(player.getName() + " ignored " + target.getName());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            Set<UUID> ignored = plugin.getIgnoreManager().getIgnored(player.getUniqueId());
            return plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> !p.equals(sender))
                    .filter(p -> !ignored.contains(p.getUniqueId()) || ignored.contains(p.getUniqueId()))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}