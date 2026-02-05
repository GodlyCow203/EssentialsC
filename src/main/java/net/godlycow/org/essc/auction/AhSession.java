package net.godlycow.org.essc.auction;

public class AhSession {
    private final int page;
    private final boolean isExpiredView;

    public AhSession(int page, boolean isExpiredView) {
        this.page = page;
        this.isExpiredView = isExpiredView;
    }

    public int getPage() {
        return page;
    }

    public boolean isExpiredView() {
        return isExpiredView;
    }
}