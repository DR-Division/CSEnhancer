package com.division.events;

import com.division.enums.PenetrationType;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class WeaponPenetrationDamageEvent extends WeaponDamageEntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    private final Player shooter;
    private final Entity victim;
    private final PenetrationType type;
    private double damage;

    public WeaponPenetrationDamageEvent(Player player, Entity victim, Entity dmgSource, PenetrationType type,  String weaponTitle, double totalDmg, boolean headShot, boolean backStab, boolean critHit) {
        super(player, victim, dmgSource, weaponTitle, totalDmg, headShot, backStab, critHit);
        this.shooter = player;
        this.victim = victim;
        this.type = type;
        this.damage = totalDmg;
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

    public Entity getVictim() {
        return victim;
    }

    public PenetrationType getType() {
        return type;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

}
