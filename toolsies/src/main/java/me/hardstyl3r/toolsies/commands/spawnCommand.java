package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.LocationManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.Spawn;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
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
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.spawn")));
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
                    sender.sendMessage(l.getStringComponent("players.unknown", Placeholder.unparsed("name", args[1])));
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
                    sender.sendMessage(l.getStringComponent("getspawn.unknown_world", Placeholder.unparsed("name", args[0])));
                    return true;
                }
                s = locationManager.getSpawn(Bukkit.getWorld(args[0]));
            }
            if (s == null) {
                sender.sendMessage(l.getStringComponent("spawn.unknown_spawn", Placeholder.unparsed("name", args[0])));
                return true;
            }
            if (locationManager.isLocationIdenticalExact(s.getLocation(), target.getLocation())) {
                sender.sendMessage(l.getStringComponent("spawn.spawn_is_current_location" + (target == sender ? "" : "_sender"), Placeholder.unparsed("name", target.getName())));
                return true;
            }
            target.teleport(s.getLocation());
            if (target == sender) {
                sender.sendMessage(l.getStringComponent("spawn.teleported_to_spawn"));
            } else {
                sender.sendMessage(l.getStringComponent("spawn.teleported_to_spawn_sender", Placeholder.unparsed("name", target.getName())));
                target.sendMessage(userManager.getUser(target).getLocale().getStringComponent("spawn.teleported_to_spawn_sender", Placeholder.unparsed("admin", sender.getName())));
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