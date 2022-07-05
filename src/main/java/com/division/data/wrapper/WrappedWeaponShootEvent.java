package com.division.data.wrapper;

import com.division.events.WeaponPrepareInteractEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import com.shampaggon.crackshot.events.WeaponShootEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class WrappedWeaponShootEvent {

    private final Player p;
    private final String weapon;
    private final boolean isPrepareShoot;
    private final boolean isPreShoot;
    private final boolean isShoot;
    private final boolean isLeftClick;
    private final boolean isCancelled;
    private final Entity projectile;
    private String returnValue;

    private WrappedWeaponShootEvent(Player p, String weapon, boolean isPrepareShoot, boolean isPreShoot, boolean isShoot, boolean isLeftClick, boolean isCancelled, Entity projectile) {
        this.p = p;
        this.weapon = weapon;
        this.isPrepareShoot = isPrepareShoot;
        this.isPreShoot = isPreShoot;
        this.isShoot = isShoot;
        this.isLeftClick = isLeftClick;
        this.isCancelled = isCancelled;
        this.projectile = projectile;
        returnValue = "";
    }

    public Player getPlayer() {
        return this.p;
    }

    public String getWeaponTitle() {
        return this.weapon;
    }

    public boolean isPrepareShoot() {
        return isPrepareShoot;
    }

    public boolean isPreShoot() {
        return isPreShoot;
    }

    public boolean isShoot() {
        return isShoot;
    }

    public Entity getProjectile() {
        return projectile;
    }

    public boolean isLeftClick() {
        return isLeftClick;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public String getReturnValue() {
        return this.returnValue;
    }

    public void setReturnValue(String value) {
        this.returnValue = value;
    }

    public static class Builder {

        private Player p;
        private String weaponTitle;
        private boolean isPrepareShoot;
        private boolean isPreShoot;
        private boolean isShoot;
        private boolean isLeftClick;
        private boolean isCancelled;
        private Entity projectile;

        public Builder() {
            p = null;
            weaponTitle = null;
            isPrepareShoot = false;
            isPreShoot = false;
            isShoot = false;
            isLeftClick = false;
            isCancelled = false;
            projectile = null;
        }

        public Builder setData(WeaponPrepareInteractEvent event) {
            this.p = event.getPlayer();
            this.weaponTitle = event.getWeaponTitle();
            this.isPrepareShoot = true;
            this.isLeftClick = event.isLeftClick();
            this.isCancelled = event.isCancelled();
            return this;
        }

        public Builder setData(WeaponPrepareShootEvent event) {
            this.p = event.getPlayer();
            this.weaponTitle = event.getWeaponTitle();
            this.isPrepareShoot = true;
            this.isLeftClick = false;
            this.isCancelled = event.isCancelled();
            return this;
        }

        public Builder setData(WeaponPreShootEvent event) {
            this.p = event.getPlayer();
            this.weaponTitle = event.getWeaponTitle();
            this.isPreShoot = true;
            this.isLeftClick = event.isLeftClick();
            return this;
        }

        public Builder setData(WeaponShootEvent event) {
            this.p = event.getPlayer();
            this.weaponTitle = event.getWeaponTitle();
            this.isShoot = true;
            this.projectile = event.getProjectile();
            return this;
        }

        public WrappedWeaponShootEvent Build() {
            return new WrappedWeaponShootEvent(p, weaponTitle, isPrepareShoot, isPreShoot, isShoot, isLeftClick, isCancelled, projectile);
        }
    }
}
