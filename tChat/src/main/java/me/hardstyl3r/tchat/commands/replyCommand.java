package me.hardstyl3r.tchat.commands;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.tchat.managers.MessagingManagement;
import me.hardstyl3r.tchat.managers.MessagingManager;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class replyCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final MessagingManager messagingManager;
    private final MessagingManagement messagingManagement;

    public replyCommand(TChat plugin, UserManager userManager, LocaleManager localeManager, MessagingManager messagingManager, MessagingManagement messagingManagement) {
        plugin.getCommand("reply").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.messagingManager = messagingManager;
        this.messagingManagement = messagingManagement;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(l.getStringComponent("console_sender"));
            return true;
        }
        if (!sender.hasPermission("toolsies.reply")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.reply")));
            return true;
        }
        if (args.length > 0) {
            Player p = (Player) sender;
            UUID victim = messagingManager.getConversation(p);
            if (victim == null) {
                sender.sendMessage(l.getStringComponent("reply.has_no_convo"));
                return true;
            }
            if (Bukkit.getPlayer(victim) == null) {
                sender.sendMessage(l.getStringComponent("reply.recipient_offline"));
                return true;
            }
            Player target = Bukkit.getPlayer(messagingManager.getConversation(p));
            String message = localeManager.createMessage(args, 0);
            messagingManagement.sendMessage(sender, target, message, l);
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.reply")) {
            Locale l = userManager.determineLocale(sender);
            return Collections.singletonList(localeManager.formatArgument(l.getString("common.message"), true));
        }
        return Collections.emptyList();
    }
}
