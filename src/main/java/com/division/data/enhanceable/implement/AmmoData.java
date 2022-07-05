package com.division.data.enhanceable.implement;

import com.division.data.AmmoStrategy;
import com.division.data.LoreStrategy;
import com.division.data.Weapon;
import com.division.data.enhanceable.Reloadable;
import com.division.data.enhanceable.Shootable;
import com.division.data.wrapper.WrappedWeaponReloadEvent;
import com.division.data.wrapper.WrappedWeaponShootEvent;
import com.division.enums.Priority;
import com.division.hook.ConfigParser;
import com.division.util.CEUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AmmoData implements Shootable, Reloadable {

    private final ConfigParser parser;
    private Priority priority;
    private boolean isEnabled;
    private AmmoStrategy strategy;
    private Weapon weaponData;
    private final List<String> ammo;
    private final List<String> tempAmmo;
    private boolean isFirstShot;

    public AmmoData(Weapon weaponData, ConfigParser parser) {
        isEnabled = true;
        this.weaponData = weaponData;
        tempAmmo = new ArrayList<>();
        this.parser = parser;
        strategy = new LoreStrategy(parser);
        ammo = new ArrayList<>();
        priority = Priority.NORMAL;
    }


    public void init(int capacity) {
         for (int i = 0; i < capacity; i++)
             addAmmo(null);
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Priority value) {
        this.priority = value;
    }

    public void setStrategy(AmmoStrategy strategy) {
        this.strategy = strategy;
    }

    public AmmoStrategy getStrategy() {
        return strategy;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isVirtual() {
        return parser.getBoolean(weaponData.getWeaponTitle(), ".Shooting.Cancel_Hand_Swing");
    }

    public String getArrayType() {
        String result = parser.getString(weaponData.getWeaponTitle(), ".Ammo.Data_Type");
        if (result != null && (result.equalsIgnoreCase("STACK") || result.equalsIgnoreCase("QUEUE")))
            return result;
        else
            return "STACK";
    }

    public int size() {
        return ammo.size();
    }

    public int getMaxSize(Player p, String weapon, ItemStack stack) {
        return CEUtil.getHandle().getReloadAmount(p, weapon, stack);
    }

    public int getIndex() {
        int i = 0;
        String type = getArrayType();
        if (type.equals("STACK"))
            return size() == 0 ? 0 : size() - 1;
        else
            return i;
    }

    public String getTop() {
        if (size() == 0)
            return null;
        return ammo.get(getIndex());
    }

    public String getNextAmmo() {
        if (size() == 0)
            return null;
        else {
            int index = getIndex();
            String retVal = ammo.get(index);
            ammo.remove(index);
            return retVal;
        }
    }

    public void setAmmoArray(Inventory inventory, int maxSize) {
        int i = 0;
        int require = parser.getBoolean(weaponData.getWeaponTitle(), ".Reload.Reload_Bullets_Individually") ? 1 : maxSize - size();
        if (parser.getBoolean(weaponData.getWeaponTitle(), ".Ammo.Enable")) {
            String val = null;
            for (ItemStack stack : inventory.getContents()) {
                if (stack != null && !stack.getType().toString().contains("AIR") && isValidAmmo(stack)) {
                    int amount = parser.getBoolean(weaponData.getWeaponTitle(), ".Shooting.Take_Ammo_As_Magazine") ? require : stack.getAmount();
                    val = strategy.getAmmo(stack);
                    for (int k = 0; k < amount; k++) {
                        if (i < require) {
                            getTempAmmo().add(val);
                            i++;
                        }
                        else
                            return;
                    }
                }

            }
        }
        else {
            for (int j = 0; j < require; j++) {
                getTempAmmo().add(null);
            }
        }
    }

    public boolean isValidAmmo(ItemStack stack) {
        String weaponTitle = weaponData.getWeaponTitle();
        String itemInfo = parser.getString(weaponData.getWeaponTitle(), ".Ammo.Ammo_Item_ID");
        ItemStack item = CEUtil.getHandle().csminion.parseItemStack(itemInfo);
        if (item != null) {
            String ammoName = parser.getString(weaponTitle, ".Ammo.Ammo_Name_Check");
            boolean checkName = ammoName != null;
            String name = stack.getItemMeta().getDisplayName();
            return stack.getType() == item.getType() && stack.getDurability() == item.getDurability() && (!checkName || (name != null && name.contains(ammoName)));
        }
        return false;
    }


    public void setCurrentAmmo(String weapon, ItemStack stack, int amount) {
        CEUtil.setItemStackCapacity(weapon, stack, amount);
    }

    public void setEnabled(boolean val) {
        this.isEnabled = val;
    }

    public boolean requireCancel() {
        //만약 가상형 총기면 캔슬
        return isVirtual();
    }

    public boolean isFirstShot() {
        return isFirstShot;
    }

    public void setFirstShot(boolean val){
        isFirstShot = val;
    }

    @Override
    public void reload(WrappedWeaponReloadEvent event) {
        if (event.isReloadStart()) {
            tempAmmo.clear();
            setAmmoArray(event.getPlayer().getInventory(), getMaxSize(event.getPlayer(), event.getWeaponTitle(), event.getPlayer().getInventory().getItemInMainHand()));
        }
        else {
            for (String val : tempAmmo) {
                addAmmo(val);
            }
        }
    }

    @Override
    public void shoot(WrappedWeaponShootEvent event) {
        if (!event.isCancelled()) {
            Player p = event.getPlayer();
            String weapon = event.getWeaponTitle();
            ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
            setFirstShot(true);
            if (event.isPrepareShoot() && isVirtual()) {
                if (size() == 0) {
                    setCurrentAmmo(weapon, stack, 0);
                }
                else if (size() == 1 && getTop() == null) {
                    event.setReturnValue(weaponData.getWeaponTitle()); //어찌됐건 발사해야됨
                    getNextAmmo(); //고의로 버림
                }
                else {
                    event.setReturnValue(getNextAmmo());
                    CEUtil.setFirstShot(p.getUniqueId(), weapon,true);
                }
            }
            else if (!isVirtual() && event.isPreShoot()) {
                event.setReturnValue(getNextAmmo()); //일단 주고 판단
                check(p, weapon, stack);
            }
        }
    }

    public void addAmmo(String val) {
        ammo.add(val);
    }

    public void check(Player p, String weapon, ItemStack stack) {
        int currentAmmo = CEUtil.getItemStackCapacity(p, weapon, stack);
        int size = size();
        if (currentAmmo != size) {
            if (currentAmmo > size) {
                for (int i = 0; i < currentAmmo - size + 1; i++) {
                    addAmmo(weapon);
                }
            }
            else {
                for (int i = 0; i < size - currentAmmo + 1; i++) {
                    getNextAmmo();

                }
            }
        }
    }

    public List<String> getTempAmmo() {
        return tempAmmo;
    }
}
