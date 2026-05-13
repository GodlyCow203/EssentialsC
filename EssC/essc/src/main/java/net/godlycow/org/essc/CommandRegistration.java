package net.godlycow.org.essc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class CommandRegistration {
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
            removed.getAliases().forEach(alias -> knownCommands.remove(alias.toLowerCase()));
            removed.unregister(commandMap);
            Bukkit.getLogger().info("[EssentialsC] Unregistered command: " + name);
        }
    }

    public static void unregisterCommands(List<String> commands) {
        commands.forEach(CommandRegistration::unregisterCommand);
    }

    public static boolean isRegistered(String name) {
        initReflection();
        if (knownCommands == null) return false;
        return knownCommands.containsKey(name.toLowerCase());
    }

    public static void registerAlias(String alias, Command command) {
        initReflection();
        if (knownCommands == null) return;
        knownCommands.put(alias.toLowerCase(), command);
    }

    public static void syncCommands() {
        EssentialsC plugin = (EssentialsC) JavaPlugin.getProvidingPlugin(CommandRegistration.class);
        plugin.getEssScheduler().runGlobalLater(() -> {
            try {
                Bukkit.getServer().getClass().getMethod("syncCommands").invoke(Bukkit.getServer());
            } catch (Exception e) {
            }
        }, 1L);
    }
}