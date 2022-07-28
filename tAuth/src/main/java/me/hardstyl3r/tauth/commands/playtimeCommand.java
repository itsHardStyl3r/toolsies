package me.hardstyl3r.tauth.commands;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.managers.LoginManager;
import me.hardstyl3r.tauth.objects.AuthUser;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class playtimeCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final LoginManager loginManager;

    public playtimeCommand(TAuth plugin, UserManager userManager, LocaleManager localeManager, LoginManager loginManager) {
        plugin.getCommand("playtime").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.loginManager = loginManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.playtime")) {
            sender.sendMessage(l.getColoredString("no_permission").replace("<permission>", "toolsies.playtime"));
            return true;
        }
        if (!(sender instanceof Player)) {
            if (args.length < 1) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (args.length < 2) {
            AuthUser authUser;
            if (args.length == 1) {
                if (!sender.hasPermission("toolsies.playtime.others")) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
                if (loginManager.getAuth(args[0]) != null) {
                    authUser = loginManager.getAuth(args[0]);
                } else {
                    sender.sendMessage(l.getColoredString("players.unknown").replace("<name>", args[0]));
                    return true;
                }
            } else {
                authUser = loginManager.getAuth((Player) sender);
            }
            sender.sendMessage(l.getColoredString("playtime.header").replace("<name>", authUser.getName()));
            if (authUser.isLoggedIn()) sender.sendMessage(l.getColoredString("playtime.session").replace("<session>", localeManager.parseTimeWithTranslate(authUser.getSessionDuration(), l)));
            sender.sendMessage(l.getColoredString("playtime.total").replace("<playtime>", localeManager.parseTimeWithTranslate(authUser.getPlaytime(), l)));
            sender.sendMessage(l.getColoredString("playtime.regdate").replace("<regdate>", localeManager.getFullDate(authUser.getRegisterDate())));
            if (authUser.getName().equals(sender.getName()))
                sender.sendMessage(l.getColoredString("playtime.footer"));
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("toolsies.playtime.others")) {
            return null;
        }
        return Collections.emptyList();
    }
}
