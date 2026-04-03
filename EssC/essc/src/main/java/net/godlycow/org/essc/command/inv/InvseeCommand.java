package net.godlycow.org.essc.command.inv;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvseeCommand extends Command {

    public InvseeCommand(EssentialsC plugin) {
        super(plugin, "invsee", "essentialsc.invsee", true, 1, "command.usage.invsee");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Player target = plugin.getServer().getPlayer(args[0]);

        if (target == null) {
            OfflinePlayer offlineTarget = plugin.getServer().getOfflinePlayer(args[0]);
            if (!offlineTarget.hasPlayedBefore() && offlineTarget.getName() == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", args[0]);
                player.sendMessage(lang.get(player, "error.player_not_found", placeholders));
                return true;
            }

            if (!player.hasPermission("essentialsc.invsee.offline")) {
                player.sendMessage(lang.get(player, "error.no_permission"));
                return true;
            }

            player.openInventory(offlineTarget.getPlayer().getInventory());

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", offlineTarget.getName());
            player.sendMessage(lang.get(player, "invsee.opened_offline", placeholders));

            plugin.debug(player.getName() + " is viewing " + offlineTarget.getName() + "'s inventory (offline)");
            return true;
        }

        if (target == player) {
            player.sendMessage(lang.get(player, "invsee.self"));
            return true;
        }

        player.openInventory(target.getInventory());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", target.getName());
        player.sendMessage(lang.get(player, "invsee.opened", placeholders));

        plugin.debug(player.getName() + " is viewing " + target.getName() + "'s inventory");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> !name.equals(sender.getName()))
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}