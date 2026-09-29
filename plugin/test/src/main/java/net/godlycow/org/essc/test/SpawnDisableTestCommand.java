package net.godlycow.org.essc.test;

import net.godlycow.org.essc.CommandRegistration;
import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

public class SpawnDisableTestCommand implements CommandExecutor {

    MiniMessage mini = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        var essc = Bukkit.getPluginManager().getPlugin("EssentialsC");

        if (essc == null) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#F54927>Error<dark_gray>] <white>EssC not found"));

            return true;
        }

        if (!(essc instanceof EssentialsC plugin)) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#F54927>Error<dark_gray>] <white>Instance not found"));
            return true;
        }

        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));

        boolean configEnabled = plugin.getConfigManager().isSpawnEnabled();

        if (configEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Spawn is <green>enabled <white>in config"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Spawn is <red>disabled <white>in config"));
        }

        if (plugin.getSpawnManager() == null) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>SpawnManager is <red>null"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>SpawnManager is <green>not null"));
        }

        String[] spawnCommands = {
                "spawn", "setspawn"
        };


        boolean anySpawnCommandRegistered = false;

        for (String spawnCommand : spawnCommands) {
            if (CommandRegistration.isOwnedByE(spawnCommand)) {
                sender.sendMessage(mini.deserialize(
                        "<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/<command> command is <green>registered",
                        Placeholder.parsed("command", spawnCommand)));
                anySpawnCommandRegistered = true;

            } else {
                sender.sendMessage(mini.deserialize(
                        "<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/<command> command is <red>not registered",
                        Placeholder.parsed("command", spawnCommand)));
            }
        }

        boolean foundSpawnListener = false;


        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
                if (listener.getPlugin().equals(plugin)) {
                    String name = listener.getListener().getClass().getSimpleName();
                    if (name.contains("Spawn")) {
                        sender.sendMessage(mini.deserialize(
                                "<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Registered listener <listener>",
                                Placeholder.parsed("listener", name)));
                        foundSpawnListener = true;
                    }
                }
            }
        }


        if (!foundSpawnListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Did not find any registered Spawn listeners"));
        }


        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));
        if (!configEnabled && plugin.getSpawnManager() == null
                && !anySpawnCommandRegistered && !foundSpawnListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Spawn is fully <red>disabled"));
        } else if (configEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Spawn is <green>enabled <white>in config"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Spawn is <yellow>partially active"));
        }
        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));

        return true;
    }
}
