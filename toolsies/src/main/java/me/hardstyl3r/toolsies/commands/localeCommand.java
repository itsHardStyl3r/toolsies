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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class localeCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;

    public localeCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager) {
        plugin.getCommand("locale").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("console_sender")));
            return true;
        }
        if (!sender.hasPermission("toolsies.locale")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.locale"));
            return true;
        }
        if (args.length == 1) {
            if (localeManager.getLocale(args[0]) == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("locale.unknown_locale")).replace("<name>", args[0]));
                return true;
            }
            Locale locale = localeManager.getLocale(args[0]);
            if (l.equals(locale)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("locale.current_locale")).replace("<name>", locale.getName()));
                return true;
            }
            User u = userManager.getUser(sender);
            u.setLocale(locale);
            userManager.updateUser(u);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', locale.getConfig().getString("locale.changed_own")).replace("<name>", locale.getName()));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("locale.available_locales")).replace("<locales>", localeManager.getLocales().toString()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("toolsies.locale")) {
            return Collections.emptyList();
        }
        List<String> allarguments = new ArrayList<>(localeManager.getLocales());
        allarguments.remove(userManager.getUser(sender).getLocale().getName());
        if (args.length == 1) {
            return localeManager.formatTabArguments(args[0], allarguments);
        }
        return Collections.emptyList();
    }
}
