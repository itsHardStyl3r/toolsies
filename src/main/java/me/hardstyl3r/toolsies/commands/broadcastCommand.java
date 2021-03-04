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
import org.bukkit.entity.Player;

public class broadcastCommand implements CommandExecutor {

    private final LocaleManager localeManager;
    private final UserManager userManager;

    public broadcastCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager) {
        plugin.getCommand("broadcast").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = localeManager.getDefault();
        if (sender instanceof Player) {
            l = userManager.getUser(sender).getLocale();
        } else {
            if (args.length == 1) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.broadcast")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.broadcast"));
            return true;
        }
        if(args.length >= 1){
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', localeManager.getConfig().getString("broadcast").replace("<message>", localeManager.createMessage(args, 0))));
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }
}
