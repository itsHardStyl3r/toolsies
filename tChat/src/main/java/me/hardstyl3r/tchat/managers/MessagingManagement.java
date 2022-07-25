package me.hardstyl3r.tchat.managers;

import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessagingManagement {

    private final MessagingManager messagingManager;
    private final LocaleManager localeManager;
    private final UserManager userManager;

    public MessagingManagement(MessagingManager messagingManager, LocaleManager localeManager, UserManager userManager) {
        this.messagingManager = messagingManager;
        this.localeManager = localeManager;
        this.userManager = userManager;
    }

    public void sendMessage(CommandSender sender, Player target, String message, Locale l) {
        if (sender.hasPermission("toolsies.msg.colored"))
            message = ChatColor.translateAlternateColorCodes('&', message);
        boolean hasTargetMsgToggled = messagingManager.hasMsgToggled(target.getUniqueId());
        boolean canBypassToggle = sender.hasPermission("toolsies.msg.bypasstoggle");
        long time = System.currentTimeMillis();
        Locale targetLocale = userManager.determineLocale(target);
        if (sender instanceof Player p) {
            if (p.getUniqueId().equals(target.getUniqueId())) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("msg.text_self"))
                        .replace("<time>", localeManager.getTime(time))
                        .replace("<name>", sender.getName())
                        .replace("<message>", message));
                return;
            }
            messagingManager.setConversation(p.getUniqueId(), target.getUniqueId());
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("msg.text_sender"))
                .replace("<time>", localeManager.getTime(time))
                .replace("<name>", target.getName())
                .replace("<message>", message));
        if (!hasTargetMsgToggled || canBypassToggle)
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', targetLocale.getString("msg.text_target"))
                    .replace("<time>", localeManager.getTime(time))
                    .replace("<name>", sender.getName())
                    .replace("<message>", message));
        if (hasTargetMsgToggled && canBypassToggle)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("msg.target_toggled_info"))
                    .replace("<name>", target.getName()));
        messagingManager.doSocialSpy(sender, target, message);
        target.playSound(target.getLocation(), Sound.BLOCK_LAVA_POP, 30F, 1.2F);
    }
}
