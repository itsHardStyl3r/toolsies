package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.User;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class localeCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;

    public localeCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager) {
        plugin.getCommand("locale").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(l.getStringComponent("console_sender"));
            return true;
        }
        if (!sender.hasPermission("toolsies.locale")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.locale")));
            return true;
        }
        if (args.length == 1) {
            if (localeManager.getLocale(args[0]) == null) {
                sender.sendMessage(l.getStringComponent("locale.unknown_locale", Placeholder.unparsed("name", args[0])));
                return true;
            }
            Locale locale = localeManager.getLocale(args[0]);
            if (l.equals(locale)) {
                sender.sendMessage(l.getStringComponent("locale.current_locale", Placeholder.unparsed("name", locale.getName())));
                return true;
            }
            User u = userManager.getUser(sender);
            u.setLocale(locale);
            userManager.updateUser(u);
            sender.sendMessage(locale.getStringComponent("locale.changed_own", Placeholder.unparsed("name", locale.getName())));
        } else {
            sender.sendMessage(l.getStringComponent("locale.available_locales", Placeholder.unparsed("locales", localeManager.getLocales().toString())));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.locale") && sender instanceof Player) {
            List<String> allarguments = new ArrayList<>(localeManager.getLocales());
            if (args.length == 1) return localeManager.formatTabArguments(args[0], allarguments);
        }
        return Collections.emptyList();
    }
}
