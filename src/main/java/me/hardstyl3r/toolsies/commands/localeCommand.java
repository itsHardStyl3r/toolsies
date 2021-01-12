package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class localeCommand implements CommandExecutor {

    private final UserManager userManager;
    private final LocaleManager localeManager;

    public localeCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager) {
        plugin.getCommand("locale").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("This command does not work in console.");
            return true;
        }
        User u = userManager.getUser(sender);
        if (!sender.hasPermission("toolsies.locale")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', u.getLocale().getConfig().getString("no_permission")).replace("<permission>", "toolsies.locale"));
            return true;
        }
        if(args.length == 1){
            if(localeManager.getLocale(args[0]) == null){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', u.getLocale().getConfig().getString("locale.unknown_locale")).replace("<name>", args[0]));
                return true;
            }
            Locale l = localeManager.getLocale(args[0]);
            if(u.getLocale().equals(l)){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', u.getLocale().getConfig().getString("locale.current_locale")).replace("<name>", l.getName()));
                return true;
            }
            u.setLocale(l);
            userManager.updateUser(u);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("locale.changed_own")).replace("<name>", l.getName()));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', u.getLocale().getConfig().getString("locale.available_locales")).replace("<locales>", localeManager.getLocales().toString()));
        }
        return true;
    }
}
