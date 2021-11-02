package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class getbanCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public getbanCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("getban").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.getban")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.getban"));
            return true;
        }
        if (args.length == 1) {
            String target = args[0];
            punishmentManager.deleteIfExpired(PunishmentType.BAN, target);
            if (!punishmentManager.isPunished(PunishmentType.BAN, target)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("unban.is_not_banned")).replace("<name>", target));
                return true;
            }
            Punishment ban = punishmentManager.getPunishment(PunishmentType.BAN, target);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("getban.getban_header")).replace("<name>", target).replace("<id>", String.valueOf(ban.getId())));
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
            if (ban.getUUID() != null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("getban.entries.uuid")).replace("<uuid>", ban.getUUID().toString()));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.getban")) {
            if (args.length == 1) {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getConfig().getString("common.player"), true));
            }
        }
        return Collections.emptyList();
    }
}
