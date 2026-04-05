package net.godlycow.org.essc.command.auction;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.auction.AhSoundManager;
import net.godlycow.org.essc.auction.gui.AhGuiManager;
import net.godlycow.org.essc.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;

public class AhCommand extends Command {
    private final AhGuiManager guiManager;
    private final AhSoundManager soundManager;

    public AhCommand(EssentialsC plugin) {
        super(plugin, "ah", "essentialsc.ah.use", true, 0, "command.usage.ah");
        this.soundManager = new AhSoundManager(plugin);
        this.guiManager = new AhGuiManager(plugin, soundManager);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!checkEnabled(sender)) return true;
        Player player = (Player) sender;

        if (args.length == 0) {
            guiManager.openMainGui(player, 1);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "sell", "s" -> handleSell(player, args);
            case "cancel", "c" -> handleCancel(player, args);
            case "expired", "e" -> guiManager.openExpiredGui(player);
            case "listings", "l", "my" -> guiManager.openListingsGui(player, 1);
            case "reload", "rl" -> handleReload(player);
            case "help", "?" -> sendUsage(player);
            default -> {
                try {
                    guiManager.openMainGui(player, Integer.parseInt(sub));
                } catch (NumberFormatException e) {
                    player.sendMessage(lang.get(player, "ah.invalid_subcommand"));
                    soundManager.playError(player);
                }
            }
        }
        return true;
    }

    private boolean checkEnabled(CommandSender sender) {
        if (!plugin.getConfigManager().isAHEnabled()) {
            sender.sendMessage(lang.get(sender, "ah.disabled"));
            return false;
        }
        if (plugin.getAuctionManager() == null) {
            sender.sendMessage(lang.get(sender, "ah.not_loaded"));
            return false;
        }
        return true;
    }

    private void handleSell(Player player, String[] args) {
        if (!player.hasPermission("essentialsc.ah.sell")) {
            player.sendMessage(lang.get(player, "error.no_permission"));
            soundManager.playError(player);
            return;
        }

        if (args.length < 2) {
            player.sendMessage(lang.get(player, "command.usage.ah_sell"));
            return;
        }

        BigDecimal price = parsePrice(player, args[1]);
        if (price == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(lang.get(player, "ah.no_item"));
            soundManager.playError(player);
            return;
        }

        if (!validatePriceLimits(player, price)) return;

        long duration = plugin.getConfigManager().getAHDuration();
        plugin.getAuctionManager().createAuction(player, item, price, duration).thenAccept(success -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    player.sendMessage(lang.get(player, "ah.listed", Map.of(
                            "price", plugin.getEconomyManager().format(price),
                            "duration", String.valueOf(duration / 3600000)
                    )));
                    soundManager.playSuccess(player);
                } else {
                    player.sendMessage(lang.get(player, "ah.max_auctions_reached"));
                    soundManager.playError(player);
                }
            });
        });
    }

    private BigDecimal parsePrice(Player player, String input) {
        try {
            BigDecimal price = new BigDecimal(input);
            if (price.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
            return price;
        } catch (NumberFormatException e) {
            player.sendMessage(lang.get(player, "ah.invalid_price"));
            soundManager.playError(player);
            return null;
        }
    }

    private boolean validatePriceLimits(Player player, BigDecimal price) {
        BigDecimal min = plugin.getConfigManager().getAHMinPrice();
        BigDecimal max = plugin.getConfigManager().getAHMaxPrice();

        if (!player.hasPermission("essentialsc.ah.bypass.price.min") && price.compareTo(min) < 0) {
            player.sendMessage(lang.get(player, "ah.price_too_low",
                    Map.of("min", plugin.getEconomyManager().format(min))));
            soundManager.playError(player);
            return false;
        }

        if (max != null && !player.hasPermission("essentialsc.ah.bypass.price.max") && price.compareTo(max) > 0) {
            player.sendMessage(lang.get(player, "ah.price_too_high",
                    Map.of("max", plugin.getEconomyManager().format(max))));
            soundManager.playError(player);
            return false;
        }
        return true;
    }

    private void handleCancel(Player player, String[] args) {
        if (!player.hasPermission("essentialsc.ah.cancel")) {
            player.sendMessage(lang.get(player, "error.no_permission"));
            soundManager.playError(player);
            return;
        }

        if (args.length < 2) {
            player.sendMessage(lang.get(player, "command.usage.ah_cancel"));
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(lang.get(player, "ah.invalid_id"));
            soundManager.playError(player);
            return;
        }

        plugin.getAuctionManager().cancelAuction(player, id).thenAccept(success -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.sendMessage(lang.get(player, "ah.cancelled"));
                    soundManager.playCancel(player);
                } else {
                    player.sendMessage(lang.get(player, "ah.not_your_auction"));
                    soundManager.playError(player);
                }
            });
        });
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("essentialsc.ah.reload")) {
            player.sendMessage(lang.get(player, "error.no_permission"));
            soundManager.playError(player);
            return;
        }
        plugin.getAuctionManager().reload();
        player.sendMessage(lang.get(player, "ah.reloaded"));
        soundManager.playSuccess(player);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essentialsc.ah.use")) return Collections.emptyList();
        if (!plugin.getConfigManager().isAHEnabled()) return Collections.emptyList();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("sell", "expired", "listings", "help"));
            if (sender.hasPermission("essentialsc.ah.cancel")) subs.add("cancel");
            if (sender.hasPermission("essentialsc.ah.reload")) subs.add("reload");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("cancel") &&
                sender instanceof Player player && sender.hasPermission("essentialsc.ah.cancel")) {
            return plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId()).stream()
                    .map(a -> String.valueOf(a.getId()))
                    .filter(id -> id.startsWith(args[1]))
                    .toList();
        }

        return Collections.emptyList();
    }

    public void openMainGui(Player player, int page) { guiManager.openMainGui(player, page); }
    public void openExpiredGui(Player player) { guiManager.openExpiredGui(player); }
    public void openListingsGui(Player player, int page) { guiManager.openListingsGui(player, page); }
    public void openHistoryTypeGui(Player player) { guiManager.openHistoryTypeGui(player); }
    public void openSellHistoryGui(Player player, int page) { guiManager.openSellHistoryGui(player, page); }
    public void openBuyHistoryGui(Player player, int page) { guiManager.openBuyHistoryGui(player, page); }

    public AhSoundManager getSoundManager() { return soundManager; }
    public net.godlycow.org.essc.auction.gui.AhItemFactory getItemFactory() { return guiManager.getItemFactory(); }
}