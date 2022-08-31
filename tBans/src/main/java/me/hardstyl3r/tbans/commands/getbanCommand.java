package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class getbanCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public getbanCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("getban").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.getban")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.getban")));
            return true;
        }
        if (args.length == 1) {
            String target = args[0];
            punishmentManager.deleteIfExpired(PunishmentType.BAN, target);
            if (!punishmentManager.isPunished(PunishmentType.BAN, target)) {
                sender.sendMessage(l.getStringComponent("unban.is_not_banned", Placeholder.unparsed("name", target)));
                return true;
            }
            Punishment ban = punishmentManager.getPunishment(PunishmentType.BAN, target);
            sender.sendMessage(l.getStringComponent("getban.getban_header", Placeholder.unparsed("name", target), Placeholder.unparsed("id", String.valueOf(ban.getId()))));
            sender.sendMessage(l.getStringComponent("getban.entries.type", Placeholder.unparsed("type", ban.getType().name())));
            sender.sendMessage(l.getStringComponent("getban.entries.admin", Placeholder.unparsed("admin", ban.getAdmin())));
            if (ban.getReason() != null) {
                sender.sendMessage(l.getStringComponent("getban.entries.reason", Placeholder.unparsed("reason", ban.getReason())));
            }
            sender.sendMessage(l.getStringComponent("getban.entries.date", Placeholder.unparsed("date", localeManager.getFullDate(ban.getDate()))));
            if (ban.getDuration() != null) {
                sender.sendMessage(l.getStringComponent("getban.entries.duration", Placeholder.unparsed("duration", localeManager.getFullDate(ban.getDuration()))));
                sender.sendMessage(l.getStringComponent("getban.entries.remaining", Placeholder.unparsed("remaining", localeManager.parseTimeWithTranslate(ban.getRemaining(), l))));
            }
            if (ban.getUUID() != null) {
                sender.sendMessage(l.getStringComponent("getban.entries.uuid", Placeholder.unparsed("uuid", ban.getUUID().toString())));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.getban")) {
            if (args.length == 1) {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.player"), true));
            }
        }
        return Collections.emptyList();
    }
}
