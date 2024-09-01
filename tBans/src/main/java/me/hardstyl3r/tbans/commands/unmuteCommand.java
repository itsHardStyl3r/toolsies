package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.hardstyl3r.tbans.enums.PunishmentType.MUTE;

public class unmuteCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    /**
     * A command to unmute a player.
     * Permissions: 'toolsies.unmute'
     */
    public unmuteCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("unmute").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.unmute")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.unmute")));
            return true;
        }
        if (args.length == 1) {
            String target = args[0];
            if (!punishmentManager.isPunished(target, MUTE)) {
                sender.sendMessage(l.getStringComponent("unmute.player_is_not_muted", Placeholder.unparsed("player_name", target)));
                return true;
            }
            Punishment mute = punishmentManager.getPunishment(target, MUTE);
            punishmentManager.deletePunishment(mute);
            sender.sendMessage(l.getStringComponent("unmute.player_unmuted", Placeholder.unparsed("player_name", mute.getName())));
            Player p = Bukkit.getPlayerExact(target);
            if (p != null)
                p.sendMessage(userManager.determineLocale(p).getStringComponent("unmute.unmute_notification",
                        Placeholder.unparsed("sender_name", sender.getName())));
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.unmute")) {
            if (args.length == 1) {
                List<String> arguments = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers())
                    if (punishmentManager.isPunished(p.getUniqueId(), MUTE)) arguments.add(p.getName());
                return localeManager.formatTabArguments(args[0], arguments);
            }
        }
        return Collections.emptyList();
    }
}
