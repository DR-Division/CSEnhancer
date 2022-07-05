package com.division.listener;

import com.division.events.WeaponDetonateEvent;
import com.division.hook.ConfigParser;
import com.division.hook.CrackShotAPI;
import com.division.util.CEUtil;
import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponExplodeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class WeaponExplosionListener implements Listener {

    @EventHandler
    public void onProjectileExplode(WeaponExplodeEvent event) {
        Player p = event.getPlayer();
        Location loc = event.getLocation();
        String weapon = event.getWeaponTitle();
        ConfigParser parser = ConfigParser.getInstance();
        if (parser.getBoolean(weapon, ".Shooting.Active_C4")) {
            int radius = parser.getInt(weapon, ".Explosions.Explosion_Radius");
            CEUtil.activeC4(event.getLocation(), p, radius);
        }
        if (parser.getBoolean(weapon, ".Shrapnel.Enhanced_Shrapnel")) {
            String shrapnelWeapon = parser.getString(weapon, ".Shrapnel.Shrapnel_Type");
            if (shrapnelWeapon != null && !shrapnelWeapon.equals(weapon)) {
                CEUtil.shootShrapnel(p, shrapnelWeapon, parser, loc);
            }
        }
    }


}
