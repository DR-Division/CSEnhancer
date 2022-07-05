package com.division.hook;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CrackShotAPI {

    private static CrackShotAPI instance;
    private CSUtility utility;

    static {
        instance = new CrackShotAPI();
    }

    private CrackShotAPI() {
        utility = new CSUtility();
    }

    public static CrackShotAPI getInstance() {
        return instance;
    }

    public String getWeaponTitle(ItemStack stack) {
        return utility.getWeaponTitle(stack);
    }

    public CSDirector getHandle() {
        return utility.getHandle();
    }

    public ItemStack generateWeapon(String weapon) {
        return utility.generateWeapon(weapon);
    }

}
