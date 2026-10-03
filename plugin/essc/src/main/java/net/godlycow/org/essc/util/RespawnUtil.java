package net.godlycow.org.essc.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class RespawnUtil {

    private static volatile Method respawnLocationMethod;
    private static volatile boolean respawnLocationMethodChecked;
    private static volatile boolean respawnLocationMethodAvailable;

    private RespawnUtil() {
    }

    public static Location getBedSpawnLocation(Player player) {
        Location stored = getStoredRespawnLocationIfAvailable(player);

        if (isRespawnLocationMethodAvailable()) {
            return stored;
        }
        //fallback for older servers
        try {
            return player.getBedSpawnLocation();
        } catch (IllegalStateException | UnsupportedOperationException e) {
            return null;
        } catch (RuntimeException e) {

            return null;
        }
    }

    private static boolean isRespawnLocationMethodAvailable() {
        return respawnLocationMethodChecked && respawnLocationMethodAvailable;
    }

    //get stored respawn without actually loading it

    private static Location getStoredRespawnLocationIfAvailable(Player player) {
        try {
            Method method = respawnLocationMethod;
            if (method == null && !respawnLocationMethodChecked) {
                synchronized (RespawnUtil.class) {
                    method = respawnLocationMethod;
                    if (method == null && !respawnLocationMethodChecked) {

                        try {
                            method = player.getClass().getMethod("getRespawnLocation", boolean.class);
                            respawnLocationMethod = method;
                            respawnLocationMethodAvailable = true;

                        } catch (NoSuchMethodException e) {
                            respawnLocationMethodAvailable = false;

                        } finally {
                            respawnLocationMethodChecked = true;
                        }
                    }
                }
            }

            if (method == null) {
                return null;
            }

            return (Location) method.invoke(player, Boolean.FALSE);
        } catch (ReflectiveOperationException e) {
            //ignore all region errors
            return null;
        } catch (IllegalStateException | UnsupportedOperationException e) {
            return null;

        } catch (RuntimeException e) {
            return null;
        }
    }
}
