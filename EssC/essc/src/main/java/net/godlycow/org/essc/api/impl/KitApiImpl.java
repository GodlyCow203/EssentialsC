package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.KitApi;
import net.godlycow.org.essc.api.kit.Kit;
import net.godlycow.org.essc.api.kit.KitSyncHook;
import net.godlycow.org.essc.kit.KitManager;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class KitApiImpl implements KitApi {

    private final KitManager manager;
    private KitSyncHook apiHook;

    public KitApiImpl(KitManager manager) {
        this.manager = manager;
    }

    @Override
    public Kit getKit(String name) {
        net.godlycow.org.essc.kit.Kit k = manager.getKit(name);
        return k == null ? null : toApiKit(k);
    }

    @Override
    public Collection<Kit> getKits() {
        return manager.getKits().stream()
                .map(this::toApiKit)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(Player player, Kit kit) {
        return manager.hasPermission(player, toManagerKit(kit));
    }

    @Override
    public boolean canClaim(Player player, Kit kit) {
        return manager.canClaim(player, toManagerKit(kit));
    }

    @Override
    public CompletableFuture<Boolean> canClaimAsync(Player player, Kit kit) {
        if (!hasPermission(player, kit)) {
            return CompletableFuture.completedFuture(false);
        }

        net.godlycow.org.essc.kit.Kit managerKit = toManagerKit(kit);

        if (kit.isOneTime() && manager.hasClaimed(player, managerKit)) {
            return CompletableFuture.completedFuture(false);
        }

        if (kit.getMaxClaims() > 0) {
            int count = manager.getClaimCount(player, managerKit);
            if (count >= kit.getMaxClaims()) {
                return CompletableFuture.completedFuture(false);
            }
        }

        return getCooldownRemainingAsync(player, kit)
                .thenApply(remaining -> remaining <= 0);
    }

    @Override
    public boolean hasClaimed(Player player, Kit kit) {
        return manager.hasClaimed(player, toManagerKit(kit));
    }

    @Override
    public int getClaimCount(Player player, Kit kit) {
        return manager.getClaimCount(player, toManagerKit(kit));
    }

    @Override
    public long getCooldownRemaining(Player player, Kit kit) {
        return manager.getCooldownRemaining(player, toManagerKit(kit));
    }

    @Override
    public CompletableFuture<Long> getCooldownRemainingAsync(Player player, Kit kit) {
        return manager.getCooldownRemainingAsync(player, toManagerKit(kit));
    }

    @Override
    public void giveKit(Player player, Kit kit) {
        manager.giveKit(player, toManagerKit(kit));
    }

    @Override
    public void setNetworkSyncHook(KitSyncHook hook) {
        this.apiHook = hook;

        if (hook == null) {
            manager.clearNetworkHook();
            return;
        }

        manager.setNetworkHook(new net.godlycow.org.essc.kit.KitSyncHook() {
            @Override
            public void onKitClaimed(UUID uuid, String kitName, long claimedAt, String serverId) {
                hook.onKitClaimed(uuid, kitName, claimedAt, serverId);
            }

            @Override
            public CompletableFuture<Long> getNetworkLastClaimed(UUID uuid, String kitName) {
                return hook.getNetworkLastClaimed(uuid, kitName);
            }
        });
    }

    @Override
    public KitSyncHook getNetworkSyncHook() {
        return apiHook;
    }

    private Kit toApiKit(net.godlycow.org.essc.kit.Kit k) {
        return new Kit(
                k.getName(),
                k.getDisplayName(),
                k.getPermission(),
                k.getCooldown(),
                k.isOneTime(),
                k.isFirstJoin(),
                k.getMaxClaims(),
                k.getItems(),
                k.getDescription(),
                k.isNetworkSync()
        );
    }

    private net.godlycow.org.essc.kit.Kit toManagerKit(Kit k) {
        return new net.godlycow.org.essc.kit.Kit(
                k.getName(),
                k.getDisplayName(),
                k.getPermission(),
                k.getCooldown(),
                k.isOneTime(),
                k.isFirstJoin(),
                k.getMaxClaims(),
                k.getItems(),
                k.getDescription(),
                k.isNetworkSync()
        );
    }
}