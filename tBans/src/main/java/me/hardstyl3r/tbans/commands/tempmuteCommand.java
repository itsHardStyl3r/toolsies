package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class tempmuteCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public tempmuteCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("tempmute").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.tempmute")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("no_permission")).replace("<permission>", "toolsies.tempmute"));
            return true;
        }
        if (args.length > 1) {
            String target = args[0];
            punishmentManager.deleteIfExpired(PunishmentType.MUTE, target);
            if (punishmentManager.isPunished(PunishmentType.MUTE, target)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("mute.is_muted")).replace("<name>", target));
                return true;
            }
            if (target.length() > punishmentManager.getMaximumNickLength()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("ban.name_too_long")).replace("<length>", String.valueOf(punishmentManager.getMaximumNickLength())));
                return true;
            }
            if (userManager.getUser(target) == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("players.unknown")).replace("<name>", args[0]));
                return true;
            }
            if (!localeManager.isValidStringTime(args[1])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("tempban.incorrect_time")));
                return true;
            }
            long duration = localeManager.parseTimeFromString(args[1]);
            long minimumDuration = punishmentManager.getMinimumDuration(PunishmentType.MUTE);
            if (duration < minimumDuration) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("tempban.duration_too_short")).replace("<duration>", localeManager.parseTimeWithTranslate(minimumDuration, l)));
                return true;
            }
            String admin = sender.getName();
            String reason = (args.length > 2 ? localeManager.createMessage(args, 2) : null);
            UUID uuid = userManager.getUserIgnoreCase(target).getUUID();
            Punishment punishment = punishmentManager.createPunishment(PunishmentType.MUTE, uuid, target, admin, reason, duration);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("tempmute.tempmute"))
                    .replace("<name>", target)
                    .replace("<duration>", localeManager.parseTimeWithTranslate(duration, l)));
            Player p = Bukkit.getPlayerExact(target);
            if (Bukkit.getPlayerExact(target) != null) {
                Locale pl = userManager.determineLocale(p);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        pl.getString("tempmute.tempmute_target"))
                        .replace("<admin>", punishment.getAdmin())
                        .replace("<reason>", (punishment.getReason() == null ? "brak" : punishment.getReason()))
                        .replace("<duration>", localeManager.parseTimeWithTranslate(duration, pl)));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.tempmute")) {
            Locale l = userManager.determineLocale(sender);
            if (args.length == 1) {
                return null;
            } else if(args.length == 2) {
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.duration"), true));
            } else if(args.length == 3) {
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.player"), true));
            } else {
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.reason"), false));
            }
        }
        return Collections.emptyList();
    }
}
