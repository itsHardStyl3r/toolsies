package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.LocationManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

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
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(l.getStringComponent("console_sender"));
            return true;
        }
        if (!sender.hasPermission("toolsies.setspawn")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.setspawn")));
            return true;
        }
        Location location = ((Player) sender).getLocation();
        World w = location.getWorld();
        Location currentSpawn = locationManager.getSpawn(w);
        if (locationManager.isLocationIdentical(location, currentSpawn)) {
            if (locationManager.getDefaultSpawn().getWorld() == w) {
                sender.sendMessage(l.getStringComponent("setspawn.spawn_always_preferred"));
                return true;
            }
            locationManager.switchPreferredSpawn(w);
            sender.sendMessage(l.getStringComponent("setspawn.spawn_"
                    + (locationManager.isSpawnPreferred(w) ? "" : "un") + "set_preferred"));
            return true;
        }
        locationManager.setSpawn(location);
        sender.sendMessage(l.getStringComponent("setspawn.setspawn", Placeholder.unparsed("world_name", w.getName())));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}