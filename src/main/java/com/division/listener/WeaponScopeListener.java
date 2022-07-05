package com.division.listener;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.division.CrackShotEnhancer;
import com.division.data.Weapon;
import com.division.data.enhanceable.implement.AmmoData;
import com.division.data.enhanceable.implement.GrenadeCookData;
import com.division.data.manager.WeaponManager;
import com.division.hook.ConfigParser;
import com.division.hook.CrackShotAPI;
import com.division.util.CEUtil;
import com.shampaggon.crackshot.events.WeaponScopeEvent;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;


public class WeaponScopeListener implements Listener {

    private final CrackShotEnhancer Plugin;
    private WeaponManager manager;

    public WeaponScopeListener(CrackShotEnhancer Plugin, WeaponManager manager) {
        this.Plugin = Plugin;
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onScope(WeaponScopeEvent event) {
        Player shooter = event.getPlayer();
        ConfigParser hook = ConfigParser.getInstance();
        if (hook.getBoolean(event.getWeaponTitle(), ".Scope.Thermal_Scope"))
            setGlow(event.getPlayer(), event.isZoomIn());
        if (hook.getBoolean(event.getWeaponTitle(), ".Scope.Extra_Zoom"))
            setExtraScope(event.getPlayer(), event.isZoomIn());
        if (event.isZoomIn() && hook.getBoolean(event.getWeaponTitle(), ".Scope.Automatic_Zoom")) {
            int distance = hook.getInt(event.getWeaponTitle(), ".Shooting.Projectile_Speed");
            Player min = null;
            for (Entity e : event.getPlayer().getWorld().getNearbyEntities(shooter.getLocation(), distance, distance, distance)) {
                if (e instanceof Player && e != event.getPlayer()) {
                    if (min == null || shooter.getLocation().distance(e.getLocation()) < shooter.getLocation().distance(e.getLocation()))
                        min = (Player) e;
                }
            }
            if (min != null) {
                Vector result = min.getLocation().toVector().subtract(shooter.getLocation().toVector()).normalize();
                Location temp = shooter.getLocation();
                temp.setDirection(result);
                WrapperPlayServerPosition position = new WrapperPlayServerPosition();
                position.setX(0);
                position.setY(0);
                position.setZ(0);
                position.setYaw(temp.getYaw());
                position.setPitch(temp.getPitch());
                position.setFlags(new HashSet<>(Arrays.asList(WrapperPlayServerPosition.PlayerTeleportFlag.X, WrapperPlayServerPosition.PlayerTeleportFlag.Y, WrapperPlayServerPosition.PlayerTeleportFlag.Z)));
                position.sendPacket(shooter);
            }
        }


    }

    //버그 방지
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        String title = CEUtil.getWeaponTitle(event.getItemDrop().getItemStack());
        if (CEUtil.getWeaponTitle(event.getPlayer().getInventory().getItemInMainHand()) != null) {
            setExtraScope(event.getPlayer(), false);
            event.getPlayer().removeMetadata("CE_Thermal", Plugin);
        }
        if (title != null) {
            Weapon data = manager.getWeapon(event.getItemDrop().getItemStack(), title);
            if (data != null) {
                List<AmmoData> ammoData = data.getEnhance(AmmoData.class);
                if (ammoData.size() != 0) {
                    AmmoData main = ammoData.get(0);
                    if (CEUtil.getItemStackCapacity(event.getPlayer(), title, event.getItemDrop().getItemStack()) != main.size() && main.isVirtual())
                        CEUtil.setItemStackCapacity(title, event.getItemDrop().getItemStack(), main.size());
                }
            }
        }
    }


    private void setGlow(Player p, boolean value) {
        int distance = Bukkit.getViewDistance();
        if (value) {
            p.setMetadata("CE_Thermal", new FixedMetadataValue(Plugin, true));
            for (Entity entity : p.getWorld().getNearbyEntities(p.getLocation(), distance * distance, distance * distance, distance * distance)) {
                if (entity instanceof LivingEntity && entity != p) {
                    WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
                    WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(entity);
                    List<WrappedWatchableObject> objects = dataWatcher.getWatchableObjects();
                    objects.get(0).setValue((byte) ((byte) objects.get(0).getValue() | 0x40));
                    meta.setMetadata(objects);
                    meta.setEntityID(entity.getEntityId());
                    meta.sendPacket(p);
                }
            }
        }
        else {
            p.removeMetadata("CE_Thermal", Plugin);
            for (Entity entity : p.getWorld().getNearbyEntities(p.getLocation(), distance * distance + 50, distance * distance + 50, distance * distance + 50)) {
                if (entity instanceof LivingEntity && entity != p) {
                    WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
                    WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(entity);
                    List<WrappedWatchableObject> objects = dataWatcher.getWatchableObjects();
                    objects.get(0).setValue((byte) ((byte) objects.get(0).getValue() & ~0x40));
                    meta.setMetadata(objects);
                    meta.setEntityID(entity.getEntityId());
                    meta.sendPacket(p);
                }
            }
        }
    }

    private void setExtraScope(Player p, boolean value) {
        Bukkit.getScheduler().runTaskLater(Plugin, () -> {
            if (value) {
                p.removePotionEffect(PotionEffectType.SPEED);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 9999, 10, false, false), true);
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999, 200, false, false), false);
                p.setWalkSpeed(-0.2f);
            }
            else {
                p.removePotionEffect(PotionEffectType.SLOW);
                p.removePotionEffect(PotionEffectType.JUMP);
                p.setWalkSpeed(0.2f);
                CrackShotAPI.getInstance().getHandle().unscopePlayer(p, true);
            }
        }, 1L);

    }
}
