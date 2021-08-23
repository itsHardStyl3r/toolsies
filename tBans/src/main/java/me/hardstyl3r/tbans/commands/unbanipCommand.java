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

public class unbanipCommand implements CommandExecutor {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public unbanipCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("unban-ip").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.unban-ip")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.unban-ip"));
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
            punishmentManager.deletePunishment(ban, sender.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("unban.unban")).replace("<name>", ban.getName()));
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }
}