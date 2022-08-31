package me.hardstyl3r.tchat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.tchat.managers.ChatManager;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AsyncChatListener implements Listener {

    private final ChatManager chatManager;
    private final UserManager userManager;
    private final LocaleManager localeManager;

    public AsyncChatListener(TChat plugin, ChatManager chatManager, UserManager userManager, LocaleManager localeManager) {
        this.chatManager = chatManager;
        this.userManager = userManager;
        this.localeManager = localeManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if (!chatManager.isLocked() || p.hasPermission("toolsies.chat.lock.bypass")) {
            return;
        }
        Locale l = userManager.determineLocale(p);
        p.sendMessage(l.getStringComponent("chat.toggle.chat_locked"));
        e.setCancelled(true);
    }


    @EventHandler
    public void onChatCooldown(AsyncChatEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        if (!chatManager.hasChatCooldown(p)) {
            chatManager.applyChatCooldown(p);
        } else {
            Long chatCooldown = chatManager.getChatCooldown(p).getRemaining();
            Locale l = userManager.determineLocale(p.getUniqueId());
            p.sendMessage(l.getStringComponent("chat.cooldown.chat.onChat", Placeholder.unparsed("time", localeManager.parseTimeWithTranslate(chatCooldown, l))));
            e.setCancelled(true);
        }
    }
}
