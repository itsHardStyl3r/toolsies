package me.hardstyl3r.toolsies.objects;

import org.bukkit.entity.Player;

import java.util.UUID;

public class User {

    private final UUID uuid;
    private String name;
    private Locale locale;

    public User(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public User(Player p) {
        this.name = p.getName();
        this.uuid = p.getUniqueId();
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String s) {
        this.name = s;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale l) {
        this.locale = l;
    }
}