package net.godlycow.org.essc.command.item;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantCommand extends Command {

    public EnchantCommand(EssentialsC plugin) {
        super(plugin, "enchant", "essentialsc.enchant", true, 1, "command.usage.enchant");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(lang.get(player, "enchant.no_item"));
            plugin.debug("Enchant failed: " + player.getName() + " not holding item");
            return true;
        }

        Enchantment enchantment = getEnchantment(args[0]);
        if (enchantment == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("enchantment", args[0]);
            player.sendMessage(lang.get(player, "enchant.invalid", placeholders));
            plugin.debug("Enchant failed: Invalid enchantment '" + args[0] + "'");
            return true;
        }

        int level = 1;
        if (args.length > 1) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(lang.get(player, "enchant.invalid_level"));
                plugin.debug("Enchant failed: Invalid level '" + args[1] + "'");
                return true;
            }
        }

        if (level < 1) {
            player.sendMessage(lang.get(player, "enchant.invalid_level"));
            plugin.debug("Enchant failed: Level < 1 (" + level + ")");
            return true;
        }

        item.addUnsafeEnchantment(enchantment, level);
        plugin.debug("Enchanted " + item.getType() + " for " + player.getName() +
                " with " + getEnchantmentName(enchantment) + " level " + level);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("enchantment", getEnchantmentName(enchantment));
        placeholders.put("level", String.valueOf(level));
        player.sendMessage(lang.get(player, "enchant.success", placeholders));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(Enchantment.values())
                    .map(this::getEnchantmentName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }
        return Collections.emptyList();
    }

    private Enchantment getEnchantment(String name) {
        Enchantment byKey = Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase()));
        if (byKey != null) return byKey;

        for (Enchantment e : Enchantment.values()) {
            if (getEnchantmentName(e).equalsIgnoreCase(name) ||
                    e.getKey().getKey().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    private String getEnchantmentName(Enchantment enchantment) {
        return enchantment.getKey().getKey().toLowerCase();
    }
}