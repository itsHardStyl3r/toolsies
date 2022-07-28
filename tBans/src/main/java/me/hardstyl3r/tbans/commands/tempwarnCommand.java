package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class tempwarnCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;
    private final PunishmentType type = PunishmentType.WARN;

    public tempwarnCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("tempwarn").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.tempwarn")) {
            sender.sendMessage(l.getColoredString("no_permission").replace("<permission>", "toolsies.tempwarn"));
            return true;
        }
        if (args.length > 1) {
            String target = args[0];
            if (!punishmentManager.canSenderPunishTarget(sender, target, type)) {
                sender.sendMessage(l.getColoredString("warn.priority_too_high"));
                return true;
            }
            if (target.length() > punishmentManager.getMaximumNickLength()) {
                sender.sendMessage(l.getColoredString("ban.name_too_long").replace("<length>", String.valueOf(punishmentManager.getMaximumNickLength())));
                return true;
            }
            if (userManager.getUser(target) == null) {
                sender.sendMessage(l.getColoredString("players.unknown").replace("<name>", args[0]));
                return true;
            }
            if (!localeManager.isValidStringTime(args[1])) {
                sender.sendMessage(l.getColoredString("tempban.incorrect_time"));
                return true;
            }
            long duration = localeManager.parseTimeFromString(args[1]);
            long minimumDuration = punishmentManager.getMinimumDuration(PunishmentType.BAN);
            if (duration < minimumDuration) {
                sender.sendMessage(l.getColoredString("tempban.duration_too_short").replace("<duration>", localeManager.parseTimeWithTranslate(minimumDuration, l)));
                return true;
            }
            String admin = sender.getName();
            String reason = (args.length > 2 ? localeManager.createMessage(args, 2) : null);
            UUID uuid = userManager.getUserIgnoreCase(target).getUUID();
            Punishment punishment = punishmentManager.createPunishment(type, uuid, target, admin, reason, duration);
            sender.sendMessage(l.getColoredString("tempwarn.tempwarn_sender")
                    .replace("<name>", target)
                    .replace("<duration>", localeManager.parseTimeWithTranslate(duration, l)));
            Player p = Bukkit.getPlayerExact(target);
            if (Bukkit.getPlayerExact(target) != null) {
                Locale pl = userManager.determineLocale(p);
                p.sendMessage(pl.getColoredString("tempwarn.tempwarn_target")
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
        if (sender.hasPermission("toolsies.tempwarn")) {
            Locale l = userManager.determineLocale(sender);
            if (args.length == 1) {
                return null;
            } else if (args.length == 2) {
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.duration"), true));
            } else if (args.length == 3) {
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.player"), true));
            } else {
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.reason"), false));
            }
        }
        return Collections.emptyList();
    }
}
