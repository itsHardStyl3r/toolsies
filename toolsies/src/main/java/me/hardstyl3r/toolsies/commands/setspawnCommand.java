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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class setspawnCommand implements CommandExecutor, TabCompleter {

    private final LocaleManager localeManager;
    private final UserManager userManager;
    private final LocationManager locationManager;

    public setspawnCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager, LocationManager locationManager) {
        plugin.getCommand("setspawn").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.locationManager = locationManager;
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
        if (!sender.hasPermission("toolsies.setspawn")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.setspawn"));
            return true;
        }
        if (args.length == 0) {
            Player p = (Player) sender;
            if (locationManager.getSpawn(p.getLocation()) != null && locationManager.isLocationIdenticalExact(locationManager.getSpawn(p.getLocation()).getLocation(), p.getLocation())) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("setspawn.spawn_is_current_location")));
                return true;
            }
            boolean def = locationManager.getDefaultSpawn() == null;
            Spawn s = locationManager.setSpawn(p);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("setspawn.setspawn" + (def ? "_default" : ""))).replace("<name>", s.getName()));
        } else if (args.length == 2) {
            Spawn s = locationManager.getSpawn(args[0]);
            if (args[1].equalsIgnoreCase("preferred")) {
                boolean b = !s.isPreferred();
                s.setPreferred(b);
                locationManager.saveSpawn(s);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("setspawn.set_preferred_" + b)).replace("<name>", s.getName()));
            } else if (args[1].equalsIgnoreCase("default")) {
                Spawn sold = locationManager.getDefaultSpawn();
                if (s.isDefault()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("setspawn.set_default_already")).replace("<name>", s.getName()));
                    return true;
                }
                s.setDefault(true);
                if (sold != null) {
                    sold.setDefault(false);
                    locationManager.saveSpawn(sold);
                }
                locationManager.saveSpawn(s);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("setspawn.set_default")).replace("<name>", s.getName()));
            } else {
                localeManager.sendUsage(sender, cmd, l);
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("toolsies.setspawn")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            ArrayList<String> worlds = locationManager.getSpawns();
            return (worlds.isEmpty() ? Collections.emptyList() : localeManager.formatTabArguments(args[0], worlds));
        } else if (args.length == 2) {
            return localeManager.formatTabArguments(args[1], Arrays.asList("preferred", "default"));
        }
        return Collections.emptyList();
    }
}