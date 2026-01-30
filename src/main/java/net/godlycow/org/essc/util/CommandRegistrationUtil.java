package net.godlycow.org.essc.util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class CommandRegistrationUtil {
    private static CommandMap commandMap;
    private static Map<String, Command> knownCommands;

    @SuppressWarnings("unchecked")
    private static void initReflection() {
        if (commandMap != null) return;

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            if (commandMap instanceof SimpleCommandMap simpleCommandMap) {
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                knownCommands = (Map<String, Command>) knownCommandsField.get(simpleCommandMap);
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to access CommandMap", e);
        }
    }

    public static void unregisterCommand(String name) {
        initReflection();
        if (knownCommands == null) return;

        Command removed = knownCommands.remove(name.toLowerCase());
        if (removed != null) {
            // Also remove aliases
            removed.getAliases().forEach(alias -> knownCommands.remove(alias.toLowerCase()));
            removed.unregister(commandMap);
            Bukkit.getLogger().info("[EssentialsC] Unregistered command: " + name);
        }
    }

    public static void unregisterCommands(List<String> commands) {
        commands.forEach(CommandRegistrationUtil::unregisterCommand);
    }

    public static void syncCommands() {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(CommandRegistrationUtil.class), () -> {
            try {
                Bukkit.getServer().getClass().getMethod("syncCommands").invoke(Bukkit.getServer());
            } catch (Exception e) {
            }
        }, 1L);
    }
}