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
import org.bukkit.entity.Player;

import java.util.UUID;

public class warnCommand implements CommandExecutor {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public warnCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("warn").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.warn")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.warn"));
            return true;
        }
        if (args.length > 1) {
            String target = args[0];
            if (target.length() > punishmentManager.getMaximumNickLength()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("ban.name_too_long")).replace("<length>", String.valueOf(punishmentManager.getMaximumNickLength())));
                return true;
            }
            if (userManager.getUser(target) == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("players.unknown")).replace("<name>", args[0]));
                return true;
            }
            String admin = sender.getName();
            String reason = localeManager.createMessage(args, 1);
            UUID uuid = userManager.getUserIgnoreCase(target).getUUID();
            Punishment punishment = punishmentManager.createPunishment(PunishmentType.WARN, uuid, target, admin, reason, null);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("warn.warn_sender")).replace("<name>", target));
            Player p = Bukkit.getPlayerExact(target);
            if (Bukkit.getPlayerExact(target) != null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        userManager.determineLocale(p).getConfig().getString("warn.warn_target"))
                        .replace("<admin>", punishment.getAdmin())
                        .replace("<reason>", punishment.getReason()));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }
}
