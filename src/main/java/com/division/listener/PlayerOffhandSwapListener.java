package com.division.listener;

import com.division.CrackShotEnhancer;
import com.division.data.Weapon;
import com.division.data.enhanceable.implement.GrenadeCookData;
import com.division.data.enhanceable.implement.MeleeData;
import com.division.data.manager.WeaponManager;
import com.division.data.wrapper.WrappedWeaponReloadEvent;
import com.division.hook.ConfigParser;
import com.division.util.CEUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerOffhandSwapListener implements Listener {

    private WeaponManager manager;
    private ConfigParser parser;
    private CrackShotEnhancer Plugin;

    public PlayerOffhandSwapListener(WeaponManager manager, ConfigParser parser, CrackShotEnhancer Plugin) {
        this.manager = manager;
        this.parser = parser;
        this.Plugin = Plugin;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        Player p = event.getPlayer();
        if (p.isSneaking()) {
            event.setCancelled(true);
            ItemStack stack = p.getInventory().getItemInMainHand();
            String weapon = CEUtil.getWeaponTitle(stack);
            Weapon data = manager.getWeapon(stack, weapon);
            if (data == null) {
                manager.addWeapon(p.getUniqueId(), weapon, stack);
                data = manager.getWeapon(stack, weapon);
            }
            if (data != null) {
                checkMelee(data, weapon);
                checkCooking(weapon, data);
                data.interact(p);
            }


        }
    }
    public void checkMelee(Weapon weapon, String weaponTitle) {
        String melee = parser.getString(weaponTitle, ".Shooting.Melee_Weapon");
        if (melee != null && CEUtil.isWeaponExist(melee) && weapon.getEnhance(MeleeData.class).size() == 0) {
            weapon.addEnhance(new MeleeData(parser, weaponTitle));
        }
    }

    public void checkCooking(String weapon, Weapon data) {
        if (parser.getBoolean(weapon, ".Explosions.Enable_Cooking") && data.getEnhance(GrenadeCookData.class).size() == 0) {
            data.addEnhance(new GrenadeCookData(parser, Plugin, data, manager));
        }
    }
}
