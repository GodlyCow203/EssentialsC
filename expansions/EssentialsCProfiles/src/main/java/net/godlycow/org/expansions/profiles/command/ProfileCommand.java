package net.godlycow.org.expansions.profiles.command;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.expansions.profiles.EssentialsCProfiles;
import net.godlycow.org.expansions.profiles.gui.ProfileGui;
import net.godlycow.org.expansions.profiles.messages.MessagesManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final EssentialsCProfiles plugin;
    private final EssentialsC essc;
    private final ProfileGui gui;
    private final MessagesManager msg;

    public ProfileCommand(EssentialsCProfiles plugin, EssentialsC essc,
                          ProfileGui gui, MessagesManager msg) {
        this.plugin = plugin;
        this.essc   = essc;
        this.gui    = gui;
        this.msg    = msg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("essentialscprofiles.reload")) {
                sender.sendMessage(msg.get("commands.no-permission"));
                return true;
            }
            msg.load();
            sender.sendMessage(MM.deserialize(
                    "<#A6E3A1>EssentialsCProfiles messages reloaded."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg.get("commands.players-only"));
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("essentialscprofiles.profile")) {
                player.sendMessage(msg.get("commands.no-permission"));
                return true;
            }
            gui.open(player, player, false);
            return true;
        }

        if (!player.hasPermission("essentialscprofiles.profile.others")) {
            player.sendMessage(msg.get("commands.no-permission-others"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getPlayerExact(targetName) != null
                ? Bukkit.getPlayerExact(targetName)
                : Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(msg.get("commands.player-not-found", "player", targetName));
            return true;
        }

        gui.open(player, target, false);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> completions = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> sender.hasPermission("essentialscprofiles.profile.others"))
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
            if ("reload".startsWith(partial) && sender.hasPermission("essentialscprofiles.reload")) {
                completions.add("reload");
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
