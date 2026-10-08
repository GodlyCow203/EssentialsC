package net.godlycow.org.essc.modules;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeManager implements Listener {


    private final EssentialsC plugin;
    private final Set<UUID> frozen = ConcurrentHashMap.newKeySet();
    private final Map<UUID, org.bukkit.Location> anchors = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastBlockedMessage = new ConcurrentHashMap<>();

    private volatile boolean allowLook = true;
    private volatile boolean blockTeleport = true;
    private volatile boolean blockDropPickup = true;
    private volatile boolean blockInventory = true;
    private volatile boolean blockBreakPlace = true;
    private volatile boolean blockInteract = true;
    private volatile boolean persistOnReconnect = true;
    private volatile Set<String> allowedCommands = ConcurrentHashMap.newKeySet();

    public FreezeManager(EssentialsC plugin) {
        this.plugin = plugin;
        loadConfig();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.debug("Freeze Manager successfully initialized");
    }

    public void reload() {
        loadConfig();
        plugin.debug("Freeze config entries reloaded");
    }

    private void loadConfig()
    {
        this.allowLook = plugin.getConfigManager().isFreezeAllowLook();
        this.blockTeleport = plugin.getConfigManager().isFreezeBlockTeleport();
        this.blockDropPickup = plugin.getConfigManager().isFreezeBlockDropPickup();
        this.blockInventory = plugin.getConfigManager().isFreezeBlockInventory();
        this.blockBreakPlace = plugin.getConfigManager().isFreezeBlockBreakPlace();
        this.blockInteract = plugin.getConfigManager().isFreezeBlockInteract();
        this.persistOnReconnect = plugin.getConfigManager().isFreezePersistOnReconnect();
        Set<String> allowed = ConcurrentHashMap.newKeySet();
        for (String cmd : plugin.getConfigManager().getFreezeAllowedCommands()) {
            String clean = cmd.toLowerCase().trim();

            if (clean.startsWith("/"))
                clean = clean.substring(1);
            if (!clean.isEmpty())
                allowed.add(clean.split(" ")[0]);
        }

        this.allowedCommands = allowed;
    }


    public boolean isFrozen(UUID uuid) {
        if (frozen.contains(uuid))
            return true;

        return plugin.getUserManager() != null && plugin.getUserManager().isFrozen(uuid);
    }

    public boolean isFrozen(Player player) {
        return player != null && isFrozen(player.getUniqueId());
    }

    public boolean isExempt(Player player) {
        return player != null && player.hasPermission("essentialsc.freeze.bypass");
    }



    public void freeze(Player target) {
        boolean added = frozen.add(target.getUniqueId());

        anchors.put(target.getUniqueId(), target.getLocation().clone());
        if (plugin.getUserManager() != null) {
            plugin.getUserManager().setFrozen(target.getUniqueId(), true);
        }
        if (added) {
            plugin.debug("Froze " + target.getName());
        }
    }

    public void unfreeze(Player target) {
        unfreeze(target.getUniqueId());
    }

    public void unfreeze(UUID uuid) {
        frozen.remove(uuid);
        anchors.remove(uuid);
        if (plugin.getUserManager() != null) {
            plugin.getUserManager().setFrozen(uuid, false);
        }
    }

    public void shutdown() {
        HandlerList.unregisterAll(this);
        plugin.debug("FreezeManager shut down");
    }

    private void sendBlockedMessage(Player player, String langKey) {
        sendBlockedMessage(player, langKey, null);
    }

    private void sendBlockedMessage(Player player, String langKey, Map<String, String>  placeholders) {

        long now = System.currentTimeMillis();
        Long last = lastBlockedMessage.get(player.getUniqueId());

        if (last != null && now - last < 2000L)
            return;
        lastBlockedMessage.put(player.getUniqueId(), now);
        if (placeholders == null)
        {
            player.sendMessage(plugin.getLanguageManager().get(player, langKey));
        }
        else
        {
            player.sendMessage(plugin.getLanguageManager().get(player, langKey, placeholders));
        }
    }



    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isFrozen(player))
            return;
        if (event.getTo() == null)
        {
            return;
        }

        var from = event.getFrom();
        var to = event.getTo();

        boolean moved = Double.compare(from.getX(), to.getX()) != 0
                || Double.compare(from.getY(), to.getY()) != 0
                || Double.compare(from.getZ(), to.getZ()) != 0;
        boolean lookChanged = Float.compare(from.getYaw(), to.getYaw()) != 0
                || Float.compare(from.getPitch(), to.getPitch()) != 0;


        if (!moved && (allowLook || !lookChanged))
            return;


        Location anchor = anchors.computeIfAbsent(
                player.getUniqueId(), k -> from.clone());
        Location locked = anchor.clone();
        if (allowLook) {
            locked.setYaw(to.getYaw());
            locked.setPitch(to.getPitch());
        }


        event.setCancelled(true);
        event.setTo(locked);

        player.teleportAsync(locked);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!blockTeleport)
            return;
        Player player = event.getPlayer();

        if (!isFrozen(player))
            return;
        event.setCancelled(true);

        sendBlockedMessage(player, "freeze.teleport_blocked");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();
        if (!isFrozen(player))
            return;

        String msg = event.getMessage().toLowerCase();
        if (!msg.startsWith("/"))
            return;
        String cmd = msg.substring(1).split(" ")[0];

        //strip command namespace
        int colon = cmd.indexOf(':');
        if (colon >= 0) cmd = cmd.substring(colon + 1);

        if (allowedCommands.contains(cmd))
            return;

        event.setCancelled(true);
        sendBlockedMessage(player, "freeze.command_blocked", Map.of("command", cmd));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!blockDropPickup)
            return;
        Player player = event.getPlayer();
        if (!isFrozen(player))
            return;
        event.setCancelled(true);

        sendBlockedMessage(player, "freeze.action_blocked");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(PlayerAttemptPickupItemEvent event) {

        if (! blockDropPickup)
            return;
        Player player = event.getPlayer();
        if (!isFrozen(player))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {

        if (!blockInventory)
            return;

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!isFrozen(player))
            return;


        event.setCancelled(true);
        sendBlockedMessage(player, "freeze.action_blocked");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!blockInventory)
            return;

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!isFrozen(player))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {

        if (!blockBreakPlace)
            return;


        Player player = event.getPlayer();

        if (!isFrozen(player))
            return;

        event.setCancelled(true);
        sendBlockedMessage(player, "freeze.action_blocked");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!blockBreakPlace)
            return;
        Player player = event.getPlayer();
        if (!isFrozen(player))
            return;

        event.setCancelled(true);
        sendBlockedMessage(player, "freeze.action_blocked");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!blockInteract)
            return;
        Player player = event.getPlayer();
        if (!isFrozen(player))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {

        if (!blockInteract)

            return;
        Player player = event.getPlayer();
        if (!isFrozen(player))
            return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player))
            return;

        if (!isFrozen(player))
            return;

        event.setCancelled(true);

        sendBlockedMessage(player, "freeze.action_blocked");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        anchors.remove(uuid);
        lastBlockedMessage.remove(uuid);
        if (!persistOnReconnect) {

            unfreeze(uuid);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!persistOnReconnect)
            return;
        if (plugin.getUserManager() == null)
            return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getUserManager().findProfile(uuid).thenAccept(profile ->
        {
            if (profile == null || !profile.isFrozen())
                return;
            player.getScheduler().runDelayed(plugin, task ->
            {

                if (!player.isOnline())
                    return;

                frozen.add(uuid);
                anchors.put(uuid, player.getLocation().clone());
                player.sendMessage(plugin.getLanguageManager().get(player, "freeze.join_still_frozen"));
                plugin.debug("[FreezeManager] re-applied frozen state for " + player.getName());


            }, null, 5L);
        });
    }
}
