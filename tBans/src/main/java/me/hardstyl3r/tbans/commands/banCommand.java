package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static me.hardstyl3r.tbans.enums.PunishmentType.BAN;
import static me.hardstyl3r.tbans.enums.PunishmentType.IP;

public class banCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    /**
     * A command to ban players and IP addresses.
     * Permissions: 'toolsies.ban', 'toolsies.ban.ip', 'toolsies.ban.bypasspriority', 'toolsies.ip.bypasspriority'
     */
    public banCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("ban").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.ban")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.ban")));
            return true;
        }
        if (args.length > 0) {
            String target = args[0];
            PunishmentType type = (StringUtils.isIPv4(target) && sender.hasPermission("toolsies.ban.ip") ? IP : BAN);
            if (!punishmentManager.canSenderPunishTarget(sender, target, type)) {
                sender.sendMessage(l.getStringComponent("ban.priority_too_high"));
                return true;
            }
            if (punishmentManager.isPunished(target, type)) {
                sender.sendMessage(l.getStringComponent("punishments.target_is_banned",
                        Placeholder.unparsed("target_name", target),
                        Formatter.choice("target", (type == IP ? 1 : 0))));
                return true;
            }
            long duration = 0;
            String admin = sender.getName();
            String reason = (args.length > 1 ? localeManager.createMessage(args, 1) : null);
            if (args.length > 1 && localeManager.isValidStringTime(args[1])) {
                duration = localeManager.parseTimeFromString(args[1]);
                long minimumDuration = punishmentManager.getMinimumDuration(type);
                if (duration < minimumDuration) {
                    sender.sendMessage(l.getStringComponent("punishments.duration_too_short", Placeholder.unparsed("duration", localeManager.parseTimeWithTranslate(minimumDuration, l))));
                    return true;
                }
                reason = (args.length > 2 ? localeManager.createMessage(args, 2) : null);
            }
            UUID uuid = (userManager.getUserIgnoreCase(target) == null || type == IP ? null : userManager.getUserIgnoreCase(target).getUUID());
            Punishment ban = punishmentManager.createPunishment(type, uuid, target, admin, reason, duration);
            sender.sendMessage(l.getStringComponent("ban.target_banned",
                    Formatter.choice("target", (type == IP ? 1 : 0)),
                    Placeholder.unparsed("target_name", target),
                    Formatter.choice("choice", duration),
                    Placeholder.unparsed("duration", localeManager.parseTimeWithTranslate(duration, l))));
            for (Player p : Bukkit.getOnlinePlayers()) {
                Locale locale = userManager.determineLocale(p);
                if ((p.getAddress() != null && p.getAddress().getAddress().getHostAddress().equals(target) && type == IP) || p.getName().equalsIgnoreCase(target)) {
                    Component message = locale.getStringComponent("ban.messages." + (type == IP ? "ip_" : "") + "kick_header",
                            Placeholder.unparsed("sender_name", admin),
                            Formatter.choice("choice", duration));
                    if (reason != null) message = message.append(locale.getStringComponent("ban.messages.reason",
                            Placeholder.unparsed("reason", reason)));
                    if (duration > 1)
                        message = message.append(locale.getStringComponent("ban.messages.expires_after",
                                Placeholder.unparsed("duration", localeManager.parseTimeWithTranslate(duration, locale)),
                                Placeholder.unparsed("duration_date", localeManager.getFullDate(System.currentTimeMillis() + duration))));
                    message = message.append(locale.getStringComponent("ban.messages.footer"));
                    p.kick(message, PlayerKickEvent.Cause.BANNED);
                } else if (type != IP) {
                    p.sendMessage(locale.getStringComponent("ban." + (duration > 1 ? "temp" : "") + "ban_broadcast_message",
                            Placeholder.unparsed("player_name", target),
                            Placeholder.unparsed("sender_name", sender.getName()),
                            Placeholder.unparsed("duration", localeManager.parseTimeWithTranslate(duration, l))));
                }
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.ban")) {
            Locale l = userManager.determineLocale(sender);
            if (args.length == 1) {
                List<String> arguments = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    arguments.add(p.getName());
                    String address = p.getAddress().getAddress().getHostAddress();
                    if (!arguments.contains(address) && sender.hasPermission("toolsies.ban.ip"))
                        arguments.add(p.getAddress().getAddress().getHostAddress());
                }
                return localeManager.formatTabArguments(args[0], arguments);
            } else if (args.length == 2) {
                return Collections.singletonList(localeManager.formatArgument(localeManager.getConfig().getString("duration_argument") + " | " + l.getString("common.reason"), false));
            } else {
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.reason"), false));
            }
        }
        return Collections.emptyList();
    }
}
