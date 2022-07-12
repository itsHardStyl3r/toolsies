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

public class unmuteCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public unmuteCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("unmute").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.unmute")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("no_permission")).replace("<permission>", "toolsies.unmute"));
            return true;
        }
        if (args.length == 1) {
            String target = args[0];
            punishmentManager.deleteIfExpired(PunishmentType.MUTE, target);
            if (!punishmentManager.isPunished(PunishmentType.MUTE, target)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("unmute.is_not_muted")).replace("<name>", target));
                return true;
            }
            Punishment mute = punishmentManager.getPunishment(PunishmentType.MUTE, target);
            punishmentManager.deletePunishment(mute, sender.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("unmute.unmute")).replace("<name>", mute.getName()));
            Player p = Bukkit.getPlayer(mute.getUUID());
            if (Bukkit.getPlayer(mute.getUUID()) != null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                userManager.determineLocale(p).getString("unmute.unmute_target"))
                        .replace("<admin>", sender.getName()));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.unmute")) {
            if (args.length == 1) {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.player"), true));
            }
        }
        return Collections.emptyList();
    }
}
