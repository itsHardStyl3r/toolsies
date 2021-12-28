package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.LocationManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.Spawn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class getspawnCommand implements CommandExecutor, TabCompleter {

    private final LocaleManager localeManager;
    private final UserManager userManager;
    private final LocationManager locationManager;

    public getspawnCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager, LocationManager locationManager) {
        plugin.getCommand("getspawn").setExecutor(this);
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
        if (!sender.hasPermission("toolsies.getspawn")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("no_permission")).replace("<permission>", "toolsies.getspawn"));
            return true;
        }
        if (args.length <= 1) {
            World w = Bukkit.getWorlds().get(0);
            if (sender instanceof Player) {
                w = ((Player) sender).getWorld();
            }
            if (args.length == 1) {
                if (Bukkit.getWorld(args[0]) == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getString("getspawn.unknown_world"))
                            .replace("<name>", args[0]));
                    return true;
                } else if (locationManager.getSpawn(args[0]) == null) {
                    Location loc = Bukkit.getWorld(args[0]).getSpawnLocation();
                    for (String m : l.getStringList("getspawn.small")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', m)
                                .replace("<name>", loc.getWorld().getName())
                                .replace("<x>", String.valueOf(loc.getBlockX()))
                                .replace("<y>", String.valueOf(loc.getBlockY()))
                                .replace("<z>", String.valueOf(loc.getBlockZ()))
                                .replace("<yaw>", String.valueOf(loc.getYaw()))
                                .replace("<pitch>", String.valueOf(loc.getPitch())));
                    }
                    return true;
                }
                w = Bukkit.getWorld(args[0]);
            }
            if (locationManager.getSpawn(w) == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("getspawn.unknown_spawn"))
                        .replace("<name>", args[0]));
                return true;
            }
            Spawn s = locationManager.getSpawn(w);
            for (String m : l.getStringList("getspawn.details")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', m)
                        .replace("<name>", s.getLocation().getWorld().getName())
                        .replace("<x>", String.valueOf(s.getLocation().getBlockX()))
                        .replace("<y>", String.valueOf(s.getLocation().getBlockY()))
                        .replace("<z>", String.valueOf(s.getLocation().getBlockZ()))
                        .replace("<yaw>", String.valueOf(s.getLocation().getYaw()))
                        .replace("<pitch>", String.valueOf(s.getLocation().getPitch()))
                        .replace("<distance>", (sender instanceof Player && s.getLocation().getWorld() == ((Player) sender).getWorld() ? String.valueOf(Math.round(s.getLocation().distance(((Player) sender).getLocation()))) : "N/A"))
                        .replace("<owner>", userManager.getUser(s.getOwner()).getName())
                        .replace("<time>", String.valueOf(s.getAdded()))
                        .replace("<preferred>", localeManager.translateBoolean(l, s.isPreferred()))
                        .replace("<default>", localeManager.translateBoolean(l, s.isDefault())));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.getspawn")) {
            if (args.length == 1) {
                ArrayList<String> worlds = new ArrayList<>();
                for (World w : Bukkit.getWorlds()) worlds.add(w.getName());
                return localeManager.formatTabArguments(args[0], worlds);
            }
        }
        return Collections.emptyList();
    }
}