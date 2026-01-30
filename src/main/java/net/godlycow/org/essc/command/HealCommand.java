package net.godlycow.org.essc.command;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class HealCommand extends Command {

    public HealCommand(EssentialsC plugin) {
        super(plugin, "heal", "essentialsc.heal", true, 0, "command.usage.heal");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        plugin.debug("Healing initiated for " + player.getName());

        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(maxHealth);
        plugin.debug("Set health to " + maxHealth);

        if (plugin.getConfig().getBoolean("heal.feed-player", true)) {
            player.setFoodLevel(20);
            player.setSaturation(20);
            plugin.debug("Fed player to full saturation");
        }

        player.setFireTicks(0);

        if (plugin.getConfig().getBoolean("heal.clear-negative-effects", true)) {
            var badEffects = player.getActivePotionEffects().stream()
                    .filter(e -> isNegativeEffect(e.getType()))
                    .toList();

            badEffects.forEach(e -> player.removePotionEffect(e.getType()));
            plugin.debug("Cleared " + badEffects.size() + " negative effects");
        }

        player.sendMessage(lang.get(player, "heal.success"));
        return true;
    }

    private boolean isNegativeEffect(PotionEffectType type) {
        return type == PotionEffectType.POISON
                || type == PotionEffectType.WITHER
                || type == PotionEffectType.BLINDNESS
                || type == PotionEffectType.SLOWNESS
                || type == PotionEffectType.HUNGER
                || type == PotionEffectType.WEAKNESS
                || type == PotionEffectType.UNLUCK
                || type == PotionEffectType.MINING_FATIGUE;
    }
}
