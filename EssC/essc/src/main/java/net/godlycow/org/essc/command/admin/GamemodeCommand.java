package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamemodeCommand extends Command {

    private static final Map<String, GameMode> NAME_TO_MODE = Map.of(
            "gms",  GameMode.SURVIVAL,
            "gmc",  GameMode.CREATIVE,
            "gmsp", GameMode.SPECTATOR,
            "gma",  GameMode.ADVENTURE
    );

    private static final Map<String, String> NAME_TO_PERMISSION = Map.of(
            "gm",   "essentialsc.gamemode",
            "gms",  "essentialsc.gamemode.survival",
            "gmc",  "essentialsc.gamemode.creative",
            "gmsp", "essentialsc.gamemode.spectator",
            "gma",  "essentialsc.gamemode.adventure"
    );

    private final GameMode fixedMode;

    public GamemodeCommand(EssentialsC plugin, String name) {
        super(plugin, name, NAME_TO_PERMISSION.get(name), true, 0, "command.usage." + name);
        this.fixedMode = NAME_TO_MODE.get(name);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        GameMode targetMode;
        Player target;

        if (fixedMode != null) {
            targetMode = fixedMode;
            target = resolveTarget(player, args, 0);
        } else {
            if (args.length < 1) {
                sendUsage(sender);
                return true;
            }
            targetMode = parseGameMode(args[0]);
            if (targetMode == null) {
                player.sendMessage(lang.get(player, "gamemode.invalid", Map.of("input", args[0])));
                return true;
            }
            target = resolveTarget(player, args, 1);
        }

        if (target == null) {
            return true;
        }

        if (target != player && !player.hasPermission(getPermission() + ".others")) {
            player.sendMessage(lang.get(player, "error.no_permission"));
            return true;
        }

        target.setGameMode(targetMode);

        String modeName = gameModeName(targetMode);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode", modeName);
        placeholders.put("target", target.getName());

        if (target == player) {
            player.sendMessage(lang.get(player, "gamemode.changed", placeholders));
        } else {
            player.sendMessage(lang.get(player, "gamemode.changed.other", placeholders));
            Map<String, String> targetPlaceholders = new HashMap<>();
            targetPlaceholders.put("mode", modeName);
            targetPlaceholders.put("player", player.getName());
            target.sendMessage(lang.get(target, "gamemode.changed.by", targetPlaceholders));
        }

        plugin.debug(player.getName() + " set gamemode of " + target.getName() + " to " + targetMode.name());
        return true;
    }

    private Player resolveTarget(Player sender, String[] args, int argIndex) {
        if (args.length > argIndex) {
            Player found = plugin.getServer().getPlayer(args[argIndex]);
            if (found == null) {
                sender.sendMessage(lang.get(sender, "error.player_not_found", Map.of("player", args[argIndex])));
                return null;
            }
            return found;
        }
        return sender;
    }

    private GameMode parseGameMode(String input) {
        return switch (input.toLowerCase()) {
            case "survival", "s", "0" -> GameMode.SURVIVAL;
            case "creative", "c", "1" -> GameMode.CREATIVE;
            case "adventure", "a", "2" -> GameMode.ADVENTURE;
            case "spectator", "sp", "3" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    private String gameModeName(GameMode mode) {
        return switch (mode) {
            case SURVIVAL -> "Survival";
            case CREATIVE -> "Creative";
            case ADVENTURE -> "Adventure";
            case SPECTATOR -> "Spectator";
        };
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (fixedMode != null) {
            if (args.length == 1 && sender.hasPermission(getPermission() + ".others")) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .toList();
            }
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> modes = Arrays.asList("survival", "creative", "adventure", "spectator");
            return modes.stream()
                    .filter(m -> m.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && sender.hasPermission(getPermission() + ".others")) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}