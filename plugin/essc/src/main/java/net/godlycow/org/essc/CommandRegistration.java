package net.godlycow.org.essc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private static boolean isOwnedByEssentials(Command command) {
        if (command instanceof PluginCommand pc) {
            return pc.getPlugin() instanceof EssentialsC;
        }
        return false;
    }

    public static void unregisterIfOwnedByE(String name) {
        initReflection();
        if (knownCommands == null)
            return;

        String key = name.toLowerCase(Locale.ROOT);
        Command existing = knownCommands.get(key);
        if (existing == null || !isOwnedByEssentials(existing)) {
            return;
        }

        List<String> labelsToRestore = new ArrayList<>();

        String bareName = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
        labelsToRestore.add(bareName);

        for (String alias : new ArrayList<>(existing.getAliases()))
        {
            labelsToRestore.add(alias.toLowerCase(Locale.ROOT));
        }


        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, Command> entry : knownCommands.entrySet())
        {
            if (entry.getValue() == existing)
            {
                keysToRemove.add(entry.getKey());
            }
        }


        for (String removeKey : keysToRemove)
        {
            knownCommands.remove(removeKey, existing);
        }

        try {
            existing.unregister(commandMap);
        } catch (Exception ignored) {}


        for (String label : labelsToRestore)
        {
            String bare = label.toLowerCase(Locale.ROOT);
            if (bare.contains(":"))
            {
                bare = bare.substring(bare.indexOf(':') + 1);
            }

            if (bare.isEmpty() || knownCommands.containsKey(bare))
                continue;

            Command candidate = knownCommands.get("minecraft:" + bare);
            String candidateKey = "minecraft:" + bare;


            if (candidate == null)
            {
                for (Map.Entry<String, Command> entry : knownCommands.entrySet())
                {
                    String entryKey =  entry.getKey();
                    if (entryKey.endsWith(":" + bare)) {
                        if (entryKey.startsWith("essentialsc:"))
                            continue;
                        if (isOwnedByEssentials(entry.getValue()))
                            continue;
                        candidate = entry.getValue();
                        candidateKey = entryKey;


                        break;
                    }
                }
            }

            if (candidate != null)
            {
                knownCommands.put(bare, candidate);
                try {
                    candidate.setLabel(bare);
                } catch (Exception ignored) {}
                Bukkit.getLogger().info("[EssentialsC] Restored command /" + bare + " from " + candidateKey);
            }
        }

        Bukkit.getLogger().info("[EssentialsC] Unregistered command: " + name);
    }

    public static void unregisterCommand(String name) {
        initReflection();
        if (knownCommands == null) return;

        String key = name.toLowerCase(Locale.ROOT);

        if (key.contains(":")) {
            return;
        }

        Command removed = knownCommands.remove(key);
        if (removed != null)
        {
            for (String alias : new ArrayList<>(removed.getAliases()))
            {
                knownCommands.remove(alias.toLowerCase(Locale.ROOT), removed);
            }
            try {
                removed.unregister(commandMap);
            } catch (Exception ignored) {}


            Bukkit.getLogger().info("[EssentialsC] Unregistered command: " + name);
        }
    }

    public static void unregisterCommands(List<String> commands) {
        commands.forEach(CommandRegistration::unregisterIfOwnedByE);
    }

    public static boolean isRegistered(String name) {
        initReflection();
        if (knownCommands == null) return false;
        return knownCommands.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public static void registerAlias(String alias, Command command) {
        initReflection();
        if (knownCommands == null) return;
        knownCommands.put(alias.toLowerCase(Locale.ROOT), command);
    }

    public static List<String> getEssentialsCCommands() {
        initReflection();
        if (knownCommands == null) return List.of();
        return knownCommands.values().stream()
                .filter(cmd -> cmd instanceof PluginCommand pc && pc.getPlugin() instanceof EssentialsC)
                .map(Command::getName)
                .distinct()
                .sorted()
                .toList();
    }

    public static void syncCommands() {
        EssentialsC plugin = JavaPlugin.getPlugin(EssentialsC.class);
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, ignored -> {
            try {
                Bukkit.getServer().getClass().getMethod("syncCommands").invoke(Bukkit.getServer());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 1L);
    }
}