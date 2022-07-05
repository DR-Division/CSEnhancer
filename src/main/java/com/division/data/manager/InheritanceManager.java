package com.division.data.manager;

import com.division.CrackShotEnhancer;
import com.division.hook.ConfigParser;
import com.shampaggon.crackshot.CSDirector;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.bukkit.Bukkit;

import java.util.*;

public class InheritanceManager {

    private final CrackShotEnhancer Plugin;
    private final CSDirector director;
    private ConfigParser parser;
    private WeaponManager manager;

    public InheritanceManager(CrackShotEnhancer Plugin, CSDirector director, ConfigParser parser, WeaponManager manager) {
        this.Plugin = Plugin;
        this.director = director;
        this.parser = parser;
        this.manager = manager;
    }

    public void checkInheritance() {
        manager.clear();
        Map<String, String> inherits = new HashMap<>();
        for (String key : director.parentlist.values()) {
            String value = parser.getString(key, ".Item_Information.Inheritance");
            if (value != null && director.parentlist.containsValue(value) && parser.getString(value, ".Item_Information.Inheritance") == null) {
                inherits.put(key, value);
            }
        }
        for (String key : inherits.keySet()) {
            //String AK47, AK47_TEMPLATE
            String value = inherits.get(key);
            for (Map.Entry<String, String> node : new HashMap<>(CSDirector.strings).entrySet()) {
                //key = AK47_TEMPLATE.Item_Information.Item_Name
                String inheritKey = node.getKey();
                String inheritValue = node.getValue(); //"HI"
                if (inheritKey.contains(".") && !inheritKey.contains("Item_Information")) {
                    String[] inheritWeapon = inheritKey.split("\\.");
                    StringBuilder inheritNode = new StringBuilder();
                    if (inheritWeapon[0].equalsIgnoreCase(value)) {
                        String lastNode;
                        for (int i = 1; i < inheritWeapon.length; i++)
                            inheritNode.append(".").append(inheritWeapon[i]);
                        lastNode = inheritNode.toString();
                        if (parser.getString(key, lastNode) == null) {
                            CSDirector.strings.put(key + lastNode, inheritValue);
                        }
                    }
                }
            }
            for (Map.Entry<String, Boolean> node : new HashMap<>(CSDirector.bools).entrySet()) {
                //key = AK47_TEMPLATE.Item_Information.Item_Name
                String inheritKey = node.getKey();
                Boolean inheritValue = node.getValue(); //"HI"
                if (inheritKey.contains(".") && !inheritKey.contains("Item_Information")) {
                    String[] inheritWeapon = inheritKey.split("\\.");
                    StringBuilder inheritNode = new StringBuilder();
                    if (inheritWeapon[0].equalsIgnoreCase(value)) {
                        String lastNode;
                        for (int i = 1; i < inheritWeapon.length; i++)
                            inheritNode.append(".").append(inheritWeapon[i]);
                        lastNode = inheritNode.toString();
                        CSDirector.bools.putIfAbsent(key + lastNode, inheritValue);
                    }
                }
            }
            for (Map.Entry<String, Integer> node : new HashMap<>(CSDirector.ints).entrySet()) {
                //key = AK47_TEMPLATE.Item_Information.Item_Name
                String inheritKey = node.getKey();
                int inheritValue = node.getValue(); //"HI"
                if (inheritKey.contains(".") && !inheritKey.contains("Item_Information")) {
                    String[] inheritWeapon = inheritKey.split("\\.");
                    StringBuilder inheritNode = new StringBuilder();
                    if (inheritWeapon[0].equalsIgnoreCase(value)) {
                        String lastNode;
                        for (int i = 1; i < inheritWeapon.length; i++)
                            inheritNode.append(".").append(inheritWeapon[i]);
                        lastNode = inheritNode.toString();
                        CSDirector.ints.putIfAbsent(key + lastNode, inheritValue);
                    }
                }
            }
            for (Map.Entry<String, Double> node : new HashMap<>(CSDirector.dubs).entrySet()) {
                //key = AK47_TEMPLATE.Item_Information.Item_Name
                String inheritKey = node.getKey();
                double inheritValue = node.getValue(); //"HI"
                if (inheritKey.contains(".") && !inheritKey.contains("Item_Information")) {
                    String[] inheritWeapon = inheritKey.split("\\.");
                    StringBuilder inheritNode = new StringBuilder();
                    if (inheritWeapon[0].equalsIgnoreCase(value)) {
                        String lastNode;
                        for (int i = 1; i < inheritWeapon.length; i++)
                            inheritNode.append(".").append(inheritWeapon[i]);
                        lastNode = inheritNode.toString();
                        if (!CSDirector.dubs.containsKey(key + lastNode) || CSDirector.dubs.get(key + lastNode) == 0.0)
                            CSDirector.dubs.putIfAbsent(key + lastNode, inheritValue);
                    }
                }
            }
            String rde = parser.getString(key,  ".Explosive_Devices.Device_Info");
            if (rde != null) {
                String[] rdeRefined = rde.split("-");
                if (rdeRefined.length == 3) {
                    director.rdelist.put(rdeRefined[1], key);
                }
            }
            String rdeInfo = parser.getString(key, ".Explosive_Devices.Device_Type");
            if (rdeInfo != null && rdeInfo.equalsIgnoreCase("trap")) {
                String itemName = parser.getString(key,".Item_Information.Item_Name");
                String displayName = director.toDisplayForm(itemName);
                director.boobs.put(displayName, key);
            }
        }


    }

}
