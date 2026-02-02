package net.godlycow.org.essc.bootstrap;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.*;
import net.godlycow.org.essc.command.admin.RealNameCommand;
import net.godlycow.org.essc.command.home.*;
import net.godlycow.org.essc.command.inv.*;
import net.godlycow.org.essc.command.kit.*;
import net.godlycow.org.essc.command.player.*;
import net.godlycow.org.essc.command.server.UptimeCommand;
import net.godlycow.org.essc.command.spawn.*;
import net.godlycow.org.essc.command.tpa.*;
import net.godlycow.org.essc.util.CommandRegistrationUtil;
import org.bukkit.command.PluginCommand;

public class CommandRegistrar {

    private final EssentialsC plugin;

    public CommandRegistrar(EssentialsC plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        registerCommands();
        CommandRegistrationUtil.syncCommands();
    }

    private void registerCommands() {
        register("heal", new HealCommand(plugin));
        register("essc", new EsscCommand(plugin));
        register("feed", new FeedCommand(plugin));
        register("ping", new PingCommand(plugin));
        register("fly", new FlyCommand(plugin));
        register("god", new GodCommand(plugin));
        register("vanish", new VanishCommand(plugin));
        register("repair", new RepairCommand(plugin));
        register("rename", new RenameCommand(plugin));
        register("scoreboard", new ScoreboardCommand(plugin));
        register("tpa", new TPACommand(plugin));
        register("tpahere", new TPAHereCommand(plugin));
        register("tpaccept", new TPAcceptCommand(plugin));
        register("tpdeny", new TPADenyCommand(plugin));
        register("tpcancel", new TPACancelCommand(plugin));
        register("tpaignore", new TPAIgnoreCommand(plugin));
        register("tpatoggle", new TPAToggleCommand(plugin));
        register("tpaqueue", new TPAQueueCommand(plugin));
        register("sethome", new SetHomeCommand(plugin));
        register("home", new HomeCommand(plugin));
        register("delhome", new DelHomeCommand(plugin));
        register("homes", new HomesCommand(plugin));
        register("spawn", new SpawnCommand(plugin));
        register("setspawn", new SetSpawnCommand(plugin));
        register("invsee", new InvseeCommand(plugin));
        register("clearinventory", new ClearInventoryCommand(plugin));
        register("enderchest", new EnderChestCommand(plugin));
        register("endersee", new EnderSeeCommand(plugin));
        register("speed", new SpeedCommand(plugin));
        register("anvil", new AnvilCommand(plugin));
        register("craftingtable", new CraftingTableCommand(plugin));
        register("back", new BackCommand(plugin));
        register("kit", new KitCommand(plugin));
        register("kits", new KitsCommand(plugin));
        if (plugin.getConfigManager().isNickEnabled()) {
            register("nick", new NickCommand(plugin));
            register("realname", new RealNameCommand(plugin));
        }
        register("realname", new RealNameCommand(plugin));
        register("playtime", new PlaytimeCommand(plugin));
        register("uptime", new UptimeCommand(plugin));
    }

    private void register(String name, Command command) {
        PluginCommand pluginCommand = plugin.getCommand(name);
        if (pluginCommand == null) {
            plugin.getLogger().warning("Command '" + name + "' not found in plugin.yml");
            return;
        }

        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

        for (String alias : command.getAliases()) {
            PluginCommand aliasCmd = plugin.getCommand(alias);
            if (aliasCmd != null) {
                aliasCmd.setExecutor(command);
                aliasCmd.setTabCompleter(command);
            }
        }
    }
}
