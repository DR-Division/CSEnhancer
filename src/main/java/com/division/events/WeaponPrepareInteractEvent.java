package com.division.events;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WeaponPrepareInteractEvent extends Event implements Cancellable{

    private Player p;
    private boolean isCancelled;
    private String weaponTitle;
    private static final HandlerList HANDLERS = new HandlerList();
    private final boolean isLeftClick;

    public WeaponPrepareInteractEvent(Player player, String weaponTitle, boolean isLeftClick) {
        this.isLeftClick = isLeftClick;
        this.weaponTitle = weaponTitle;
        this.p = player;
        isCancelled = false;
    }

    public boolean isLeftClick() {
        return this.isLeftClick;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
    public Player getPlayer() {
        return p;
    }

    public String getWeaponTitle() {
        return weaponTitle;
    }
}
