package com.division.hook;

import com.division.util.CEUtil;
import com.shampaggon.crackshot.CSDirector;

public class ConfigParser {

    private static ConfigParser instance;
    private CSDirector director;

    static {
        instance = new ConfigParser();
    }
    private ConfigParser() {
        director = CEUtil.getHandle();
    }

    public static ConfigParser getInstance() {
        return instance;
    }

    public boolean getBoolean(String weaponTitle, String extra) {
        return director.getBoolean(weaponTitle + extra);
    }

    public int getInt(String weaponTitle, String extra) {
        return director.getInt(weaponTitle + extra);
    }

    public String getString(String weaponTitle, String extra) {
        return director.getString(weaponTitle + extra);
    }
}
