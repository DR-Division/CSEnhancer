package com.division.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class WeaponBreakEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    private final Player shooter;
    private final ItemStack item;
    private final String weaponTitle;

    public WeaponBreakEvent(Player shooter, ItemStack item, String weaponTitle) {
        this.shooter = shooter;
        this.item = item;
        this.isCancelled = false;
        this.weaponTitle = weaponTitle;
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

    public ItemStack getItem(){
        return item;
    }

    public String getWeaponTitle(){
        return weaponTitle;
    }




}