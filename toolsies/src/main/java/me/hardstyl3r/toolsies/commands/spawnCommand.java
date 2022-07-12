package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.LocationManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.Spawn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class spawnCommand implements CommandExecutor, TabCompleter {

    private final LocaleManager localeManager;
    private final UserManager userManager;
    private final LocationManager locationManager;

    public spawnCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager, LocationManager locationManager) {
        plugin.getCommand("spawn").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.locationManager = locationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            if (args.length != 2) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.spawn")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("no_permission")).replace("<permission>", "toolsies.spawn"));
            return true;
        }
        if (args.length <= 2) {
            Player target = null;
            if (sender instanceof Player) {
                target = (Player) sender;
            }
            if (args.length == 2) {
                if (!sender.hasPermission("toolsies.spawn.others")) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
                target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getString("players.unknown")).replace("<name>", args[1]));
                    return true;
                }
            }
            Spawn s = locationManager.getDefaultSpawn();
            if (args.length >= 1) {
                if (!sender.hasPermission("toolsies.spawn.bypass")) {
                    if (!sender.hasPermission("toolsies.spawn." + args[0].toLowerCase())) {
                        localeManager.sendUsage(sender, cmd, l);
                        return true;
                    }
                }
                if (Bukkit.getWorld(args[0]) == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    l.getString("getspawn.unknown_world"))
                            .replace("<name>", args[0]));
                    return true;
                }
                s = locationManager.getSpawn(Bukkit.getWorld(args[0]));
            }
            if (s == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getString("spawn.unknown_spawn"))
                        .replace("<name>", args[0]));
                return true;
            }
            if (locationManager.isLocationIdenticalExact(s.getLocation(), target.getLocation())) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("spawn.spawn_is_current_location" + (target == sender ? "" : "_sender"))
                                .replace("<name>", target.getName())));
                return true;
            }
            target.teleport(s.getLocation());
            if (target == sender) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("spawn.teleported_to_spawn")));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getString("spawn.teleported_to_spawn_sender"))
                        .replace("<name>", target.getName()));
                target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                userManager.getUser(target).getLocale().getString("spawn.teleported_to_spawn_target"))
                        .replace("<admin>", sender.getName()));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.spawn")) {
            if (args.length == 1)
                return localeManager.formatTabArguments(args[0], locationManager.getAvailableSpawns(sender));
            if (args.length == 2) if (sender.hasPermission("toolsies.spawn.others")) return null;
        }
        return Collections.emptyList();
    }
}