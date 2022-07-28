package me.hardstyl3r.tauth.commands;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.managers.LoginManagement;
import me.hardstyl3r.tauth.managers.LoginManager;
import me.hardstyl3r.tauth.objects.AuthUser;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class authCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final LoginManager loginManager;
    private final LoginManagement loginManagement;

    public authCommand(TAuth plugin, UserManager userManager, LocaleManager localeManager, LoginManager loginManager, LoginManagement loginManagement) {
        plugin.getCommand("auth").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.loginManager = loginManager;
        this.loginManagement = loginManagement;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            if (args.length != 2) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (!sender.hasPermission("toolsies.auth")) {
            sender.sendMessage(l.getColoredString("no_permission").replace("<permission>", "toolsies.auth"));
            return true;
        }
        if (args.length >= 2) {
            String target = args[1];
            //to-do: Rethink comment and silent/hidden system
            String comment = localeManager.createMessage(args, 2);
            boolean silent = sender.hasPermission("toolsies.auth.silent") && comment.contains("-silent");
            if (silent) comment = comment.replace("-silent", "");
            Player p = Bukkit.getPlayerExact(target);
            if (loginManager.isUUID(target)) {
                p = Bukkit.getPlayer(UUID.fromString(target));
            }
            AuthUser authUser = loginManager.getAuth(p);
            if (authUser == null || !authUser.isRegistered()) {
                sender.sendMessage(l.getColoredString("players.unknown").replace("<name>", target));
                return true;
            }
            if (args[0].equalsIgnoreCase("logout")) {
                if (!authUser.isLoggedIn()) {
                    sender.sendMessage(l.getColoredString("auth.logout_not_logged_in").replace("<name>", p.getName()));
                    return true;
                }
                if (p == sender) {
                    Bukkit.dispatchCommand(sender, "logout");
                    return true;
                }
                sender.sendMessage(l.getColoredString("auth.logout_sender").replace("<name>", p.getName()));
                p.sendMessage(l.getColoredString("auth.logout_target"));
                loginManagement.performLogout(p, comment);
            } else if (args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(l.getColoredString("auth.reload_sender").replace("<name>", target));
                if (p != null && !silent) {
                    Bukkit.getPlayerExact(target).sendMessage(l.getColoredString("auth.reload_target"));
                }
                loginManager.refreshAuth(authUser);
            } else if (args[0].equalsIgnoreCase("info")) {
                sender.sendMessage(l.getColoredString("auth.info.header").replace("<name>", authUser.getName()));
                sender.sendMessage(l.getColoredString("auth.info.uuid").replace("<uuid>", authUser.getUUID().toString()));
                sender.sendMessage(l.getColoredString("auth.info.ip").replace("<ip>", authUser.getIp()).replace("<regip>", authUser.getRegisterIp()));
                sender.sendMessage(l.getColoredString("auth.info.regdate").replace("<regdate>", localeManager.getFullDate(authUser.getRegisterDate())));
                sender.sendMessage(l.getColoredString("auth.info.email").replace("<email>", (authUser.getEmail() == null ? "N/A" : authUser.getEmail())));
                Location loc = authUser.getLastLocation();
                sender.sendMessage(l.getColoredString("auth.info.lastlocation")
                        .replace("<x>", String.valueOf(loc.getBlockX()))
                        .replace("<y>", String.valueOf(loc.getBlockY()))
                        .replace("<z>", String.valueOf(loc.getBlockZ()))
                        .replace("<world>", loc.getWorld().getName()));
                sender.sendMessage(l.getColoredString("auth.info.lastlogin").replace("<lastlogin>", localeManager.getFullDate(authUser.getLastLoginDate())));
                ArrayList<AuthUser> multis = loginManager.getMultiAccounts(authUser);
                sender.sendMessage(l.getColoredString("auth.info.multis").replace("<multi>", (multis.isEmpty() ? "0" : getNames(multis).toString())));
            } else if (args[0].equalsIgnoreCase("login")) {
                if (authUser.isLoggedIn()) {
                    sender.sendMessage(l.getColoredString("auth.login_logged_in").replace("<name>", target));
                    return true;
                }
                sender.sendMessage(l.getColoredString("auth.login_sender").replace("<name>", p.getName()));
                p.sendMessage(l.getColoredString("auth.login_target"));
                loginManagement.performLogin(p, comment);
            } else {
                localeManager.sendUsage(sender, cmd, l);
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    private ArrayList<String> getNames(ArrayList<AuthUser> users) {
        ArrayList<String> names = new ArrayList<>();
        for (AuthUser authUser : users) {
            names.add(authUser.getName());
        }
        return names;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.auth")) {
            ArrayList<String> allarguments = new ArrayList<>(Arrays.asList("logout", "login", "reload", "info"));
            Locale l = userManager.determineLocale(sender);
            if (args.length == 1) return localeManager.formatTabArguments(args[0], allarguments);
            if (args.length == 2)
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.player"), true));
        }
        return Collections.emptyList();
    }
}
