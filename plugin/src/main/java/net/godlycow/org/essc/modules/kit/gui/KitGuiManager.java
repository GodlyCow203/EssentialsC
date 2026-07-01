package net.godlycow.org.essc.modules.kit.gui;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.kit.Kit;
import net.godlycow.org.essc.plugin.gui.GuiFramework;
import net.godlycow.org.essc.plugin.gui.GuiTemplate;
import net.godlycow.org.essc.plugin.gui.GuiButton;
import net.godlycow.org.essc.util.ComponentHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KitGuiManager {

    private static final int[] KIT_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43
    };
    private static final int PER_PAGE = KIT_SLOTS.length;

    private final EssentialsC plugin;
    private final GuiFramework guiFramework;
    private final KitSoundManager sounds;
    private final Map<UUID, Integer> activeSessions = new ConcurrentHashMap<>();

    public KitGuiManager(EssentialsC plugin, GuiFramework guiFramework) {
        this.plugin = plugin;
        this.guiFramework = guiFramework;
        this.sounds = new KitSoundManager(plugin);

        plugin.getServer().getPluginManager().registerEvents(
                new KitGuiListener(plugin, this), plugin);
    }

    public void openKitList(Player player, int page) {
        GuiTemplate template = guiFramework.getTemplate("kit_list");
        if (template == null) {
            plugin.getLogger().warning("[KitGUI] Missing GUI template: " + "kit_list" + ".yml");
            return;
        }

        List<Kit> autoKits = new ArrayList<>();
        Map<Integer, Kit> pinnedKits = new HashMap<>();

        for (Kit kit : accessibleKits(player)) {
            int slot = kit.getGuiSlot();
            if (slot >= 0 && slot < 54) {
                pinnedKits.put(slot, kit);
            } else {
                autoKits.add(kit);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) autoKits.size() / PER_PAGE));
        int safePage = Math.max(1, Math.min(page, totalPages));

        Component title = template.resolveTitle(player, plugin, Map.of("page", String.valueOf(safePage), "total", String.valueOf(totalPages)));

        KitGuiHolder holder = new KitGuiHolder(safePage);
        Inventory gui = Bukkit.createInventory(holder, template.getSize(), title);
        holder.setInventory(gui);

        guiFramework.fillStaticItems(gui, "kit_list", player);

        for (Map.Entry<Integer, Kit> entry : pinnedKits.entrySet()) {
            gui.setItem(entry.getKey(), buildKitItem(player, entry.getValue()));
        }

        if (autoKits.isEmpty() && pinnedKits.isEmpty()) {
            GuiButton emptyBtn = template.getItem("empty");
            if (emptyBtn != null) {
                gui.setItem(31, guiFramework.getItemBuilder().build(emptyBtn, player));
            }
        } else {
            int start = (safePage - 1) * PER_PAGE;
            int end = Math.min(start + PER_PAGE, autoKits.size());
            for (int i = start; i < end; i++) {
                int slotIndex = i - start;
                if (slotIndex < KIT_SLOTS.length) {
                    int targetSlot = KIT_SLOTS[slotIndex];
                    if (!pinnedKits.containsKey(targetSlot)) {
                        gui.setItem(targetSlot, buildKitItem(player, autoKits.get(i)));
                    }
                }
            }
        }

        GuiButton navPrev = template.getItem("nav-prev");
        GuiButton navNext = template.getItem("nav-next");

        gui.setItem(48, safePage > 1 && navPrev != null
                ? guiFramework.getItemBuilder().build(navPrev, player) : buildFillerPane());

        gui.setItem(50, safePage < totalPages && navNext != null
                ? guiFramework.getItemBuilder().build(navNext, player) : buildFillerPane());

        activeSessions.put(player.getUniqueId(), safePage);
        player.getScheduler().run(plugin, task -> {
            if (player.isOnline()) {
                player.openInventory(gui);
                sounds.playOpen(player);
            }
        }, null);
    }

    public void handleKitClick(Player player, String kitName) {
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) return;

        if (!plugin.getKitManager().hasPermission(player, kit)) {
            sounds.playDenied(player);
            player.sendMessage(plugin.getLanguageManager().get(player, "kit.no_permission",
                    Map.of("kit", kit.getDisplayName())));
            return;
        }

        plugin.getKitManager().getCooldownRemainingAsync(player, kit).thenAccept(cooldown -> {
            player.getScheduler().run(plugin, task -> {
                if (!player.isOnline()) return;

                if (!plugin.getKitManager().canClaim(player, kit)) {
                    sounds.playDenied(player);

                    if (kit.isOneTime() && plugin.getKitManager().hasClaimed(player, kit)) {
                        player.sendMessage(plugin.getLanguageManager().get(player, "kit.one_time_used",
                                Map.of("kit", kit.getDisplayName())));
                        return;
                    }

                    if (kit.getMaxClaims() > 0) {
                        int count = plugin.getKitManager().getClaimCount(player, kit);
                        if (count >= kit.getMaxClaims()) {
                            player.sendMessage(plugin.getLanguageManager().get(player, "kit.max_claims_reached",
                                    Map.of("kit", kit.getDisplayName(), "max", String.valueOf(kit.getMaxClaims()))));
                            return;
                        }
                    }

                    if (cooldown > 0 && !plugin.getKitManager().hasCooldownBypass(player, kit)) {
                        player.sendMessage(plugin.getLanguageManager().get(player, "kit.cooldown_active",
                                Map.of("kit", kit.getDisplayName(), "time", formatTime(cooldown))));
                        return;
                    }

                    return;
                }

                if (cooldown > 0 && !plugin.getKitManager().hasCooldownBypass(player, kit)) {
                    sounds.playDenied(player);
                    player.sendMessage(plugin.getLanguageManager().get(player, "kit.cooldown_active",
                            Map.of("kit", kit.getDisplayName(), "time", formatTime(cooldown))));
                    return;
                }

                sounds.playClaim(player);
                plugin.getKitManager().giveKit(player, kit);
            }, null);
        });
    }

    public void handlePageTurn(Player player, int newPage) {
        sounds.playPageTurn(player);
        openKitList(player, newPage);
    }

    public void handleClose(Player player) {
        sounds.playClose(player);
    }

    public void clearSession(UUID uuid) {
        activeSessions.remove(uuid);
    }

    private List<Kit> accessibleKits(Player player) {
        Collection<Kit> all = plugin.getKitManager().getKits();
        List<Kit> result = new ArrayList<>();
        for (Kit kit : all) {
            if (plugin.getKitManager().hasPermission(player, kit)) {
                result.add(kit);
            }
        }
        return result;
    }

    private ItemStack buildKitItem(Player player, Kit kit) {
        boolean canClaim = plugin.getKitManager().canClaim(player, kit);
        boolean onCooldown = !canClaim && kit.getCooldown() > 0;
        boolean oneTimeUsed = kit.isOneTime() && plugin.getKitManager().hasClaimed(player, kit);

        Material material = resolveIcon(kit, canClaim);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Component name;
        if (canClaim) {
            name = plugin.getLanguageManager().get(player, "kit.gui.item.kit.name.available",
                    Map.of("kit", kit.getDisplayName()));
        } else if (oneTimeUsed) {
            name = plugin.getLanguageManager().get(player, "kit.gui.item.kit.name.used",
                    Map.of("kit", kit.getDisplayName()));
        } else {
            name = plugin.getLanguageManager().get(player, "kit.gui.item.kit.name.cooldown",
                    Map.of("kit", kit.getDisplayName()));
        }

        meta.displayName(ComponentHelper.noItalic(name));

        List<Component> lore = new ArrayList<>();

        if (!kit.getDescription().isEmpty()) {
            lore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player,
                    "kit.gui.item.kit.lore.description",
                    Map.of("description", kit.getDescription()))));
            lore.add(ComponentHelper.noItalic(Component.empty()));
        }

        lore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player,
                "kit.gui.item.kit.lore.items",
                Map.of("count", String.valueOf(kit.getItems().size())))));

        if (kit.getCooldown() > 0) {
            lore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player,
                    "kit.gui.item.kit.lore.cooldown",
                    Map.of("time", formatTime(kit.getCooldown())))));
        }

        if (onCooldown) {
            long remaining = plugin.getKitManager().getCooldownRemaining(player, kit);
            lore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player,
                    "kit.gui.item.kit.lore.ready_in",
                    Map.of("time", formatTime(remaining)))));
        }

        if (kit.getMaxClaims() > 0) {
            int claimCount = plugin.getKitManager().getClaimCount(player, kit);
            lore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player,
                    "kit.gui.item.kit.lore.claims",
                    Map.of("count", String.valueOf(claimCount), "max", String.valueOf(kit.getMaxClaims())))));
        }

        if (canClaim) {
            lore.add(ComponentHelper.noItalic(Component.empty()));
            lore.add(ComponentHelper.noItalic(plugin.getLanguageManager().get(player,
                    "kit.gui.item.kit.lore.click_to_claim")));
        }

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        NamespacedKey kitKey = new NamespacedKey(plugin, "kit_name");
        meta.getPersistentDataContainer().set(kitKey, PersistentDataType.STRING, kit.getName());

        item.setItemMeta(meta);
        return item;
    }

    private Material resolveIcon(Kit kit, boolean canClaim) {
        String iconStr = kit.getGuiIcon();
        if (iconStr != null && !iconStr.isEmpty()) {
            Material parsed = Material.matchMaterial(iconStr.toUpperCase());
            if (parsed != null) {
                return parsed;
            }
            plugin.getLogger().warning("[KitGUI] Invalid gui-icon '" + iconStr + "' for kit " + kit.getName() + ", using default.");
        }
        return canClaim ? Material.CHEST : Material.BARREL;
    }

    private ItemStack buildFillerPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatTime(long seconds) {
        Duration dur = Duration.ofSeconds(seconds);
        long hours = dur.toHours();
        long mins = dur.toMinutesPart();
        long secs = dur.toSecondsPart();
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, mins, secs);
        }
        if (mins > 0) {
            return String.format("%dm %ds", mins, secs);
        }
        return secs + "s";
    }
}