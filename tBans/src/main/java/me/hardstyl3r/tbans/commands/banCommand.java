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

public class banCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;
    private final PunishmentType type = PunishmentType.BAN;

    public banCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("ban").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.ban")) {
            sender.sendMessage(l.getColoredString("no_permission").replace("<permission>", "toolsies.ban"));
            return true;
        }
        if (args.length > 0) {
            String target = args[0];
            punishmentManager.deleteIfExpired(type, target);
            if (!punishmentManager.canSenderPunishTarget(sender, target, type)) {
                sender.sendMessage(l.getColoredString("ban.priority_too_high"));
                return true;
            }
            if (punishmentManager.isPunished(type, target)) {
                sender.sendMessage(l.getColoredString("ban.is_banned").replace("<name>", target));
                return true;
            }
            if (target.length() > punishmentManager.getMaximumNickLength()) {
                sender.sendMessage(l.getColoredString("ban.name_too_long").replace("<length>", String.valueOf(punishmentManager.getMaximumNickLength())));
                return true;
            }
            String admin = sender.getName();
            String reason = (args.length > 1 ? localeManager.createMessage(args, 1) : null);
            UUID uuid = (userManager.getUserIgnoreCase(target) == null ? null : userManager.getUserIgnoreCase(target).getUUID());
            Punishment punishment = punishmentManager.createPunishment(type, uuid, target, admin, reason, null);
            sender.sendMessage(l.getColoredString("ban.ban").replace("<name>", target));
            Player p = Bukkit.getPlayerExact(target);
            if (Bukkit.getPlayerExact(target) != null) {
                p.kickPlayer(punishmentManager.formatMessage(punishment, userManager.determineLocale(p), "kick"));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.ban")) {
            if (args.length == 1) {
                return null;
            } else {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.reason"), false));
            }
        }
        return Collections.emptyList();
    }
}
