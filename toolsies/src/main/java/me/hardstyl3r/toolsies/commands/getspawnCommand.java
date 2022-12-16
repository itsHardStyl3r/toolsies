package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.LocationManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
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
            if (args.length != 1) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.getspawn")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.getspawn")));
            return true;
        }
        if (args.length < 2) {
            Location spawn = locationManager.getDefaultSpawn();
            if (sender instanceof Player) {
                spawn = locationManager.getSpawn(((Player) sender).getWorld());
            }
            if (args.length == 1) {
                World w = Bukkit.getWorld(args[0]);
                if (w == null) {
                    sender.sendMessage(l.getStringComponent("getspawn.unknown_world", Placeholder.unparsed("name", args[0])));
                    return true;
                }
                spawn = locationManager.getSpawn(w);
            }
            sender.sendMessage(l.getStringComponent("getspawn.details",
                    Placeholder.unparsed("name", spawn.getWorld().getName()),
                    Placeholder.unparsed("x", String.valueOf(spawn.getBlockX())),
                    Placeholder.unparsed("y", String.valueOf(spawn.getBlockY())),
                    Placeholder.unparsed("z", String.valueOf(spawn.getBlockZ())),
                    Placeholder.unparsed("yaw", String.valueOf(spawn.getYaw())),
                    Placeholder.unparsed("pitch", String.valueOf(spawn.getPitch())),
                    Placeholder.unparsed("preferred", localeManager.translateBoolean(l,
                            locationManager.isSpawnPreferred(spawn.getWorld())))));
            return true;
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