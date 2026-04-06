package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.TabApi;
import net.godlycow.org.essc.tab.TabManager;
import org.bukkit.entity.Player;

public class TabApiImpl implements TabApi {

    private final TabManager manager;

    public TabApiImpl(TabManager manager) {
        this.manager = manager;
    }

    @Override
    public void updatePlayerTab(Player player) {
        manager.updatePlayerTab(player);
    }

    @Override
    public void refreshAll() {
        manager.refreshAll();
    }

    @Override
    public boolean isLuckPermsEnabled() {
        return manager.isEnabled();
    }

    @Override
    public boolean isUsingTABPlugin() {
        return manager.isUsingTABPlugin();
    }

    @Override
    public void reload() {
        manager.reload();
    }
}