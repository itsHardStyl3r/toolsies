package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.LocationManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.Spawn;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class delspawnCommand implements CommandExecutor, TabCompleter {

    private final LocaleManager localeManager;
    private final UserManager userManager;
    private final LocationManager locationManager;

    public delspawnCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager, LocationManager locationManager) {
        plugin.getCommand("delspawn").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.locationManager = locationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            if (args.length > 1) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.delspawn")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.delspawn"));
            return true;
        }
        if (args.length <= 1) {
            Spawn s = locationManager.getSpawn(args[0]);
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length == 0) {
                    s = locationManager.getSpawn(p.getLocation());
                    if (!locationManager.isLocationIdentical(s.getLocation(), p.getLocation())) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("delspawn.no_spawn_specified")));
                        return true;
                    }
                }
            }
            if (s == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("delspawn.unknown_spawn")).replace("<name>", args[0]));
                return true;
            }
            if (s.isDefault()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("delspawn.delspawn_default")).replace("<name>", s.getName()));
                return true;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("delspawn.delspawn")).replace("<name>", s.getName()));
            locationManager.deleteSpawn(s);
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.delspawn")) {
            if (args.length == 1) return localeManager.formatTabArguments(args[0], locationManager.getSpawns());
        }
        return Collections.emptyList();
    }
}
