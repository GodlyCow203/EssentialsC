package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DelHomeCommand extends Command {

    public DelHomeCommand(EssentialsC plugin) {
        super(plugin, "delhome", "essentialsc.delhome", true, 0, "command.usage.delhome");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        boolean guiMode = plugin.getConfigManager().getHomeMode().equals("gui");

        if (args.length == 0) {
            if (guiMode) {
                plugin.getHomeGuiManager().openHomeList(player);
            } else {
                player.sendMessage(lang.get(player, "home.delete.no_name_provided"));
            }
            return true;
        }

        String name = args[0].toLowerCase();

        if (guiMode) {
            plugin.getHomeManager().getHome(player.getUniqueId(), name).whenComplete((home, err) -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (home == null) {
                        player.sendMessage(lang.get(player, "home.delete.not_found", Map.of("name", name)));
                        return;
                    }
                    plugin.getHomeGuiManager().openConfirmDelete(player, home, player.getUniqueId());
                });
            });
        } else {
            if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                deleteHome(player, name);
            } else {
                player.sendMessage(lang.get(player, "home.delete.confirm"));
            }
        }

        return true;
    }

    private void deleteHome(Player player, String name) {
        plugin.getHomeManager().deleteHome(player.getUniqueId(), name).whenComplete((success, err) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.sendMessage(lang.get(player, "home.delete.success", Map.of("name", name)));
                } else {
                    player.sendMessage(lang.get(player, "home.delete.not_found", Map.of("name", name)));
                }
            });
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}