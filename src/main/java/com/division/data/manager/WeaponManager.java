package com.division.data.manager;

import com.division.data.Weapon;
import com.division.data.wrapper.WrappedWeaponShootEvent;
import com.division.util.CEUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeaponManager {

    private final Map<UUID, Weapon> weapons;

    public WeaponManager() {
        this.weapons = new HashMap<>();
    }

    public Weapon getWeapon(UUID data) {
        return weapons.getOrDefault(data, null);
    }

    public Weapon getWeapon(ItemStack data) {
        UUID parse;
        if (data == null || data.getType().toString().contains("AIR"))
            return null;
        ItemMeta meta = data.getItemMeta();
        if (meta.getLocalizedName() != null) {
            parse = CEUtil.parseUUID(meta.getLocalizedName());
            return parse == null ? null : getWeapon(parse);
        }
        return null;
    }

    public Weapon getWeapon(ItemStack data, String weapon) {
        Weapon main = getWeapon(data);
        if (main != null) {
            Weapon iter = main;
            if (main.getWeaponTitle().equals(weapon))
                return main;
            while (iter.getNext() != null) {
                iter = iter.getNext();
                if (iter.getWeaponTitle().equals(weapon))
                    return iter;
            }
        }
        return null;
    }

    public void clear() {
        weapons.clear();
    }

    public boolean isDataExist(UUID data) {
        return weapons.containsKey(data);
    }


    public boolean isDataExist(ItemStack stack, String weapon) {
        Weapon result;
        result = getWeapon(stack);
        if (result != null) {
            String title = result.getWeaponTitle();
            if (title.equalsIgnoreCase(weapon)) {
                return true;
            }
            else {
                Weapon iter = result;
                while (iter.getNext() != null) {
                    iter = iter.getNext();
                    if (iter.getWeaponTitle().equals(weapon))
                        return true;
                }
                return false;
            }
        }
        else {
            return false;
        }
    }

    public void addWeapon(UUID creator, String weapon, ItemStack stack) {
        Weapon weaponObject = new Weapon(creator, weapon);
        if (getWeapon(stack) != null) {
            //already exist
            Weapon iter = getWeapon(stack);
            while (iter.getNext() != null) {
                iter = iter.getNext();
            }
            //iter는 link의 마지막
            iter.setNext(weaponObject);
            weaponObject.setPrevious(iter);
        }
        else {
            UUID key;
            ItemMeta meta = stack.getItemMeta();
            if (meta.getLocalizedName() != null && CEUtil.parseUUID(meta.getLocalizedName()) != null) {
                //이미 존재, 하지만 데이터가 없음.
                key = CEUtil.parseUUID(meta.getLocalizedName());
            }
            else {
                key = UUID.randomUUID();
                meta.setLocalizedName(key.toString());
                stack.setItemMeta(meta);
            }
            weapons.put(key, weaponObject);
        }
    }

}