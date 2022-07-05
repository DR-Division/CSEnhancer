package com.division.util;

import com.division.CrackShotEnhancer;
import com.division.data.manager.InheritanceManager;
import com.division.data.manager.WeaponManager;

public class EnhancerAPI {

    private static CrackShotEnhancer Plugin;
    private static InheritanceManager manager;
    private static WeaponManager weaponManager;
    private static boolean initialized;

    static {
        Plugin = null;
        manager = null;
        weaponManager = null;
        initialized = false;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void setInitialize() {
        initialized = true;
    }

    public static void inject(CrackShotEnhancer enhancer, InheritanceManager inheritanceManager, WeaponManager weaponData) {
        Plugin = enhancer;
        manager = inheritanceManager;
        weaponManager = weaponData;
        setInitialize();
    }

    public static CrackShotEnhancer getPlugin() {
        return Plugin;
    }

    public static WeaponManager getWeaponManager() {
        return weaponManager;
    }

    public static InheritanceManager getInheritanceManager() {
        return manager;
    }


}
