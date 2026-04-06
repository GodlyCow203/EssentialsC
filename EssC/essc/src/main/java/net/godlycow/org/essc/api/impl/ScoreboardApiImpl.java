package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.ScoreboardApi;
import net.godlycow.org.essc.scoreboard.ScoreboardManager;
import org.bukkit.entity.Player;

public class ScoreboardApiImpl implements ScoreboardApi {

    private final ScoreboardManager manager;

    public ScoreboardApiImpl(ScoreboardManager manager) {
        this.manager = manager;
    }

    @Override
    public void toggle(Player player) {
        manager.toggle(player);
    }

    @Override
    public boolean isEnabled(Player player) {
        return manager.isEnabled(player);
    }

    @Override
    public boolean isGloballyEnabled() {
        return manager.isGloballyEnabled();
    }

    @Override
    public void reload() {
        manager.reload();
    }
}