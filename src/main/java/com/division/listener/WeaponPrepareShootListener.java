package com.division.listener;

import com.division.CrackShotEnhancer;
import com.division.data.Weapon;
import com.division.data.enhanceable.implement.AmmoData;
import com.division.data.manager.WeaponManager;
import com.division.data.wrapper.WrappedWeaponShootEvent;
import com.division.events.WeaponPrepareInteractEvent;
import com.division.hook.ConfigParser;
import com.division.util.CEUtil;
import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.MaterialManager;
import com.shampaggon.crackshot.events.WeaponAttachmentToggleEvent;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import com.shampaggon.crackshot.events.WeaponScopeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Iterator;
import java.util.List;

public class WeaponPrepareShootListener implements Listener {

    private ConfigParser parser;
    private CrackShotEnhancer Plugin;
    private WeaponManager manager;

    public WeaponPrepareShootListener(ConfigParser parser, CrackShotEnhancer Plugin, WeaponManager manager) {
        this.parser = parser;
        this.Plugin = Plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onPrepareShoot(WeaponPrepareShootEvent event) {
        //호출된경우 무기가 발사된경우다.
        if (!event.isCancelled() && event.getWeaponTitle() != null) {
            Player p = event.getPlayer();
            ItemStack stack = p.getInventory().getItemInMainHand();
            String weapon = event.getWeaponTitle();
            Weapon data = manager.getWeapon(stack, weapon);
            if (data == null) {
                manager.addWeapon(event.getPlayer().getUniqueId(), event.getWeaponTitle(), event.getPlayer().getInventory().getItemInMainHand());
                data = manager.getWeapon(stack, weapon);
            }
            if (parser.getBoolean(event.getWeaponTitle(), ".Ammo.Enable") || parser.getBoolean(event.getWeaponTitle(), ".Shooting.Cancel_Hand_Swing")) {
                List<AmmoData> list = data.getEnhance(AmmoData.class);
                if (list.size() == 0) {
                    boolean exit = false;
                    Weapon main = manager.getWeapon(event.getPlayer().getInventory().getItemInMainHand());
                    while (main != null) {
                        if (!parser.getBoolean(main.getWeaponTitle(), ".Item_Information.Attachments.Type") && main != data) {
                            for (AmmoData val : main.getEnhance(AmmoData.class)) {
                                data.addEnhance(val);
                                exit = true;
                                break;
                            }
                        }
                        if (exit)
                            break;
                        main = main.getNext();
                        if (main == null) {
                            AmmoData ammo = new AmmoData(data, parser);
                            ammo.init(CEUtil.getItemStackCapacity(p, weapon, stack));
                            data.addEnhance(ammo);
                            break;
                        }
                    }

                }
            }
            //일반 이벤트(사격)
            for (AmmoData val : data.getEnhance(AmmoData.class)) {
                if (val.requireCancel()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInteractPrepare(WeaponPrepareInteractEvent event) {
        if (!event.isCancelled() && event.getWeaponTitle() != null) {
            Player p = event.getPlayer();
            ItemStack stack = p.getInventory().getItemInMainHand();
            String weapon = event.getWeaponTitle();
            Weapon data = manager.getWeapon(stack, weapon);

            if (data != null && !p.hasMetadata(weapon + "shootDelay" + p.getInventory().getHeldItemSlot() + event.isLeftClick())) {
                String nextWeapon;
                WrappedWeaponShootEvent shootEvent = new WrappedWeaponShootEvent.Builder().setData(event).Build();
                nextWeapon = data.shoot(shootEvent);
                List<AmmoData> ammoData = data.getEnhance(AmmoData.class);
                if (nextWeapon != null && !shootEvent.isCancelled() && CEUtil.isWeaponExist(nextWeapon)) {
                    CEUtil.fireWeapon(p, nextWeapon, shootEvent.isLeftClick(), true);
                }
                if (ammoData.size() != 0 && ammoData.get(0).size() != 0 && nextWeapon == null) {
                    CEUtil.fireWeapon(p, data.getWeaponTitle(), shootEvent.isLeftClick(), true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        //PrepareInteract 호출부
        if (event.getAction() != Action.PHYSICAL) {
            Player shooter = event.getPlayer();
            ItemStack item = shooter.getInventory().getItemInMainHand();
            String parent_node = CEUtil.getWeaponTitle(item);
            CSDirector director = CEUtil.getHandle();
            if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock().getType().toString().contains("WALL_SIGN")) {
                return;
            }
            if (parent_node == null) {
                return;
            }

            if (!parser.getBoolean(parent_node, ".Item_Information.Melee_Mode") && !director.validHotbar(shooter, parent_node)) {
                return;
            }

            boolean rightShoot = parser.getBoolean(parent_node, ".Shooting.Right_Click_To_Shoot");
            boolean dualWield = director.isDualWield(shooter, parent_node, item);
            boolean leftClick = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
            boolean rightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
            boolean rdeEnable = parser.getBoolean(parent_node, ".Explosive_Devices.Enable");
            String[] attachment = director.getAttachment(parent_node, item);
            if (attachment[0] != null) {
                if (dualWield || (attachment[0].equalsIgnoreCase("accessory") && rdeEnable)) {
                    return;
                }

            }

            if (!item.getItemMeta().getDisplayName().contains("§")) {
                return;
            }

            if ((!rightShoot || !rightClick) && (rightShoot || !leftClick) && !dualWield) {
                return;
            }
            if (!rdeEnable && item.getType() != Material.BOW) {
                String projType = parser.getString(parent_node, ".Shooting.Projectile_Type");
                boolean underwater = parser.getBoolean(parent_node, ".Extras.Disable_Underwater");
                String[] validTypes = new String[]{"arrow", "snowball", "egg", "grenade", "flare", "fireball", "witherskull", "energy", "splash"};
                if (underwater) {
                    Location loc = shooter.getEyeLocation();
                    if (loc.getBlock().getType().toString().toUpperCase().endsWith("WATER")) {
                        return;
                    }
                }
                if (projType != null) {
                    for (String type : validTypes) {
                        if (projType.equalsIgnoreCase(type)) {
                            if (CEUtil.getItemStackCapacity(shooter, parent_node, item) == 0) {
                                CEUtil.getHandle().delayedReload(shooter, parent_node);
                            }
                            else {
                                WeaponPrepareInteractEvent prepareEvent = new WeaponPrepareInteractEvent(shooter, parent_node, leftClick);
                                Plugin.getServer().getPluginManager().callEvent(prepareEvent);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
}
