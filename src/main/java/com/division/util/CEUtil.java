package com.division.util;

import com.division.data.Weapon;
import com.division.data.enhanceable.implement.AmmoData;
import com.division.data.enhanceable.implement.GrenadeCookData;
import com.division.data.manager.WeaponManager;
import com.division.events.WeaponDetonateEvent;
import com.division.hook.ConfigParser;
import com.division.hook.CrackShotAPI;
import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;
import com.shampaggon.crackshot.events.WeaponFireRateEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CEUtil {

    private static final CSUtility utility;
    private static final CSDirector director;
    private static WeaponManager manager;
    private static final HashMap<UUID, HashMap<String, AtomicBoolean>> firstShootMap;

    static {
        utility = new CSUtility();
        director = utility.getHandle();
        manager = null;
        firstShootMap = new HashMap<>();
    }

    public static boolean isFirstShot(UUID player, String weapon) {
        if (firstShootMap.containsKey(player)) {
            if (!firstShootMap.get(player).containsKey(weapon)) {
                firstShootMap.get(player).put(weapon, new AtomicBoolean(true));
            }
        }
        else {
            firstShootMap.put(player, new HashMap<>());
            firstShootMap.get(player).put(weapon, new AtomicBoolean(true));
        }
        return firstShootMap.get(player).get(weapon).get();
    }

    public static void setFirstShot(UUID player, String weapon, boolean val) {
        isFirstShot(player, weapon);
        firstShootMap.get(player).get(weapon).set(val);
    }

    public static void injectManager(WeaponManager manager) {
        CEUtil.manager = manager;
    }

    public static CSDirector getHandle() {
        return director;
    }

    public static boolean isWeaponExist(String name) {
        return director.parentlist.containsValue(name);
    }

    public static UUID parseUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String getWeaponTitle(ItemStack stack) {
        return utility.getWeaponTitle(stack);
    }

    public static void createExplosion(Player p, String weapon, int time) {
        CEUtil.getHandle().launchGrenade(p, weapon, time, p.getLocation().getDirection(), null, 0);
    }

    public static int getItemStackCapacity(Player player, String weapon, ItemStack item) {
        return director.getAmmoBetweenBrackets(player, weapon, item);

    }

    public static void setItemStackCapacity(String weapon, ItemStack stack, int capacity) {
        if (!director.getBoolean(weapon + ".Shooting.Dual_Wield")) //unSupport
            director.csminion.replaceBrackets(stack, String.valueOf(capacity), weapon);
    }

    public static void explodeProjectile(List<Projectile> inputs) {
        for (Projectile projectile : inputs) {
            Player p = (Player) projectile.getShooter();
            String title = projectile.getMetadata("projParentNode").get(0).asString();
            if (isWeaponExist(title)) {
                director.projectileExplosion(projectile, title, true, p, false, false, null, null, false, 0);
                projectile.remove();
            }
        }
    }

    public static boolean dropGrenade(Player p, Weapon data, Item item) {
        List<GrenadeCookData> list = data.getEnhance(GrenadeCookData.class);
        for (GrenadeCookData i : list) {
            if (i.isRunning()) {
                CEUtil.getHandle().launchGrenade(p, data.getWeaponTitle(), i.getRunnable().getLeftTime(), p.getLocation().getDirection(), null, 0);
                item.remove();
                return true;
            }
        }
        return false;
    }

    public static void shootShrapnel(Player p, String shrapnel, ConfigParser parser, Location exploded) {
        for (Entity entity : p.getWorld().getNearbyEntities(exploded, 3, 3, 3)) {
            if (entity instanceof FallingBlock) {
                Projectile projectile;
                List<String> projectiles = Stream.of("arrow", "snowball", "egg", "fireball", "wither_skull", "splash_potion").map(data -> data = data.toUpperCase()).collect(Collectors.toList());
                String projType = parser.getString(shrapnel, ".Shooting.Projectile_Type").toUpperCase();
                String dragRemInfo = parser.getString(shrapnel, ".Shooting.Removal_Or_Drag_Delay");
                final String[] dragRem = dragRemInfo == null ? null : dragRemInfo.split("-");
                if (dragRem != null) {
                    try {
                        Integer.valueOf(dragRem[0]);
                    }
                    catch (NumberFormatException var51) {
                        p.sendMessage(director.heading + "For the weapon '" + shrapnel + "', the 'Removal_Or_Drag_Delay' node is incorrectly configured.");
                        return;
                    }
                }
                if (projectiles.contains(projType)) {
                    try {
                        EntityType type = EntityType.valueOf(projType);
                        projectile = (Projectile) p.getWorld().spawnEntity(entity.getLocation(), type);
                        projectile.setMetadata("CS_Hardboiled", new FixedMetadataValue(director, true));
                        projectile.setMetadata("CS_NoDeflect", new FixedMetadataValue(director, true));
                        projectile.setVelocity(entity.getVelocity());
                        projectile.setMetadata("projParentNode", new FixedMetadataValue(director, shrapnel));
                        director.callShootEvent(p, projectile, shrapnel);
                        director.playSoundEffects(projectile, shrapnel, ".Shooting.Sounds_Projectile", false, null);
                        if (dragRem != null) {
                            director.prepareTermination(projectile, Boolean.parseBoolean(dragRem[1]), Long.valueOf(dragRem[0]));
                        }
                        entity.remove();
                    }
                    catch (IllegalArgumentException e) {
                        Bukkit.getLogger().info("[CrackShot] " + projType + " isn't projectile");
                    }
                }
            }
        }
    }

    public static void fireWeapon(Player p, boolean leftClick) {
        ItemStack stack = p.getInventory().getItemInMainHand();
        String weaponTitle = utility.getWeaponTitle(stack);
        if (weaponTitle != null) {
            director.fireProjectile(p, weaponTitle, leftClick);
        }
    }


    public static void fireWeapon(Player p, String weapon, boolean isLeftClick, boolean isActualShot) {
        int gunSlot = p.getInventory().getHeldItemSlot();
        final int projAmount = director.getInt(weapon + ".Shooting.Projectile_Amount");
        final boolean oneTime = director.getBoolean(weapon + ".Extras.One_Time_Use");
        final String proType = director.getString(weapon + ".Shooting.Projectile_Type");
        ItemStack item = p.getInventory().getItemInMainHand();
        boolean isFullyAuto = director.getBoolean(weapon + ".Fully_Automatic.Enable");
        int fireRate = director.getInt(weapon + ".Fully_Automatic.Fire_Rate");
        boolean burstEnable = director.getBoolean(weapon + ".Burstfire.Enable");
        int burstShots = director.getInt(weapon + ".Burstfire.Shots_Per_Burst");
        int burstDelay = director.getInt(weapon + ".Burstfire.Delay_Between_Shots_In_Burst");
        boolean shootDisable = director.getBoolean(weapon + ".Shooting.Disable");
        int shootDelay = director.getInt(weapon + ".Shooting.Delay_Between_Shots");
        final Location projLoc = p.getEyeLocation().toVector().add(p.getLocation().getDirection().multiply(0.2D)).toLocation(p.getWorld());
        AtomicReference<String> finalWeapon = new AtomicReference<>(ChatColor.stripColor(weapon));
        if (!shootDisable && !p.hasMetadata(weapon + "shootDelay" + gunSlot + isLeftClick) && !p.hasMetadata("togglesnoShooting" + gunSlot) && !oneTime && projAmount != 0) {
            Weapon data = manager.getWeapon(item, weapon);
            final double zoomAcc = director.getDouble(weapon + ".Scope.Zoom_Bullet_Spread");
            final boolean sneakOn = director.getBoolean(weapon + ".Sneak.Enable");
            boolean sneakToShoot = director.getBoolean(weapon + ".Sneak.Sneak_Before_Shooting");
            final boolean sneakNoRec = director.getBoolean(weapon + ".Sneak.No_Recoil");
            final double sneakAcc = director.getDouble(weapon + ".Sneak.Bullet_Spread");
            String dragRemInfo = director.getString(weapon + ".Shooting.Removal_Or_Drag_Delay");
            final String[] dragRem = dragRemInfo == null ? null : dragRemInfo.split("-");
            if (dragRem != null) {
                try {
                    Integer.valueOf(dragRem[0]);
                }
                catch (NumberFormatException var51) {
                    p.sendMessage(director.heading + "For the weapon '" + weapon + "', the 'Removal_Or_Drag_Delay' node is incorrectly configured.");
                    return;
                }
            }
            if (isActualShot && (isFullyAuto || burstEnable)) {
                if (data == null) {
                    //메인 무기가 아니고 특수탄일때
                    isFullyAuto = false;
                    burstEnable = false;

                }
            }

            if (!sneakToShoot || p.isSneaking() && !director.isAir(p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType())) {
                director.terminateReload(p);
                if (!burstEnable) {
                    burstShots = 1;
                }

                if (isFullyAuto) {
                    burstShots = 5;
                    burstDelay = 1;
                }

                final double projSpeed = (double) director.getInt(weapon + ".Shooting.Projectile_Speed") * 0.1D;
                final boolean setOnFire = director.getBoolean(weapon + ".Shooting.Projectile_Flames");
                final boolean noBulletDrop = director.getBoolean(weapon + ".Shooting.Remove_Bullet_Drop");
                if (!director.getBoolean(weapon + ".Scope.Zoom_Before_Shooting") || p.hasMetadata("ironsights")) {
                    int shootReloadBuffer = director.getInt(weapon + ".Reload.Shoot_Reload_Buffer");
                    if (shootReloadBuffer > 0) {
                        Map<Integer, Long> lastShot = director.last_shot_list.computeIfAbsent(p.getName(), k -> new HashMap<>());
                        lastShot.put(gunSlot, System.currentTimeMillis());
                    }
                    int burstStart = 0;
                    if (isFullyAuto) {
                        WeaponFireRateEvent event = new WeaponFireRateEvent(p, weapon, item, fireRate);
                        director.getServer().getPluginManager().callEvent(event);
                        fireRate = event.getFireRate();
                        String pName = p.getName();
                        if (!director.rpm_ticks.containsKey(pName)) {
                            director.rpm_ticks.put(pName, 1);
                        }

                        if (!director.rpm_shots.containsKey(pName)) {
                            director.rpm_shots.put(pName, 0);
                        }

                        burstStart = director.rpm_shots.get(pName);
                        director.rpm_shots.put(pName, 5);
                    }
                    final int fireRateFinal = fireRate;

                    for (int burst = burstStart; burst < burstShots; ++burst) {
                        final boolean isLastShot = burst >= burstShots - 1;
                        int finalBurstStart = burstStart;
                        int finalBurst = burst;
                        boolean finalIsFullyAuto = isFullyAuto;
                        int task_ID = Bukkit.getScheduler().scheduleSyncDelayedTask(director, () -> {
                            int detectedAmmo;
                            if (finalIsFullyAuto) {
                                String pName = p.getName();
                                if (!director.rpm_shots.containsKey(pName) || !director.rpm_ticks.containsKey(pName))
                                    return;
                                int shotsLeft = director.rpm_shots.get(pName) - 1;
                                director.rpm_shots.put(pName, shotsLeft);
                                detectedAmmo = director.rpm_ticks.get(pName);
                                director.rpm_ticks.put(pName, detectedAmmo >= 20 ? 1 : detectedAmmo + 1);
                                if (shotsLeft == 0) {
                                    director.burst_task_IDs.remove(pName);
                                }
                                if (!director.isValid(detectedAmmo, fireRateFinal)) {
                                    return;
                                }
                            }


                            if (isActualShot) {

                                if (data != null) {
                                    //가상형 총기 - 현재 총 사용시
                                    List<AmmoData> list = data.getEnhance(AmmoData.class);
                                    if (list.size() != 0) {
                                        if (finalBurst == finalBurstStart || isFirstShot(p.getUniqueId(), weapon)) {
                                            //버스트 시작 + 모든 첫번째 사격
                                            list.get(0).setFirstShot(false);
                                            setFirstShot(p.getUniqueId(), weapon, false);
                                        }
                                        else if (list.get(0).getTop() == null && list.get(0).size() > 0 && list.get(0).isVirtual()) {
                                            list.get(0).getNextAmmo();
                                            finalWeapon.set(data.getWeaponTitle());
                                        }
                                        else {
                                            finalWeapon.set(list.get(0).getNextAmmo());
                                        }
                                    }
                                    else
                                        finalWeapon.set(null);
                                }
                                if (finalWeapon.get() == null) {
                                    return;
                                }
                            }



                            double bulletSpread = director.getDouble(finalWeapon.get() + ".Shooting.Bullet_Spread");
                            if (p.isSneaking() && sneakOn) {
                                bulletSpread = sneakAcc;
                            }

                            if (p.hasMetadata("ironsights")) {
                                bulletSpread = zoomAcc;
                            }

                            if (bulletSpread == 0.0D) {
                                bulletSpread = 0.1D;
                            }

                            boolean noVertRecoil = director.getBoolean(finalWeapon.get() + ".Abilities.No_Vertical_Recoil");
                            boolean jetPack = director.getBoolean(finalWeapon.get() + ".Abilities.Jetpack_Mode");
                            double recoilAmount = (double) director.getInt(finalWeapon.get() + ".Shooting.Recoil_Amount") * 0.1D;
                            if (recoilAmount != 0.0D && (!sneakOn || !sneakNoRec || !p.isSneaking())) {
                                if (!jetPack) {
                                    Vector velToAdd = p.getLocation().getDirection().multiply(-recoilAmount);
                                    if (noVertRecoil) {
                                        velToAdd.multiply(new Vector(1, 0, 1));
                                    }

                                    p.setVelocity(velToAdd);
                                }
                                else {
                                    p.setVelocity(new Vector(0.0D, recoilAmount, 0.0D));
                                }
                            }

                            boolean clearFall = director.getBoolean(finalWeapon.get() + ".Shooting.Reset_Fall_Distance");
                            if (clearFall) {
                                p.setFallDistance(0.0F);
                            }

                            director.csminion.giveParticleEffects(p, finalWeapon.get(), ".Particles.Particle_p_Shoot", true, null);
                            director.csminion.givePotionEffects(p, finalWeapon.get(), ".Potion_Effects.Potion_Effect_Shooter", "shoot");
                            director.csminion.displayFireworks(p, finalWeapon.get(), ".Fireworks.Firework_p_Shoot");
                            director.csminion.runCommand(p, finalWeapon.get());
                            if (director.getBoolean(finalWeapon.get() + ".Abilities.Hurt_Effect")) {
                                p.playEffect(EntityEffect.HURT);
                            }

                            String projectile_type = director.getString(finalWeapon.get() + ".Shooting.Projectile_Type");
                            int timer = director.getInt(finalWeapon.get() + ".Explosions.Explosion_Delay");
                            boolean airstrike = director.getBoolean(finalWeapon.get() + ".Airstrikes.Enable");
                            if (airstrike) {
                                timer = director.getInt(finalWeapon.get() + ".Airstrikes.Flare_Activation_Delay");
                            }

                            String soundsShoot = director.getString(finalWeapon.get() + ".Shooting.Sounds_Shoot");

                            WeaponPreShootEvent event = new WeaponPreShootEvent(p, null, null, 0.0, false); //dummy
                            if (isActualShot) {
                                if (data == null) {
                                    Weapon newData = manager.getWeapon(item);
                                    if (newData != null && newData.getEnhance(AmmoData.class).size() != 0 && newData.getEnhance(AmmoData.class).get(0).isVirtual())
                                        event = new WeaponPreShootEvent(p, newData.getWeaponTitle(), soundsShoot, bulletSpread, isLeftClick);
                                    else
                                        event = new WeaponPreShootEvent(p, finalWeapon.get(), soundsShoot, bulletSpread, isLeftClick);
                                    director.plugin.getServer().getPluginManager().callEvent(event);
                                }
                                else {
                                    if (data.getEnhance(AmmoData.class).size() != 0 && data.getEnhance(AmmoData.class).get(0).isVirtual()) {
                                        event = new WeaponPreShootEvent(p, data.getWeaponTitle(), soundsShoot, bulletSpread, isLeftClick);
                                        director.plugin.getServer().getPluginManager().callEvent(event);
                                    }
                                }

                            }
                            else
                                 event = new WeaponPreShootEvent(p, finalWeapon.get(), soundsShoot, bulletSpread, isLeftClick);
                            director.playSoundEffects(p, finalWeapon.get(), null, false, null, event.getSounds());
                            if (!event.isCancelled()) {
                                bulletSpread = event.getBulletSpread();

                                for (int i = 0; i < projAmount; ++i) {
                                    Random r = new Random();
                                    double yaw = Math.toRadians(-p.getLocation().getYaw() - 90.0F);
                                    double pitch = Math.toRadians(-p.getLocation().getPitch());
                                    double[] spread = new double[]{1.0D, 1.0D, 1.0D};

                                    for (int t = 0; t < 3; ++t) {
                                        spread[t] = (r.nextDouble() - r.nextDouble()) * bulletSpread * 0.1D;
                                    }

                                    double x = Math.cos(pitch) * Math.cos(yaw) + spread[0];
                                    double y = Math.sin(pitch) + spread[1];
                                    double z = -Math.sin(yaw) * Math.cos(pitch) + spread[2];
                                    Vector dirVel = new Vector(x, y, z);
                                    if (proType != null) {
                                        if (!proType.equalsIgnoreCase("grenade") && !proType.equalsIgnoreCase("flare")) {
                                            if (proType.equalsIgnoreCase("energy")) {
                                                energyShot(p, getHandle(), finalWeapon.get());
                                            }
                                            else if (proType.equalsIgnoreCase("splash")) {
                                                ThrownPotion splashPot = p.getWorld().spawn(projLoc, ThrownPotion.class);
                                                ItemStack potType = director.csminion.parseItemStack(director.getString(finalWeapon.get() + ".Shooting.Projectile_Subtype"));
                                                if (potType != null) {
                                                    try {
                                                        splashPot.setItem(potType);
                                                    }
                                                    catch (IllegalArgumentException var49) {
                                                        p.sendMessage(director.heading + "The value for 'Projectile_Subtype' of weapon '" + finalWeapon.get() + "' is not a splash potion!");
                                                    }
                                                }

                                                if (setOnFire) {
                                                    splashPot.setFireTicks(6000);
                                                }

                                                if (noBulletDrop) {
                                                    director.noArcInArchery(splashPot, dirVel.multiply(projSpeed));
                                                }

                                                splashPot.setShooter(p);
                                                splashPot.setMetadata("projParentNode", new FixedMetadataValue(director.plugin, finalWeapon.get()));
                                                splashPot.setVelocity(dirVel.multiply(projSpeed));
                                                director.callShootEvent(p, splashPot, finalWeapon.get());
                                                if (dragRem != null) {
                                                    director.prepareTermination(splashPot, Boolean.parseBoolean(dragRem[1]), Long.valueOf(dragRem[0]));
                                                }
                                            }
                                            else {
                                                Projectile snowball;
                                                if (projectile_type.equalsIgnoreCase("arrow")) {
                                                    snowball = (Projectile) p.getWorld().spawnEntity(projLoc, EntityType.ARROW);
                                                }
                                                else if (projectile_type.equalsIgnoreCase("egg")) {
                                                    snowball = (Projectile) p.getWorld().spawnEntity(projLoc, EntityType.EGG);
                                                    snowball.setMetadata("CS_Hardboiled", new FixedMetadataValue(director, true));
                                                }
                                                else if (projectile_type.equalsIgnoreCase("fireball")) {
                                                    snowball = p.launchProjectile(LargeFireball.class);
                                                    if (Boolean.parseBoolean(director.getString(finalWeapon.get() + ".Shooting.Projectile_Subtype"))) {
                                                        snowball.setMetadata("CS_NoDeflect", new FixedMetadataValue(director, true));
                                                    }
                                                }
                                                else if (projectile_type.equalsIgnoreCase("witherskull")) {
                                                    snowball = p.launchProjectile(WitherSkull.class);
                                                }
                                                else {
                                                    snowball = (Projectile) p.getWorld().spawnEntity(projLoc, EntityType.SNOWBALL);
                                                }

                                                if (setOnFire) {
                                                    snowball.setFireTicks(6000);
                                                }

                                                if (noBulletDrop) {
                                                    director.noArcInArchery(snowball, dirVel.multiply(projSpeed));
                                                }

                                                snowball.setShooter(p);
                                                snowball.setVelocity(dirVel.multiply(projSpeed));
                                                snowball.setMetadata("projParentNode", new FixedMetadataValue(director, finalWeapon.get()));
                                                director.callShootEvent(p, snowball, finalWeapon.get());
                                                director.playSoundEffects(snowball, finalWeapon.get(), ".Shooting.Sounds_Projectile", false, null);
                                                if (dragRem != null) {
                                                    director.prepareTermination(snowball, Boolean.parseBoolean(dragRem[1]), Long.valueOf(dragRem[0]));
                                                }
                                            }
                                        }
                                        else {
                                            director.launchGrenade(p, finalWeapon.get(), timer, dirVel.multiply(projSpeed), null, 0);
                                        }
                                    }
                                }

                            }

                        }, (long) burstDelay * burst + 1L);
                        String user = p.getName();
                        Collection<Integer> values = director.burst_task_IDs.computeIfAbsent(user, k -> new ArrayList<>());
                        values.add(task_ID);
                    }
                    p.setMetadata(weapon + "shootDelay" + gunSlot + isLeftClick, new FixedMetadataValue(director, true));
                    director.csminion.tempVars(p, weapon + "shootDelay" + gunSlot + isLeftClick, (long) shootDelay);
                }
            }
        }
    }

    public static void activeC4(Location target, Player p) {
        HashMap<String, String> list = new HashMap<>(); //이름 - 폭발물 이름 형식 저장
        for (Entity entity : p.getWorld().getNearbyEntities(target, 1.5, 1.5, 1.5)) {
            if (entity instanceof Item) {
                Map<String, Map<String, ArrayDeque<Item>>> bombs = director.itembombs;
                for (String name : bombs.keySet()) {
                    for (String bombName : bombs.get(name).keySet()) {
                        for (Item item : bombs.get(name).get(bombName)) {
                            if (item.getLocation().equals(entity.getLocation()))
                                list.put(name, bombName);
                        }
                    }
                }
            }
        }
        actualC4Explode(p, target, list);
    }

    public static void activeC4(Location target, Player p, int radius) {
        CSDirector director = CrackShotAPI.getInstance().getHandle();
        HashMap<String, String> list = new HashMap<>(); //이름 - 폭발물 이름 형식 저장
        for (Entity entity : p.getWorld().getNearbyEntities(target, radius, radius, radius)) {
            if (entity instanceof Item) {
                Map<String, Map<String, ArrayDeque<Item>>> bombs = director.itembombs;
                for (String name : bombs.keySet()) {
                    for (String bombName : bombs.get(name).keySet()) {
                        for (Item item : bombs.get(name).get(bombName)) {
                            if (item.getLocation().equals(entity.getLocation()) && !name.equalsIgnoreCase(p.getName()))
                                list.put(name, bombName);
                        }
                    }
                }
            }
        }
        actualC4Explode(p, target, list);
    }

    private static void actualC4Explode(Player p, Location target, Map<String, String> map) {
        if (map.size() != 0) {
            WeaponDetonateEvent event = new WeaponDetonateEvent(p, target, map);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                for (String key : map.keySet()) {
                    Player t = Bukkit.getPlayer(key);
                    ItemStack stack = CrackShotAPI.getInstance().generateWeapon(map.get(key));
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName("«-1»");
                    stack.setItemMeta(meta);
                    director.detonateC4(t, stack, map.get(key), "itembomb");
                }
            }
        }
    }

    public static void energyShot(Player shooter, CSDirector Plugin, String weaponTitle) {
        int projAmount = Plugin.getInt(weaponTitle + ".Shooting.Projectile_Amount");
        for (int i = 0; i < projAmount; ++i) {
            Vector dirVel = shooter.getLocation().getDirection();
            PermissionAttachment attachment = shooter.addAttachment(Plugin);
            attachment.setPermission("nocheatplus", true);
            attachment.setPermission("anticheat.check.exempt", true);
            String proOre = Plugin.getString(weaponTitle + ".Shooting.Projectile_Subtype");
            if (proOre == null) {
                shooter.sendMessage(Plugin.heading + "The weapon '" + weaponTitle + "' does not have a value for 'Projectile_Subtype'.");
                return;
            }

            String[] proInfo = proOre.split("-");
            if (proInfo.length != 4) {
                shooter.sendMessage(Plugin.heading + "The value provided for 'Projectile_Subtype' of the weapon '" + weaponTitle + "' has an incorrect format.");
                return;
            }

            int wallLimit = 0;
            int hitCount = 0;
            int wallCount = 0;

            int range;
            int hitLimit;
            double radius;
            try {
                range = Integer.parseInt(proInfo[0]);
                hitLimit = Integer.parseInt(proInfo[3]);
                radius = Double.parseDouble(proInfo[1]);
                if (proInfo[2].equalsIgnoreCase("all")) {
                    wallLimit = -1;
                }
                else if (!proInfo[2].equalsIgnoreCase("none")) {
                    wallLimit = Integer.parseInt(proInfo[2]);
                }


            }
            catch (NumberFormatException var50) {
                shooter.sendMessage(Plugin.heading + "The value provided for 'Projectile_Subtype' of the weapon '" + weaponTitle + "' contains an invalid number.");
                return;
            }

            HashSet<Block> hitBlocks = new HashSet<>();
            HashSet<Integer> hitMobs = new HashSet<>();
            Vector vecShift = dirVel.normalize().multiply(radius);
            Location locStart = shooter.getEyeLocation();

            label238:
            for (double k = 0.0D; k < (double) range; k += radius) {
                locStart.add(vecShift);
                Block hitBlock = locStart.getBlock();
                if (hitBlock.getType() != Material.AIR) {
                    if (wallLimit != -1 && !hitBlocks.contains(hitBlock)) {
                        ++wallCount;
                        if (wallCount > wallLimit) {
                            break;
                        }

                        hitBlocks.add(hitBlock);
                    }
                }
                else {
                    for (Entity ent : locStart.getWorld().getNearbyEntities(locStart, radius, radius, radius)) {
                        if (ent instanceof LivingEntity && ent != shooter && !hitMobs.contains(ent.getEntityId()) && !ent.isDead()) {
                            if (ent instanceof Player) {
                                ent.setMetadata("CS_Energy", new FixedMetadataValue(Plugin, weaponTitle));
                                ((LivingEntity) ent).damage(0.0D, shooter);
                            }
                            else
                                Plugin.dealDamage(shooter, (LivingEntity) ent, null, weaponTitle);

                            hitMobs.add(ent.getEntityId());
                            ++hitCount;
                            if (hitLimit != 0 && hitCount >= hitLimit) {
                                break label238;
                            }
                        }
                    }
                }
            }

            Plugin.callShootEvent(shooter, null, weaponTitle);
            Plugin.playSoundEffects(shooter, weaponTitle, ".Shooting.Sounds_Projectile", false, null);
            shooter.removeAttachment(attachment);
        }

    }
}
