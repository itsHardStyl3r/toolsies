package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.PermissionsManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Group;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class groupCommand implements CommandExecutor {

    private final UserManager userManager;
    private final PermissionsManager permissionManager;
    private final LocaleManager localeManager;

    public groupCommand(Toolsies plugin, UserManager userManager, PermissionsManager permissionManager, LocaleManager localeManager) {
        plugin.getCommand("group").setExecutor(this);
        this.userManager = userManager;
        this.permissionManager = permissionManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = localeManager.getDefault();
        if (sender instanceof Player) {
            l = userManager.getUser(sender).getLocale();
        } else {
            if (!(args.length == 1 || args.length == 3)) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.group")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("no_permission"))
                    .replace("<permission>", "toolsies.group"));
            return true;
        }
        if (args.length == 0) {
            User u = userManager.getUser(sender);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("group.current_group"))
                    .replace("<group>", u.getMainGroup().getName() + (sender.isOp() ? " (" + permissionManager.listGroups(u.getGroups()) + ")" : "")));
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("toolsies.group.list")) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
                List<String> groups = permissionManager.getGroups();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("group.list" + (groups.size() == 0 ? "_none" : "")))
                        .replace("<group>", groups.toString()));
            } else if (userManager.getUser(args[0]) != null) {
                if (!sender.hasPermission("toolsies.group.others")) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
                User u = userManager.getUser(args[0]);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("group.current_group_sender"))
                        .replace("<player>", u.getName())
                        .replace("<group>", u.getMainGroup().getName() + (sender.isOp() ? " (" + permissionManager.listGroups(u.getGroups()) + ")" : "")));
            } else {
                localeManager.sendUsage(sender, cmd, l);
            }
        } else if (args.length <= 3) {
            List<String> arguments = Arrays.asList("set", "add", "remove", "info");
            if (!arguments.contains(args[0])) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
            for (String s : arguments) {
                if (args[0].equalsIgnoreCase(s) && !sender.hasPermission("toolsies.group." + s)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("no_permission"))
                            .replace("<permission>", "toolsies.group." + s));
                    return true;
                }
            }
            if (permissionManager.getGroup(args[1]) == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("group.unknown_group"))
                        .replace("<name>", args[1]));
                return true;
            }
            Group group = permissionManager.getGroup(args[1]);
            Player target = null;
            User uTarget = null;
            if (sender instanceof Player) {
                target = ((Player) sender);
                uTarget = userManager.getUser(target);
            }
            if (args.length == 3) {
                target = Bukkit.getPlayerExact(args[2]);
                if (userManager.getUser(args[2]) != null) {
                    uTarget = userManager.getUser(args[2]);
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("players.unknown"))
                            .replace("<name>", args[2]));
                    return true;
                }
            }
            ArrayList<Group> groups = new ArrayList<>(uTarget.getGroups());
            if (args[0].equalsIgnoreCase("set")) {
                if (groups.size() == 1 && groups.contains(group)) {
                    if (target == sender) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("group.current_group_already"))
                                .replace("<name>", group.getName()));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("group.current_group_already_sender"))
                                .replace("<name>", group.getName())
                                .replace("<player>", uTarget.getName()));
                    }
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("group.set_group"))
                            .replace("<name>", group.getName()));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("group.set_group_sender"))
                            .replace("<name>", group.getName())
                            .replace("<player>", uTarget.getName()));
                    if (target != null) {
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("group.set_group_target"))
                                .replace("<name>", group.getName())
                                .replace("<admin>", sender.getName()));
                    }
                }
                uTarget.setGroups(Collections.singletonList(group));
            } else if (args[0].equalsIgnoreCase("add")) {
                if (groups.contains(group)) {
                    if (target == sender) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("group.current_group_already"))
                                .replace("<name>", group.getName()));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("group.current_group_already_sender"))
                                .replace("<name>", group.getName())
                                .replace("<player>", uTarget.getName()));
                    }
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("group.add_group"))
                            .replace("<name>", group.getName()));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("group.add_group_sender"))
                            .replace("<name>", group.getName())
                            .replace("<player>", uTarget.getName()));
                    if (target != null) {
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("group.add_group_target"))
                                .replace("<name>", group.getName())
                                .replace("<admin>", sender.getName()));
                    }
                }
                groups.add(group);
                uTarget.setGroups(groups);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!groups.contains(group)) {
                    if (target == sender) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("group.not_in_the_group"))
                                .replace("<name>", group.getName()));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("group.not_in_the_group_sender"))
                                .replace("<name>", group.getName())
                                .replace("<player>", uTarget.getName()));
                    }
                    return true;
                }
                if (groups.size() == 1) {
                    if (permissionManager.getDefaultGroups().contains(uTarget.getMainGroup())) {
                        if (target == sender) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    l.getConfig().getString("group.remove_will_remove_last_group"))
                                    .replace("<name>", group.getName()));
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    l.getConfig().getString("group.remove_will_remove_last_group_sender"))
                                    .replace("<name>", group.getName())
                                    .replace("<player>", uTarget.getName()));
                        }
                        return true;
                    }
                }
                if (target == sender) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("group.remove_group"))
                            .replace("<name>", group.getName()));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("group.remove_group_sender"))
                            .replace("<name>", group.getName())
                            .replace("<player>", uTarget.getName()));
                    if (target != null) {
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("group.remove_group_target"))
                                .replace("<name>", group.getName())
                                .replace("<admin>", sender.getName()));
                    }
                }
                groups.remove(group);
                if (groups.size() == 0) {
                    uTarget.setGroups(permissionManager.getDefaultGroups());
                    System.out.println("Restored defaults.");
                } else {
                    uTarget.setGroups(groups);
                }
            } else {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
            userManager.updateUser(uTarget);
            if (target != null) {
                permissionManager.startPermissions(target, uTarget);
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }
}
