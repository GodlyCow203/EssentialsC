package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.punishment.PunishmentManager;
import net.godlycow.org.essc.punishment.PunishmentManager.BanEntry;
import net.godlycow.org.essc.punishment.PunishmentManager.IpBanEntry;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BanListCommand extends Command {

    private final PunishmentManager punishmentManager;
    private static final int BANS_PER_PAGE = 10;

    public BanListCommand(EssentialsC plugin, PunishmentManager punishmentManager) {
        super(plugin, "banlist", "essentialsc.banlist", false, 0, "command.usage.banlist");
        this.punishmentManager = punishmentManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String subCommand = "all";
        int page = 1;

        for (String arg : args) {
            if (arg.matches("\\d+")) {
                try {
                    page = Integer.parseInt(arg);
                    if (page < 1) page = 1;
                } catch (NumberFormatException ignored) {}
            } else {
                subCommand = arg.toLowerCase();
            }
        }

        switch (subCommand) {
            case "players", "player", "p" -> showPlayerBans(sender, page);
            case "ips", "ip", "i"         -> showIpBans(sender, page);
            case "history", "h"           -> showRecentBans(sender, page);
            default                       -> showAllBans(sender, page);
        }

        return true;
    }


    private void showPlayerBans(CommandSender sender, int page) {
        List<BanEntry> bans = punishmentManager.getActiveBans();

        if (bans.isEmpty()) {
            sender.sendMessage(lang.get(sender, "banlist.no_player_bans"));
            return;
        }

        int totalPages = totalPages(bans.size());
        showBanListHeader(sender, "player", bans.size(), page, totalPages);

        int start = (page - 1) * BANS_PER_PAGE;
        int end   = Math.min(start + BANS_PER_PAGE, bans.size());
        for (int i = start; i < end; i++) {
            sender.sendMessage(playerBanEntry(sender, bans.get(i), i + 1));
        }

        showBanListFooter(sender, page, totalPages);
    }

    private void showIpBans(CommandSender sender, int page) {
        List<IpBanEntry> bans = punishmentManager.getActiveIpBans();

        if (bans.isEmpty()) {
            sender.sendMessage(lang.get(sender, "banlist.no_ip_bans"));
            return;
        }

        int totalPages = totalPages(bans.size());
        showBanListHeader(sender, "ip", bans.size(), page, totalPages);

        int start = (page - 1) * BANS_PER_PAGE;
        int end   = Math.min(start + BANS_PER_PAGE, bans.size());
        for (int i = start; i < end; i++) {
            sender.sendMessage(ipBanEntry(sender, bans.get(i), i + 1));
        }

        showBanListFooter(sender, page, totalPages);
    }

    private void showAllBans(CommandSender sender, int page) {
        List<BanEntry>   playerBans = punishmentManager.getActiveBans();
        List<IpBanEntry> ipBans     = punishmentManager.getActiveIpBans();
        int total = playerBans.size() + ipBans.size();

        if (total == 0) {
            sender.sendMessage(lang.get(sender, "banlist.no_bans"));
            return;
        }

        List<Object> all = new ArrayList<>();
        all.addAll(playerBans);
        all.addAll(ipBans);
        all.sort((a, b) -> {
            long ta = (a instanceof BanEntry be)     ? be.time() : ((IpBanEntry) a).time();
            long tb = (b instanceof BanEntry be)     ? be.time() : ((IpBanEntry) b).time();
            return Long.compare(tb, ta);
        });

        int totalPages = totalPages(all.size());
        if (page > totalPages) page = totalPages;

        sender.sendMessage(lang.get(sender, "banlist.all.header", Map.of(
                "page",    String.valueOf(page),
                "total",   String.valueOf(totalPages),
                "count",   String.valueOf(total),
                "players", String.valueOf(playerBans.size()),
                "ips",     String.valueOf(ipBans.size())
        )));

        int start = (page - 1) * BANS_PER_PAGE;
        int end   = Math.min(start + BANS_PER_PAGE, all.size());
        for (int i = start; i < end; i++) {
            Object entry = all.get(i);
            if (entry instanceof BanEntry be)        sender.sendMessage(playerBanEntry(sender, be, i + 1));
            else if (entry instanceof IpBanEntry ie) sender.sendMessage(ipBanEntry(sender, ie, i + 1));
        }

        showBanListFooter(sender, page, totalPages);
        sender.sendMessage(lang.get(sender, "banlist.filter_hint"));
    }

    private void showRecentBans(CommandSender sender, int page) {
        List<Object> all = new ArrayList<>();
        all.addAll(punishmentManager.getAllBans());
        all.addAll(punishmentManager.getAllIpBans());
        all.sort((a, b) -> {
            long ta = (a instanceof BanEntry be)     ? be.time() : ((IpBanEntry) a).time();
            long tb = (b instanceof BanEntry be)     ? be.time() : ((IpBanEntry) b).time();
            return Long.compare(tb, ta);
        });

        sender.sendMessage(lang.get(sender, "banlist.history.header"));

        int count = 0;
        for (Object entry : all) {
            if (count >= 10) break;
            count++;
            if (entry instanceof BanEntry be)        sender.sendMessage(playerBanEntry(sender, be, count));
            else if (entry instanceof IpBanEntry ie) sender.sendMessage(ipBanEntry(sender, ie, count));
        }
    }


    private void showBanListHeader(CommandSender sender, String type, int count, int page, int totalPages) {
        sender.sendMessage(lang.get(sender, "banlist." + type + ".header", Map.of(
                "type",  type,
                "count", String.valueOf(count),
                "page",  String.valueOf(page),
                "total", String.valueOf(totalPages)
        )));
    }

    private void showBanListFooter(CommandSender sender, int page, int totalPages) {
        if (totalPages <= 1) return;
        sender.sendMessage(lang.get(sender, "banlist.footer", Map.of(
                "current", String.valueOf(page),
                "total",   String.valueOf(totalPages),
                "prev",    String.valueOf(page - 1),
                "next",    String.valueOf(page + 1)
        )));
    }

    private Component playerBanEntry(CommandSender sender, BanEntry entry, int index) {
        boolean isTemp = entry.expires() > 0;
        return lang.get(sender, isTemp ? "banlist.entry.player.temp" : "banlist.entry.player.perm", Map.of(
                "index",   String.valueOf(index),
                "player",  entry.name(),
                "reason",  entry.reason(),
                "banner",  entry.banner(),
                "time",    formatTimeAgo(entry.time()),
                "expires", isTemp ? formatTimeRemaining(entry.expires()) : "Never"
        ));
    }

    private Component ipBanEntry(CommandSender sender, IpBanEntry entry, int index) {
        boolean isTemp = entry.expires() > 0;
        return lang.get(sender, isTemp ? "banlist.entry.ip.temp" : "banlist.entry.ip.perm", Map.of(
                "index",   String.valueOf(index),
                "ip",      entry.ip(),
                "reason",  entry.reason(),
                "banner",  entry.banner(),
                "time",    formatTimeAgo(entry.time()),
                "expires", isTemp ? formatTimeRemaining(entry.expires()) : "Never"
        ));
    }

    private int totalPages(int size) {
        return Math.max(1, (int) Math.ceil(size / (double) BANS_PER_PAGE));
    }

    private String formatTimeAgo(long timestamp) {
        long diff    = System.currentTimeMillis() - timestamp;
        long days    = TimeUnit.MILLISECONDS.toDays(diff);
        long hours   = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        if (days    > 0) return days + "d ago";
        if (hours   > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return "Just now";
    }

    private String formatTimeRemaining(long timestamp) {
        long diff    = timestamp - System.currentTimeMillis();
        if (diff <= 0) return "Expired";
        long days    = TimeUnit.MILLISECONDS.toDays(diff);
        long hours   = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        if (days    > 0) return days + "d " + hours + "h";
        if (hours   > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("players", "ips", "all", "history", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].matches("players|ips|all")) {
            return List.of("1", "2", "3");
        }
        return Collections.emptyList();
    }
}