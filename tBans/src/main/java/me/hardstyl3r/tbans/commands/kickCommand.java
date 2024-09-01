package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class kickCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final PunishmentManager punishmentManager;
    private final PunishmentType type = PunishmentType.KICK;

    /**
     * A command to kick a player.
     * Permissions: 'toolsies.kick', 'toolsies.kick.bypasspriority'
     */
    public kickCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("kick").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.kick")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.kick")));
            return true;
        }
        if (args.length > 0) {
            String target = args[0];
            if (userManager.getUser(target) == null) {
                sender.sendMessage(l.getStringComponent("players.unknown", Placeholder.unparsed("player_name", args[0])));
                return true;
            }
            if (!punishmentManager.canSenderPunishTarget(sender, target, type)) {
                sender.sendMessage(l.getStringComponent("kick.priority_too_high"));
                return true;
            }
            Player p = Bukkit.getPlayerExact(target);
            if (p == null) {
                sender.sendMessage(l.getStringComponent("players.offline", Placeholder.unparsed("player_name", args[0])));
                return true;
            }
            String reason = (args.length > 1 ? localeManager.createMessage(args, 1) : null);
            sender.sendMessage(l.getStringComponent("kick.player_kicked", Placeholder.unparsed("player_name", target)));
            Locale victimLocale = userManager.determineLocale(p);
            Component message = victimLocale.getStringComponent("kick.messages.header",
                    Placeholder.unparsed("sender_name", sender.getName()));
            if (reason != null) message = message.append(victimLocale.getStringComponent("kick.messages.reason",
                    Placeholder.unparsed("reason", reason)));
            p.kick(message, PlayerKickEvent.Cause.KICK_COMMAND);
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.kick")) {
            if (args.length == 1) {
                return null;
            } else {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.reason"), false));
            }
        }
        return Collections.emptyList();
    }
}
