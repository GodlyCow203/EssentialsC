package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.Home;
import net.godlycow.org.essc.api.HomeManager;
import net.godlycow.org.essc.api.event.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HomeManagerImpl implements HomeManager {
    private final EssentialsC plugin;
    private final net.godlycow.org.essc.home.HomeManager internalManager;

    public HomeManagerImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.internalManager = plugin.getHomeManager();
    }

    @Override
    public @NotNull CompletableFuture<Optional<Home>> getHome(@NotNull UUID player, @NotNull String name) {
        return internalManager.getHome(player, name)
                .thenApply(h -> Optional.ofNullable(h).map(HomeImpl::fromInternal));
    }

    @Override
    public @NotNull CompletableFuture<List<Home>> getHomes(@NotNull UUID player) {
        return internalManager.getHomes(player)
                .thenApply(list -> list.stream()
                        .map(HomeImpl::fromInternal)
                        .collect(Collectors.toList()));
    }

    @Override
    public @NotNull CompletableFuture<Integer> getHomeCount(@NotNull UUID player) {
        return internalManager.getHomeCount(player);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> hasHome(@NotNull UUID player, @NotNull String name) {
        return internalManager.homeExists(player, name);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> setHome(@NotNull Player player, @NotNull String name, @NotNull Location location) {
        HomeImpl home = new HomeImpl(player.getUniqueId(), name, location, System.currentTimeMillis() / 1000);

        HomeCreateEvent event = new HomeCreateEvent(player, home);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            if (event.getCancelReason() != null) {
                player.sendMessage(event.getCancelReason());
            }
            return CompletableFuture.completedFuture(false);
        }

        return internalManager.setHome(player, name, location);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> deleteHome(@NotNull UUID player, @NotNull String name) {
        return getHome(player, name).thenCompose(optHome -> {
            if (optHome.isEmpty()) return CompletableFuture.completedFuture(false);

            Player p = Bukkit.getPlayer(player);
            HomeDeleteEvent event = new HomeDeleteEvent(p, optHome.get(), false);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                if (p != null && event.getCancelReason() != null) {
                    p.sendMessage(event.getCancelReason());
                }
                return CompletableFuture.completedFuture(false);
            }

            return internalManager.deleteHome(player, name);
        });
    }

    @Override
    public @NotNull CompletableFuture<Integer> deleteAllHomes(@NotNull UUID player) {
        return getHomes(player).thenCompose(homes -> {
            List<CompletableFuture<Boolean>> futures = homes.stream()
                    .map(h -> deleteHome(player, h.getName()))
                    .toList();

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (int) futures.stream().filter(f -> f.join()).count());
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleport(@NotNull Player player, @NotNull Home home) {
        HomeTeleportEvent event = new HomeTeleportEvent(player, home, HomeTeleportEvent.TeleportCause.API);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            if (event.getCancelReason() != null) {
                player.sendMessage(event.getCancelReason());
            }
            return CompletableFuture.completedFuture(false);
        }

        internalManager.startTeleport(player, toInternal(home));
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleport(@NotNull Player player, @NotNull String homeName) {
        return getHome(player.getUniqueId(), homeName).thenCompose(opt -> {
            if (opt.isPresent()) {
                return teleport(player, opt.get());
            }
            return CompletableFuture.completedFuture(false);
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportInstantly(@NotNull Player player, @NotNull Home home) {
        Location loc = home.toLocation();
        if (loc == null) return CompletableFuture.completedFuture(false);

        Bukkit.getScheduler().runTask(plugin, () -> player.teleport(loc));
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public boolean cancelTeleport(@NotNull Player player) {
        if (internalManager.hasPendingTeleport(player)) {
            internalManager.cancelTeleport(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasPendingTeleport(@NotNull Player player) {
        return internalManager.hasPendingTeleport(player);
    }

    @Override
    public int getMaxHomes(@NotNull Player player) {
        HomeLimitCheckEvent event = new HomeLimitCheckEvent(player, internalManager.getMaxHomes(player));
        Bukkit.getPluginManager().callEvent(event);
        return event.getMaxHomes();
    }

    @Override
    public @NotNull CompletableFuture<Integer> getRemainingHomes(@NotNull Player player) {
        return getHomeCount(player.getUniqueId())
                .thenApply(count -> Math.max(0, getMaxHomes(player) - count));
    }

    @Override
    public boolean isOnCooldown(@NotNull Player player) {
        return internalManager.isOnCooldown(player);
    }

    @Override
    public long getRemainingCooldown(@NotNull Player player) {
        return internalManager.getRemainingCooldown(player);
    }

    @Override
    public @NotNull CompletableFuture<Optional<Home>> getHomeAdmin(@NotNull UUID owner, @NotNull String name) {
        return getHome(owner, name);
    }

    @Override
    public @NotNull CompletableFuture<List<Home>> getHomesAdmin(@NotNull UUID owner) {
        return getHomes(owner);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> setHomeAdmin(@NotNull UUID owner, @NotNull String name, @NotNull Location location) {
        return internalManager.setHome(Bukkit.getOfflinePlayer(owner).getPlayer(), name, location);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> deleteHomeAdmin(@NotNull UUID owner, @NotNull String name) {
        return internalManager.deleteHome(owner, name);
    }

    private net.godlycow.org.essc.home.Home toInternal(Home home) {
        Location loc = home.toLocation();
        if (loc == null) {
            throw new IllegalStateException("World not loaded: " + home.getWorld());
        }
        return new net.godlycow.org.essc.home.Home(
                home.getOwner(), home.getName(), loc, home.getCreatedAt()
        );
    }
}