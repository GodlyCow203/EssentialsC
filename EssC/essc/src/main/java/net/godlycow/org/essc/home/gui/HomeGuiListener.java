package net.godlycow.org.essc.home.gui;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class HomeGuiListener implements Listener {

    private final EssentialsC plugin;
    private final GuiManager manager;

    public HomeGuiListener(EssentialsC plugin, GuiManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof GuiManager.HomeHolder holder)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() != inv) return;
        if (event.getCurrentItem() == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (!clicked.hasItemMeta()) return;

        var pdc = clicked.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(manager.getItems().getActionKey(), PersistentDataType.STRING)) return;

        String action = pdc.get(manager.getItems().getActionKey(), PersistentDataType.STRING);
        if (action == null) return;

        manager.playSound(player, GuiManager.GuiSound.CLICK);
        processAction(player, action, holder);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GuiManager.HomeHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getInventory().getHolder() instanceof GuiManager.HomeHolder) {
            plugin.getEssScheduler().runForEntityLater(player, () -> {
                if (player.getOpenInventory().getTopInventory().getHolder()
                        instanceof GuiManager.HomeHolder) return;
                manager.clearState(player);
            }, 1L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        manager.clearState(player);
    }

    private void processAction(Player player, String action, GuiManager.HomeHolder holder) {
        String[] parts = action.split(":", 5);
        String type = parts[0];
        String data = parts.length > 1 ? parts[1] : "";
        String extra = parts.length > 2 ? parts[2] : "";
        String extra2 = parts.length > 3 ? parts[3] : "";
        String extra3 = parts.length > 4 ? parts[4] : "";

        UUID targetUuid = holder.getTargetUuid() != null ? holder.getTargetUuid() : player.getUniqueId();

        switch (type) {
            case "action" -> handleGuiAction(player, data, extra, extra2, extra3, holder);
            case "home" -> {
                plugin.getHomeManager().getHome(player.getUniqueId(), data).whenComplete((home, err) -> {
                    if (home != null) {
                        plugin.getEssScheduler().runForEntity(player, () -> manager.openHomeDetails(player, home, player.getUniqueId()));
                    }
                });
            }
            case "home_admin" -> {
                UUID adminTarget = holder.getTargetUuid();
                if (adminTarget != null) {
                    plugin.getHomeManager().getHome(adminTarget, data).whenComplete((home, err) -> {
                        if (home != null) {
                            plugin.getEssScheduler().runForEntity(player, () -> manager.openHomeDetails(player, home, adminTarget));
                        }
                    });
                }
            }
        }
    }

    private void handleGuiAction(Player player, String action, String data, String extra2, String extra3, GuiManager.HomeHolder holder) {
        var state = manager.getState(player);
        UUID targetUuid = holder.getTargetUuid() != null ? holder.getTargetUuid() : player.getUniqueId();

        if (action.equals("teleport") || action.equals("rename") || action.equals("update") ||
                action.equals("delete") || action.equals("confirm_delete") || action.equals("confirm_update") ||
                action.equals("back_details")) {
            if (!extra2.isEmpty()) {
                try {
                    targetUuid = UUID.fromString(extra2);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        final UUID finalTargetUuid = targetUuid;

        switch (action) {
            case "close" -> {
                manager.closeGui(player);
                manager.playSound(player, GuiManager.GuiSound.CLOSE);
            }
            case "back" -> {
                if (holder.getMode() == GuiManager.GuiMode.PLAYER_TYPE_SELECT) {
                    manager.openHomeList(player);
                } else if (holder.getMode() == GuiManager.GuiMode.ADMIN_PLAYER) {
                    manager.openPlayerManagementTypeSelection(player);
                } else if (holder.getViewMode() == GuiManager.GuiViewMode.ADMIN) {
                    manager.openPlayerManagementTypeSelection(player);
                } else {
                    manager.openHomeList(player);
                }
            }
            case "back_details" -> {
                final UUID backTarget = finalTargetUuid;
                plugin.getHomeManager().getHome(backTarget, data).whenComplete((home, err) -> {
                    if (home != null) {
                        plugin.getEssScheduler().runForEntity(player, () -> manager.openHomeDetails(player, home, backTarget));
                    } else {
                        plugin.getEssScheduler().runForEntity(player, () -> {
                            if (backTarget.equals(player.getUniqueId())) {
                                manager.openHomeList(player);
                            } else {
                                manager.openPlayerHomes(player, backTarget, Bukkit.getOfflinePlayer(backTarget).getName());
                            }
                        });
                    }
                });
            }
            case "back_player" -> {
                if (!data.isEmpty()) {
                    try {
                        UUID target = UUID.fromString(data);
                        manager.openPlayerHomes(player, target, Bukkit.getOfflinePlayer(target).getName());
                    } catch (IllegalArgumentException e) {
                        manager.openPlayerManagementTypeSelection(player);
                    }
                }
            }
            case "create" -> manager.openCreateGui(player);
            case "create_named" -> manager.openCreateSign(player);
            case "create_default" -> manager.handleCreate(player, plugin.getConfigManager().getDefaultHomeName());
            case "teleport" -> manager.handleTeleport(player, data, finalTargetUuid);
            case "rename" -> manager.openRenameSign(player, data, finalTargetUuid);
            case "update" -> {
                final UUID updateTarget = finalTargetUuid;
                plugin.getHomeManager().getHome(updateTarget, data).whenComplete((home, err) -> {
                    if (home != null) {
                        plugin.getEssScheduler().runForEntity(player, () -> manager.openConfirmUpdate(player, home, updateTarget));
                    }
                });
            }
            case "delete" -> {
                final UUID deleteTarget = finalTargetUuid;
                plugin.getHomeManager().getHome(deleteTarget, data).whenComplete((home, err) -> {
                    if (home != null) {
                        plugin.getEssScheduler().runForEntity(player, () -> manager.openConfirmDelete(player, home, deleteTarget));
                    }
                });
            }
            case "confirm_delete" -> manager.handleDelete(player, data, finalTargetUuid);
            case "confirm_update" -> manager.handleUpdate(player, data, finalTargetUuid);
            case "page" -> {
                int page = Integer.parseInt(data);
                if (state != null) {
                    if (state.viewMode() == GuiManager.GuiViewMode.ADMIN && state.playerListType() != null) {
                        manager.openPlayerList(player, state.playerListType(), page, state.search());
                    } else {
                        manager.openHomeList(player, page, state.sort(), state.search());
                    }
                } else {
                    manager.openHomeList(player);
                }
            }
            case "sort" -> {
                GuiManager.SortMode current = state != null ? state.sort() : GuiManager.SortMode.ALPHABETICAL;
                GuiManager.SortMode next = switch (current) {
                    case ALPHABETICAL -> GuiManager.SortMode.DATE_CREATED;
                    case DATE_CREATED -> GuiManager.SortMode.WORLD;
                    case WORLD -> GuiManager.SortMode.ALPHABETICAL;
                };
                if (state != null) {
                    manager.openHomeList(player, state.page(), next, state.search());
                } else {
                    manager.openHomeList(player);
                }
            }
            case "refresh" -> {
                if (state != null && state.viewMode() == GuiManager.GuiViewMode.ADMIN && holder.getMode() == GuiManager.GuiMode.PLAYER_LIST) {
                    manager.openPlayerList(player, state.playerListType(), 0, state.search());
                } else {
                    manager.openHomeList(player);
                }
            }
            case "search" -> manager.openSearchSign(player);
            case "search_player" -> manager.openPlayerSearchSign(player);
            case "manage_players" -> manager.openPlayerManagementTypeSelection(player);
            case "player_type" -> {
                try {
                    GuiManager.PlayerListType type = GuiManager.PlayerListType.valueOf(data);
                    manager.openPlayerList(player, type, 0, null);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Invalid player list type");
                }
            }
            case "player" -> {
                UUID playerUuid = UUID.fromString(data);
                String playerName = extra2;
                manager.openPlayerHomes(player, playerUuid, playerName);
            }
        }
    }
}