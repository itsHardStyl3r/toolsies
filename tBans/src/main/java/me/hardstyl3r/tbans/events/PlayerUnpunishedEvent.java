package me.hardstyl3r.tbans.events;

import me.hardstyl3r.tbans.objects.Punishment;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerUnpunishedEvent extends Event implements Cancellable {

    private final Punishment punishment;
    private final String sender;
    private boolean isCancelled;

    public PlayerUnpunishedEvent(Punishment punishment, String sender) {
        this.punishment = punishment;
        this.sender = sender;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    public Punishment getPunishment() {
        return this.punishment;
    }

    public String getSender() {
        return sender;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
