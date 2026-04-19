package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.softwares.SchedulerTask;
import net.godlycow.org.essc.command.Command;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NickCommand extends Command {

    public NickCommand(EssentialsC plugin) {
        super(plugin, "nick", "essentialsc.nick", true, 0, "command.usage.nick");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (plugin.getBedrockUtil().isBedrockPlayer(player)) {
            player.sendMessage(lang.get(player, "nick.error.bedrock_not_supported"));
            plugin.debug("Denied nick for Bedrock player: " + player.getName());
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        if (args.length == 1) {
            String arg = args[0];

            if (arg.equalsIgnoreCase("off") || arg.equalsIgnoreCase("clear")) {
                return handleSelfReset(player);
            }

            return handleSelfSet(player, arg);
        }

        if (args.length >= 2) {
            if (!player.hasPermission("essentialsc.nick.others")) {
                player.sendMessage(lang.get(player, "error.no_permission"));
                plugin.debug("Denied: " + player.getName() + " lacks permission essentialsc.nick.others");
                return true;
            }

            Player target = plugin.getBedrockUtil().resolvePlayer(args[0]);
            if (target == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", args[0]);
                player.sendMessage(lang.get(player, "error.player_not_found", placeholders));
                return true;
            }

            if (plugin.getBedrockUtil().isBedrockPlayer(target)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", target.getName());
                player.sendMessage(lang.get(player, "nick.error.bedrock_not_supported_other", placeholders));
                plugin.debug("Denied nick-other for Bedrock player: " + target.getName());
                return true;
            }

            String nickArg = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

            if (nickArg.equalsIgnoreCase("off") || nickArg.equalsIgnoreCase("clear")) {
                return handleOtherReset(player, target);
            }

            return handleOtherSet(player, target, nickArg);
        }

        return true;
    }

    private boolean handleSelfSet(Player player, String nickname) {
        String processed = processNick(nickname);
        if (!validateSync(player, processed)) return true;

        checkAndSet(player, player.getUniqueId(), player, processed, true);
        return true;
    }

    private boolean handleOtherSet(Player admin, Player target, String nickname) {
        String processed = processNick(nickname);
        if (!validateSync(admin, processed)) return true;

        checkAndSet(admin, target.getUniqueId(), target, processed, false);
        return true;
    }

    private boolean handleSelfReset(Player player) {
        plugin.getNickManager().removeNickname(player.getUniqueId()).thenRun(() -> {
            plugin.getEssScheduler().runForEntity(player, () -> {
                plugin.getNickManager().clearNickname(player);
                player.sendMessage(lang.get(player, "nick.success.reset.self"));
                plugin.debug(player.getName() + " reset own nickname");
            });
        });
        return true;
    }

    private boolean handleOtherReset(Player admin, Player target) {
        if (!admin.hasPermission("essentialsc.nick.reset")) {
            admin.sendMessage(lang.get(admin, "error.no_permission"));
            plugin.debug("Denied: " + admin.getName() + " lacks permission essentialsc.nick.reset");
            return true;
        }

        plugin.getNickManager().removeNickname(target.getUniqueId()).thenRun(() -> {
            plugin.getEssScheduler().runForEntity(admin, () -> {
                plugin.getNickManager().clearNickname(target);

                Map<String, String> adminPlaceholders = new HashMap<>();
                adminPlaceholders.put("target", target.getName());
                admin.sendMessage(lang.get(admin, "nick.success.reset.other", adminPlaceholders));

                Map<String, String> targetPlaceholders = new HashMap<>();
                targetPlaceholders.put("admin", admin.getName());
                target.sendMessage(lang.get(target, "nick.success.reset.by", targetPlaceholders));

                plugin.debug(admin.getName() + " reset " + target.getName() + "'s nickname");
            });
        });
        return true;
    }

    private boolean validateSync(Player player, String nickname) {
        String plain = PlainTextComponentSerializer.plainText().serialize(plugin.getMiniMessage().deserialize(nickname));
        int min = plugin.getConfigManager().getNickMinLength();
        int max = plugin.getConfigManager().getNickMaxLength();

        if (plain.length() < min) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("min", String.valueOf(min));
            placeholders.put("current", String.valueOf(plain.length()));
            player.sendMessage(lang.get(player, "nick.error.too_short", placeholders));
            return false;
        }

        if (plain.length() > max) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max", String.valueOf(max));
            placeholders.put("current", String.valueOf(plain.length()));
            player.sendMessage(lang.get(player, "nick.error.too_long", placeholders));
            return false;
        }

        if (plugin.getConfigManager().isNickBlacklistEnabled()) {
            String check = plugin.getConfigManager().isNickNormalizeEnabled() ? plain.toLowerCase() : plain;
            for (String word : plugin.getConfigManager().getNickBlacklistWords()) {
                if (check.contains(word.toLowerCase())) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("word", word);
                    player.sendMessage(lang.get(player, "nick.error.blacklisted", placeholders));
                    return false;
                }
            }
        }

        return true;
    }

    private void checkAndSet(Player sender, UUID targetUuid, Player targetPlayer, String nickname, boolean self) {
        if (plugin.getConfigManager().isNickUnique()) {
            plugin.getNickManager().isNicknameTaken(nickname, targetUuid).thenAccept(taken -> {
                plugin.getEssScheduler().runForEntity(sender, () -> {
                    if (taken) {
                        sender.sendMessage(lang.get(sender, "nick.error.exists"));
                        return;
                    }
                    doSetNickname(sender, targetUuid, targetPlayer, nickname, self);
                });
            });
        } else {
            doSetNickname(sender, targetUuid, targetPlayer, nickname, self);
        }
    }

    private void doSetNickname(Player sender, UUID targetUuid, Player targetPlayer, String nickname, boolean self) {
        plugin.getNickManager().setNickname(targetUuid, nickname).thenRun(() -> {
            plugin.getEssScheduler().runForEntity(sender, () -> {
                plugin.getNickManager().applyNickname(targetPlayer);
                String plainNick = PlainTextComponentSerializer.plainText().serialize(plugin.getMiniMessage().deserialize(nickname));

                if (self) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("nick", plainNick);
                    sender.sendMessage(lang.get(sender, "nick.success.self", placeholders));
                    plugin.debug(sender.getName() + " set own nickname to: " + nickname);
                } else {
                    Map<String, String> adminPlaceholders = new HashMap<>();
                    adminPlaceholders.put("target", targetPlayer.getName());
                    adminPlaceholders.put("nick", plainNick);
                    sender.sendMessage(lang.get(sender, "nick.success.other", adminPlaceholders));

                    Map<String, String> targetPlaceholders = new HashMap<>();
                    targetPlaceholders.put("admin", sender.getName());
                    targetPlaceholders.put("nick", plainNick);
                    targetPlayer.sendMessage(lang.get(targetPlayer, "nick.success.by", targetPlaceholders));

                    plugin.debug(sender.getName() + " set " + targetPlayer.getName() + "'s nickname to: " + nickname);
                }
            });
        });
    }

    private String processNick(String input) {
        if (!plugin.getConfigManager().isNickColorsAllowed()) {
            input = input.replace("&", "").replace("§", "");
        }
        if (!plugin.getConfigManager().isNickFormatAllowed()) {
            input = input.replaceAll("(?i)&[klmnor]", "");
        }
        return input;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("essentialsc.nick.others")) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}