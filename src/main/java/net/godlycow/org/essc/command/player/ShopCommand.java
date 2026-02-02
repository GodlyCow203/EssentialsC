package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ShopCommand extends Command {

    public ShopCommand(EssentialsC plugin) {
        super(plugin, "shop", "essentialsc.shop", true, 0, "command.usage.shop");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (!plugin.getConfigManager().isShopEnabled()) {
            player.sendMessage(lang.get(player, "shop.disabled"));
            return true;
        }

        if (args.length == 0) {
            plugin.getShopManager().openMainShop(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("essentialsc.shop.admin")) {
                player.sendMessage(lang.get(player, "error.no_permission"));
                return true;
            }

            plugin.getShopManager().reload();
            player.sendMessage(lang.get(player, "shop.reload-success"));
            plugin.debug("Shop reloaded by " + player.getName());
            return true;
        }

        String category = args[0];
        int page = 1;

        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {}
        }

        plugin.getShopManager().openCategory(player, category, page);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new java.util.ArrayList<>();
            completions.add("reload");
            completions.addAll(plugin.getShopManager().getCategories().keySet());
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}