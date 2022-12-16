package me.hardstyl3r.tchat.managers;

import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
            message = StringUtils.translateBothColorCodes(message);
        boolean hasTargetMsgToggled = messagingManager.hasMsgToggled(target.getUniqueId());
        boolean canBypassToggle = sender.hasPermission("toolsies.msg.bypasstoggle");
        long time = System.currentTimeMillis();
        Locale targetLocale = userManager.determineLocale(target);
        if (sender instanceof Player p) {
            if (p.getUniqueId().equals(target.getUniqueId())) {
                sender.sendMessage(l.getStringComponent("msg.text_self", Placeholder.unparsed("time", localeManager.getTime(time)), Placeholder.unparsed("name", sender.getName()), Placeholder.unparsed("message", message)));
                return;
            }
            messagingManager.setConversation(p.getUniqueId(), target.getUniqueId());
        }
        sender.sendMessage(l.getStringComponent("msg.text_sender",
                Placeholder.unparsed("time", localeManager.getTime(time)),
                Placeholder.unparsed("name", target.getName()),
                Placeholder.unparsed("message", message))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName() + " ")));
        if (!hasTargetMsgToggled || canBypassToggle)
            target.sendMessage(targetLocale.getStringComponent("msg.text_target",
                    Placeholder.unparsed("time", localeManager.getTime(time)),
                    Placeholder.unparsed("name", sender.getName()),
                    Placeholder.unparsed("message", message))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName() + " ")));
        if (hasTargetMsgToggled && canBypassToggle)
            sender.sendMessage(l.getStringComponent("msg.target_toggled_info", Placeholder.unparsed("name", target.getName())));
        messagingManager.doSocialSpy(sender, target, message);
        target.playSound(target.getLocation(), Sound.BLOCK_LAVA_POP, 30F, 1.2F);
    }
}
