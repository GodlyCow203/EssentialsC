package net.godlycow.org.essc.kit;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.database.Database;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class KitManager implements Listener {
    private final EssentialsC plugin;
    private final Database database;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String, Kit> kits = new ConcurrentHashMap<>();
    private final File kitsFile;
    private FileConfiguration kitsConfig;

    private final Map<UUID, Map<String, PlayerKitData>> playerCache = new ConcurrentHashMap<>();

    public KitManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin, "kits.db");
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");

        try {
            database.connect();
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize kit database: " + e.getMessage());
        }

        loadKits();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.debug("KitManager initialized");
    }

    private void createTables() throws SQLException {
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS kit_claims (
                    uuid TEXT NOT NULL,
                    kit_name TEXT NOT NULL,
                    last_claimed INTEGER DEFAULT 0,
                    claim_count INTEGER DEFAULT 0,
                    PRIMARY KEY (uuid, kit_name)
                )
            """)) {
            stmt.execute();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerData(player.getUniqueId());

        if (!player.hasPlayedBefore()) {
            plugin.debug("First join detected for " + player.getName() + ", checking first-join kits");

            for (Kit kit : kits.values()) {
                if (kit.isFirstJoin()) {
                    plugin.debug("Giving first-join kit: " + kit.getName());
                    giveKit(player, kit);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerCache.remove(event.getPlayer().getUniqueId());
    }

    public void loadKits() {
        kits.clear();

        if (!kitsFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }

        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        ConfigurationSection kitsSection = kitsConfig.getConfigurationSection("kits");

        if (kitsSection == null) {
            plugin.getLogger().warning("No kits defined in kits.yml");
            return;
        }

        for (String kitName : kitsSection.getKeys(false)) {
            try {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);
                if (kitSection == null) continue;

                String displayName = kitSection.getString("display-name", kitName);
                String permission = "essentialsc.kit." + kitName.toLowerCase();
                long cooldown = kitSection.getLong("cooldown", 0);
                boolean oneTime = kitSection.getBoolean("one-time", false);
                boolean firstJoin = kitSection.getBoolean("first-join", false);
                int maxClaims = kitSection.getInt("max-claims", 0);
                String description = kitSection.getString("description", "");

                List<ItemStack> items = new ArrayList<>();
                List<Map<?, ?>> itemsList = kitSection.getMapList("items");

                for (Map<?, ?> itemMap : itemsList) {
                    ItemStack item = loadItemFromMap(itemMap);
                    if (item != null) {
                        items.add(item);
                    }
                }

                Kit kit = new Kit(kitName.toLowerCase(), displayName, permission, cooldown,
                        oneTime, firstJoin, maxClaims, items, description);
                kits.put(kitName.toLowerCase(), kit);

                registerPermission(permission);

                plugin.debug("Loaded kit: " + kitName + " (" + items.size() + " items)");

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load kit '" + kitName + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + kits.size() + " kits");
    }

    private void registerPermission(String permission) {
        try {
            if (Bukkit.getPluginManager().getPermission(permission) == null) {
                Permission perm = new Permission(permission, "Access to kit " + permission.replace("essentialsc.kit.", ""), PermissionDefault.OP);
                Bukkit.getPluginManager().addPermission(perm);
                plugin.debug("Registered permission: " + permission);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register permission " + permission + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ItemStack loadItemFromMap(Map<?, ?> map) {
        try {
            Object typeObj = map.get("type");
            if (!(typeObj instanceof String typeName)) return null;

            org.bukkit.Material material = org.bukkit.Material.matchMaterial(typeName);
            if (material == null) {
                plugin.getLogger().warning("Invalid material: " + typeName);
                return null;
            }

            int amount = 1;
            Object amountObj = map.get("amount");
            if (amountObj instanceof Number n) {
                amount = n.intValue();
            }

            ItemStack item = new ItemStack(
                    material,
                    Math.min(amount, material.getMaxStackSize())
            );

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;

            Object nameObj = map.get("name");
            if (nameObj instanceof String name) {
                meta.displayName(mm.deserialize(name));
            }

            Object loreObj = map.get("lore");
            if (loreObj instanceof List<?> loreList) {
                List<Component> loreComponents = new ArrayList<>();
                for (Object line : loreList) {
                    if (line instanceof String s) {
                        loreComponents.add(mm.deserialize(s));
                    }
                }
                meta.lore(loreComponents);
            }

            Object enchObj = map.get("enchantments");
            if (enchObj instanceof Map<?, ?> enchMap) {
                for (Map.Entry<?, ?> entry : enchMap.entrySet()) {
                    if (!(entry.getKey() instanceof String key)) continue;
                    if (!(entry.getValue() instanceof Number lvl)) continue;

                    Enchantment enchant = Enchantment.getByName(key.toUpperCase());
                    if (enchant != null) {
                        meta.addEnchant(enchant, lvl.intValue(), true);
                    }
                }
            }

            Object flagsObj = map.get("flags");
            if (flagsObj instanceof List<?> flags) {
                for (Object flag : flags) {
                    if (flag instanceof String s) {
                        try {
                            meta.addItemFlags(ItemFlag.valueOf(s.toUpperCase()));
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }

            Object unbreakableObj = map.get("unbreakable");
            if (unbreakableObj instanceof Boolean unbreakable) {
                meta.setUnbreakable(unbreakable);
            }

            Object cmdObj = map.get("custom-model-data");
            if (cmdObj instanceof Number n) {
                meta.setCustomModelData(n.intValue());
            }

            Object nbtObj = map.get("nbt");
            if (nbtObj instanceof Map<?, ?> nbtMap) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                for (Map.Entry<?, ?> entry : nbtMap.entrySet()) {
                    if (!(entry.getKey() instanceof String key)) continue;

                    NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
                    Object value = entry.getValue();

                    if (value instanceof String s) {
                        container.set(namespacedKey, PersistentDataType.STRING, s);
                    } else if (value instanceof Integer i) {
                        container.set(namespacedKey, PersistentDataType.INTEGER, i);
                    } else if (value instanceof Double d) {
                        container.set(namespacedKey, PersistentDataType.DOUBLE, d);
                    } else if (value instanceof Number n) {
                        container.set(namespacedKey, PersistentDataType.LONG, n.longValue());
                    }
                }
            }

            item.setItemMeta(meta);
            return item;

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load item: " + e.getMessage());
            return null;
        }
    }


    public void loadPlayerData(UUID uuid) {
        database.async(conn -> {
            Map<String, PlayerKitData> data = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT kit_name, last_claimed, claim_count FROM kit_claims WHERE uuid = ?"
            )) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    data.put(rs.getString("kit_name"), new PlayerKitData(
                            rs.getLong("last_claimed"),
                            rs.getInt("claim_count")
                    ));
                }
            }
            return data;
        }).thenAccept(data -> {
            playerCache.put(uuid, data);
            plugin.debug("Loaded kit data for " + uuid + " (" + data.size() + " entries)");
        });
    }

    public Kit getKit(String name) {
        return kits.get(name.toLowerCase());
    }

    public Collection<Kit> getKits() {
        return kits.values();
    }

    public boolean hasPermission(Player player, Kit kit) {
        return player.hasPermission(kit.getPermission()) || player.hasPermission("essentialsc.kits.admin");
    }

    public boolean canClaim(Player player, Kit kit) {
        if (!hasPermission(player, kit)) return false;

        Map<String, PlayerKitData> data = playerCache.getOrDefault(player.getUniqueId(), new HashMap<>());
        PlayerKitData claimData = data.get(kit.getName());

        if (kit.isOneTime()) {
            if (claimData != null && claimData.claimCount > 0) {
                return false;
            }
        }

        if (kit.getMaxClaims() > 0) {
            if (claimData != null && claimData.claimCount >= kit.getMaxClaims()) {
                return false;
            }
        }

        if (kit.getCooldown() > 0 && !player.hasPermission("essentialsc.kits.admin")) {
            if (claimData != null) {
                long cooldownEnd = claimData.lastClaimed + (kit.getCooldown() * 1000);
                if (System.currentTimeMillis() < cooldownEnd) {
                    return false;
                }
            }
        }

        return true;
    }

    public long getCooldownRemaining(Player player, Kit kit) {
        Map<String, PlayerKitData> data = playerCache.get(player.getUniqueId());
        if (data == null) return 0;

        PlayerKitData claimData = data.get(kit.getName());
        if (claimData == null || claimData.lastClaimed == 0) return 0;

        long cooldownEnd = claimData.lastClaimed + (kit.getCooldown() * 1000);
        long remaining = cooldownEnd - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    public boolean hasClaimed(Player player, Kit kit) {
        Map<String, PlayerKitData> data = playerCache.get(player.getUniqueId());
        if (data == null) return false;
        PlayerKitData claimData = data.get(kit.getName());
        return claimData != null && claimData.claimCount > 0;
    }

    public int getClaimCount(Player player, Kit kit) {
        Map<String, PlayerKitData> data = playerCache.get(player.getUniqueId());
        if (data == null) return 0;
        PlayerKitData claimData = data.get(kit.getName());
        return claimData != null ? claimData.claimCount : 0;
    }

    public void giveKit(Player player, Kit kit) {
        for (ItemStack item : kit.getItems()) {
            if (item == null || item.getType().isAir()) continue;

            Map<Integer, ItemStack> overflow = player.getInventory().addItem(item.clone());

            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        long now = System.currentTimeMillis();
        database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("""
            INSERT INTO kit_claims (uuid, kit_name, last_claimed, claim_count)
            VALUES (?, ?, ?, 1)
            ON CONFLICT(uuid, kit_name) DO UPDATE SET
                last_claimed = excluded.last_claimed,
                claim_count = claim_count + 1
        """)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, kit.getName());
                stmt.setLong(3, now);
                stmt.executeUpdate();
            }
            return null;
        }).thenRun(() -> {
            playerCache.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .merge(kit.getName(), new PlayerKitData(now, 1),
                            (old, newData) -> new PlayerKitData(now, old.claimCount + 1));
        });

        player.sendMessage(plugin.getLanguageManager().get(player, "kit.claim.success",
                Map.of("kit", kit.getDisplayName())));

        if (plugin.getDiscordSRVHook() != null) {
            plugin.getDiscordSRVHook().sendKitClaimEmbed(
                    player.getUniqueId(),
                    player.getName(),
                    kit
            );
        }

        plugin.debug("Player " + player.getName() + " claimed kit " + kit.getName());
    }
    public void reload() {
        loadKits();
        playerCache.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            loadPlayerData(p.getUniqueId());
        }
        plugin.debug("Kit configuration reloaded");
    }

    public void shutdown() {
        database.disconnect();
    }

    private static class PlayerKitData {
        final long lastClaimed;
        final int claimCount;

        PlayerKitData(long lastClaimed, int claimCount) {
            this.lastClaimed = lastClaimed;
            this.claimCount = claimCount;
        }
    }
}