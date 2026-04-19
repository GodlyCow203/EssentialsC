package net.godlycow.org.essc.home.gui;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.softwares.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SignHandler {

    private final EssentialsC plugin;
    private final GuiManager manager;

    private final Map<UUID, PendingInput> pending = new ConcurrentHashMap<>();
    private final Map<UUID, SchedulerTask> timeouts = new ConcurrentHashMap<>();
    private final Map<UUID, Location> originalBlocks = new ConcurrentHashMap<>();

    public SignHandler(EssentialsC plugin, GuiManager manager) {
        this.plugin = plugin;
        this.manager = manager;

        plugin.getServer().getPluginManager().registerEvents(new SignListener(), plugin);
    }

    public void openRenameSign(Player player, String currentName, UUID targetUuid) {
        openSign(player, InputType.RENAME, currentName, targetUuid, new String[]{
                "^^^^^^^^^^^^^^^",
                "Enter new name:",
                currentName,
                ""
        });
    }

    public void openCreateSign(Player player) {
        openSign(player, InputType.CREATE, null, player.getUniqueId(), new String[]{
                "^^^^^^^^^^^^^^^",
                "Enter home name:",
                "",
                ""
        });
    }

    public void openSearchSign(Player player) {
        openSign(player, InputType.SEARCH_HOMES, null, player.getUniqueId(), new String[]{
                "^^^^^^^^^^^^^^^",
                "Search homes:",
                "",
                ""
        });
    }

    public void openPlayerSearchSign(Player player) {
        openSign(player, InputType.SEARCH_PLAYERS, null, player.getUniqueId(), new String[]{
                "^^^^^^^^^^^^^^^",
                "Search players:",
                "",
                ""
        });
    }

    private void openSign(Player player, InputType type, String targetHome, UUID targetUuid, String[] lines) {
        cancel(player);

        Location loc = player.getLocation().clone();
        loc.setY(Math.max(-64, loc.getY() - 5));

        Block block = loc.getBlock();
        originalBlocks.put(player.getUniqueId(), loc.clone());

        block.setType(Material.OAK_SIGN);
        if (block.getState() instanceof Sign sign) {
            for (int i = 0; i < 4; i++) {
                sign.getSide(Side.FRONT).setLine(i, lines[i]);
            }
            sign.update();

            pending.put(player.getUniqueId(), new PendingInput(type, targetHome, targetUuid));

            player.openSign(sign, Side.FRONT);

            SchedulerTask task = plugin.getEssScheduler().runForEntityLater(player, () -> {
                if (pending.remove(player.getUniqueId()) != null) {
                    restoreBlock(player);
                    player.sendMessage(plugin.getLanguageManager().get(player, "home.gui.input_timeout"));
                    returnToPrevious(player, type, targetUuid);
                }
            }, 600L);

            timeouts.put(player.getUniqueId(), task);
        }
    }

    private void handleInput(Player player, String[] lines) {
        PendingInput input = pending.remove(player.getUniqueId());
        SchedulerTask task = timeouts.remove(player.getUniqueId());
        if (task != null) task.cancel();

        restoreBlock(player);

        if (input == null) return;

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line != null && !line.isEmpty() && !line.contains("^")) {
                if (sb.length() > 0) sb.append("_");
                sb.append(line.trim());
            }
        }

        String result = sb.toString();

        if (result.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "home.gui.input_cancelled"));
            returnToPrevious(player, input.type(), input.targetUuid());
            return;
        }

        switch (input.type()) {
            case RENAME -> manager.handleRename(player, input.targetHome(), result, input.targetUuid());
            case CREATE -> manager.handleCreate(player, result);
            case SEARCH_HOMES -> {
                manager.openHomeList(player, 0, GuiManager.SortMode.ALPHABETICAL, result);
            }
            case SEARCH_PLAYERS -> {
                manager.handlePlayerSearch(player, result);
            }
        }
    }

    private void returnToPrevious(Player player, InputType type, UUID targetUuid) {
        switch (type) {
            case RENAME -> {
                if (targetUuid.equals(player.getUniqueId())) {
                    manager.openHomeList(player);
                } else {
                    manager.openPlayerHomes(player, targetUuid, Bukkit.getOfflinePlayer(targetUuid).getName());
                }
            }
            case CREATE -> manager.openHomeList(player);
            case SEARCH_HOMES -> manager.openHomeList(player);
            case SEARCH_PLAYERS -> manager.openPlayerManagementTypeSelection(player);
        }
    }

    private void restoreBlock(Player player) {
        Location loc = originalBlocks.remove(player.getUniqueId());
        if (loc != null) {
            Block block = loc.getBlock();
            block.setType(Material.AIR);
        }
    }

    public void cancel(Player player) {
        PendingInput input = pending.remove(player.getUniqueId());
        SchedulerTask task = timeouts.remove(player.getUniqueId());
        if (task != null) task.cancel();
        restoreBlock(player);
    }

    public void shutdown() {
        for (UUID uuid : pending.keySet()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) cancel(p);
        }
    }

    private class SignListener implements org.bukkit.event.Listener {
        @EventHandler
        public void onSignChange(org.bukkit.event.block.SignChangeEvent event) {
            Player player = event.getPlayer();
            if (!pending.containsKey(player.getUniqueId())) return;

            String[] lines = new String[4];
            for (int i = 0; i < 4; i++) {
                lines[i] = event.getLine(i);
            }

            event.setCancelled(true);
            handleInput(player, lines);
        }
    }

    private record PendingInput(InputType type, String targetHome, UUID targetUuid) {

    }

    public enum InputType {
        RENAME, CREATE, SEARCH_HOMES, SEARCH_PLAYERS
    }
}