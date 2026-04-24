package net.godlycow.org.essc.api.kit;

import org.bukkit.inventory.ItemStack;
import java.util.List;

public interface Kit {
    String getName();
    String getDisplayName();
    String getRequiredPermission();
    long getCooldownInSeconds();
    boolean isOneTimeUse();
    boolean isGrantedOnFirstJoin();
    int getMaximumClaimsAllowed();
    List<ItemStack> getItemStacks();
    String getKitDescription();
    boolean isSynchronizedAcrossNetwork();
}