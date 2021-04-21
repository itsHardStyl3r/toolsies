package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.objects.Spawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class LocationManager {

    private final FileConfiguration config;
    private final ConfigManager configManager;

    private final HashMap<String, Spawn> spawns = new HashMap<>();

    public LocationManager(ConfigManager configManager) {
        this.configManager = configManager;
        config = configManager.loadConfig(null, "locations");
        loadSpawns();
    }

    private Location getLocation(String world, String path) {
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    private void loadSpawns() {
        if (config.getConfigurationSection("spawn") == null) {
            System.out.println("Could not find any spawns.");
            return;
        }
        for (String s : config.getConfigurationSection("spawn").getKeys(false)) {
            if (Bukkit.getWorld(s) == null) {
                System.out.println("Could not find world " + s + ".");
                continue;
            }
            Spawn spawn = new Spawn(getLocation(s, "spawn." + s));
            spawn.setPreferred(config.getBoolean("spawn." + s + ".preferred"));
            spawn.setOwner(UUID.fromString(config.getString("spawn." + s + ".owner")));
            spawn.setDefault(config.getBoolean("spawn." + s + ".default"));
            spawn.setAdded(config.getLong("spawn." + s + ".added"));
            spawns.put(s, spawn);
            System.out.println("Found Spawn for world " + s + ".");
        }
    }

    public Spawn setSpawn(Player p) {
        Location l = p.getLocation();
        boolean b = getDefaultSpawn() == null;
        Spawn spawn = new Spawn(l);
        spawn.setOwner(p.getUniqueId());
        spawn.setPreferred(b);
        spawn.setDefault(b);
        if (spawns.get(l.getWorld().getName()) != null) {
            System.out.println("Replaced previous " + l.getWorld().getName() + " spawn.");
            spawn.setLocation(l);
        } else {
            System.out.println("Created new spawn for " + l.getWorld().getName() + ".");
            spawns.put(l.getWorld().getName(), spawn);
        }
        saveSpawn(spawn);
        l.getWorld().setSpawnLocation(l);
        return spawn;
    }

    public Spawn getSpawn(Location l) {
        return getSpawn(l.getWorld().getName());
    }

    public Spawn getSpawn(World w) {
        return getSpawn(w.getName());
    }

    public Spawn getSpawn(String s) {
        return spawns.get(s);
    }

    public Spawn getDefaultSpawn() {
        for (Spawn s : spawns.values()) {
            if (s.isDefault()) return s;
        }
        return null;
    }

    public ArrayList<String> getAvailableSpawns(CommandSender sender) {
        ArrayList<String> worlds = new ArrayList<>();
        for (Spawn s : spawns.values()) {
            if (sender.hasPermission("toolsies.spawn." + s.getName())) {
                if (Bukkit.getWorld(s.getName()) != null) {
                    worlds.add(s.getName());
                }
            }
        }
        return worlds;
    }

    public ArrayList<String> getSpawns() {
        ArrayList<String> worlds = new ArrayList<>();
        for (Spawn s : spawns.values()) {
            worlds.add(s.getName());
        }
        return worlds;
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

    public void saveSpawn(Spawn s) {
        Location l = s.getLocation();
        String path = "spawn." + l.getWorld().getName();
        config.set(path + ".x", l.getX());
        config.set(path + ".y", l.getY());
        config.set(path + ".z", l.getZ());
        config.set(path + ".yaw", l.getYaw());
        config.set(path + ".pitch", l.getPitch());
        config.set(path + ".owner", s.getOwner().toString());
        config.set(path + ".added", s.getAdded());
        config.set(path + ".preferred", s.isPreferred());
        config.set(path + ".default", s.isDefault());
        configManager.saveConfig(null, config, "locations");
    }

    public void deleteSpawn(Spawn s) {
        spawns.remove(s.getName());
        config.set("spawn." + s.getLocation().getWorld().getName(), null);
        configManager.saveConfig(null, config, "locations");
    }
}
