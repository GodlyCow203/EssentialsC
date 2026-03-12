package net.godlycow.org.essc.rtp;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RTPGuiManager implements Listener {

    private final EssentialsC plugin;
    private final RTPManager rtpManager;
    private final MiniMessage miniMessage;

    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    private final Map<UUID, BukkitTask> updateTasks = new HashMap<>();

    private final int[] WORLD_SLOTS = {11, 13, 15};
    private final String[] WORLD_NAMES = {"world", "world_nether", "world_the_end"};
    private final Material[] WORLD_MATERIALS = {
            Material.GRASS_BLOCK,
            Material.NETHERRACK,
            Material.END_STONE
    };
    private final String[] WORLD_KEYS = {"overworld", "nether", "end"};

    public RTPGuiManager(EssentialsC plugin, RTPManager rtpManager) {
        this.plugin = plugin;
        this.rtpManager = rtpManager;
        this.miniMessage = plugin.getMiniMessage();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {

        if (!player.hasPermission("essentialsc.rtp")) {
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.no_permission"));
            return;
        }

        Component title = plugin.getLanguageManager().get(player, "rtp.gui.title");

        Inventory inv = Bukkit.createInventory(null, 27, title);

        ItemStack filler = createFiller();

        for (int i = 0; i < 27; i++) {
            if (!isWorldSlot(i)) {
                inv.setItem(i, filler);
            }
        }

        for (int i = 0; i < 3; i++) {
            updateWorldItem(inv, i, player);
        }

        player.openInventory(inv);
        openInventories.put(player.getUniqueId(), inv);

        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);

        startUpdateTask(player);
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    private boolean isWorldSlot(int slot) {
        for (int s : WORLD_SLOTS) {
            if (s == slot) return true;
        }
        return false;
    }

    private void updateWorldItem(Inventory inv, int index, Player viewer) {

        String worldName = WORLD_NAMES[index];
        World world = Bukkit.getWorld(worldName);

        if (world == null) return;

        Material material = WORLD_MATERIALS[index];
        String worldKey = WORLD_KEYS[index];

        boolean enabled = rtpManager.isWorldEnabled(worldName);
        int playerCount = rtpManager.getPlayerCountInWorld(worldName);
        boolean hasPermission = rtpManager.hasWorldPermission(viewer, worldName);

        ItemStack item = new ItemStack(enabled && hasPermission ? material : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        Component displayName = plugin.getLanguageManager()
                .get(viewer, "rtp.gui." + worldKey + ".name");

        meta.displayName(displayName);

        List<Component> lore = new ArrayList<>();

        if (!hasPermission) {
            lore.add(plugin.getLanguageManager().get(viewer, "rtp.gui.status.no_permission"));
        } else if (enabled) {
            lore.add(plugin.getLanguageManager().get(viewer, "rtp.gui.status.enabled"));
        } else {
            lore.add(plugin.getLanguageManager().get(viewer, "rtp.gui.status.disabled"));
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("count", String.valueOf(playerCount));
        placeholders.put("world", worldName);

        lore.add(plugin.getLanguageManager().get(viewer, "rtp.gui.players", placeholders));

        if (!hasPermission) {
            lore.add(plugin.getLanguageManager().get(viewer, "rtp.gui.locked"));
        } else if (enabled) {
            lore.add(plugin.getLanguageManager().get(viewer, "rtp.gui.click_to_teleport"));
        } else {
            lore.add(plugin.getLanguageManager().get(viewer, "rtp.gui.world_disabled_lore"));
        }

        RTPManager.WorldRTPSettings settings = rtpManager.getWorldSettings(worldName);

        Map<String, String> radius = new HashMap<>();
        radius.put("min", String.valueOf(settings.minRadius()));
        radius.put("max", String.valueOf(settings.maxRadius()));

        lore.add(plugin.getLanguageManager().get(viewer, "rtp.gui.radius", radius));

        meta.lore(lore);
        item.setItemMeta(meta);

        inv.setItem(WORLD_SLOTS[index], item);
    }

    private void startUpdateTask(Player player) {

        UUID uuid = player.getUniqueId();

        BukkitTask old = updateTasks.remove(uuid);
        if (old != null) old.cancel();

        BukkitTask task = new BukkitRunnable() {

            @Override
            public void run() {

                if (!player.isOnline() || !openInventories.containsKey(uuid)) {
                    cancel();
                    return;
                }

                Inventory inv = openInventories.get(uuid);

                for (int i = 0; i < 3; i++) {
                    updateWorldItem(inv, i, player);
                }
            }

        }.runTaskTimer(plugin, 20L, 20L);

        updateTasks.put(uuid, task);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inv = openInventories.get(player.getUniqueId());
        if (inv == null || event.getInventory() != inv) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        int worldIndex = -1;

        for (int i = 0; i < WORLD_SLOTS.length; i++) {
            if (WORLD_SLOTS[i] == slot) {
                worldIndex = i;
                break;
            }
        }

        if (worldIndex == -1) return;

        String worldName = WORLD_NAMES[worldIndex];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.world_not_found"));
            return;
        }

        if (!rtpManager.hasWorldPermission(player, worldName)
                || !rtpManager.isWorldEnabled(worldName)) {

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

        player.closeInventory();

        rtpManager.startRTP(player, world);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!(event.getPlayer() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();

        if (!openInventories.containsKey(uuid)) return;

        openInventories.remove(uuid);

        BukkitTask task = updateTasks.remove(uuid);
        if (task != null) task.cancel();

        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 1f, 1f);
    }

    public void shutdown() {

        updateTasks.values().forEach(BukkitTask::cancel);
        updateTasks.clear();
        openInventories.clear();
    }
}