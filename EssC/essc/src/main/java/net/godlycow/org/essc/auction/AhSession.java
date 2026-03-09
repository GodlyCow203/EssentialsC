package net.godlycow.org.essc.auction;

import java.util.UUID;

public class AhSession {
    private final UUID playerUuid;
    private final int page;
    private final boolean expiredView;
    private final boolean listingsView;
    private final boolean historyView;
    private final boolean sellHistoryView;
    private final boolean buyHistoryView;
    private final long createdAt;
    private volatile boolean processing;

    public AhSession(UUID playerUuid, int page, boolean expiredView, boolean listingsView,
                     boolean historyView, boolean sellHistoryView, boolean buyHistoryView) {
        this.playerUuid = playerUuid;
        this.page = page;
        this.expiredView = expiredView;
        this.listingsView = listingsView;
        this.historyView = historyView;
        this.sellHistoryView = sellHistoryView;
        this.buyHistoryView = buyHistoryView;
        this.createdAt = System.currentTimeMillis();
        this.processing = false;
    }

    public static AhSession main(UUID uuid, int page) {
        return new AhSession(uuid, page, false, false, false, false, false);
    }

    public static AhSession expired(UUID uuid) {
        return new AhSession(uuid, 1, true, false, false, false, false);
    }

    public static AhSession listings(UUID uuid, int page) {
        return new AhSession(uuid, page, false, true, false, false, false);
    }

    public static AhSession historyType(UUID uuid) {
        return new AhSession(uuid, 1, false, false, true, false, false);
    }

    public static AhSession sellHistory(UUID uuid, int page) {
        return new AhSession(uuid, page, false, false, false, true, false);
    }

    public static AhSession buyHistory(UUID uuid, int page) {
        return new AhSession(uuid, page, false, false, false, false, true);
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public int getPage() { return page; }
    public boolean isExpiredView() { return expiredView; }
    public boolean isListingsView() { return listingsView; }
    public boolean isHistoryView() { return historyView; }
    public boolean isSellHistoryView() { return sellHistoryView; }
    public boolean isBuyHistoryView() { return buyHistoryView; }
    public long getCreatedAt() { return createdAt; }
    public boolean isStale() { return System.currentTimeMillis() - createdAt > 300000; }

    public synchronized boolean tryAcquireProcessing() {
        if (processing) return false;
        processing = true;
        return true;
    }

    public synchronized void releaseProcessing() {
        processing = false;
    }
}