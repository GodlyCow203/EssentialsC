package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DelHomeCommand extends Command {

    public DelHomeCommand(EssentialsC plugin) {
        super(plugin, "delhome", "essentialsc.delhome", true, 1, "command.usage.delhome");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String homeName = args[0].toLowerCase();

        plugin.debug("Delete home request: " + player.getName() + " -> '" + homeName + "'");

        plugin.getHomeManager().deleteHome(player.getUniqueId(), homeName).thenAccept(success -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.sendMessage(lang.get(player, "home.delete.success", Map.of("name", homeName)));
                    plugin.debug("Deleted home '" + homeName + "' for " + player.getName());
                } else {
                    player.sendMessage(lang.get(player, "home.delete.not_found", Map.of("name", homeName)));
                    plugin.debug("Failed to delete home '" + homeName + "' (not found)");
                }
            });
        });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            plugin.getHomeManager().getHomes(player.getUniqueId()).thenAccept(homes -> {
                homes.forEach(h -> suggestions.add(h.getName()));
            });
            return suggestions;
        }
        return super.tabComplete(sender, args);
    }
}