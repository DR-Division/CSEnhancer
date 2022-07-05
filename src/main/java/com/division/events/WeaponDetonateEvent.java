package com.division.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;

public class WeaponDetonateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    private final Player shooter;
    private final Location location;
    private Map<String, String> detonateData;

    public WeaponDetonateEvent(Player shooter, Location location, Map<String, String> detonateData) {
        this.shooter = shooter;
        this.location = location;
        this.detonateData = detonateData;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getShooter() {
        return shooter;
    }

    public Location getLocation() {
        return location;
    }

    public Map<String,String> getDetonateData() {
        return detonateData;
    }


}
