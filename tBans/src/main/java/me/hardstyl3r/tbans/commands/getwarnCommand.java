package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class getwarnCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public getwarnCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("getwarn").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.getwarn")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.getwarn")));
            return true;
        }
        if (args.length == 1) {
            if (!StringUtils.isNumeric(args[0])) {
                sender.sendMessage(l.getStringComponent("getwarn.wrong_id"));
                return true;
            }
            int i = Integer.parseInt(args[0]);
            Punishment warn = punishmentManager.getPunishmentById(PunishmentType.WARN, i);
            punishmentManager.deleteIfExpired(warn);
            if (warn == null) {
                sender.sendMessage(l.getStringComponent("getwarn.no_such_warn", Placeholder.unparsed("id", args[0])));
                return true;
            }
            sender.sendMessage(l.getStringComponent("getwarn.getwarn_header", Placeholder.unparsed("id", args[0]), Placeholder.unparsed("player_name", warn.getName())));
            sender.sendMessage(l.getStringComponent("getwarn.entries.admin", Placeholder.unparsed("admin", warn.getAdmin())));
            sender.sendMessage(l.getStringComponent("getwarn.entries.reason", Placeholder.unparsed("reason", warn.getReason())));
            sender.sendMessage(l.getStringComponent("getwarn.entries.date", Placeholder.unparsed("date", localeManager.getFullDate(warn.getDate()))));
            if (warn.getDuration() != null) {
                sender.sendMessage(l.getStringComponent("getwarn.entries.duration", Placeholder.unparsed("duration", localeManager.getFullDate(warn.getDuration()))));
                sender.sendMessage(l.getStringComponent("getwarn.entries.remaining", Placeholder.unparsed("remaining", localeManager.parseTimeWithTranslate(warn.getRemaining(), l))));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.getwarn")) {
            if (args.length == 1) {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.id"), true));
            }
        }
        return Collections.emptyList();
    }
}
