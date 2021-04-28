package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
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

public class broadcastCommand implements CommandExecutor, TabCompleter {

    private final LocaleManager localeManager;
    private final UserManager userManager;

    public broadcastCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager) {
        plugin.getCommand("broadcast").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.broadcast")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.broadcast"));
            return true;
        }
        if (args.length > 0) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', localeManager.getConfig().getString("broadcast").replace("<message>", localeManager.createMessage(args, 0))));
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.broadcast")) {
            if (args.length == 1) {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getConfig().getString("common.message"), true));
            }
        }
        return Collections.emptyList();
    }
}
