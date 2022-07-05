package com.division.listener;

import com.division.enums.PenetrationType;
import com.division.events.WeaponDetonateEvent;
import com.division.events.WeaponPenetrationDamageEvent;
import com.division.events.WeaponPenetrationEvent;
import com.division.hook.ConfigParser;
import com.division.hook.CrackShotAPI;
import com.division.util.CEUtil;
import com.shampaggon.crackshot.CSDirector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProjectileInteractListener implements Listener {

    @EventHandler
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player p = (Player) event.getEntity().getShooter();
            ItemStack stack = p.getInventory().getItemInMainHand();
            if (stack != null && stack.getType().toString().contains("BOW")) {
                CrackShotAPI api = CrackShotAPI.getInstance();
                String title = CEUtil.getWeaponTitle(stack);
                if (title != null && ConfigParser.getInstance().getBoolean(title, ".Shooting.Bow_Fixer")) {
                    //무기를 발사했으나 풀차징이 안되서 화살이 날아간 경우
                    CrackShotAPI.getInstance().getHandle().fireProjectile(p, title, false);
                    stack.setDurability((short)0);
                    event.getEntity().remove();
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();
        if (source instanceof Player) {
            Player p = (Player) source;
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item != null && CEUtil.getWeaponTitle(item) != null && (event.getHitBlock() != null || event.getHitEntity() != null)) {
                boolean entityPenetrate;
                boolean wallPenetrate;
                boolean isCrit;
                boolean actC4;
                int penetrateRange;
                int maxPenetrate;
                Location start;
                Vector next;
                PenetrationType type;
                isCrit = false;
                String title = CEUtil.getWeaponTitle(item);
                ConfigParser hook = ConfigParser.getInstance();
                entityPenetrate = hook.getBoolean(title, ".Shooting.Entity_Penetration");
                wallPenetrate = hook.getBoolean(title, ".Shooting.Wall_Penetration");
                actC4 = hook.getBoolean(title, ".Shooting.Active_C4");
                penetrateRange = hook.getInt(title, ".Shooting.Penetration_Range");
                maxPenetrate = hook.getInt(title, ".Shooting.Max_Penetration");
                if (maxPenetrate == 0)
                    maxPenetrate = 999;
                if (entityPenetrate || wallPenetrate || actC4) {
                    if (event.getHitBlock() != null) {
                        start = fixLocation(event.getHitBlock().getLocation());
                        type = PenetrationType.BLOCK;
                    }
                    else {
                        start = event.getHitEntity().getLocation();
                        type = PenetrationType.ENTITY;
                    }
                    if (actC4)
                        CEUtil.activeC4(start.clone().add(new Vector(0,1,0)), p); //c4 총알 폭파 구현
                    if (((entityPenetrate && type == PenetrationType.ENTITY) || (wallPenetrate && type == PenetrationType.BLOCK)) && penetrateRange > 0) {
                        WeaponPenetrationEvent penetrateEvent = new WeaponPenetrationEvent(p, start, type, penetrateRange, maxPenetrate);
                        Bukkit.getPluginManager().callEvent(penetrateEvent);
                        if (!penetrateEvent.isCancelled() && penetrateEvent.getRange() != 0) {
                            next = start.toVector().subtract(p.getLocation().toVector()).normalize();
                            //블럭 반복을 통한 엔티티 수집 및 관통 구현
                            BlockIterator iterator = new BlockIterator(p.getWorld(), start.toVector(), next, 1, penetrateEvent.getRange());
                            ArrayList<LivingEntity> entities = new ArrayList<>();
                            while (iterator.hasNext()) {
                                Block block = iterator.next();
                                for (Entity entity : p.getWorld().getNearbyEntities(block.getLocation(), 0.4, 3, 0.4)) {
                                    if (entity instanceof LivingEntity) {
                                        LivingEntity victim = (LivingEntity) entity;
                                        if (!entities.contains(victim) && entities.size() <= penetrateEvent.getMaxPenetration()) {
                                            entities.add(victim);
                                        }
                                    }
                                }
                            }
                            for (LivingEntity entity : entities) {
                                int victimDmg = hook.getInt(title, ".Shooting.Projectile_Damage");
                                int chance = hook.getInt(title, ".Critical_Hits.Chance");
                                if (hook.getBoolean(title, ".Critical_Hits.Enable")) {
                                    int random = ThreadLocalRandom.current().nextInt(1, 101);
                                    if (chance >= random) {
                                        victimDmg += hook.getInt(title, ".Critical_Hits.Bonus_Damage");
                                        isCrit = true;
                                    }
                                }
                                WeaponPenetrationDamageEvent damageEvent = new WeaponPenetrationDamageEvent(p, entity, null, type, CEUtil.getWeaponTitle(item), victimDmg, false, false, isCrit);
                                Bukkit.getPluginManager().callEvent(damageEvent);
                                if (!damageEvent.isCancelled()) {
                                    WeaponDamageEntityEvent dmgEvent = new WeaponDamageEntityEvent(p, entity, null, CEUtil.getWeaponTitle(item), victimDmg, false, false, isCrit);
                                    Bukkit.getPluginManager().callEvent(dmgEvent);
                                    if (!dmgEvent.isCancelled())
                                        entity.damage(damageEvent.getDamage());
                                }
                            }
                        }
                    }
                }
            }
        }
    }





    public Location fixLocation(Location value) {
        value.subtract(new Vector(0, 1, 0));
        value.setX(Math.round(value.getX()) + 0.5);
        value.setY(Math.round(value.getY()));
        value.setZ(Math.round(value.getZ()) + 0.5);
        return value;
    }


}
