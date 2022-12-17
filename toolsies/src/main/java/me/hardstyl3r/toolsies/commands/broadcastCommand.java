package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class broadcastCommand implements CommandExecutor, TabCompleter {

    private final LocaleManager localeManager;
    private final UserManager userManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public broadcastCommand(Toolsies plugin, UserManager userManager, LocaleManager localeManager) {
        plugin.getCommand("broadcast").setExecutor(this);
        plugin.getCommand("broadcastraw").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies." + cmd.getName())) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies." + cmd.getName())));
            return true;
        }
        if (args.length > 0) {
            String input = StringUtils.translateBothColorCodes(localeManager.createMessage(args, 0));
            Component message = Component.text(input);
            try {
                if (sender.hasPermission("toolsies.broadcast.minimessage"))
                    message = miniMessage.deserialize(input);
            } catch (Exception ignored) {
            }
            if (!(sender instanceof Player)) sender.sendMessage(message);
            for (Player p : Bukkit.getOnlinePlayers())
                p.sendMessage((cmd.getName().equalsIgnoreCase("broadcast") ?
                        userManager.determineLocale(p).getStringComponent("broadcast.style",
                                Placeholder.component("message", message)) : message));
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies." + cmd.getName())) {
            if (args.length > 0)
                return Collections.singletonList(localeManager.formatArgument(
                        userManager.determineLocale(sender).getString("common.message"), (args.length == 1)));
        }
        return Collections.emptyList();
    }
}
