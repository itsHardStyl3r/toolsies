package me.hardstyl3r.tauth.listeners;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.enums.AuthType;
import me.hardstyl3r.tauth.managers.LoginManagement;
import me.hardstyl3r.tauth.managers.LoginManager;
import me.hardstyl3r.tauth.objects.AuthUser;
import me.hardstyl3r.toolsies.managers.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AuthPlayerPropsListeners implements Listener {

    private final LoginManager loginManager;
    private final UserManager userManager;
    private final LoginManagement loginManagement;

    public AuthPlayerPropsListeners(TAuth plugin, LoginManager loginManager, UserManager userManager, LoginManagement loginManagement) {
        this.loginManager = loginManager;
        this.userManager = userManager;
        this.loginManagement = loginManagement;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        AuthUser authUser = loginManager.getAuth(p);
        if (!authUser.isRegistered()) {
            p.sendMessage(userManager.determineLocale(p).getColoredString("register.register"));
            loginManager.setKickTask(p, AuthType.REGISTER);
        } else if (!authUser.isLoggedIn()) {
            p.sendMessage(userManager.determineLocale(p).getColoredString("login.login"));
            loginManager.setKickTask(p, AuthType.LOGIN);
        }
        loginManagement.initAuth(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        loginManagement.performQuit(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent e) {
        loginManagement.performQuit(e.getPlayer());
    }
}
