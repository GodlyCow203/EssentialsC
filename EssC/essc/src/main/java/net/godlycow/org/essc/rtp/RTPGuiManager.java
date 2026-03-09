package net.godlycow.org.essc.rtp;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
    private final Material[] WORLD_MATERIALS = {Material.GRASS_BLOCK, Material.NETHERRACK, Material.END_STONE};
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

        Inventory inv = Bukkit.createInventory(null, 27, miniMessage.deserialize(
                getTitle()
        ));

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

        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
        startUpdateTask(player);
    }

    private String getTitle() {
        Component title = plugin.getLanguageManager().get(null, "rtp.gui.title");
        return LegacyComponentSerializer.legacySection().serialize(title).replace("§", "&");
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    private boolean isWorldSlot(int slot) {
        for (int worldSlot : WORLD_SLOTS) {
            if (worldSlot == slot) return true;
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

        Component displayName = plugin.getLanguageManager().get(null, "rtp.gui." + worldKey + ".name");
        meta.displayName(displayName);

        List<Component> lore = new ArrayList<>();

        if (!hasPermission) {
            lore.add(plugin.getLanguageManager().get(null, "rtp.gui.status.no_permission"));
        } else if (enabled) {
            lore.add(plugin.getLanguageManager().get(null, "rtp.gui.status.enabled"));
        } else {
            lore.add(plugin.getLanguageManager().get(null, "rtp.gui.status.disabled"));
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("count", String.valueOf(playerCount));
        placeholders.put("world", worldName);
        lore.add(plugin.getLanguageManager().get(null, "rtp.gui.players", placeholders));

        if (!hasPermission) {
            lore.add(plugin.getLanguageManager().get(null, "rtp.gui.locked"));
        } else if (enabled) {
            lore.add(plugin.getLanguageManager().get(null, "rtp.gui.click_to_teleport"));
        } else {
            lore.add(plugin.getLanguageManager().get(null, "rtp.gui.world_disabled_lore"));
        }

        RTPManager.WorldRTPSettings settings = rtpManager.getWorldSettings(worldName);
        Map<String, String> radiusPlaceholders = new HashMap<>();
        radiusPlaceholders.put("min", String.valueOf(settings.minRadius()));
        radiusPlaceholders.put("max", String.valueOf(settings.maxRadius()));
        lore.add(plugin.getLanguageManager().get(null, "rtp.gui.radius", radiusPlaceholders));

        meta.lore(lore);
        item.setItemMeta(meta);

        inv.setItem(WORLD_SLOTS[index], item);
    }

    private void startUpdateTask(Player player) {
        UUID uuid = player.getUniqueId();

        if (updateTasks.containsKey(uuid)) {
            updateTasks.get(uuid).cancel();
        }

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

        if (!rtpManager.hasWorldPermission(player, worldName)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (!rtpManager.isWorldEnabled(worldName)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        player.closeInventory();
        rtpManager.startRTP(player, world);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        if (openInventories.containsKey(uuid)) {
            openInventories.remove(uuid);

            if (updateTasks.containsKey(uuid)) {
                updateTasks.get(uuid).cancel();
                updateTasks.remove(uuid);
            }

            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 1.0f, 1.0f);
        }
    }

    public void shutdown() {
        updateTasks.values().forEach(BukkitTask::cancel);
        updateTasks.clear();
        openInventories.clear();
    }
}