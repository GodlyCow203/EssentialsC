package net.godlycow.org.essc.command.economy;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class BaltopCommand extends Command {

    public BaltopCommand(EssentialsC plugin) {
        super(plugin, "baltop", "essentialsc.baltop", false, 0, "command.usage.baltop");
        this.aliases = new String[]{"balancetop"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {}
        }

        final int perPage = 10;
        final int finalPage = page;

        sender.sendMessage(lang.get(sender, "baltop.loading"));

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Map<UUID, BigDecimal> top = plugin.getEconomyManager().getTopBalances(perPage * finalPage).get();

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    displayTop(sender, top, finalPage, perPage);
                });
            } catch (InterruptedException | ExecutionException e) {
                sender.sendMessage(lang.get(sender, "error.internal"));
                plugin.debug("Failed to load baltop: " + e.getMessage());
            }
        });

        return true;
    }

    private void displayTop(CommandSender sender, Map<UUID, BigDecimal> balances, int page, int perPage) {
        List<Map.Entry<UUID, BigDecimal>> entries = new ArrayList<>(balances.entrySet());

        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, entries.size());

        if (start >= entries.size()) {
            sender.sendMessage(lang.get(sender, "baltop.empty"));
            return;
        }

        sender.sendMessage(lang.get(sender, "baltop.header", Map.of("page", String.valueOf(page))));

        for (int i = start; i < end; i++) {
            Map.Entry<UUID, BigDecimal> entry = entries.get(i);
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (name == null) name = "Unknown";

            int rank = i + 1;
            String formatted = plugin.getEconomyManager().format(entry.getValue());

            sender.sendMessage(lang.get(sender, "baltop.entry",
                    Map.of(
                            "rank", String.valueOf(rank),
                            "player", name,
                            "balance", formatted
                    )));
        }

        if (end < entries.size()) {
            sender.sendMessage(lang.get(sender, "baltop.next", Map.of("page", String.valueOf(page + 1))));
        }

        sender.sendMessage(lang.get(sender, "baltop.footer"));
    }
}