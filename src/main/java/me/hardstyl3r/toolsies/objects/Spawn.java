package me.hardstyl3r.toolsies.objects;

import org.bukkit.Location;

public class Spawn extends tLocation {

    private boolean preferred;
    private boolean def;

    public Spawn(Location l) {
        super(l.getWorld().getName(), l);
        setAdded(System.currentTimeMillis());
    }

    public void setPreferred(boolean b) {
        this.preferred = b;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setDefault(boolean b) {
        this.def = b;
    }

    public boolean isDefault() {
        return this.def;
    }
}
