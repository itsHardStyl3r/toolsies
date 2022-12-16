package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.LocationManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.Spawn;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.getspawn")));
            return true;
        }
        if (args.length <= 1) {
            World w = Bukkit.getWorlds().get(0);
            if (sender instanceof Player) {
                w = ((Player) sender).getWorld();
            }
            if (args.length == 1) {
                if (Bukkit.getWorld(args[0]) == null) {
                    sender.sendMessage(l.getStringComponent("getspawn.unknown_world", Placeholder.unparsed("name", args[0])));
                    return true;
                } else if (locationManager.getSpawn(args[0]) == null) {
                    Location loc = Bukkit.getWorld(args[0]).getSpawnLocation();
                    sender.sendMessage(l.getStringComponent("getspawn.small",
                            Placeholder.unparsed("<name>", loc.getWorld().getName()),
                            Placeholder.unparsed("<x>", String.valueOf(loc.getBlockX())),
                            Placeholder.unparsed("<y>", String.valueOf(loc.getBlockY())),
                            Placeholder.unparsed("<z>", String.valueOf(loc.getBlockZ())),
                            Placeholder.unparsed("<yaw>", String.valueOf(loc.getYaw())),
                            Placeholder.unparsed("<pitch>", String.valueOf(loc.getPitch()))));
                    return true;
                }
                w = Bukkit.getWorld(args[0]);
            }
            if (locationManager.getSpawn(w) == null) {
                sender.sendMessage(l.getStringComponent("getspawn.unknown_spawn", Placeholder.unparsed("name", args[0])));
                return true;
            }
            Spawn s = locationManager.getSpawn(w);
            sender.sendMessage(l.getStringComponent("getspawn.details",
                    Placeholder.unparsed("<name>", s.getLocation().getWorld().getName()),
                    Placeholder.unparsed("<x>", String.valueOf(s.getLocation().getBlockX())),
                    Placeholder.unparsed("<y>", String.valueOf(s.getLocation().getBlockY())),
                    Placeholder.unparsed("<z>", String.valueOf(s.getLocation().getBlockZ())),
                    Placeholder.unparsed("<yaw>", String.valueOf(s.getLocation().getYaw())),
                    Placeholder.unparsed("<pitch>", String.valueOf(s.getLocation().getPitch())),
                    Placeholder.unparsed("<distance>", (sender instanceof Player && s.getLocation().getWorld() == ((Player) sender).getWorld() ? String.valueOf(Math.round(s.getLocation().distance(((Player) sender).getLocation()))) : "N/A")),
                    Placeholder.unparsed("<owner>", userManager.getUser(s.getOwner()).getName()),
                    Placeholder.unparsed("<time>", String.valueOf(s.getAdded())),
                    Placeholder.unparsed("<preferred>", localeManager.translateBoolean(l, s.isPreferred())),
                    Placeholder.unparsed("<default>", localeManager.translateBoolean(l, s.isDefault()))));
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