package com.division.data.enhanceable.implement;

import com.division.data.enhanceable.Interactable;
import com.division.enums.Priority;
import com.division.hook.ConfigParser;
import com.division.util.CEUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MeleeData implements Interactable {

    private Priority priority;
    private boolean isEnabled;
    private ConfigParser parser;
    private String weapon;

    public MeleeData(ConfigParser parser, String weapon) {
        this.parser = parser;
        this.weapon = weapon;
        this.isEnabled = true;
        priority = Priority.NORMAL;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Priority value) {
        this.priority = value;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean val) {
        this.isEnabled = val;
    }

    @Override
    public void interact(Player p) {
        String melee = parser.getString(weapon, ".Shooting.Melee_Weapon");
        if (CEUtil.isWeaponExist(melee)) {
            CEUtil.fireWeapon(p, melee, false, false); //무조건 우클릭 공격무기.
        }
    }
}
