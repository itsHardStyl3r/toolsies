package me.hardstyl3r.tchat.commands;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.tchat.managers.MessagingManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class socialspyCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final MessagingManager messagingManager;

    public socialspyCommand(TChat plugin, UserManager userManager, MessagingManager messagingManager) {
        plugin.getCommand("socialspy").setExecutor(this);
        this.userManager = userManager;
        this.messagingManager = messagingManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(l.getColoredString(l.getString("console_sender")));
            return true;
        }
        if (!sender.hasPermission("toolsies.socialspy")) {
            sender.sendMessage(l.getColoredString("no_permission").replace("<permission>", "toolsies.socialspy"));
            return true;
        }
        UUID uuid = ((Player) sender).getUniqueId();
        sender.sendMessage(l.getColoredString("socialspy.toggle_" + messagingManager.hasSocialspyToggled(uuid)));
        messagingManager.toggleSocialspy(uuid);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
