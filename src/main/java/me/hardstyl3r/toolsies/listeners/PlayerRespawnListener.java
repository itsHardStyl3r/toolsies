package me.hardstyl3r.toolsies.listeners;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.LocationManager;
import me.hardstyl3r.toolsies.objects.Spawn;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final LocationManager locationManager;

    public PlayerRespawnListener(Toolsies plugin, LocationManager locationManager) {
        this.locationManager = locationManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        Spawn s = locationManager.getSpawn(p.getLocation());
        e.setRespawnLocation(s.isPreferred() ? s.getLocation() : locationManager.getDefaultSpawn().getLocation());
    }
}
