package me.hardstyl3r.tauth.events;

import me.hardstyl3r.tauth.enums.AuthType;
import me.hardstyl3r.tauth.objects.AuthUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerAuthSuccessfulEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player loggedIn;
    private final AuthUser user;
    private final AuthType type;
    private boolean isCancelled;

    public PlayerAuthSuccessfulEvent(Player loggedIn, AuthUser user, AuthType type) {
        this.loggedIn = loggedIn;
        this.user = user;
        this.type = type;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    public Player getPlayer() {
        return this.loggedIn;
    }

    public AuthUser getAuthUser() {
        return this.user;
    }

    public AuthType getType() {
        return type;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
