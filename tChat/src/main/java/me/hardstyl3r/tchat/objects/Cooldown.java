package me.hardstyl3r.tchat.objects;

import me.hardstyl3r.tchat.enums.CooldownType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Cooldown {

    private final UUID uuid;
    private final CooldownType type;
    private final Long when;
    private Long duration;

    public Cooldown(UUID uuid, CooldownType type) {
        this.uuid = uuid;
        this.type = type;
        this.when = System.currentTimeMillis();
    }

    public Cooldown(Player p, CooldownType type) {
        this.uuid = p.getUniqueId();
        this.type = type;
        this.when = System.currentTimeMillis();
    }

    public UUID getUUID() {
        return uuid;
    }

    public CooldownType getType() {
        return type;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long l) {
        this.duration = l;
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() >= (when + duration);
    }

    public Long getRemaining() {
        long sum = (when + duration) - System.currentTimeMillis();
        return (sum <= 0 ? 0L : sum);
    }
}
