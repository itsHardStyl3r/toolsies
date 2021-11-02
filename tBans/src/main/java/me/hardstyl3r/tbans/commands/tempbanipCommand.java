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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class tempbanipCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;
    private final FileConfiguration config;

    public tempbanipCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager, FileConfiguration config) {
        plugin.getCommand("tempban-ip").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.tempban-ip")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.tempban-ip"));
            return true;
        }
        if (args.length > 1) {
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
            if (!localeManager.isValidStringTime(args[1])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("tempban.incorrect_time")));
                return true;
            }
            long duration = localeManager.parseTimeFromString(args[1]);
            long minimumDuration = punishmentManager.getMinimumDuration(PunishmentType.BAN);
            if (duration < minimumDuration) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("tempban.duration_too_short")).replace("<duration>", localeManager.parseTimeWithTranslate(minimumDuration, l)));
                return true;
            }
            String admin = sender.getName();
            String reason = (args.length > 2 ? localeManager.createMessage(args, 2) : null);
            Punishment punishment = punishmentManager.createPunishment(target, admin, reason, duration);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("tempban-ip.tempban-ip")).replace("<address>", target.getHostAddress()));
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.tempban")) {
            Locale l = userManager.determineLocale(sender);
            if (args.length == 1) {
                ArrayList<String> ips = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ips.add(p.getAddress().getAddress().getHostAddress());
                }
                return localeManager.formatTabArguments(args[0], ips);
            } else if(args.length == 2) {
                return Collections.singletonList(localeManager.formatArgument(l.getConfig().getString("common.duration"), true));
            } else if(args.length == 3) {
                return Collections.singletonList(localeManager.formatArgument(l.getConfig().getString("common.player"), true));
            } else {
                return Collections.singletonList(localeManager.formatArgument(l.getConfig().getString("common.reason"), false));
            }
        }
        return Collections.emptyList();
    }
}
