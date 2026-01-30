package net.godlycow.org.essc.command;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EcoCommand extends Command {

    public EcoCommand(EssentialsC plugin) {
        super(plugin, "eco", "essentialsc.eco.admin", false, 3, "command.usage.eco");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String action = args[0].toLowerCase();
        String targetName = args[1];

        BigDecimal amount;
        try {
            amount = new BigDecimal(args[2]).setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) < 0 && !action.equals("set")) {
                sender.sendMessage(lang.get(sender, "error.negative_amount"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(lang.get(sender, "error.invalid_number", Map.of("input", args[2])));
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null && !action.equals("set")) {
            sender.sendMessage(lang.get(sender, "error.player_not_found", Map.of("player", targetName)));
            return true;
        }

        switch (action) {
            case "give" -> handleGive(sender, target, amount);
            case "take" -> handleTake(sender, target, amount);
            case "set" -> handleSet(sender, targetName, amount);
            case "reset" -> handleReset(sender, target);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleGive(CommandSender sender, Player target, BigDecimal amount) {
        plugin.getEconomyManager().deposit(target.getUniqueId(), amount).thenAccept(success -> {
            String formatted = plugin.getEconomyManager().format(amount);
            sender.sendMessage(lang.get(sender, "eco.give",
                    Map.of("amount", formatted, "player", target.getName())));
            target.sendMessage(lang.get(target, "eco.give.notify", Map.of("amount", formatted)));
            plugin.debug(sender.getName() + " gave " + formatted + " to " + target.getName());
        });
    }

    private void handleTake(CommandSender sender, Player target, BigDecimal amount) {
        plugin.getEconomyManager().withdraw(target.getUniqueId(), amount).thenAccept(success -> {
            String formatted = plugin.getEconomyManager().format(amount);
            if (success) {
                sender.sendMessage(lang.get(sender, "eco.take",
                        Map.of("amount", formatted, "player", target.getName())));
                target.sendMessage(lang.get(target, "eco.take.notify", Map.of("amount", formatted)));
            } else {
                sender.sendMessage(lang.get(sender, "eco.take.failed",
                        Map.of("player", target.getName())));
            }
        });
    }

    private void handleSet(CommandSender sender, String targetName, BigDecimal amount) {
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            plugin.getEconomyManager().setBalance(target.getUniqueId(), amount).thenAccept(success -> {
                String formatted = plugin.getEconomyManager().format(amount);
                sender.sendMessage(lang.get(sender, "eco.set",
                        Map.of("amount", formatted, "player", targetName)));
                plugin.debug(sender.getName() + " set " + targetName + "'s balance to " + formatted);
            });
        } else {
            sender.sendMessage(lang.get(sender, "error.player_not_found", Map.of("player", targetName)));
        }
    }

    private void handleReset(CommandSender sender, Player target) {
        BigDecimal starting = new BigDecimal(plugin.getConfig().getString("economy.starting-balance", "100.00"));
        plugin.getEconomyManager().setBalance(target.getUniqueId(), starting).thenAccept(success -> {
            String formatted = plugin.getEconomyManager().format(starting);
            sender.sendMessage(lang.get(sender, "eco.reset",
                    Map.of("player", target.getName(), "balance", formatted)));
            target.sendMessage(lang.get(target, "eco.reset.notify", Map.of("balance", formatted)));
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("give", "take", "set", "reset");
        } else if (args.length == 2) {
            return null;
        } else if (args.length == 3 && !args[0].equalsIgnoreCase("reset")) {
            return List.of("100", "500", "1000");
        }
        return Collections.emptyList();
    }
}