package net.godlycow.org.essc.home.gui;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class GuiAction {

    private final EssentialsC plugin;
    private final GuiManager manager;

    public GuiAction(EssentialsC plugin, GuiManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void handleCreate(Player player, String name) {
        if (!name.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage(lang(player, "home.set.invalid_name"));
            manager.playSound(player, GuiManager.GuiSound.ERROR);
            return;
        }
        if (name.length() > 16) {
            player.sendMessage(lang(player, "home.set.name_too_long"));
            manager.playSound(player, GuiManager.GuiSound.ERROR);
            return;
        }

        UUID uuid = player.getUniqueId();

        plugin.getHomeManager().getHomeCount(uuid).whenComplete((count, err) -> {
            int max = plugin.getHomeManager().getMaxHomes(player);

            if (count >= max) {
                sync(() -> {
                    player.sendMessage(lang(player, "home.set.limit_reached",
                            Map.of("limit", String.valueOf(max))));
                    manager.playSound(player, GuiManager.GuiSound.ERROR);
                    manager.openHomeList(player);
                });
                return;
            }

            plugin.getHomeManager().setHome(player, name, player.getLocation()).whenComplete((success, err2) -> {
                sync(() -> {
                    if (success) {
                        player.sendMessage(lang(player, "home.set.success", Map.of("name", name)));
                        manager.playSound(player, GuiManager.GuiSound.SUCCESS);
                    } else {
                        player.sendMessage(lang(player, "home.set.failed", Map.of("name", name)));
                        manager.playSound(player, GuiManager.GuiSound.ERROR);
                    }
                    manager.openHomeList(player);
                });
            });
        });
    }

    public void handleDelete(Player player, String homeName, UUID targetUuid) {
        plugin.getHomeManager().deleteHome(targetUuid, homeName).whenComplete((success, err) -> {
            sync(() -> {
                if (success) {
                    player.sendMessage(lang(player, "home.delete.success", Map.of("name", homeName)));
                    manager.playSound(player, GuiManager.GuiSound.SUCCESS);
                } else {
                    player.sendMessage(lang(player, "home.delete.not_found", Map.of("name", homeName)));
                    manager.playSound(player, GuiManager.GuiSound.ERROR);
                }
                returnToAppropriateView(player, targetUuid);
            });
        });
    }

    public void handleRename(Player player, String oldName, String newName, UUID targetUuid) {
        if (!newName.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage(lang(player, "home.set.invalid_name"));
            manager.playSound(player, GuiManager.GuiSound.ERROR);
            returnToAppropriateView(player, targetUuid);
            return;
        }
        if (newName.length() > 16) {
            player.sendMessage(lang(player, "home.set.name_too_long"));
            manager.playSound(player, GuiManager.GuiSound.ERROR);
            returnToAppropriateView(player, targetUuid);
            return;
        }

        plugin.getHomeManager().homeExists(targetUuid, newName).whenComplete((exists, err) -> {
            if (exists) {
                sync(() -> {
                    player.sendMessage(lang(player, "home.set.already_exists", Map.of("name", newName)));
                    manager.playSound(player, GuiManager.GuiSound.ERROR);
                    returnToAppropriateView(player, targetUuid);
                });
                return;
            }

            plugin.getHomeManager().getHome(targetUuid, oldName).whenComplete((home, err2) -> {
                if (home == null) {
                    sync(() -> {
                        player.sendMessage(lang(player, "home.not_found", Map.of("name", oldName)));
                        manager.playSound(player, GuiManager.GuiSound.ERROR);
                        returnToAppropriateView(player, targetUuid);
                    });
                    return;
                }

                plugin.getHomeManager().setHome(targetUuid, newName, home.toLocation(plugin.getServer()))
                        .whenComplete((ok, err3) -> {
                            if (!ok) {
                                sync(() -> {
                                    player.sendMessage(lang(player, "home.set.failed", Map.of("name", newName)));
                                    manager.playSound(player, GuiManager.GuiSound.ERROR);
                                    returnToAppropriateView(player, targetUuid);
                                });
                                return;
                            }

                            plugin.getHomeManager().deleteHome(targetUuid, oldName).whenComplete((del, err4) ->
                                    sync(() -> {
                                        player.sendMessage(lang(player, "home.gui.rename_success",
                                                Map.of("old", oldName, "new", newName)));
                                        manager.playSound(player, GuiManager.GuiSound.SUCCESS);
                                        returnToAppropriateView(player, targetUuid);
                                    })
                            );
                        });
            });
        });
    }

    public void handleUpdate(Player player, String homeName, UUID targetUuid) {
        Location loc = player.getLocation();
        plugin.getHomeManager().setHome(targetUuid, homeName, loc).whenComplete((success, err) ->
                sync(() -> {
                    if (success) {
                        player.sendMessage(lang(player, "home.set.updated", Map.of("name", homeName)));
                        manager.playSound(player, GuiManager.GuiSound.SUCCESS);
                    } else {
                        player.sendMessage(lang(player, "home.set.failed", Map.of("name", homeName)));
                        manager.playSound(player, GuiManager.GuiSound.ERROR);
                    }
                    returnToAppropriateView(player, targetUuid);
                })
        );
    }

    public void handleTeleport(Player player, String homeName, UUID targetUuid) {
        manager.closeGui(player);

        plugin.getHomeManager().getHome(targetUuid, homeName).whenComplete((home, err) ->
                sync(() -> {
                    if (home == null) {
                        player.sendMessage(lang(player, "home.teleport.not_found", Map.of("name", homeName)));
                        manager.playSound(player, GuiManager.GuiSound.ERROR);
                        return;
                    }

                    manager.playSound(player, GuiManager.GuiSound.TELEPORT);

                    if (targetUuid.equals(player.getUniqueId())) {
                        plugin.getHomeManager().startTeleport(player, home);
                    } else {
                        Location loc = home.toLocation(plugin.getServer());
                        if (loc != null) {
                            player.teleport(loc);
                            player.sendMessage(lang(player, "home.admin.teleported_to_other",
                                    Map.of("player",
                                            Bukkit.getOfflinePlayer(targetUuid).getName() != null
                                                    ? Bukkit.getOfflinePlayer(targetUuid).getName() : targetUuid.toString(),
                                            "name", homeName)));
                        }
                    }
                })
        );
    }

    public void handlePlayerSearch(Player player, String query) {
        GuiManager.PlayerState state = manager.getState(player);
        GuiManager.PlayerListType type = (state != null && state.playerListType() != null)
                ? state.playerListType()
                : GuiManager.PlayerListType.ALL;
        manager.openPlayerList(player, type, 0, query);
    }


    private void returnToAppropriateView(Player player, UUID targetUuid) {
        if (targetUuid.equals(player.getUniqueId())) {
            manager.openHomeList(player);
        } else {
            String name = Bukkit.getOfflinePlayer(targetUuid).getName();
            manager.openPlayerHomes(player, targetUuid, name != null ? name : targetUuid.toString());
        }
    }

    private net.kyori.adventure.text.Component lang(Player player, String key) {
        return plugin.getLanguageManager().get(player, key);
    }

    private net.kyori.adventure.text.Component lang(Player player, String key, java.util.Map<String, String> placeholders) {
        return plugin.getLanguageManager().get(player, key, placeholders);
    }

    private void sync(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }
}