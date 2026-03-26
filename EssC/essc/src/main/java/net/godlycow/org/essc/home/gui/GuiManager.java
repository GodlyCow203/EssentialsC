package net.godlycow.org.essc.home.gui;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.home.Home;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManager {

    private static final long CLICK_COOLDOWN = 150;

    private final EssentialsC plugin;
    private final HomeItems items;
    private final GuiBuilder builder;
    private final GuiAction actionHandler;
    private final SignHandler signHandler;

    private final Map<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();
    private final Map<UUID, Long> clickCooldowns = new ConcurrentHashMap<>();

    public GuiManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.items = new HomeItems(plugin);
        this.builder = new GuiBuilder(plugin, items);
        this.actionHandler = new GuiAction(plugin, this);
        this.signHandler = new SignHandler(plugin, this);

        plugin.getServer().getPluginManager().registerEvents(
                new HomeGuiListener(plugin, this), plugin);

        startCleanupTask();
    }

    public boolean isGuiMode() {
        return plugin.getConfigManager().getHomeMode().equals("gui");
    }


    public void openHomeList(Player player) {
        openHomeList(player, 0, SortMode.ALPHABETICAL, null);
    }

    public void openHomeList(Player player, int page, SortMode sort, String search) {
        if (!checkCooldown(player)) return;

        plugin.getHomeManager().getHomes(player.getUniqueId()).whenComplete((homes, err) -> {
            if (err != null) {
                player.sendMessage(plugin.getLanguageManager().get(player, "home.gui.error"));
                return;
            }

            List<Home> sorted = builder.sortHomes(homes, sort);
            if (search != null && !search.isEmpty()) {
                sorted.removeIf(h -> !h.getName().toLowerCase().contains(search.toLowerCase()));
            }

            int totalPages = Math.max(1, (sorted.size() + GuiBuilder.HOMES_PER_PAGE - 1) / GuiBuilder.HOMES_PER_PAGE);
            int targetPage = Math.min(page, totalPages - 1);

            playerStates.put(player.getUniqueId(),
                    new PlayerState(targetPage, sort, search, sorted, GuiViewMode.OWN_HOMES, null));

            runSync(() -> {
                if (!player.isOnline()) return;
                Inventory gui = builder.buildHomeList(player, sorted, targetPage, totalPages, sort);
                applyHolder(gui, player.getUniqueId(), GuiMode.LIST, null, GuiViewMode.OWN_HOMES);
                player.openInventory(gui);
                playSound(player, GuiSound.OPEN);
            });
        });
    }

    public void openPlayerManagementTypeSelection(Player player) {
        if (!checkCooldown(player)) return;
        if (!player.hasPermission("essentialsc.home.admin")) {
            player.sendMessage(plugin.getLanguageManager().get(player, "home.admin.no_permission"));
            return;
        }

        runSync(() -> {
            if (!player.isOnline()) return;
            Inventory gui = builder.buildPlayerManagement(player);
            applyHolder(gui, null, GuiMode.PLAYER_TYPE_SELECT, null, GuiViewMode.ADMIN);
            player.openInventory(gui);
            playSound(player, GuiSound.OPEN);
        });
    }

    public void openPlayerList(Player player, PlayerListType type, int page, String search) {
        if (!checkCooldown(player)) return;

        plugin.getHomeManager().getAllHomeOwners().whenComplete((uuids, err) -> {
            if (err != null || uuids == null) {
                runSync(() -> player.sendMessage(plugin.getLanguageManager().get(player, "home.gui.error")));
                return;
            }

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                List<OfflinePlayer> players = new ArrayList<>();
                for (UUID uuid : uuids) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    if (p.getName() == null) continue;

                    boolean add = switch (type) {
                        case ONLINE  -> p.isOnline();
                        case OFFLINE -> !p.isOnline();
                        case ALL     -> true;
                    };
                    if (add) players.add(p);
                }

                if (search != null && !search.isEmpty()) {
                    players.removeIf(p -> !p.getName().toLowerCase().contains(search.toLowerCase()));
                }
                players.sort(Comparator.comparing(OfflinePlayer::getName, String.CASE_INSENSITIVE_ORDER));

                int totalPages = Math.max(1, (players.size() + GuiBuilder.HOMES_PER_PAGE - 1) / GuiBuilder.HOMES_PER_PAGE);
                int targetPage = Math.min(page, totalPages - 1);

                playerStates.put(player.getUniqueId(),
                        new PlayerState(targetPage, SortMode.ALPHABETICAL, search, null, GuiViewMode.ADMIN, type));

                final List<OfflinePlayer> finalPlayers = players;
                runSync(() -> {
                    if (!player.isOnline()) return;
                    Inventory gui = builder.buildPlayerList(player, finalPlayers, targetPage, totalPages, type);
                    applyHolder(gui, null, GuiMode.PLAYER_LIST, null, GuiViewMode.ADMIN);
                    player.openInventory(gui);
                    playSound(player, GuiSound.OPEN);
                });
            });
        });
    }

    public void openPlayerHomes(Player player, UUID targetUuid, String targetName) {
        if (!checkCooldown(player)) return;

        plugin.getHomeManager().getHomes(targetUuid).whenComplete((homes, err) ->
                runSync(() -> {
                    if (!player.isOnline()) return;
                    Inventory gui = builder.buildPlayerHomes(player, targetUuid, targetName, homes);
                    applyHolder(gui, targetUuid, GuiMode.ADMIN_PLAYER, null, GuiViewMode.ADMIN);
                    player.openInventory(gui);
                    playSound(player, GuiSound.OPEN);
                })
        );
    }

    public void openHomeDetails(Player player, Home home, UUID targetUuid) {
        if (!checkCooldown(player)) return;

        runSync(() -> {
            if (!player.isOnline()) return;
            boolean isAdmin = !targetUuid.equals(player.getUniqueId());
            Inventory gui = builder.buildHomeDetails(player, home, targetUuid);
            applyHolder(gui, targetUuid, GuiMode.DETAILS, home.getName(),
                    isAdmin ? GuiViewMode.ADMIN : GuiViewMode.OWN_HOMES);
            player.openInventory(gui);
            playSound(player, GuiSound.OPEN);
        });
    }

    public void openCreateGui(Player player) {
        if (!checkCooldown(player)) return;

        runSync(() -> {
            if (!player.isOnline()) return;
            Inventory gui = builder.buildCreate(player);
            applyHolder(gui, player.getUniqueId(), GuiMode.CREATE, null, GuiViewMode.OWN_HOMES);
            player.openInventory(gui);
            playSound(player, GuiSound.OPEN);
        });
    }

    public void openConfirmDelete(Player player, Home home, UUID targetUuid) {
        if (!checkCooldown(player)) return;

        runSync(() -> {
            if (!player.isOnline()) return;
            Inventory gui = builder.buildConfirmDelete(player, home, targetUuid);
            applyHolder(gui, targetUuid, GuiMode.CONFIRM_DELETE, home.getName(),
                    targetUuid.equals(player.getUniqueId()) ? GuiViewMode.OWN_HOMES : GuiViewMode.ADMIN);
            player.openInventory(gui);
            playSound(player, GuiSound.WARNING);
        });
    }

    public void openConfirmUpdate(Player player, Home home, UUID targetUuid) {
        if (!checkCooldown(player)) return;

        runSync(() -> {
            if (!player.isOnline()) return;
            Inventory gui = builder.buildConfirmUpdate(player, home, targetUuid);
            applyHolder(gui, targetUuid, GuiMode.CONFIRM_UPDATE, home.getName(),
                    targetUuid.equals(player.getUniqueId()) ? GuiViewMode.OWN_HOMES : GuiViewMode.ADMIN);
            player.openInventory(gui);
            playSound(player, GuiSound.OPEN);
        });
    }

    public void openSearchSign(Player player)       { signHandler.openSearchSign(player); }
    public void openPlayerSearchSign(Player player) { signHandler.openPlayerSearchSign(player); }
    public void openRenameSign(Player player, String currentName, UUID targetUuid) {
        signHandler.openRenameSign(player, currentName, targetUuid);
    }
    public void openCreateSign(Player player) { signHandler.openCreateSign(player); }


    public void handleCreate(Player player, String name){
        actionHandler.handleCreate(player, name);
    }
    public void handleDelete(Player player, String name, UUID targetUuid) {
        actionHandler.handleDelete(player, name, targetUuid);
    }
    public void handleRename(Player player, String oldName, String newName, UUID targetUuid){
        actionHandler.handleRename(player, oldName, newName, targetUuid);
    }
    public void handleUpdate(Player player, String name, UUID targetUuid){
        actionHandler.handleUpdate(player, name, targetUuid);
    }
    public void handleTeleport(Player player, String name, UUID targetUuid){
        actionHandler.handleTeleport(player, name, targetUuid); }

    public void handlePlayerSearch(Player player, String query){
        actionHandler.handlePlayerSearch(player, query);
    }


    public void closeGui(Player player) {
        player.closeInventory();
        playerStates.remove(player.getUniqueId());
    }

    public void clearState(Player player) {
        playerStates.remove(player.getUniqueId());
    }

    public PlayerState getState(Player player) {
        return playerStates.get(player.getUniqueId());
    }

    public HomeItems getItems() { return items; }


    public void playSound(Player player, GuiSound type) {
        if (!plugin.getConfigManager().isHomeSounds()) return;
        runSync(() -> {
            if (!player.isOnline()) return;

            Sound sound;
            float volume = 0.5f;
            float pitch = 1.0f;

            switch (type) {
                case CLICK   -> sound = Sound.UI_BUTTON_CLICK;
                case OPEN    -> sound = Sound.BLOCK_CHEST_OPEN;
                case CLOSE   -> sound = Sound.BLOCK_CHEST_CLOSE;
                case SUCCESS -> { sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP; pitch = 1.2f; }
                case ERROR   -> sound = Sound.ENTITY_VILLAGER_NO;
                case TELEPORT -> { sound = Sound.ENTITY_ENDERMAN_TELEPORT; volume = 0.6f; }
                case WARNING -> sound = Sound.BLOCK_NOTE_BLOCK_PLING;
                default      -> sound = Sound.UI_BUTTON_CLICK;
            }

            player.playSound(player.getLocation(), sound, volume, pitch);
        });
    }

    private void applyHolder(Inventory gui, UUID targetUuid, GuiMode mode,
                             String homeName, GuiViewMode viewMode) {
        if (gui.getHolder() instanceof HomeHolder holder) {
            holder.setInventory(gui);
        }
    }

    private boolean checkCooldown(Player player) {
        long now = System.currentTimeMillis();
        Long last = clickCooldowns.get(player.getUniqueId());
        if (last != null && (now - last) < CLICK_COOLDOWN) return false;
        clickCooldowns.put(player.getUniqueId(), now);
        return true;
    }

    private void runSync(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            playerStates.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired(now)) {
                    Player p = plugin.getServer().getPlayer(entry.getKey());
                    if (p != null && p.isOnline()) p.closeInventory();
                    return true;
                }
                return false;
            });
        }, 1200L, 1200L);
    }

    public void shutdown() {
        new HashSet<>(playerStates.keySet()).forEach(uuid -> {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) p.closeInventory();
        });
        playerStates.clear();
    }


    public enum SortMode {
        ALPHABETICAL, DATE_CREATED, WORLD
    }
    public enum GuiMode {
        LIST, DETAILS, CREATE, CONFIRM_DELETE, CONFIRM_UPDATE, ADMIN_PLAYER, PLAYER_TYPE_SELECT, PLAYER_LIST
    }
    public enum GuiViewMode {
        OWN_HOMES, ADMIN
    }
    public enum PlayerListType {
        ONLINE, OFFLINE, ALL
    }
    public enum GuiSound {
        CLICK, OPEN, CLOSE, SUCCESS, ERROR, TELEPORT, WARNING
    }

    public record PlayerState(int page, SortMode sort, String search, List<Home> currentList,
                              GuiViewMode viewMode, PlayerListType playerListType, long timestamp) {
        public PlayerState(int page, SortMode sort, String search, List<Home> currentList,
                           GuiViewMode viewMode, PlayerListType playerListType) {
            this(page, sort, search, currentList, viewMode, playerListType, System.currentTimeMillis());
        }
        public boolean isExpired(long now) { return (now - timestamp) > 300_000L; }
    }

    public static class HomeHolder implements InventoryHolder {
        private final GuiManager manager;
        private final GuiMode mode;
        private final String homeName;
        private final UUID targetUuid;
        private final GuiViewMode viewMode;
        private Inventory inventory;

        public HomeHolder(GuiManager manager, GuiMode mode, String homeName,
                          UUID targetUuid, GuiViewMode viewMode) {
            this.manager    = manager;
            this.mode       = mode;
            this.homeName   = homeName;
            this.targetUuid = targetUuid;
            this.viewMode   = viewMode;
        }

        @Override public Inventory getInventory() { return inventory; }
        public void setInventory(Inventory inv)   { this.inventory = inv; }
        public GuiManager getManager()        { return manager; }
        public GuiMode getMode()                  { return mode; }
        public String getHomeName()               { return homeName; }
        public UUID getTargetUuid()               { return targetUuid; }
        public GuiViewMode getViewMode()          { return viewMode; }
    }
}