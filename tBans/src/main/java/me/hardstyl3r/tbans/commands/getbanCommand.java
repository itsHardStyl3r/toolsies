package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static me.hardstyl3r.tbans.enums.PunishmentType.BAN;
import static me.hardstyl3r.tbans.enums.PunishmentType.IP;

public class getbanCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    /**
     * A command to check if player or IP address is banned and retrieve information of the punishment.
     * Permissions: 'toolsies.getban', 'toolsies.getban.ip'
     */
    public getbanCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("getban").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.getban")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.getban")));
            return true;
        }
        if (args.length == 1) {
            String target = args[0];
            PunishmentType type = BAN;
            if (StringUtils.isIPv4(target) && sender.hasPermission("toolsies.getban.ip")) {
                type = IP;
            }
            if (!punishmentManager.isPunished(target, type)) {
                sender.sendMessage(l.getStringComponent("punishments.target_is_not_banned",
                        Placeholder.unparsed("target_name", target),
                        Formatter.choice("target", (type == IP ? 1 : 0))));
                return true;
            }
            Punishment ban = punishmentManager.getPunishment(target, type);
            sender.sendMessage(l.getStringComponent("getban.getban_header", Placeholder.unparsed("target_name", target), Placeholder.unparsed("id", String.valueOf(ban.getId()))));
            sender.sendMessage(l.getStringComponent("getban.entries.type", Placeholder.unparsed("type", ban.getType().name())));
            sender.sendMessage(l.getStringComponent("getban.entries.admin", Placeholder.unparsed("admin", ban.getSender())));
            if (ban.getReason() != null)
                sender.sendMessage(l.getStringComponent("getban.entries.reason", Placeholder.unparsed("reason", ban.getReason())));
            sender.sendMessage(l.getStringComponent("getban.entries.date", Placeholder.unparsed("date", localeManager.getFullDate(ban.getDate()))));
            if (!ban.isPermanent()) {
                sender.sendMessage(l.getStringComponent("getban.entries.duration", Placeholder.unparsed("duration", localeManager.getFullDate(ban.getDuration()))));
                sender.sendMessage(l.getStringComponent("getban.entries.remaining", Placeholder.unparsed("remaining", localeManager.parseTimeWithTranslate(ban.getRemaining(), l))));
            }
            if (ban.getUUID() != null)
                sender.sendMessage(l.getStringComponent("getban.entries.uuid", Placeholder.unparsed("uuid", ban.getUUID().toString())));
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
