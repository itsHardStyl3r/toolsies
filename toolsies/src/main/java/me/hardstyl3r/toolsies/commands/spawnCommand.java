package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.LocationManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
            if (args.length != 1) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.spawn")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.spawn")));
            return true;
        }
        Player target = null;
        if (sender instanceof Player) {
            target = (Player) sender;
        }
        Location spawn = locationManager.getDefaultSpawn();
        if (args.length == 1 && sender.hasPermission("toolsies.spawn.others")) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(l.getStringComponent("players.unknown", Placeholder.unparsed("player_name", args[0])));
                return true;
            }
        }
        if (locationManager.isLocationIdenticalExact(spawn, target.getLocation())) {
            sender.sendMessage(l.getStringComponent("spawn.currently_on_spawn" + (target == sender ? "" : "_sender"), Placeholder.unparsed("player_name", target.getName())));
            return true;
        }
        target.teleport(spawn);
        if (target == sender) {
            sender.sendMessage(l.getStringComponent("spawn.teleported_to_spawn"));
        } else {
            sender.sendMessage(l.getStringComponent("spawn.teleported_to_spawn_sender", Placeholder.unparsed("player_name", target.getName())));
            target.sendMessage(userManager.determineLocale(target).getStringComponent("spawn.teleported_to_spawn_target", Placeholder.unparsed("sender_name", sender.getName())));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.spawn"))
            if (args.length == 1 && sender.hasPermission("toolsies.spawn.others")) return null;
        return Collections.emptyList();
    }
}