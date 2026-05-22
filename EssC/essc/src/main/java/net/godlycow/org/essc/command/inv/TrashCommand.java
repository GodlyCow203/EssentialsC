package net.godlycow.org.essc.command.inv;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.plugin.gui.GuiButton;
import net.godlycow.org.essc.plugin.gui.GuiFramework;
import net.godlycow.org.essc.plugin.gui.GuiTemplate;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrashCommand extends Command implements Listener {

    private static final String TEMPLATE_ID = "trash";
    private static final DateTimeFormatter LOG_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GuiFramework guiFramework;
    private final Set<UUID> openTrash = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final File logFile;

    public TrashCommand(EssentialsC plugin, GuiFramework guiFramework) {
        super(plugin, "trash", "essentialsc.trash", true, 0, "command.usage.trash");
        this.guiFramework = guiFramework;
        this.logFile = new File(plugin.getDataFolder(), "trash.log");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        GuiTemplate template = guiFramework.getTemplate(TEMPLATE_ID);
        if (template == null) {
            plugin.getLogger().warning("[Trash] Missing GUI template: trash.yml");
            player.sendMessage(lang.get(player, "error.internal"));
            return true;
        }

        Set<Integer> borderSlots = resolveBorderSlots(template);

        Component title = template.resolveTitle(player, plugin);
        Inventory inv = Bukkit.createInventory(new TrashHolder(player.getUniqueId(), borderSlots), template.getSize(), title);

        guiFramework.fillStaticItems(inv, TEMPLATE_ID, player);

        openTrash.add(player.getUniqueId());
        player.openInventory(inv);

        playSound(player, "open");
        player.sendMessage(lang.get(player, "trash.opened"));
        plugin.debug(player.getName() + " opened trash");
        return true;
    }

    private Set<Integer> resolveBorderSlots(GuiTemplate template) {
        Set<Integer> slots = new HashSet<>();
        for (GuiButton button : template.getItems().values()) {
            slots.addAll(button.getSlots());
        }
        return slots;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof TrashHolder holder)) return;
        if (!holder.getOwner().equals(player.getUniqueId())) return;

        Inventory topInv = event.getInventory();
        Inventory clickedInv = event.getClickedInventory();

        if (clickedInv == topInv) {
            int slot = event.getSlot();

            if (holder.isBorderSlot(slot)) {
                event.setCancelled(true);
                return;
            }

            ItemStack cursor = event.getCursor();
            boolean placingFromCursor = cursor != null && !cursor.getType().isAir();

            if (placingFromCursor && isBlacklisted(cursor.getType())) {
                event.setCancelled(true);
                player.sendMessage(lang.get(player, "trash.error.blacklisted"));
                playSound(player, "blacklisted");
                plugin.debug(player.getName() + " tried to trash blacklisted item: " + cursor.getType());
                return;
            }

            ItemStack current = event.getCurrentItem();
            boolean takingFromTrash = current != null && !current.getType().isAir();

            if (takingFromTrash && (cursor == null || cursor.getType().isAir())) {
                event.setCancelled(true);
            }
        } else {
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                ItemStack shifted = event.getCurrentItem();
                if (shifted != null && !shifted.getType().isAir()) {
                    if (isBlacklisted(shifted.getType())) {
                        event.setCancelled(true);
                        player.sendMessage(lang.get(player, "trash.error.blacklisted"));
                        playSound(player, "blacklisted");
                        plugin.debug(player.getName() + " tried to trash blacklisted item via shift-click: " + shifted.getType());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof TrashHolder holder)) return;
        if (!holder.getOwner().equals(player.getUniqueId())) return;

        if (!openTrash.remove(player.getUniqueId())) return;

        Inventory inv = event.getInventory();
        boolean logEnabled = plugin.getConfig().getBoolean("trash.log-disposals", false);

        for (int i = 0; i < inv.getSize(); i++) {
            if (holder.isBorderSlot(i)) continue;

            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            if (isBlacklisted(item.getType())) continue;

            if (logEnabled) {
                logDisposal(player, item);
            }

            plugin.debug(player.getName() + " trashed " + item.getAmount() + "x " + item.getType());
        }

        inv.clear();
        playSound(player, "close");
    }

    private boolean isBlacklisted(Material material) {
        List<String> blacklist = plugin.getConfig().getStringList("trash.blacklisted-materials");
        for (String entry : blacklist) {
            try {
                if (Material.valueOf(entry.toUpperCase()) == material) {
                    return true;
                }
            } catch (IllegalArgumentException ignored) {}
        }
        return false;
    }

    private void playSound(Player player, String soundKey) {
        File file = new File(plugin.getDataFolder(), "guis/trash.yml");
        if (!file.exists()) return;

        plugin.getEssScheduler().runAsync(() -> {
            try {
                YamlConfiguration raw = YamlConfiguration.loadConfiguration(file);
                ConfigurationSection section = raw.getConfigurationSection("sounds." + soundKey);
                if (section == null) return;
                if (!section.getBoolean("enabled", true)) return;

                String soundName = section.getString("sound", "UI_BUTTON_CLICK");
                float volume = (float) section.getDouble("volume", 1.0);
                float pitch = (float) section.getDouble("pitch", 1.0);

                Sound sound;
                try {
                    sound = Sound.valueOf(soundName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[Trash] Invalid sound '" + soundName + "' in trash.yml");
                    return;
                }

                plugin.getEssScheduler().runForEntity(player, () ->
                        player.playSound(player.getLocation(), sound, volume, pitch)
                );
            } catch (Exception e) {
                plugin.debug("[Trash] Sound playback error: " + e.getMessage());
            }
        });
    }

    private void logDisposal(Player player, ItemStack item) {
        plugin.getEssScheduler().runAsync(() -> {
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                String timestamp = LocalDateTime.now().format(LOG_FORMAT);
                writer.println("[" + timestamp + "] " + player.getName()
                        + " (" + player.getUniqueId() + ") trashed "
                        + item.getAmount() + "x " + item.getType().name());
            } catch (IOException e) {
                plugin.getLogger().warning("[Trash] Failed to write to trash.log: " + e.getMessage());
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    public static final class TrashHolder implements InventoryHolder {

        private final UUID owner;
        private final Set<Integer> borderSlots;

        public TrashHolder(UUID owner, Set<Integer> borderSlots) {
            this.owner = owner;
            this.borderSlots = borderSlots;
        }

        public UUID getOwner() {
            return owner;
        }

        public boolean isBorderSlot(int slot) {
            return borderSlots.contains(slot);
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}