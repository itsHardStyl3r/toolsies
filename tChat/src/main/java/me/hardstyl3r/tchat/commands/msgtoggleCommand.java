package me.hardstyl3r.tchat.commands;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.tchat.managers.MessagingManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class msgtoggleCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final MessagingManager messagingManager;

    public msgtoggleCommand(TChat plugin, UserManager userManager, MessagingManager messagingManager) {
        plugin.getCommand("msgtoggle").setExecutor(this);
        this.userManager = userManager;
        this.messagingManager = messagingManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(l.getStringComponent("console_sender"));
            return true;
        }
        if (!sender.hasPermission("toolsies.msgtoggle")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.msgtoggle")));
            return true;
        }
        UUID uuid = ((Player) sender).getUniqueId();
        sender.sendMessage(l.getStringComponent("msg.toggle_" + messagingManager.hasMsgToggled(uuid)));
        messagingManager.toggleMsgToggle(uuid);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
