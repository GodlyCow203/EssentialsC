package net.godlycow.org.essc.expansion.mysql.command;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.expansion.mysql.MySQLExpansion;
import net.godlycow.org.essc.expansion.mysql.config.ExpansionConfig;
import net.godlycow.org.essc.expansion.mysql.storage.ConnectionPool;
import net.godlycow.org.essc.expansion.mysql.sync.EconomySyncService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MySQLCommand extends Command {

    private static final String USER_PERMISSION = "esscmysql.use";
    private static final String ADMIN_PERMISSION = "esscmysql.admin";

    private final MySQLExpansion expansion;
    private final ConnectionPool pool;
    private final ExpansionConfig config;
    private final EconomySyncService economySync;

    public MySQLCommand(MySQLExpansion expansion, EssentialsC plugin, ConnectionPool  pool, ExpansionConfig config,  EconomySyncService economySync) {
        super(plugin, "mysql", null, false, 0, "mysql.help.header");
        this.expansion = expansion;
        this.pool = pool;
        this.config = config;
        this.economySync = economySync;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, args);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "status" -> {
                if (!checkPermission(sender, "esscmysql.use"))
                    return true;
                status(sender);
            }
            case "version" -> {
                if (!checkPermission(sender, "esscmysql.use"))

                    return true;
                version(sender);
            }

            case "help" -> sendHelp ( sender, args);
            case "baltop" -> {
                if (!checkPermission(sender, "esscmysql.use"))
                    return true;
                baltop(sender, args);
            }
            case "reload" -> {
                if (!checkPermission(sender, "esscmysql.admin"))
                    return true;
                reload(sender);
            }
            case "test" -> {
                if (!checkPermission(sender, "esscmysql.admin"))
                    return true;
                test(sender);
            }
            case "push" -> {
                if (!checkPermission(sender, "esscmysql.admin"))
                    return true;
                push(sender, args);
            }
            case "pull" -> {
                if (!checkPermission(sender, "esscmysql.admin"))
                    return true;
                pull(sender, args);
            }
            default -> sendHelp(sender, args);
        }
        return true;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(lang.get(sender, "error.no_permission"));
            return false;
        }
        return true;
    }

    @Override
    protected void sendHelp(CommandSender sender, String[] args) {

        sender.sendMessage(lang.get(sender, "mysql.help.header"));

        if (sender.hasPermission("esscmysql.use")) {
            sender.sendMessage(lang.get(sender, "mysql.help.entry",
                      Map.of("command", "status", "description", lang.getRaw(sender, "mysql.help.status"))));
            sender.sendMessage(lang.get(sender, "mysql.help.entry",

                    Map.of("command", "version", "description", lang.getRaw(sender, "mysql.help.version"))));
            sender.sendMessage(lang.get(sender, "mysql.help.entry",
                    Map.of("command", "baltop", "description", lang.getRaw(sender, "mysql.help.baltop"))));
        }
        if (sender.hasPermission("esscmysql.admin")) {
            sender.sendMessage(lang.get(sender, "mysql.help.entry",
                    Map.of("command", "reload", "description", lang.getRaw(sender, "mysql.help.reload"))));
            sender.sendMessage(lang.get(sender, "mysql.help.entry",
                    Map.of("command", "test", "description", lang.getRaw(sender, "mysql.help.test"))));
            sender.sendMessage(lang.get(sender, "mysql.help.entry",
                    Map.of("command", "push", "description", lang.getRaw(sender, "mysql.help.push"))));
            sender.sendMessage(lang.get(sender, "mysql.help.entry",
                    Map.of("command", "pull", "description", lang.getRaw(sender, "mysql.help.pull"))));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> commands = new ArrayList<>();

            if (sender.hasPermission("esscmysql.use"))
                commands.addAll(List.of("status", "version", "baltop", "help"));

            if (sender.hasPermission("esscmysql.admin"))
                commands.addAll(List.of("reload", "test", "push", "pull"));

            return commands.stream()

                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }

        if (args.length == 2 &&
                (args[0].equalsIgnoreCase("push") || args[0].equalsIgnoreCase("pull"))) {

            String partial = args[1].toLowerCase(Locale.ROOT);
            List<String> result = new ArrayList<>();

            if ("all".startsWith(partial))
                result.add("all");

            for (Player player : Bukkit.getOnlinePlayers()) {

                if (player.getName().toLowerCase(Locale.ROOT).startsWith(partial))
                    result.add(player.getName());
            }



            return result;
        }

        return List.of();
    }


    private void status(CommandSender sender) {

        sender.sendMessage(lang.get(sender, "mysql.status.header"));
        boolean connected = pool.isConnected();

        sender.sendMessage(lang.get(sender, "mysql.status.connection",
                Map.of("state", lang.getRaw(sender, connected ? "mysql.status.connected" : "mysql.status.disconnected"))));
        sender.sendMessage(lang.get(sender, "mysql.status.database",
                Map.of("database", config.getMysqlDatabase(), "host", config.getMysqlHost(), "port", String.valueOf(config.getMysqlPort()))));
        sender.sendMessage(lang.get(sender, "mysql.status.server_id", Map.of("id", config.getServerId())));
        sender.sendMessage(lang.get(sender, "mysql.status.economy",
                Map.of("state", lang.getRaw(sender, economySync.isEnabled() ? "mysql.status.economy_enabled" : "mysql.status.economy_disabled"))));
        sender.sendMessage(lang.get(sender, "mysql.status.synced_players", Map.of("count", String.valueOf(Bukkit.getOnlinePlayers().size()))));

        long uptime = (System.currentTimeMillis() - expansion.getStartTime()) / 1000;
        sender.sendMessage(lang.get(sender, "mysql.status.uptime", Map.of("uptime", formatUptime(uptime))));
    }

    private void version(CommandSender sender) {

        sender.sendMessage(lang.get(sender, "mysql.version.header"));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "Version", "value", expansion.getDescription().getVersion())));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "MySQL Host", "value", config.getMysqlHost() + ":" + config.getMysqlPort())));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "Database", "value", config.getMysqlDatabase())));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "Table Prefix", "value",  config.getTablePrefix())));
        sender.sendMessage(lang.get(sender, "mysql.version.server_id", Map.of("id", config.getServerId())));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "Economy Sync", "value", economySync.isEnabled() ? "enabled" : "disabled")));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "Homes", "value", config.isHomesEnabled() ? "enabled" : "disabled")));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "Warps", "value", config.isWarpsEnabled() ? "enabled" : "disabled")));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "Nicknames", "value", config.isNicknamesEnabled()  ? "enabled" : "disabled")));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "Punishments", "value", config.isPunishmentsEnabled() ? "enabled" : "disabled")));
        sender.sendMessage(lang.get(sender, "mysql.version.line", Map.of("name", "Kits", "value",  config.isKitsEnabled() ? "enabled" : "disabled")));
    }

    private void reload(CommandSender sender) {
        sender.sendMessage(lang.get(sender, "mysql.reload.start"));
        boolean success = expansion.reloadExpansion();

        if (success) {
            sender.sendMessage(lang.get(sender, "mysql.reload.success"));
        } else {
            sender.sendMessage(lang.get(sender, "mysql.reload.failed", Map.of("reason", "could not (re)connect to MySQL")));
        }
    }

    private void test(CommandSender sender) {
        if (!pool.isConnected()) {
            sender.sendMessage(lang.get(sender, "mysql.error.not_configured"));
            return;
        }
        sender.sendMessage(lang.get(sender, "mysql.test.start"));
        pool.testConnection().whenComplete((latency, error) -> Bukkit.getScheduler().runTask(expansion, () -> {
            if (error != null) {
                sender.sendMessage(lang.get(sender, "mysql.test.failed",
                        Map.of("reason", error.getCause() == null
                                ? error.getMessage() : error.getCause().getMessage())));
            } else {
                sender.sendMessage(lang.get(sender, "mysql.test.success",
                        Map.of("latency", String.valueOf(latency))));
            }
        }));
    }

    private void push(CommandSender sender, String[] args) {
        if (!economySync.isEnabled()) {
            sender.sendMessage(lang.get(sender, "mysql.error.economy_disabled"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(lang.get(sender, "mysql.error.usage", Map.of("usage", "/mysql push <player|all>")));
            return;
        }
        String target = args[1];
        if (target.equalsIgnoreCase("all")) {
            sender.sendMessage(lang.get(sender, "mysql.push.all_start"));
            economySync.forcePushAll().whenComplete((count, error) -> Bukkit.getScheduler().runTask(expansion, () ->
                    sender.sendMessage(lang.get(sender, "mysql.push.all_done", Map.of("count", String.valueOf(count == null ? 0 : count))))));
            return;
        }

        Player player = Bukkit.getPlayer(target);
        if (player == null) {
            sender.sendMessage(lang.get(sender, "mysql.error.player_not_found", Map.of("name", target)));
            return;
        }
        UUID uuid = player.getUniqueId();
        var manager = plugin.getEconomyManager();
        if (manager == null) {
            sender.sendMessage(lang.get(sender, "mysql.error.economy_disabled"));
            return;
        }
        var  balance = manager.getCachedBalance(uuid);
        sender.sendMessage(lang.get(sender, "mysql.push.start", Map.of("name", player.getName())));
        economySync.push(uuid, player.getName(), balance).whenComplete((v, error) -> Bukkit.getScheduler().runTask(expansion, () ->
                sender.sendMessage(lang.get(sender, "mysql.push.done", Map.of("name", player.getName(), "balance", manager.format(balance))))));
    }

    private void pull(CommandSender sender, String[] args) {
        if (!economySync.isEnabled()) {
            sender.sendMessage(lang.get(sender, "mysql.error.economy_disabled"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(lang.get(sender, "mysql.error.usage", Map.of("usage", "/mysql pull <player|all>")));
            return;
        }
        String target = args[1];
        if (target.equalsIgnoreCase("all")) {
            sender.sendMessage(lang.get(sender, "mysql.pull.all_start"));
            economySync.forcePullAll().whenComplete((count, error) -> Bukkit.getScheduler().runTask(expansion, () ->
                    sender.sendMessage(lang.get(sender, "mysql.pull.all_done", Map.of("count", String.valueOf(count == null ? 0 : count))))));
            return;
        }

        Player player = Bukkit.getPlayer(target);
        if (player == null) {
            sender.sendMessage(lang.get(sender, "mysql.error.player_not_found", Map.of("name", target)));
            return;
        }
        UUID uuid = player.getUniqueId();
        var manager = plugin.getEconomyManager();
        if (manager == null) {
            sender.sendMessage(lang.get(sender, "mysql.error.economy_disabled"));
            return;
        }
        sender.sendMessage(lang.get(sender, "mysql.pull.start", Map.of("name", player.getName())));
        economySync.pullSingle(player).whenComplete((balance, error) -> Bukkit.getScheduler().runTask(expansion, () -> {
            if (error != null || balance == null) {
                sender.sendMessage(lang.get(sender, "mysql.pull.not_found", Map.of("name", player.getName())));
            } else {
                sender.sendMessage(lang.get(sender, "mysql.pull.done", Map.of("name", player.getName(), "balance", manager.format(balance))));
            }
        }));
    }

    private void baltop(CommandSender sender, String[] args) {
        if (!economySync.isEnabled()) {
            sender.sendMessage(lang.get(sender, "mysql.error.economy_disabled"));
            return;
        }
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(lang.get(sender, "mysql.error.page_invalid", Map.of("input", args[1])));
                return;
            }
        }
        int pageSize = config.getBaltopPageSize();
        final int requestedPage = page;
        int limit = pageSize * page + pageSize;

        economySync.getNetworkTop(limit).whenComplete((entries, error) -> Bukkit.getScheduler().runTask(expansion, () -> {
            if (error != null || entries == null || entries.isEmpty()) {
                sender.sendMessage(lang.get(sender, "mysql.baltop.empty"));
                return;
            }
            int pages = Math.max(requestedPage, (int) Math.ceil((double) entries.size() / pageSize));
            if (entries.size() <= pageSize * (requestedPage - 1)) {
                sender.sendMessage(lang.get(sender, "mysql.baltop.page_out_of_range", Map.of("page", String.valueOf(requestedPage), "pages", String.valueOf(pages))));
                return;
            }
            sender.sendMessage(lang.get(sender, "mysql.baltop.header", Map.of("page", String.valueOf(requestedPage), "pages", String.valueOf(pages))));
            int from = pageSize * (requestedPage - 1);
            int to = Math.min(from + pageSize, entries.size());
            var manager = plugin.getEconomyManager();
            for (int i = from; i < to; i++) {
                var entry = entries.get(i);
                String formatted = manager != null ? manager.format(entry.balance()) : entry.balance().toPlainString();
                sender.sendMessage(lang.get(sender, "mysql.baltop.entry", Map.of(
                        "rank", String.valueOf(i + 1),
                        "player", entry.name() == null ? entry.uuid().toString() : entry.name(),
                        "balance", formatted)));
            }
        }));
    }

    private String formatUptime(long seconds) {
        long d = seconds / 86400;
        long h = (seconds % 86400) / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return d + "d " + h + "h " + m + "m " + s + "s";
    }
}
