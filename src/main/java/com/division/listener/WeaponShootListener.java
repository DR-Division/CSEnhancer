package com.division.listener;

import com.division.CrackShotEnhancer;
import com.division.data.Weapon;
import com.division.data.enhanceable.implement.RemoteExplosionData;
import com.division.data.manager.WeaponManager;
import com.division.data.wrapper.WrappedWeaponShootEvent;
import com.division.events.WeaponBreakEvent;
import com.division.events.WeaponRayTraceEvent;
import com.division.hook.ConfigParser;
import com.division.hook.CrackShotAPI;
import com.division.util.CEUtil;
import com.division.util.Raytrace;
import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import com.shampaggon.crackshot.events.WeaponShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class WeaponShootListener implements Listener {

    private ScriptEngineManager manager;
    private ScriptEngine engine;
    private WeaponManager weaponManager;

    public WeaponShootListener(WeaponManager weaponManager) {
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("js");
        this.weaponManager = weaponManager;
    }

    @EventHandler
    public void onWeaponPreShoot(WeaponPreShootEvent event) {
        Player p = event.getPlayer();
        ItemStack stack = p.getInventory().getItemInMainHand();
        ConfigParser hook = ConfigParser.getInstance();
        String title = event.getWeaponTitle();
        String proType = hook.getString(title, ".Shooting.Projectile_Type");
        Weapon data = weaponManager.getWeapon(stack, event.getWeaponTitle());
        if (proType != null && proType.equals("energy") && hook.getBoolean(title, ".Shooting.Decrease_Lag_On_Energy")) {
            event.setCancelled(true);
            CEUtil.energyShot(event.getPlayer(), CrackShotAPI.getInstance().getHandle(), title);
        }
        if (data != null) {
            WrappedWeaponShootEvent shootEvent = new WrappedWeaponShootEvent.Builder().setData(event).Build();
            data.shoot(shootEvent);
            String nextWeapon = shootEvent.getReturnValue();
            if (nextWeapon != null && !shootEvent.isCancelled() && CEUtil.isWeaponExist(nextWeapon)) {
                CEUtil.fireWeapon(p, nextWeapon, shootEvent.isLeftClick(), true);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onShoot(WeaponShootEvent event) {
        Player p = event.getPlayer();
        String weaponTitle = event.getWeaponTitle();
        ItemStack stack = p.getInventory().getItemInMainHand();
        ConfigParser hook = ConfigParser.getInstance();
        Weapon data = weaponManager.getWeapon(stack, weaponTitle);
        if (event.getProjectile() != null && ConfigParser.getInstance().getBoolean(event.getWeaponTitle(), ".Shooting.Hit_Scan")) {
            //투사체가 존재하고, 옵션이 활성화 되어 있을때
            int maxDistance = hook.getInt(event.getWeaponTitle(), ".Shooting.Projectile_Speed");
            int maxPenetrate = hook.getInt(event.getWeaponTitle(), ".Shooting.Max_Penetration");
            if (maxDistance < 1)
                maxDistance = 1;
            WeaponRayTraceEvent rayTraceEvent = new WeaponRayTraceEvent(p, maxDistance, maxPenetrate);
            Bukkit.getPluginManager().callEvent(rayTraceEvent);
            if (rayTraceEvent.getRange() < 1)
                rayTraceEvent.setRange(1);
            if (!rayTraceEvent.isCancelled()) {
                int totalDmg;
                event.getProjectile().remove();
                int damage = hook.getInt(weaponTitle, ".Shooting.Projectile_Damage");
                boolean head = hook.getBoolean(weaponTitle, ".Headshot.Enable");
                boolean crit = hook.getBoolean(weaponTitle, ".Critical_Hits.Enable");
                boolean back = hook.getBoolean(weaponTitle, ".Backstab.Enable");
                boolean isHead = false;
                boolean isCrit = false;
                boolean isBack = false;
                int headShot = hook.getInt(weaponTitle, ".Headshot.Bonus_Damage");
                int critDmg = hook.getInt(weaponTitle, ".Critical_Hits.Bonus_Damage");
                int critChance = hook.getInt(weaponTitle, ".Critical_Hits.Chance");
                int backDmg = hook.getInt(weaponTitle, ".Backstab.Bonus_Damage");
                for (Entity entity : Raytrace.getEntities(p, weaponTitle, rayTraceEvent.getRange(), rayTraceEvent.getMaxPenetration())) {
                    Vector direction = p.getLocation().getDirection();
                    totalDmg = damage;
                    if (head && checkHeadShot(entity, p, direction)) {
                        totalDmg += headShot;
                        isHead = true;
                    }
                    if (crit && checkCritical(critChance)) {
                        totalDmg += critDmg;
                        isCrit = true;
                    }
                    if (back && checkBack(entity, p)) {
                        totalDmg += backDmg;
                        isBack = true;
                    }
                    WeaponDamageEntityEvent damageEvent = new WeaponDamageEntityEvent(p, entity, p, weaponTitle, totalDmg, isHead, isBack, isCrit);
                    Bukkit.getPluginManager().callEvent(damageEvent);
                    if (!damageEvent.isCancelled()) {
                        LivingEntity livingEntity = (LivingEntity) entity;
                        livingEntity.damage(totalDmg, p);
                        livingEntity.setNoDamageTicks(0);
                    }
                }
            }
        }
        if (hook.getInt(weaponTitle, ".Shooting.Break_Chance") > 0) {
            int rand;
            int chance = hook.getInt(weaponTitle, ".Shooting.Break_Chance");
            String breakMessage = hook.getString(weaponTitle, ".Shooting.Break_Message");
            rand = ThreadLocalRandom.current().nextInt(1, 101);
            if (rand <= chance) {
                WeaponBreakEvent breakEvent = new WeaponBreakEvent(p, p.getInventory().getItemInMainHand(), weaponTitle);
                Bukkit.getPluginManager().callEvent(breakEvent);
                if (!breakEvent.isCancelled()) {
                    p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    if (breakMessage != null)
                        p.sendMessage(breakMessage);
                }
            }
        }
        if (event.getProjectile() != null && hook.getBoolean(weaponTitle, ".Explosions.Apply_Delay_On_Projectile")) {
            int time = hook.getInt(weaponTitle, ".Explosions.Explosion_Delay");
            if (time > 0) {
                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(CrackShotEnhancer.class), () -> {
                    if (!event.getProjectile().isDead() && event.getProjectile().getLocation().getBlock().getType().toString().contains("AIR")) {
                        CrackShotAPI.getInstance().getHandle().projectileExplosion(event.getProjectile(), weaponTitle, true, p, false, false, null, null, false, 0);
                        event.getProjectile().remove();
                    }
                }, time);
            }
        }
        if (event.getProjectile() != null && hook.getString(weaponTitle, ".Shooting.Bullet_Spread_As_Vector") != null) {
            Vector loc = p.getLocation().getDirection();
            //PDX = 디렉션 X ...
            String value = hook.getString(weaponTitle, ".Shooting.Bullet_Spread_As_Vector").replace("PDX", String.valueOf(loc.getX())).replace("PDY", String.valueOf(loc.getY())).replace("PDZ", String.valueOf(loc.getZ()));
            String[] split = value.split("//");
            if (split.length == 3) {
                for (String val : split) {
                    if (Pattern.matches("[^0-9+-/*.Mathrndomsic()]+", val))
                        break;
                }
                try {
                    String x = String.valueOf(engine.eval(split[0]));
                    String y = String.valueOf(engine.eval(split[1]));
                    String z = String.valueOf(engine.eval(split[2]));
                    double xVal = Double.parseDouble(x);
                    double yVal = Double.parseDouble(y);
                    double zVal = Double.parseDouble(z);
                    if (!Double.isNaN(xVal) && !Double.isNaN(yVal) && !Double.isNaN(zVal))
                        event.getProjectile().setVelocity(new Vector(xVal, yVal, zVal));
                    else
                        throw new NumberFormatException("NaN");

                }
                catch (ScriptException | IllegalArgumentException e) {
                    Bukkit.getLogger().log(Level.WARNING, "[CrackShotEnhancer] " + weaponTitle + " is going to use a javascript but there was an error : " + e.getClass().getSimpleName() + "\n# : " + e.getMessage());
                }
            }
        }
        if (event.getProjectile() != null && hook.getBoolean(weaponTitle, ".Explosions.Remote_Explode") && weaponTitle.equals(CEUtil.getWeaponTitle(stack))) {
            if (data == null) {
                weaponManager.addWeapon(p.getUniqueId(), weaponTitle, stack);
            }
            data = weaponManager.getWeapon(stack, weaponTitle);
            data.addEnhance(new RemoteExplosionData());
        }

        if (data != null) {
            WrappedWeaponShootEvent shootEvent = new WrappedWeaponShootEvent.Builder().setData(event).Build();
            data.shoot(shootEvent);
        }
    }


    public boolean checkHeadShot(Entity victim, Player shooter, Vector direction) {
        Vector center = victim.getLocation().clone().add(new Vector(0, victim.getHeight() - 0.25, 0)).subtract(shooter.getEyeLocation()).toVector().normalize();
        Vector top = victim.getLocation().clone().add(new Vector(0, victim.getHeight(), 0)).subtract(shooter.getEyeLocation()).toVector().normalize();
        Vector bot = victim.getLocation().clone().add(new Vector(0, victim.getHeight() - 0.5, 0)).subtract(shooter.getEyeLocation()).toVector().normalize();
        double accuracy = center.angle(top) + center.angle(bot);
        accuracy += accuracy * 1 / 5;
        return direction.angle(top) + direction.angle(bot) < accuracy;
    }

    public boolean checkCritical(int chance) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randVal = random.nextInt(1, 101);
        return chance >= randVal;
    }

    public boolean checkBack(Entity victim, Player shooter) {
        Location victimLoc = victim.getLocation();
        Location directionLoc = victim.getLocation().clone().add(new Vector(0, victim.getHeight(), 0));
        victimLoc.setPitch(45f);
        directionLoc.setPitch(45f);
        Vector vicDirection = victimLoc.getDirection();
        Vector direction = directionLoc.subtract(shooter.getLocation()).toVector().normalize();
        return direction.angle(vicDirection) <= 1.1;
    }


}
