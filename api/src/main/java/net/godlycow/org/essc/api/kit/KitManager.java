package net.godlycow.org.essc.api.kit;

import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface KitManager {
    Collection<Kit> getLoadedKits();
    Kit findKitByName(String name);
    Collection<Kit> getKitsAvailableTo(Player player);
    boolean hasCooldownExpiredFor(Player player, Kit kit);
    CompletableFuture<Long> fetchCooldownRemainingAsync(Player player, Kit kit);
    long getRemainingCooldownSeconds(Player player, Kit kit);
    boolean hasPlayerClaimed(Player player, Kit kit);
    int getPlayerClaimCount(Player player, Kit kit);
    KitClaimProfile fetchClaimProfile(Player player, Kit kit);
    boolean isClaimAllowedFor(Player player, Kit kit);
    boolean isPermittedToUse(Player player, Kit kit);
    void reloadKitDefinitions();
    CompletableFuture<Void> claimKitForPlayer(Player player, Kit kit);
    int getTotalLoadedKitCount();
    boolean isKitLoaded(String name);
}