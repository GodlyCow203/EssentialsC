package net.godlycow.org.essc.command;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.language.HelpManager;
import net.godlycow.org.essc.language.LanguageManager;
import net.godlycow.org.essc.plugin.economy.EconomyManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.godlycow.org.essc.util.TabCompletionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class Command implements CommandExecutor, TabCompleter {
    protected final EssentialsC plugin;
    protected final LanguageManager lang;

    private final String name;
    private final String permission;
    private final boolean playerOnly;
    private final int minArgs;
    private final String usageKey;
    protected String[] aliases = new String[0];

    public Command(EssentialsC plugin, String name) {
        this(plugin, name, null, false, 0, null);
    }

    public Command(EssentialsC plugin, String name, String permission, boolean playerOnly) {
        this(plugin, name, permission, playerOnly, 0, "command.usage." + name);
    }

    public Command(EssentialsC plugin, String name, String permission, boolean playerOnly, int minArgs) {
        this(plugin, name, permission, playerOnly, minArgs, "command.usage." + name);
    }

    public Command(EssentialsC plugin, String name, String permission, boolean playerOnly, int minArgs, String usageKey) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        this.name = name;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.minArgs = minArgs;
        this.usageKey = usageKey;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command,
                             String label, String[] args) {
        plugin.debug(String.format("Command '%s' | Sender: %s | Args: [%s]",
                name, sender.getName(), String.join(", ", args)));

        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(lang.get(sender, "error.player_only"));
            return true;
        }

        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(lang.get(sender, "error.no_permission"));
            plugin.debug("Denied: " + sender.getName() + " lacks permission " + permission);
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, args);
            return true;
        }

        if (args.length < minArgs) {
            sendUsage(sender);
            return true;
        }

        if (sender instanceof Player player) {
            if (!checkCooldown(player)) {
                return true;
            }

            double cost = plugin.getCommandsConfig().getCost(name);
            if (cost > 0 && plugin.getConfigManager().isEconomyEnabled() && plugin.getEconomyManager() != null) {
                String bypassPermission = plugin.getCommandsConfig().getCostBypassPermission(name);
                boolean hasBypass = bypassPermission != null && player.hasPermission(bypassPermission);

                if (!hasBypass) {
                    chargeAndRun(player, cost, label, args);
                    return true;
                }
            }

            startCooldown(player);
        }

        return runExecute(sender, label, args);
    }

    private boolean checkCooldown(Player player) {
        long cooldownSeconds = plugin.getCommandsConfig().getCooldown(name);
        if (cooldownSeconds <= 0) {
            return true;
        }

        String bypassPermission = plugin.getCommandsConfig().getCooldownBypassPermission(name);
        if (bypassPermission  != null && player.hasPermission(bypassPermission)) {
            return true;
        }

        CommandCooldownManager cooldownManager = plugin.getCommandCooldownManager();
        long remaining = cooldownManager.getRemainingSeconds(player.getUniqueId(),  name);


        if (remaining > 0) {
            player.sendMessage(lang.get(player, "error.command_cooldown",
                    Map.of("seconds", String.valueOf(remaining), "command", name)));
            return false;
        }

        return true;
    }

    private void startCooldown(Player player) {
        long cooldownSeconds = plugin.getCommandsConfig().getCooldown(name);
        if (cooldownSeconds <= 0) {
            return;
        }

        String bypassPermission = plugin.getCommandsConfig().getCooldownBypassPermission(name);
        if (bypassPermission != null && player.hasPermission(bypassPermission)) {
            return;
        }

        plugin.getCommandCooldownManager().setCooldown(player.getUniqueId(), name, cooldownSeconds);
    }

    private void chargeAndRun(Player player,  double cost, String label, String[] args) {
        EconomyManager economy = plugin.getEconomyManager();

        BigDecimal amount = BigDecimal.valueOf(cost).setScale(2, RoundingMode.HALF_UP);
        String formattedAmount = economy.formatPlain(amount);

        String currency = amount.compareTo(BigDecimal.ONE) == 0
                ? plugin.getConfigManager().getCurrencySingular()
                : plugin.getConfigManager().getCurrencyPlural();



        economy.has(player.getUniqueId(), amount).thenAccept(hasFunds -> {
            if (!hasFunds) {

                player.sendMessage(lang.get(player, "error.insufficient_funds",
                        Map.of("amount", formattedAmount, "currency", currency)));
                plugin.debug("Denied: " + player.getName() + " cannot afford /" + name
                        + " (cost " + economy.format(amount) + ")");

                return;
            }

            economy.withdraw(player.getUniqueId(), amount).thenAccept(withdrawn -> {
                if (!withdrawn) {
                    player.sendMessage(lang.get(player, "error.insufficient_funds",
                            Map.of("amount", formattedAmount, "currency", currency)));

                    plugin.debug("Denied: " + player.getName() + " failed to pay /" + name
                            + " (cost " + economy.format(amount) + ")");
                    return;
                }


                player.getScheduler().run(plugin, task -> {
                    if (!player.isOnline()) {
                        economy.deposit(player.getUniqueId(), amount);
                        return;
                    }

                    startCooldown(player);
                    player.sendMessage(lang.get(player, "command.cost_charged",
                            Map.of("amount", formattedAmount, "currency", currency, "command", name)));
                    plugin.debug(player.getName() + " paid " + economy.format(amount) + " to run /" + name);

                    runExecute(player, label, args);
                }, () -> economy.deposit(player.getUniqueId(), amount));
            });
        });

    }

    private boolean runExecute(CommandSender sender, String label, String[] args) {
        try {
            return execute(sender, label, args);
        } catch (Exception e) {
            sender.sendMessage(lang.get(sender, "error.internal"));
            plugin.debug("Exception in " + name + ": " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public boolean execute(CommandSender sender, String label, String[] args) {
        return execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command,
                                      String alias, String[] args) {
        if (permission != null && !sender.hasPermission(permission)) {
            return Collections.emptyList();
        }

        List<String> result;

        if (args.length == 1) {
            List<String> base = tabComplete(sender, alias, args);
            if (base == null) return null;

            String partial = args[0].toLowerCase();
            if ("help".startsWith(partial)) {
                List<String> merged = new ArrayList<>(base);
                if (!merged.contains("help")) merged.add(0, "help");
                result = merged;
            } else {
                result = base;
            }
        } else {
            result = tabComplete(sender, alias, args);
        }

        return TabCompletionUtils.filterCompletions(plugin, sender, result);
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return tabComplete(sender, args);
    }

    protected void sendHelp(CommandSender sender, String[] args) {
        HelpManager helpManager = plugin.getHelpManager();
        if (helpManager == null) {
            sendUsage(sender);
            return;
        }

        String sub = args.length >= 2 ? args[1].toLowerCase() : null;
        helpManager.sendHelp(sender, name, sub);
    }

    protected void sendUsage(CommandSender sender) {
        if (usageKey != null) {
            sender.sendMessage(lang.get(sender, usageKey));
        } else {
            sender.sendMessage(Component.text("Usage: /" + name));
        }
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public String[] getAliases() {
        return aliases;
    }
}