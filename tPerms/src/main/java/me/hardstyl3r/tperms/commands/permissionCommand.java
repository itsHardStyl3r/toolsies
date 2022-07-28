package me.hardstyl3r.tperms.commands;

import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.tperms.TPerms;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import me.hardstyl3r.tperms.managers.PermissionsManager;
import me.hardstyl3r.tperms.objects.PermissibleUser;
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

public class permissionCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final PermissionsManager permissionsManager;
    private final PermissibleUserManager permissibleUserManager;

    public permissionCommand(TPerms plugin, UserManager userManager, LocaleManager localeManager, PermissionsManager permissionsManager, PermissibleUserManager permissibleUserManager) {
        plugin.getCommand("permission").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.permissionsManager = permissionsManager;
        this.permissibleUserManager = permissibleUserManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            if (args[0].equalsIgnoreCase("clear")) {
                if (args.length != 2) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
            } else if (!(args.length == 1 || args.length == 3)) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.permission")) {
            sender.sendMessage(l.getColoredString("no_permission").replace("<permission>", "toolsies.permission"));
            return true;
        }
        if (args.length == 0) {
            PermissibleUser u = permissibleUserManager.getUser(sender);
            if (!u.hasPermissions()) {
                sender.sendMessage(l.getColoredString("permission.no_permissions"));
            } else {
                sender.sendMessage(l.getColoredString("permission.current_permissions")
                        .replace("<permissions>", permissibleUserManager.serialize(u.getPermissions())));
            }
        } else if (args.length == 1) {
            if (permissibleUserManager.getUser(args[0]) != null) {
                if (!sender.hasPermission("toolsies.permission.others")) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
                PermissibleUser u = permissibleUserManager.getUser(args[0]);
                if (!u.hasPermissions()) {
                    sender.sendMessage(l.getColoredString("permission.no_permissions" + (sender.getName().equals(u.getName()) ? "" : "_sender"))
                            .replace("<player>", u.getName()));
                } else {
                    sender.sendMessage(l.getColoredString("permission.current_permissions" + (sender.getName().equals(u.getName()) ? "" : "_sender"))
                            .replace("<player>", u.getName())
                            .replace("<permissions>", permissibleUserManager.serialize(u.getPermissions())));
                }
            } else {
                localeManager.sendUsage(sender, cmd, l);
            }
        } else if (args.length <= 3) {
            List<String> arguments = Arrays.asList("add", "remove", "clear");
            if (!arguments.contains(args[0])) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
            for (String s : arguments) {
                if (args[0].equalsIgnoreCase(s) && !sender.hasPermission("toolsies.permission." + s)) {
                    sender.sendMessage(l.getColoredString("no_permission")
                            .replace("<permission>", "toolsies.permission." + s));
                    return true;
                }
            }
            String permission = args[1].toLowerCase();
            if (!permission.matches("^[\\w.]+$")) {
                sender.sendMessage(l.getColoredString("permission.illegal_characters"));
                return true;
            }
            Player target = null;
            PermissibleUser puTarget = null;
            if (sender instanceof Player) {
                target = ((Player) sender);
                puTarget = permissibleUserManager.getUser(target);
            }
            if (args.length == 3 || (args[0].equalsIgnoreCase("clear") && args.length == 2)) {
                String check = (args[0].equalsIgnoreCase("clear") ? args[1] : args[2]);
                target = Bukkit.getPlayerExact(check);
                if (permissibleUserManager.getUser(check) != null) {
                    puTarget = permissibleUserManager.getUser(check);
                } else {
                    sender.sendMessage(l.getColoredString("players.unknown")
                            .replace("<name>", check));
                    return true;
                }
            }
            List<String> permissions = new ArrayList<>(puTarget.getPermissions());
            if (args[0].equalsIgnoreCase("clear")) {
                if (!puTarget.hasPermissions()) {
                    sender.sendMessage(l.getColoredString("permission.no_permissions" + (target == sender ? "" : "_sender"))
                            .replace("<player>", puTarget.getName()));
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(l.getColoredString("permission.clear_permissions")
                            .replace("<count>", String.valueOf(puTarget.getPermissions().size())));
                } else {
                    sender.sendMessage(l.getColoredString("permission.clear_permissions_sender")
                            .replace("<count>", String.valueOf(puTarget.getPermissions().size()))
                            .replace("<player>", puTarget.getName()));
                    if (target != null) {
                        target.sendMessage(l.getColoredString("permission.clear_permissions_target")
                                .replace("<admin>", sender.getName()));
                    }
                }
                puTarget.setPermissions(Collections.emptyList());
            } else if (args[0].equalsIgnoreCase("add")) {
                if (puTarget.getPermissions().contains(permission)) {
                    sender.sendMessage(l.getColoredString("permission.has_permission_already" + (target == sender ? "" : "_sender"))
                            .replace("<player>", puTarget.getName()));
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(l.getColoredString("permission.add_permission")
                            .replace("<permission>", permission));
                } else {
                    sender.sendMessage(l.getColoredString("permission.add_permission_sender")
                            .replace("<permission>", permission)
                            .replace("<player>", puTarget.getName()));
                    if (target != null) {
                        target.sendMessage(l.getColoredString("permission.add_permission_target")
                                .replace("<admin>", sender.getName()));
                    }
                }
                permissions.add(permission);
                puTarget.setPermissions(permissions);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!puTarget.getPermissions().contains(permission)) {
                    sender.sendMessage(l.getColoredString("permission.has_permission_already" + (target == sender ? "" : "_sender"))
                            .replace("<player>", puTarget.getName()));
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(l.getColoredString("permission.remove_permission")
                            .replace("<permission>", permission));
                } else {
                    sender.sendMessage(l.getColoredString("permission.remove_permission_sender")
                            .replace("<permission>", permission)
                            .replace("<player>", puTarget.getName()));
                    if (target != null) {
                        target.sendMessage(l.getColoredString("permission.remove_permission_target")
                                .replace("<admin>", sender.getName()));
                    }
                }
                permissions.remove(permission);
                puTarget.setPermissions(permissions);
            } else {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
            permissibleUserManager.updatePermissibleUser(puTarget);
            if (target != null) {
                permissionsManager.startPermissions(target, puTarget);
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("toolsies.permission")) {
            return Collections.emptyList();
        }
        List<String> allarguments = new ArrayList<>(Arrays.asList("add", "remove", "clear"));
        allarguments.removeIf(s -> !sender.hasPermission("toolsies.permission." + s));
        Locale l = userManager.determineLocale(sender);
        if (sender.hasPermission("toolsies.permission.others")) {
            allarguments.add(localeManager.formatArgument(l.getString("common.player"), false));
        }
        if (args.length == 1) {
            return localeManager.formatTabArguments(args[0], allarguments);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("clear")) {
                return null;
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.permission"), true));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return null;
            }
        }
        return Collections.emptyList();
    }
}
