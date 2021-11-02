package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
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

public class kickCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;

    public kickCommand(TBans plugin, UserManager userManager, LocaleManager localeManager) {
        plugin.getCommand("kick").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.kick")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.kick"));
            return true;
        }
        if (args.length > 0) {
            String target = args[0];
            if (userManager.getUser(target) == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("players.unknown")).replace("<name>", args[0]));
                return true;
            }
            if (Bukkit.getPlayerExact(target) == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("players.offline")).replace("<name>", args[0]));
                return true;
            }
            String reason = (args.length > 1 ? localeManager.createMessage(args, 1) : null);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("kick.kick")).replace("<name>", target));
            Player p = Bukkit.getPlayerExact(target);
            if (Bukkit.getPlayerExact(target) != null) {
                p.kickPlayer(ChatColor.translateAlternateColorCodes('&', constructMessage(userManager.determineLocale(p), sender.getName(), reason)));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    private String constructMessage(Locale l, String admin, String reason) {
        String message = l.getConfig().getString("kick.kickmessage.header");
        message += "\n" + l.getConfig().getString("kick.kickmessage.admin").replace("<admin>", admin);
        if (reason != null) {
            message += "\n" + l.getConfig().getString("kick.kickmessage.reason").replace("<reason>", reason);
        }
        return message;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.kick")) {
            if (args.length == 1) {
                return null;
            } else {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getConfig().getString("common.reason"), false));
            }
        }
        return Collections.emptyList();
    }
}
