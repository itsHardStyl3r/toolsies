package me.hardstyl3r.toolsies.objects;

import org.bukkit.Location;

import java.util.UUID;

public class tLocation {

    private final String name;
    private Location location;
    private long added;
    private UUID owner;

    public tLocation(String name, Location l) {
        this.name = name;
        this.location = l;
    }

    public String getName() {
        return this.name;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location l) {
        this.location = l;
    }

    public Long getAdded() {
        return this.added;
    }

    public void setAdded(Long l) {
        this.added = l;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }
}
