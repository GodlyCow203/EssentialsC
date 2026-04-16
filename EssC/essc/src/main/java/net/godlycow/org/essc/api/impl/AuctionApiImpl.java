package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.AuctionApi;
import net.godlycow.org.essc.api.auction.BuyHistoryEntry;
import net.godlycow.org.essc.api.auction.SellHistoryEntry;
import net.godlycow.org.essc.auction.AuctionManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AuctionApiImpl implements AuctionApi {

    private final AuctionManager manager;

    public AuctionApiImpl(AuctionManager manager) {
        this.manager = manager;
    }

    @Override
    public List<net.godlycow.org.essc.api.auction.Auction> getActiveAuctions() {
        return manager.getActiveAuctions().stream()
                .map(this::toApi)
                .collect(Collectors.toList());
    }

    @Override
    public List<net.godlycow.org.essc.api.auction.Auction> getPlayerAuctions(UUID uuid) {
        return manager.getPlayerAuctions(uuid).stream()
                .map(this::toApi)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<net.godlycow.org.essc.api.auction.Auction> getAuction(int id) {
        return manager.getAuction(id).map(this::toApi);
    }

    @Override
    public List<ItemStack> getExpiredItems(UUID uuid) {
        return manager.getExpiredItems(uuid);
    }

    @Override
    public boolean hasExpiredItems(UUID uuid) {
        return manager.hasExpiredItems(uuid);
    }

    @Override
    public List<SellHistoryEntry> getSellHistory(UUID uuid) {
        return manager.getSellHistory(uuid).stream()
                .map(e -> new SellHistoryEntry(
                        e.getAuctionId(), e.getSellerUuid(), e.getBuyerName(),
                        e.getItem(), e.getPrice(), e.getTimestamp()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BuyHistoryEntry> getBuyHistory(UUID uuid) {
        return manager.getBuyHistory(uuid).stream()
                .map(e -> new BuyHistoryEntry(
                        e.getAuctionId(), e.getBuyerUuid(), e.getSellerName(),
                        e.getItem(), e.getPrice(), e.getTimestamp()))
                .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<Boolean> createAuction(Player seller, ItemStack item, BigDecimal price, long duration) {
        StringBuilder failReason = new StringBuilder();
        return manager.createAuction(seller, item, price, duration, failReason);
    }

    @Override
    public CompletableFuture<Boolean> buyAuction(Player buyer, int id) {
        return manager.buyAuction(buyer, id);
    }

    @Override
    public CompletableFuture<Boolean> cancelAuction(Player player, int id) {
        return manager.cancelAuction(player, id);
    }

    @Override
    public boolean claimExpiredItems(Player player) {
        return manager.claimExpiredItems(player);
    }

    private net.godlycow.org.essc.api.auction.Auction toApi(net.godlycow.org.essc.auction.Auction a) {
        return new net.godlycow.org.essc.api.auction.Auction(
                a.getId(), a.getSellerUuid(), a.getSellerName(),
                a.getItem(), a.getPrice(), a.getListedTime(), a.getDuration());
    }
}