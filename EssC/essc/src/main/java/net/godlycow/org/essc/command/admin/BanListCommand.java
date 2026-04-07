package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.punishment.PunishmentManager;
import net.godlycow.org.essc.punishment.PunishmentManager.BanEntry;
import net.godlycow.org.essc.punishment.PunishmentManager.IpBanEntry;
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
        this.aliases = new String[]{"bans"};
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
            case "ips", "ip", "i" -> showIpBans(sender, page);
            case "history", "h" -> showRecentBans(sender, page);
            default -> showAllBans(sender, page);
        }

        return true;
    }

    private void showPlayerBans(CommandSender sender, int page) {
        List<BanEntry> bans = punishmentManager.getActiveBans();

        if (bans.isEmpty()) {
            sender.sendMessage(lang.get(sender, "banlist.no_player_bans"));
            return;
        }

        showBanListHeader(sender, "player", bans.size(), page, (int) Math.ceil(bans.size() / (double) BANS_PER_PAGE));

        int start = (page - 1) * BANS_PER_PAGE;
        int end = Math.min(start + BANS_PER_PAGE, bans.size());

        for (int i = start; i < end; i++) {
            BanEntry entry = bans.get(i);
            sender.sendMessage(formatPlayerBanEntry(sender, entry, i + 1));
        }

        showBanListFooter(sender, page, (int) Math.ceil(bans.size() / (double) BANS_PER_PAGE));
    }

    private void showIpBans(CommandSender sender, int page) {
        List<IpBanEntry> bans = punishmentManager.getActiveIpBans();

        if (bans.isEmpty()) {
            sender.sendMessage(lang.get(sender, "banlist.no_ip_bans"));
            return;
        }

        showBanListHeader(sender, "ip", bans.size(), page, (int) Math.ceil(bans.size() / (double) BANS_PER_PAGE));

        int start = (page - 1) * BANS_PER_PAGE;
        int end = Math.min(start + BANS_PER_PAGE, bans.size());

        for (int i = start; i < end; i++) {
            IpBanEntry entry = bans.get(i);
            sender.sendMessage(formatIpBanEntry(sender, entry, i + 1));
        }

        showBanListFooter(sender, page, (int) Math.ceil(bans.size() / (double) BANS_PER_PAGE));
    }

    private void showAllBans(CommandSender sender, int page) {
        List<BanEntry> playerBans = punishmentManager.getActiveBans();
        List<IpBanEntry> ipBans = punishmentManager.getActiveIpBans();

        int totalBans = playerBans.size() + ipBans.size();

        if (totalBans == 0) {
            sender.sendMessage(lang.get(sender, "banlist.no_bans"));
            return;
        }

        List<Object> allBans = new ArrayList<>();
        allBans.addAll(playerBans);
        allBans.addAll(ipBans);

        allBans.sort((a, b) -> {
            long timeA = (a instanceof BanEntry be) ? be.time() : ((IpBanEntry) a).time();
            long timeB = (b instanceof BanEntry be) ? be.time() : ((IpBanEntry) b).time();
            return Long.compare(timeB, timeA);
        });

        int totalPages = (int) Math.ceil(allBans.size() / (double) BANS_PER_PAGE);
        if (page > totalPages) page = totalPages;

        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("page", String.valueOf(page));
        headerPlaceholders.put("total", String.valueOf(totalPages));
        headerPlaceholders.put("count", String.valueOf(totalBans));
        headerPlaceholders.put("players", String.valueOf(playerBans.size()));
        headerPlaceholders.put("ips", String.valueOf(ipBans.size()));
        sender.sendMessage(lang.get(sender, "banlist.all.header", headerPlaceholders));

        int start = (page - 1) * BANS_PER_PAGE;
        int end = Math.min(start + BANS_PER_PAGE, allBans.size());

        for (int i = start; i < end; i++) {
            Object entry = allBans.get(i);
            if (entry instanceof BanEntry be) {
                sender.sendMessage(formatPlayerBanEntry(sender, be, i + 1));
            } else if (entry instanceof IpBanEntry ie) {
                sender.sendMessage(formatIpBanEntry(sender, ie, i + 1));
            }
        }

        showBanListFooter(sender, page, totalPages);

        sender.sendMessage(lang.get(sender, "banlist.filter_hint"));
    }

    private void showRecentBans(CommandSender sender, int limit) {
        List<BanEntry> playerBans = punishmentManager.getAllBans();
        List<IpBanEntry> ipBans = punishmentManager.getAllIpBans();

        List<Object> allBans = new ArrayList<>();
        allBans.addAll(playerBans);
        allBans.addAll(ipBans);

        allBans.sort((a, b) -> {
            long timeA = (a instanceof BanEntry be) ? be.time() : ((IpBanEntry) a).time();
            long timeB = (b instanceof BanEntry be) ? be.time() : ((IpBanEntry) b).time();
            return Long.compare(timeB, timeA);
        });

        sender.sendMessage(lang.get(sender, "banlist.history.header"));

        int count = 0;
        for (Object entry : allBans) {
            if (count++ >= 10) break;
            if (entry instanceof BanEntry be) {
                sender.sendMessage(formatPlayerBanEntry(sender, be, count));
            } else if (entry instanceof IpBanEntry ie) {
                sender.sendMessage(formatIpBanEntry(sender, ie, count));
            }
        }
    }

    private void showBanListHeader(CommandSender sender, String type, int count, int page, int totalPages) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("type", type);
        placeholders.put("count", String.valueOf(count));
        placeholders.put("page", String.valueOf(page));
        placeholders.put("total", String.valueOf(totalPages));
        sender.sendMessage(lang.get(sender, "banlist." + type + ".header", placeholders));
    }

    private void showBanListFooter(CommandSender sender, int currentPage, int totalPages) {
        if (totalPages <= 1) return;

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("current", String.valueOf(currentPage));
        placeholders.put("total", String.valueOf(totalPages));
        placeholders.put("prev", String.valueOf(currentPage - 1));
        placeholders.put("next", String.valueOf(currentPage + 1));

        sender.sendMessage(lang.get(sender, "banlist.footer", placeholders));
    }

    private String formatPlayerBanEntry(CommandSender sender, BanEntry entry, int index) {
        boolean isTemp = entry.expires() > 0;
        String key = isTemp ? "banlist.entry.player.temp" : "banlist.entry.player.perm";

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("index", String.valueOf(index));
        placeholders.put("player", entry.name());
        placeholders.put("reason", entry.reason());
        placeholders.put("banner", entry.banner());
        placeholders.put("time", formatTimeAgo(entry.time()));
        placeholders.put("expires", isTemp ? formatTimeRemaining(entry.expires()) : "Never");

        return lang.get(sender, key, placeholders).toString();
    }

    private String formatIpBanEntry(CommandSender sender, IpBanEntry entry, int index) {
        boolean isTemp = entry.expires() > 0;
        String key = isTemp ? "banlist.entry.ip.temp" : "banlist.entry.ip.perm";

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("index", String.valueOf(index));
        placeholders.put("ip", entry.ip());
        placeholders.put("reason", entry.reason());
        placeholders.put("banner", entry.banner());
        placeholders.put("time", formatTimeAgo(entry.time()));
        placeholders.put("expires", isTemp ? formatTimeRemaining(entry.expires()) : "Never");

        return lang.get(sender, key, placeholders).toString();
    }

    private String formatTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return "Just now";
    }

    private String formatTimeRemaining(long timestamp) {
        long diff = timestamp - System.currentTimeMillis();
        if (diff <= 0) return "Expired";

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("players", "ips", "all", "history", "help");
            return options.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].matches("(players|ips|all)")) {
            return Arrays.asList("1", "2", "3");
        }
        return Collections.emptyList();
    }
}