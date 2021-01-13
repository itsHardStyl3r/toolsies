package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.KitManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class kitCommand implements CommandExecutor, TabCompleter {

    private final KitManager kitManager;
    private final UserManager userManager;

    public kitCommand(Toolsies plugin, UserManager userManager, KitManager kitManager) {
        plugin.getCommand("kit").setExecutor(this);
        this.userManager = userManager;
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command does not work in console.");
            return true;
        }
        User u = userManager.getUser(sender);
        if (!sender.hasPermission("toolsies.kit")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    u.getLocale().getConfig().getString("no_permission")).replace("<permission>", "toolsies.kit"));
            return true;
        }
        if (args.length >= 1 && args.length <= 2) {
            String kit = args[0];
            if (!kitManager.isKit(kit) || !sender.hasPermission("toolsies.kits." + kit.toLowerCase())) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        u.getLocale().getConfig().getString("kit.unknown_kit")).replace("<name>", kit));
                return true;
            }
            Player target = (Player) sender;
            if (args.length == 2) {
                if (sender.hasPermission("toolsies.kits.others")) {
                    target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                u.getLocale().getConfig().getString("players.unknown")).replace("<name>", args[1]));
                        return true;
                    }
                    if (!sender.hasPermission("toolsies.kits.others.bypass")) {
                        if (!target.hasPermission("toolsies.kits." + kit) || !sender.hasPermission("toolsies.kits.others." + kit)) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    u.getLocale().getConfig().getString("kit.cant_gift_to_that_player")).replace("<name>", kit).replace("<player>", target.getName()));
                            return true;
                        }
                    }
                    User utarget = userManager.getUser(target);
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            utarget.getLocale().getConfig().getString("kit.player_gifted")).replace("<name>", kit).replace("<admin>", sender.getName()));
                }
            }
            kitManager.giveKit(target, kit);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    u.getLocale().getConfig().getString("kit." + (sender == target ? "kit_applied" : "gifted_kit_to_player"))).replace("<name>", kit).replace("<player>", target.getName()));
            return true;
        }
        Set<String> all = kitManager.getKits(sender);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                u.getLocale().getConfig().getString("kit." + (all.isEmpty() ? "no_kits_available" : "available_kits"))).replace("<kits>", all.toString()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("toolsies.kit")) {
            return Collections.emptyList();
        }
        List<String> allarguments = new ArrayList<>(kitManager.getKits(sender));
        if (args.length == 1) {
            ArrayList<String> firstargument = new ArrayList<>();
            if (!args[0].isEmpty()) {
                for (String arg : allarguments) {
                    if (arg.toLowerCase().startsWith(args[0].toLowerCase())) {
                        firstargument.add(arg);
                    }
                }
            } else {
                return allarguments;
            }
            return firstargument;
        }
        if (args.length == 2) {
            if (!sender.hasPermission("toolsies.kits.others")) {
                return Collections.emptyList();
            }
            return null;
        }
        return Collections.emptyList();
    }
}
