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

public class BackDisableTestCommand implements CommandExecutor {

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

        boolean configEnabled = plugin.getConfigManager().isBackEnabled();


        if (configEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Back is <green>enabled <white>in config"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Back is <red>disabled <white>in config"));
        }

        if (plugin.getBackManager() == null) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>BackManager is <red>null"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>BackManager is <green>not null"));
        }

        String[] backCommands = {"back", "dback"};

        boolean anyBackCommandRegistered = false;

        for (String backCommand : backCommands) {

            if (CommandRegistration.isOwnedByE(backCommand)) {
                sender.sendMessage(mini.deserialize(
                        "<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/<command> command is <green>registered",
                        Placeholder.parsed("command", backCommand)));
                anyBackCommandRegistered = true;

            } else {
                sender.sendMessage(mini.deserialize(
                        "<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/<command> command is <red>not registered",
                        Placeholder.parsed("command", backCommand)));
            }
        }


        boolean foundBackListener = false;

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
                if (listener.getPlugin().equals(plugin)) {
                    String name = listener.getListener().getClass().getSimpleName();
                    if (name.contains("Back")) {
                        sender.sendMessage(mini.deserialize(
                                "<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Registered listener <listener>",
                                Placeholder.parsed("listener", name)));

                        foundBackListener = true;
                    }
                }
            }
        }

        if (!foundBackListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Did not find any registered Back listeners"));
        }



        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));

        if (!configEnabled && plugin.getBackManager() == null
                && !anyBackCommandRegistered && !foundBackListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Back is fully <red>disabled"));
        } else if (configEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Back is <green>enabled <white>in config"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Back is <yellow>partially active"));
        }

        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));


        return true;
    }
}
