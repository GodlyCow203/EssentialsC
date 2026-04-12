package net.godlycow.org.expansions.profiles;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class ProfileData {

    public final String     name;
    public final String     nickname;
    public final boolean    online;
    public final boolean    afk;
    public final String     afkDuration;
    public final boolean    flying;
    public final boolean    vanished;
    public final long       playtimeTicks;
    public final int        kills;
    public final int        deaths;
    public final BigDecimal balance;
    public final int        homeCount;
    public final long       firstJoin;
    public final long       lastSeen;

    private ProfileData(Builder b) {
        this.name          = b.name;
        this.nickname      = b.nickname;
        this.online        = b.online;
        this.afk           = b.afk;
        this.afkDuration   = b.afkDuration;
        this.flying        = b.flying;
        this.vanished      = b.vanished;
        this.playtimeTicks = b.playtimeTicks;
        this.kills         = b.kills;
        this.deaths        = b.deaths;
        this.balance       = b.balance;
        this.homeCount     = b.homeCount;
        this.firstJoin     = b.firstJoin;
        this.lastSeen      = b.lastSeen;
    }

    public String getPlaytime() {
        long seconds = playtimeTicks / 20;
        long days    = seconds / 86400;
        long hours   = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        if (days > 0)  return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    public String getDisplayName() {
        return nickname != null && !nickname.isEmpty() ? nickname : name;
    }


    public static CompletableFuture<ProfileData> load(OfflinePlayer target, EssentialsC essc) {
        Builder b = new Builder();
        b.name      = target.getName() != null ? target.getName() : "Unknown";
        b.online    = target.isOnline();
        b.firstJoin = target.getFirstPlayed();
        b.lastSeen  = target.isOnline() ? System.currentTimeMillis() : target.getLastPlayed();

        try { b.playtimeTicks = target.getStatistic(Statistic.PLAY_ONE_MINUTE); } catch (Exception ignored) {}
        try { b.kills         = target.getStatistic(Statistic.PLAYER_KILLS);    } catch (Exception ignored) {}
        try { b.deaths        = target.getStatistic(Statistic.DEATHS);          } catch (Exception ignored) {}

        Player online = target.getPlayer();
        if (online != null) {
            if (essc.getAfkManager() != null) {
                b.afk        = essc.getAfkManager().isAFK(online);
                b.afkDuration = b.afk ? essc.getAfkManager().getAFKDurationFormatted(online) : null;
            }
            if (essc.getFlyManager() != null) {
                b.flying = essc.getFlyManager().isFlying(online);
            }
            if (essc.getVanishManager() != null) {
                b.vanished = essc.getVanishManager().isVanished(online);
            }
        }

        if (essc.getNickManager() != null) {
            b.nickname = essc.getNickManager().getCachedNickname(target.getUniqueId());
        }

        CompletableFuture<BigDecimal> balFuture = essc.getEconomyManager() != null
                ? essc.getEconomyManager().getBalance(target.getUniqueId())
                : CompletableFuture.completedFuture(BigDecimal.ZERO);

        CompletableFuture<Integer> homesFuture = essc.getHomeManager() != null
                ? essc.getHomeManager().getHomeCount(target.getUniqueId())
                : CompletableFuture.completedFuture(0);

        return balFuture.thenCombine(homesFuture, (bal, homes) -> {
            b.balance   = bal != null ? bal : BigDecimal.ZERO;
            b.homeCount = homes != null ? homes : 0;
            return new ProfileData(b);
        });
    }

    private static class Builder {
        String      name          = "Unknown";
        String      nickname      = null;
        boolean     online        = false;
        boolean     afk           = false;
        String      afkDuration   = null;
        boolean     flying        = false;
        boolean     vanished      = false;
        long        playtimeTicks = 0;
        int         kills         = 0;
        int         deaths        = 0;
        BigDecimal  balance       = BigDecimal.ZERO;
        int         homeCount     = 0;
        long        firstJoin     = 0;
        long        lastSeen      = 0;
    }
}
