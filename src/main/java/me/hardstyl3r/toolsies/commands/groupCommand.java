package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.PermissionsManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class groupCommand implements CommandExecutor {

    private final UserManager userManager;
    private final PermissionsManager permissionManager;

    public groupCommand(Toolsies plugin, UserManager userManager, PermissionsManager permissionManager) {
        plugin.getCommand("group").setExecutor(this);
        this.userManager = userManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command does not work in console.");
            return true;
        }
        User u = userManager.getUser(sender);
        if (!sender.hasPermission("toolsies.group")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    u.getLocale().getConfig().getString("no_permission")).replace("<permission>", "toolsies.group"));
            return true;
        }
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("list")){
                if(!sender.hasPermission("toolsies.group.list")){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("no_permission")).replace("<permission>", "toolsies.group.list"));
                    return true;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        u.getLocale().getConfig().getString("group." + (permissionManager.getGroups().isEmpty() ? "list_none" : "list"))).replace("<groups>", permissionManager.getGroups().toString()));
                return true;
            }
        } else if (args.length >= 2 && args.length <= 3) {
            String group = args[1];
            if (!permissionManager.isGroup(group)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        u.getLocale().getConfig().getString("group.unknown_group")).replace("<name>", group));
                return true;
            }
            Player target = (Player) sender;
            User uTarget = u;
            if (args.length == 3) {
                target = Bukkit.getPlayerExact(args[2]);
                uTarget = userManager.getUser(args[2]);
                if (target == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("players.unknown")).replace("<name>", args[2]));
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("set")) {
                if(!sender.hasPermission("toolsies.group.set")){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("no_permission")).replace("<permission>", "toolsies.group.set"));
                    return true;
                }
                if (uTarget.getGroups().contains(group.toLowerCase()) && uTarget.getGroups().size() > 2) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.current_group_already" + (sender == target ? "_own" : "_sender"))).replace("<name>", group).replace("<player>", target.getName()));
                    return true;
                }
                uTarget.setGroups(Collections.singletonList(group));
                if(sender == target) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.set_group_own")).replace("<name>", group));
                } else {
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            uTarget.getLocale().getConfig().getString("group.set_group_target")).replace("<name>", group).replace("<admin>", sender.getName()));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.set_group_sender")).replace("<name>", group).replace("<player>", target.getName()));
                }
            } else if (args[0].equalsIgnoreCase("add")) {
                if(!sender.hasPermission("toolsies.group.add")){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("no_permission")).replace("<permission>", "toolsies.group.add"));
                    return true;
                }
                if (uTarget.getGroups().contains(group.toLowerCase())) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.current_group_already" + (sender == target ? "_own" : "_sender"))).replace("<name>", group).replace("<player>", target.getName()));
                    return true;
                }
                List<String> groups = new ArrayList<>(uTarget.getGroups());
                groups.add(group.toLowerCase());
                uTarget.setGroups(groups);
                if(sender == target) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.add_group_own")).replace("<name>", group));
                } else {
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            uTarget.getLocale().getConfig().getString("group.add_group_target")).replace("<name>", group).replace("<admin>", sender.getName()));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.add_group_sender")).replace("<name>", group).replace("<player>", target.getName()));
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if(!sender.hasPermission("toolsies.group.remove")){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("no_permission")).replace("<permission>", "toolsies.group.remove"));
                    return true;
                }
                if (!uTarget.getGroups().contains(group.toLowerCase())) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.not_in_the_group" + (sender == target ? "_own" : "_sender"))).replace("<name>", group).replace("<player>", target.getName()));
                    return true;
                }
                if(uTarget.getGroups().size() == 1){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.remove_will_remove_last_group" + (sender == target ? "_own" : "_sender"))).replace("<name>", group).replace("<player>", target.getName()));
                    return true;
                }
                List<String> groups = new ArrayList<>(uTarget.getGroups());
                groups.remove(group.toLowerCase());
                uTarget.setGroups(groups);
                if(sender == target) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.remove_group_own")).replace("<name>", group));
                } else {
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            uTarget.getLocale().getConfig().getString("group.remove_group_target")).replace("<name>", group).replace("<admin>", sender.getName()));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            u.getLocale().getConfig().getString("group.remove_group_sender")).replace("<name>", group).replace("<player>", target.getName()));
                }
            }
            userManager.updateUser(uTarget);
            permissionManager.startPermissions(target, uTarget);
            return true;
        }
        if (u.getGroups().size() == 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    u.getLocale().getConfig().getString("group.current_group")).replace("<group>", u.getMainGroup()));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    u.getLocale().getConfig().getString("group.current_group_multiple")).replace("<groups>", u.getGroups().toString()));
        }
        return true;
    }
}
