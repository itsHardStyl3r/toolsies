package me.hardstyl3r.tperms.commands;

import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.tperms.TPerms;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import me.hardstyl3r.tperms.managers.PermissionsManager;
import me.hardstyl3r.tperms.objects.Group;
import me.hardstyl3r.tperms.objects.PermissibleUser;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class groupCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PermissionsManager permissionManager;
    private final LocaleManager localeManager;
    private final PermissibleUserManager permissibleUserManager;

    public groupCommand(TPerms plugin, UserManager userManager, PermissionsManager permissionManager, LocaleManager localeManager, PermissibleUserManager permissibleUserManager) {
        plugin.getCommand("group").setExecutor(this);
        this.userManager = userManager;
        this.permissionManager = permissionManager;
        this.localeManager = localeManager;
        this.permissibleUserManager = permissibleUserManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            if (!(args.length == 1 || args.length == 3)) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.group")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.group")));
            return true;
        }
        if (args.length == 0) {
            PermissibleUser u = permissibleUserManager.getUser(sender);
            sender.sendMessage(l.getStringComponent("group.current_group",
                    Placeholder.unparsed("group", u.getMainGroup().getName() + (sender.isOp() ? " (" + String.join(", ", u.listGroups()) + ")" : ""))));
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("toolsies.group.list")) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
                List<String> groups = permissionManager.getGroups();
                sender.sendMessage(l.getStringComponent("group.list" + (groups.size() == 0 ? "_none" : ""), Placeholder.unparsed("groups", String.join(", ", groups))));
            } else if (permissibleUserManager.getUser(args[0]) != null) {
                if (!sender.hasPermission("toolsies.group.others")) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
                PermissibleUser u = permissibleUserManager.getUser(args[0]);
                sender.sendMessage(l.getStringComponent("group.current_group_sender",
                        Placeholder.unparsed("player_name", u.getName()),
                        Placeholder.unparsed("group", u.getMainGroup().getName() + (sender.isOp() ? " (" + String.join(", ", u.listGroups()) + ")" : ""))));
            } else {
                localeManager.sendUsage(sender, cmd, l);
            }
        } else if (args.length <= 3) {
            List<String> arguments = Arrays.asList("set", "add", "remove");
            if (!arguments.contains(args[0])) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
            for (String s : arguments) {
                if (args[0].equalsIgnoreCase(s) && !sender.hasPermission("toolsies.group." + s)) {
                    sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.group." + s)));
                    return true;
                }
            }
            if (permissionManager.getGroup(args[1]) == null) {
                sender.sendMessage(l.getStringComponent("group.unknown_group", Placeholder.unparsed("group_name", args[1])));
                return true;
            }
            Group group = permissionManager.getGroup(args[1]);
            Player target = null;
            PermissibleUser puTarget = null;
            if (sender instanceof Player) {
                target = ((Player) sender);
                puTarget = permissibleUserManager.getUser(target);
            }
            if (args.length == 3) {
                target = Bukkit.getPlayerExact(args[2]);
                if (permissibleUserManager.getUser(args[2]) != null) {
                    puTarget = permissibleUserManager.getUser(args[2]);
                } else {
                    sender.sendMessage(l.getStringComponent("players.unknown", Placeholder.unparsed("player_name", args[2])));
                    return true;
                }
            }
            ArrayList<Group> groups = new ArrayList<>(puTarget.getGroups());
            if (args[0].equalsIgnoreCase("set")) {
                if (groups.size() == 1 && groups.contains(group)) {
                    if (target == sender) {
                        sender.sendMessage(l.getStringComponent("group.current_group_already", Placeholder.unparsed("group_name", group.getName())));
                    } else {
                        sender.sendMessage(l.getStringComponent("group.current_group_already_sender", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("player_name", puTarget.getName())));
                    }
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(l.getStringComponent("group.set_group", Placeholder.unparsed("group_name", group.getName())));
                } else {
                    sender.sendMessage(l.getStringComponent("group.set_group_sender", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("player_name", puTarget.getName())));
                    if (target != null) {
                        target.sendMessage(l.getStringComponent("group.set_group_target", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("sender_name", sender.getName())));
                    }
                }
                puTarget.setGroups(Collections.singletonList(group));
            } else if (args[0].equalsIgnoreCase("add")) {
                if (groups.contains(group)) {
                    if (target == sender) {
                        sender.sendMessage(l.getStringComponent("group.current_group_already", Placeholder.unparsed("group_name", group.getName())));
                    } else {
                        sender.sendMessage(l.getStringComponent("group.current_group_already_sender", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("player_name", puTarget.getName())));
                    }
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(l.getStringComponent("group.add_group", Placeholder.unparsed("group_name", group.getName())));
                } else {
                    sender.sendMessage(l.getStringComponent("group.add_group_sender", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("player_name", puTarget.getName())));
                    if (target != null) {
                        target.sendMessage(l.getStringComponent("group.add_group_target", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("sender_name", sender.getName())));
                    }
                }
                groups.add(group);
                puTarget.setGroups(groups);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!groups.contains(group)) {
                    if (target == sender) {
                        sender.sendMessage(l.getStringComponent("group.not_in_the_group", Placeholder.unparsed("group_name", group.getName())));
                    } else {
                        sender.sendMessage(l.getStringComponent("group.not_in_the_group_sender", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("player_name", puTarget.getName())));
                    }
                    return true;
                }
                if (groups.size() == 1) {
                    if (permissionManager.getDefaultGroups().contains(puTarget.getMainGroup())) {
                        if (target == sender) {
                            sender.sendMessage(l.getStringComponent("group.remove_will_remove_default_group", Placeholder.unparsed("group_name", group.getName())));
                        } else {
                            sender.sendMessage(l.getStringComponent("group.remove_will_remove_default_group_sender", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("player_name", puTarget.getName())));
                        }
                        return true;
                    }
                }
                if (target == sender) {
                    sender.sendMessage(l.getStringComponent("group.remove_group", Placeholder.unparsed("group_name", group.getName())));
                } else {
                    sender.sendMessage(l.getStringComponent("group.remove_group_sender", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("player_name", puTarget.getName())));
                    if (target != null) {
                        target.sendMessage(l.getStringComponent("group.remove_group_target", Placeholder.unparsed("group_name", group.getName()), Placeholder.unparsed("sender_name", sender.getName())));
                    }
                }
                groups.remove(group);
                if (groups.size() == 0) {
                    puTarget.setGroups(permissionManager.getDefaultGroups());
                } else {
                    puTarget.setGroups(groups);
                }
            } else {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
            permissibleUserManager.updatePermissibleUser(puTarget);
            if (target != null) {
                permissionManager.startPermissions(target, puTarget);
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.group")) {
            ArrayList<String> allarguments = new ArrayList<>(Arrays.asList("set", "add", "remove", "list"));
            allarguments.removeIf(s -> !sender.hasPermission("toolsies.group." + s));
            Locale l = userManager.determineLocale(sender);
            if (sender.hasPermission("toolsies.group.others")) {
                allarguments.add(localeManager.formatArgument(l.getString("common.player"), false));
            }
            if (args.length == 1) return localeManager.formatTabArguments(args[0], allarguments);
            if (sender.hasPermission("toolsies.group.set") || sender.hasPermission("toolsies.group.add") || sender.hasPermission("toolsies.group.remove")) {
                if (args.length == 2) return localeManager.formatTabArguments(args[1], permissionManager.getGroups());
                if (args.length == 3) return null;
            }
        }
        return Collections.emptyList();
    }
}
