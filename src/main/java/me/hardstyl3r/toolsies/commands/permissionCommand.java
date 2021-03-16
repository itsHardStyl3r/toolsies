package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.PermissionsManager;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class permissionCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final PermissionsManager permissionsManager;

    public permissionCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager, PermissionsManager permissionsManager) {
        plugin.getCommand("permission").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.permissionsManager = permissionsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = localeManager.getDefault();
        if (sender instanceof Player) {
            l = userManager.getUser(sender).getLocale();
        } else {
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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.permission"));
            return true;
        }
        if (args.length == 0) {
            User u = userManager.getUser(sender);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("permission." + (u.hasPermissions() ? "current_permissions" : "no_permissions")))
                    .replace("<permissions>", userManager.serialize(u.getPermissions())));
        } else if (args.length == 1) {
            if (userManager.getUser(args[0]) != null) {
                if (!sender.hasPermission("toolsies.permission.others")) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
                User u = userManager.getUser(args[0]);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("permission." + (u.hasPermissions() ? "current_permissions" : "no_permissions") + (sender.getName().equals(u.getName()) ? "" : "_sender")))
                        .replace("<player>", u.getName())
                        .replace("<permissions>", userManager.serialize(u.getPermissions())));
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
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("no_permission"))
                            .replace("<permission>", "toolsies.permission." + s));
                    return true;
                }
            }
            String permission = args[1].toLowerCase();
            if (!permission.matches("^[\\w.]+$")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("permission.illegal_characters")));
                return true;
            }
            Player target = null;
            User uTarget = null;
            if (sender instanceof Player) {
                target = ((Player) sender);
                uTarget = userManager.getUser(target);
            }
            if (args.length == 3 || (args[0].equalsIgnoreCase("clear") && args.length == 2)) {
                String check = (args[0].equalsIgnoreCase("clear") ? args[1] : args[2]);
                target = Bukkit.getPlayerExact(check);
                if (userManager.getUser(check) != null) {
                    uTarget = userManager.getUser(check);
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("players.unknown"))
                            .replace("<name>", check));
                    return true;
                }
            }
            List<String> permissions = new ArrayList<>(uTarget.getPermissions());
            if (args[0].equalsIgnoreCase("clear")) {
                if (!uTarget.hasPermissions()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("permission.no_permissions" + (target == sender ? "" : "_sender")))
                            .replace("<player>", uTarget.getName()));
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("permission.clear_permissions"))
                            .replace("<count>", String.valueOf(uTarget.getPermissions().size())));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("permission.clear_permissions_sender"))
                            .replace("<count>", String.valueOf(uTarget.getPermissions().size()))
                            .replace("<player>", uTarget.getName()));
                    if (target != null) {
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("permission.clear_permissions_target"))
                                .replace("<admin>", sender.getName()));
                    }
                }
                uTarget.setPermissions(Collections.emptyList());
            } else if (args[0].equalsIgnoreCase("add")) {
                if (uTarget.getPermissions().contains(permission)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("permission.has_permission_already" + (target == sender ? "" : "_sender")))
                            .replace("<player>", uTarget.getName()));
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("permission.add_permission"))
                            .replace("<permission>", permission));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("permission.add_permission_sender"))
                            .replace("<permission>", permission)
                            .replace("<player>", uTarget.getName()));
                    if (target != null) {
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("permission.add_permission_target"))
                                .replace("<admin>", sender.getName()));
                    }
                }
                permissions.add(permission);
                uTarget.setPermissions(permissions);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!uTarget.getPermissions().contains(permission)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("permission.has_permission_already" + (target == sender ? "" : "_sender")))
                            .replace("<player>", uTarget.getName()));
                    return true;
                }
                if (target == sender) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("permission.remove_permission"))
                            .replace("<permission>", permission));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("permission.remove_permission_sender"))
                            .replace("<permission>", permission)
                            .replace("<player>", uTarget.getName()));
                    if (target != null) {
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("permission.remove_permission_target"))
                                .replace("<admin>", sender.getName()));
                    }
                }
                permissions.remove(permission);
                uTarget.setPermissions(permissions);
            } else {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
            userManager.updateUser(uTarget);
            if (target != null) {
                permissionsManager.startPermissions(target, uTarget);
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
        if (sender.hasPermission("toolsies.permission.others")) {
            allarguments.add(userManager.getUser(sender).getLocale().getConfig().getString("tab-completion.player.optional"));
        }
        if (args.length == 1) {
            return localeManager.formatTabArguments(args[0], allarguments);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("clear")) {
                return null;
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return Collections.singletonList(userManager.getUser(sender).getLocale().getConfig().getString("tab-completion.permission.required"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return null;
            }
        }
        return Collections.emptyList();
    }
}
