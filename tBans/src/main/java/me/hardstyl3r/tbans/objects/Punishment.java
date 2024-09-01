package me.hardstyl3r.tbans.objects;

import me.hardstyl3r.tbans.enums.PunishmentType;
import org.bukkit.ChatColor;

import java.util.UUID;

public class Punishment {

    private final String name;
    private final PunishmentType type;
    private UUID uuid;
    private Long duration;
    private Long date;
    private String sender;
    private String reason;
    private int id;

    public Punishment(String name, PunishmentType pt) {
        this.name = name.toLowerCase();
        this.type = pt;
        this.date = System.currentTimeMillis();
    }

    public String getName() {
        return this.name;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public Long getDuration() {
        return (isPermanent() ? -1L : this.duration);
    }

    public void setDuration(Long l) {
        this.duration = l;
    }

    public boolean isPermanent() {
        return this.duration == null || this.duration < 1;
    }

    public Long getDate() {
        return this.date;
    }

    public void setDate(Long l) {
        this.date = l;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String s) {
        this.sender = s;
    }

    public PunishmentType getType() {
        return this.type;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String s) {
        if (s != null) this.reason = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', s));
    }

    public Long getRemaining() {
        return (isPermanent() ? -1L : this.duration - System.currentTimeMillis());
    }

    public int getId() {
        return this.id;
    }

    public void setId(Integer i) {
        this.id = i;
    }

    public boolean isExpired() {
        return getRemaining() <= 0 && !isPermanent();
    }
}
