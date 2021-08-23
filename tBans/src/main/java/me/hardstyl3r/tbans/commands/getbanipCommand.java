package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
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

import java.net.InetAddress;

public class getbanipCommand implements CommandExecutor {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public getbanipCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("getban-ip").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.getban-ip")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.getban-ip"));
            return true;
        }
        if (args.length == 1) {
            Player p = Bukkit.getPlayerExact(args[0]);
            InetAddress target;
            if (p == null) {
                try {
                    target = InetAddress.getByName(args[0]);
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("ban-ip.incorrect_address")));
                    return true;
                }
            } else {
                target = p.getAddress().getAddress();
            }
            punishmentManager.deleteIfExpired(target);
            if (!punishmentManager.isBanned(target)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("unban.is_not_banned")).replace("<name>", target.getHostAddress()));
                return true;
            }
            Punishment ban = punishmentManager.getBan(target);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("getban.getban_header")).replace("<name>", target.getHostAddress()).replace("<id>", String.valueOf(ban.getId())));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("getban.entries.type")).replace("<type>", ban.getType().name()));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("getban.entries.admin")).replace("<admin>", ban.getAdmin()));
            if (ban.getReason() != null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("getban.entries.reason")).replace("<reason>", ban.getReason()));
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("getban.entries.date")).replace("<date>", localeManager.getFullDate(ban.getDate())));
            if (ban.getDuration() != null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("getban.entries.duration")).replace("<duration>", localeManager.getFullDate(ban.getDuration())));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("getban.entries.remaining")).replace("<remaining>", localeManager.parseTimeWithTranslate(ban.getRemaining(), l)));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }
}
