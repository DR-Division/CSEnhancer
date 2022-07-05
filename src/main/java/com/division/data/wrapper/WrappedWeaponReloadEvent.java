package com.division.data.wrapper;

import com.shampaggon.crackshot.events.WeaponReloadCompleteEvent;
import com.shampaggon.crackshot.events.WeaponReloadEvent;
import org.bukkit.entity.Player;

public class WrappedWeaponReloadEvent {

    private final boolean isReloadStart;
    private final boolean isReloadEnd;
    private final String weaponTitle;
    private final Player p;

    private WrappedWeaponReloadEvent(boolean isReloadStart, boolean isReloadEnd, String weaponTitle, Player p) {
        this.isReloadStart = isReloadStart;
        this.isReloadEnd = isReloadEnd;
        this.weaponTitle = weaponTitle;
        this.p = p;
    }

    public boolean isReloadStart() {
        return isReloadStart;
    }

    public boolean isReloadEnd() {
        return isReloadEnd;
    }

    public String getWeaponTitle() {
        return weaponTitle;
    }

    public Player getPlayer() {
        return p;
    }

    public static class Builder {

        private boolean isReloadStart;
        private boolean isReloadEnd;
        private String weaponTitle;
        private Player p;

        public Builder() {
            this.isReloadStart = false;
            this.isReloadEnd = false;
            this.weaponTitle = null;
            this.p = null;
        }

        public Builder setData(WeaponReloadEvent event) {
            this.isReloadStart = true;
            this.p = event.getPlayer();
            this.weaponTitle = event.getWeaponTitle();
            return this;
        }

        public Builder setData(WeaponReloadCompleteEvent event) {
            this.isReloadEnd = true;
            this.p = event.getPlayer();
            this.weaponTitle = event.getWeaponTitle();
            return this;
        }

        public WrappedWeaponReloadEvent Build() {
            return new WrappedWeaponReloadEvent(isReloadStart, isReloadEnd, weaponTitle, p);
        }

    }

}
