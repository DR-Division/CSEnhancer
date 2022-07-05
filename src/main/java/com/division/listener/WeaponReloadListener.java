package com.division.listener;

import com.division.data.Weapon;
import com.division.data.enhanceable.implement.AmmoData;
import com.division.data.manager.WeaponManager;
import com.division.data.wrapper.WrappedWeaponReloadEvent;
import com.division.data.wrapper.WrappedWeaponShootEvent;
import com.division.events.WeaponPrepareInteractEvent;
import com.division.hook.ConfigParser;
import com.division.util.CEUtil;
import com.shampaggon.crackshot.events.WeaponReloadCompleteEvent;
import com.shampaggon.crackshot.events.WeaponReloadEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class WeaponReloadListener implements Listener {

    private WeaponManager manager;

    public WeaponReloadListener(WeaponManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReload(WeaponReloadEvent event) {
        ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
        ConfigParser hook = ConfigParser.getInstance();
        int time = hook.getInt(event.getWeaponTitle(), ".Reload.Tactical_Reload");
        if (time != 0 && stack.getItemMeta().getDisplayName() != null && !stack.getItemMeta().getDisplayName().contains("«0»"))
            event.setReloadDuration(time);
        String weapon = event.getWeaponTitle();
        Weapon data = manager.getWeapon(stack, weapon);
        if (data == null) {
            manager.addWeapon(event.getPlayer().getUniqueId(), weapon, stack);
            data = manager.getWeapon(stack, weapon);
        }
        if (data != null) {
            if (data.getEnhance(AmmoData.class).size() == 0 && (hook.getBoolean(weapon, ".Ammo.Enable") || hook.getBoolean(weapon, ".Shooting.Cancel_Hand_Swing"))){
                AmmoData ammoData = new AmmoData(data, hook);
                ammoData.init(CEUtil.getItemStackCapacity(event.getPlayer(), weapon, stack));
                data.addEnhance(ammoData);
            }
            WrappedWeaponReloadEvent reloadEvent = new WrappedWeaponReloadEvent.Builder().setData(event).Build();
            data.reload(reloadEvent);
        }
    }

    @EventHandler
    public void onReloadComplete(WeaponReloadCompleteEvent event) {
        ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
        String weapon = event.getWeaponTitle();
        Weapon data = manager.getWeapon(stack, weapon);
        if (data != null) {
            WrappedWeaponReloadEvent reloadEvent = new WrappedWeaponReloadEvent.Builder().setData(event).Build();
            data.reload(reloadEvent);
        }
    }

}
