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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.net.Inet6Address;
import java.net.InetAddress;

public class banipCommand implements CommandExecutor {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;
    private final FileConfiguration config;

    public banipCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager, FileConfiguration config) {
        plugin.getCommand("ban-ip").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.ban-ip")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.ban-ip"));
            return true;
        }
        if (args.length > 0) {
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
            if (target instanceof Inet6Address) {
                if (!config.getBoolean("ipv6BansEnabled")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("ban-ip.ipv6_address")));
                    return true;
                }
            }
            punishmentManager.deleteIfExpired(target);
            if (punishmentManager.isBanned(target)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("ban-ip.is_banned")).replace("<address>", target.getHostAddress()));
                return true;
            }
            String admin = sender.getName();
            String reason = (args.length > 1 ? localeManager.createMessage(args, 1) : null);
            Punishment punishment = punishmentManager.createPunishment(target, admin, reason, null);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("ban-ip.ban-ip")).replace("<address>", target.getHostAddress()));
            for (Player kick : Bukkit.getOnlinePlayers()) {
                if (kick.getAddress().getAddress().equals(target)) {
                    kick.kickPlayer(punishmentManager.formatMessage(punishment, userManager.determineLocale(kick), "kick"));
                }
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }
}