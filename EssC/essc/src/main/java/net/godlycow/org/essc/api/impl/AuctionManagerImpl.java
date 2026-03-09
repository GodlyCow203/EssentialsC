package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.event.auction.Auction;
import net.godlycow.org.essc.api.event.auction.AuctionManager;
import net.godlycow.org.essc.api.event.auction.buy.AuctionBuyEvent;
import net.godlycow.org.essc.api.event.auction.buy.AuctionBuySuccessEvent;
import net.godlycow.org.essc.api.event.auction.cancel.AuctionCancelEvent;
import net.godlycow.org.essc.api.event.auction.claim.AuctionClaimEvent;
import net.godlycow.org.essc.api.event.auction.create.AuctionCreateEvent;
import net.godlycow.org.essc.api.event.auction.create.AuctionCreateSuccessEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AuctionManagerImpl implements AuctionManager {
    private final EssentialsC plugin;
    private final net.godlycow.org.essc.auction.AuctionManager internal;

    public AuctionManagerImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.internal = plugin.getAuctionManager();
    }

    @Override
    @NotNull
    public List<Auction> getActiveAuctions() {
        if (internal == null) return Collections.emptyList();
        return internal.getActiveAuctions().stream()
                .map(AuctionImpl::fromInternal)
                .collect(Collectors.toList());
    }

    @Override
    @NotNull
    public Optional<Auction> getAuction(int id) {
        if (internal == null) return Optional.empty();
        return internal.getAuction(id).map(AuctionImpl::fromInternal);
    }

    @Override
    @NotNull
    public List<Auction> getPlayerAuctions(@NotNull UUID playerUuid) {
        if (internal == null) return Collections.emptyList();
        return internal.getPlayerAuctions(playerUuid).stream()
                .map(AuctionImpl::fromInternal)
                .collect(Collectors.toList());
    }

    @Override
    public int getPlayerAuctionCount(@NotNull UUID playerUuid) {
        if (internal == null) return 0;
        return internal.getPlayerAuctions(playerUuid).size();
    }

    @Override
    public int getTotalAuctionCount() {
        if (internal == null) return 0;
        return internal.getActiveAuctions().size();
    }

    @Override
    public int getMaxAuctionsPerPlayer() {
        return plugin.getConfigManager().getAHMaxAuctions();
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> createAuction(@NotNull Player seller, @NotNull ItemStack item,
                                                    @NotNull BigDecimal price, long durationMs) {
        if (internal == null) {
            return CompletableFuture.completedFuture(false);
        }

        AuctionCreateEvent event = new AuctionCreateEvent(seller, item, price, durationMs);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            String reason = event.getCancelReason();
            if (reason != null) {
                seller.sendMessage(reason);
            }
            return CompletableFuture.completedFuture(false);
        }

        return internal.createAuction(seller, event.getItem(), event.getPrice(), event.getDuration())
                .thenCompose(success -> {
                    if (!success) {
                        return CompletableFuture.completedFuture(false);
                    }

                    CompletableFuture<Boolean> result = new CompletableFuture<>();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.getPluginManager().callEvent(
                                new AuctionCreateSuccessEvent(seller, event.getPrice(), event.getDuration())
                        );
                        result.complete(true);
                    });
                    return result;
                });
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> buyAuction(@NotNull Player buyer, int auctionId) {
        if (internal == null) {
            return CompletableFuture.completedFuture(false);
        }

        Optional<net.godlycow.org.essc.auction.Auction> opt = internal.getAuction(auctionId);
        if (opt.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        net.godlycow.org.essc.auction.Auction internalAuction = opt.get();
        Auction apiAuction = AuctionImpl.fromInternal(internalAuction);

        AuctionBuyEvent preEvent = new AuctionBuyEvent(buyer, apiAuction);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return CompletableFuture.completedFuture(false);
        }

        return internal.buyAuction(buyer, auctionId)
                .thenCompose(success -> {
                    if (!success) {
                        return CompletableFuture.completedFuture(false);
                    }

                    CompletableFuture<Boolean> eventFuture = new CompletableFuture<>();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.getPluginManager().callEvent(
                                new AuctionBuySuccessEvent(buyer, apiAuction)
                        );
                        eventFuture.complete(true);
                    });
                    return eventFuture;
                });
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> cancelAuction(@NotNull Player seller, int auctionId) {
        if (internal == null) {
            return CompletableFuture.completedFuture(false);
        }

        Optional<net.godlycow.org.essc.auction.Auction> opt = internal.getAuction(auctionId);
        if (opt.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        net.godlycow.org.essc.auction.Auction internalAuction = opt.get();
        boolean isOwner = internalAuction.getSellerUuid().equals(seller.getUniqueId());
        boolean isAdmin = seller.hasPermission("essentialsc.ah.admin");

        if (!isOwner && !isAdmin) {
            return CompletableFuture.completedFuture(false);
        }

        Auction apiAuction = AuctionImpl.fromInternal(internalAuction);

        AuctionCancelEvent event = new AuctionCancelEvent(seller, apiAuction, isAdmin);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return CompletableFuture.completedFuture(false);
        }

        return internal.cancelAuction(seller, auctionId);
    }

    @Override
    @NotNull
    public List<ItemStack> getPlayerExpiredItems(@NotNull UUID playerUuid) {
        if (internal == null) return Collections.emptyList();
        return new ArrayList<>(internal.getExpiredItems(playerUuid));
    }

    @Override
    public boolean claimExpiredItems(@NotNull Player player) {
        if (internal == null) return false;

        List<ItemStack> items = getPlayerExpiredItems(player.getUniqueId());
        if (items.isEmpty()) {
            return false;
        }

        AuctionClaimEvent event = new AuctionClaimEvent(player, items);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        return internal.claimExpiredItems(player);
    }

    @Override
    public boolean hasExpiredItems(@NotNull UUID playerUuid) {
        if (internal == null) return false;
        return !internal.getExpiredItems(playerUuid).isEmpty();
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfigManager().isAHEnabled();
    }

    @Override
    public void reload() {
        if (internal != null) {
            internal.reload();
        }
    }

    @Override
    @NotNull
    public BigDecimal getMinPrice() {
        return plugin.getConfigManager().getAHMinPrice();
    }

    @Override
    @Nullable
    public BigDecimal getMaxPrice() {
        return plugin.getConfigManager().getAHMaxPrice();
    }

    @Override
    public long getDefaultDuration() {
        return plugin.getConfigManager().getAHDuration();
    }
}