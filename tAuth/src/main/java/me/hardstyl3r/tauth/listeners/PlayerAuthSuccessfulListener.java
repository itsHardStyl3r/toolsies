package me.hardstyl3r.tauth.listeners;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.managers.LoginManager;
import org.bukkit.event.Listener;

public class PlayerAuthSuccessfulListener implements Listener {

    private final LoginManager loginManager;

    public PlayerAuthSuccessfulListener(TAuth plugin, LoginManager loginManager) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.loginManager = loginManager;
    }
}
