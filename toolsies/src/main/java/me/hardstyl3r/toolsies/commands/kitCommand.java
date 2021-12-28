package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.KitManager;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
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
    private final LocaleManager localeManager;

    public kitCommand(Toolsies plugin, UserManager userManager, KitManager kitManager, LocaleManager localeManager) {
        plugin.getCommand("kit").setExecutor(this);
        this.userManager = userManager;
        this.kitManager = kitManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            if (args.length == 1) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.kit")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("no_permission")).replace("<permission>", "toolsies.kit"));
            return true;
        }
        Set<String> kits = kitManager.getKits(sender);
        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("kit." + (kits.isEmpty() ? "no_kits_available" : "available_kits"))).replace("<kits>", kits.toString()));
        } else if (args.length >= 1 && args.length <= 2) {
            String kit = args[0].toLowerCase();
            if (!kitManager.isKit(kit) || !sender.hasPermission("toolsies.kits." + kit)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("kit." + (kits.isEmpty() ? "no_kits_available" : "available_kits"))).replace("<kits>", kits.toString()));
                return true;
            }
            Player target = null;
            if (args.length == 2) {
                if (sender.hasPermission("toolsies.kit.others")) {
                    target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getString("players.unknown")).replace("<name>", args[1]));
                        return true;
                    }
                    if (!sender.hasPermission("toolsies.kit.others.bypass")) {
                        if (!target.hasPermission("toolsies.kits." + kit) || !sender.hasPermission("toolsies.kit.others." + kit)) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    l.getString("kit." + (kits.isEmpty() ? "no_kits_available" : "available_kits"))).replace("<kits>", kits.toString()));
                            return true;
                        }
                    }
                }
            } else {
                target = (Player) sender;
            }
            kitManager.giveKit(target, kit);
            User utarget = userManager.getUser(target);
            if (sender == target) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("kit.kit_applied")).replace("<name>", kit));
            } else {
                target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        utarget.getLocale().getString("kit.player_gifted")).replace("<name>", kit).replace("<admin>", sender.getName()));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("kit.gifted_kit_to_player")).replace("<name>", kit).replace("<player>", target.getName()));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.kit")) {
            List<String> allarguments = new ArrayList<>(kitManager.getKits(sender));
            if (args.length == 1) return localeManager.formatTabArguments(args[0], allarguments);
            if (sender.hasPermission("toolsies.kit.others")) {
                if (args.length == 2) {
                    if (sender.hasPermission("toolsies.kit.others." + args[0]) || sender.hasPermission("toolsies.kit.others.bypass")) {
                        return null;
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
