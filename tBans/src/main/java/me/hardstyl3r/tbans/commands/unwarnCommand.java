package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class unwarnCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public unwarnCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("unwarn").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.unwarn")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("no_permission")).replace("<permission>", "toolsies.unwarn"));
            return true;
        }
        if (args.length == 1) {
            if (!StringUtils.isNumeric(args[0])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("unwarn.wrong_id")));
                return true;
            }
            int i = Integer.parseInt(args[0]);
            Punishment warn = punishmentManager.getPunishmentById(PunishmentType.WARN, i);
            punishmentManager.deleteIfExpired(warn);
            if (warn == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("unwarn.no_such_warn")).replace("<id>", args[0]));
                return true;
            }
            punishmentManager.deletePunishment(warn, sender.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("unwarn.unwarn")).replace("<id>", args[0]));
            Player p = Bukkit.getPlayer(warn.getUUID());
            if (Bukkit.getPlayer(warn.getUUID()) != null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        userManager.determineLocale(p).getString("unwarn.unwarn_target"))
                        .replace("<admin>", sender.getName()));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.unwarn")) {
            if (args.length == 1) {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.id"), true));
            }
        }
        return Collections.emptyList();
    }
}
