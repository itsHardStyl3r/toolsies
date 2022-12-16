package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.utils.LogUtil;
import me.hardstyl3r.toolsies.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.tools.Tool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class LocationManager {

    private final FileConfiguration locationsFile;
    private final ConfigManager configManager;
    private final Toolsies plugin;

    private final HashMap<String, Location> spawns = new HashMap<>();

    /*
    Spawn location - a custom spawn set here
    Spawnpoint - World's default spawnpoint
     */
    public LocationManager(Toolsies plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        locationsFile = configManager.loadConfig(null, "locations");
        loadSpawns();
    }

    private void loadSpawns() {
        if (locationsFile.getConfigurationSection("spawns") == null) return;
        for (String spawnEntry : locationsFile.getConfigurationSection("spawns").getKeys(false)) {
            World world = Bukkit.getWorld(spawnEntry);
            if (world == null) {
                LogUtil.warn("[toolsies] loadSpawns(): Could not find world " + spawnEntry + ". Skipping.");
                continue;
            }
            if (!StringUtils.isDouble(locationsFile.getString("spawns." + spawnEntry + ".x"))
                    || !StringUtils.isDouble(locationsFile.getString("spawns." + spawnEntry + ".y"))
                    || !StringUtils.isDouble(locationsFile.getString("spawns." + spawnEntry + ".z"))) {
                LogUtil.warn("[toolsies] loadSpawns(): Coordinates of " + spawnEntry + "'s spawn are wrong. Skipping.");
                continue;
            }
            Location spawn = new Location(world,
                    locationsFile.getDouble("spawns." + spawnEntry + ".x"),
                    locationsFile.getDouble("spawns." + spawnEntry + ".y"),
                    locationsFile.getDouble("spawns." + spawnEntry + ".z"),
                    (float) locationsFile.getDouble("spawns." + spawnEntry + ".yaw"),
                    (float) locationsFile.getDouble("spawns." + spawnEntry + ".pitch"));
            spawns.put(world.getName(), spawn);
        }
        LogUtil.info("[toolsies] loadSpawns(): Loaded " + spawns.size() + " spawns.");
    }

    public void setSpawn(Location l) {
        String worldName = l.getWorld().getName();
        if (spawns.get(worldName) != null) {
            LogUtil.info("[toolsies] setSpawn(): Replaced previous " + l.getWorld().getName() + " spawn.");
            spawns.remove(worldName);
        } else {
            LogUtil.info("[toolsies] setSpawn(): Created new " + l.getWorld().getName() + " spawn.");
        }
        spawns.put(worldName, l);
        l.getWorld().setSpawnLocation(l);
        locationsFile.set("spawns." + worldName + ".x", l.getX());
        locationsFile.set("spawns." + worldName + ".y", l.getY());
        locationsFile.set("spawns." + worldName + ".z", l.getZ());
        locationsFile.set("spawns." + worldName + ".yaw", l.getYaw());
        locationsFile.set("spawns." + worldName + ".pitch", l.getPitch());
        locationsFile.set("spawns." + worldName + ".preferred", isSpawnPreferred(l.getWorld()));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                configManager.saveConfig(plugin, locationsFile, "locations"));
    }

    /**
     * A method to return spawn location. If not set, will return world's Spawnpoint.
     *
     * @param w A world
     * @return Spawn location or Spawnpoint if null
     */
    public Location getSpawn(World w) {
        if (spawns.get(w.getName()) == null) return w.getSpawnLocation();
        return spawns.get(w.getName());
    }

    /**
     * This method returns preferred spawn of the world.
     * If spawn location in this world is not preferred, return the world's default spawn location.
     *
     * @param w A world
     * @return Location of preferred spawn
     */
    public Location getPreferredSpawn(World w) {
        if (isSpawnPreferred(w)) return getSpawn(w);
        return getDefaultSpawn();
    }

    /**
     * @return Spawn location of default world
     */
    public Location getDefaultSpawn() {
        return getSpawn(Bukkit.getWorlds().get(0));
    }

    /**
     * Determines whether world's spawn location is preferred.
     *
     * @param w A world
     * @return true if world's spawn location is preferred, false otherwise
     */
    public boolean isSpawnPreferred(World w) {
        return locationsFile.getBoolean("spawns." + w.getName() + ".preferred", false) || getDefaultSpawn().getWorld() == w;
    }

    public void switchPreferredSpawn(World w) {
        locationsFile.set("spawns." + w.getName() + ".preferred", !isSpawnPreferred(w));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                configManager.saveConfig(plugin, locationsFile, "locations"));
    }

    public boolean isLocationIdentical(Location l1, Location l2) {
        return ((l1.getX() == l2.getX())
                && (l1.getY() == l2.getY())
                && (l1.getZ() == l2.getZ()));
    }

    public boolean isLocationIdenticalExact(Location l1, Location l2) {
        return isLocationIdentical(l1, l2)
                && (l1.getYaw() == l2.getYaw())
                && (l1.getPitch() == l2.getPitch());
    }
}
