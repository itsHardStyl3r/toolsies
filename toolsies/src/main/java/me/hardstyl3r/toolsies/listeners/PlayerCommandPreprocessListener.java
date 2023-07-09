package me.hardstyl3r.toolsies.listeners;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.UserManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.help.HelpTopic;

public class PlayerCommandPreprocessListener implements Listener {

    private final UserManager userManager;

    public PlayerCommandPreprocessListener(Toolsies plugin, UserManager userManager) {
        this.userManager = userManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        String cmd = e.getMessage().split(" ")[0];
        HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(cmd);
        if (topic == null || (!topic.canSee(player) && !player.isOp())) {
            player.sendMessage(userManager.determineLocale(player).getStringComponent("unknown_command",
                    Placeholder.unparsed("command", cmd)));
            e.setCancelled(true);
        }
    }
}
