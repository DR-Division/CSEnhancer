package com.division.data;

import com.division.hook.ConfigParser;
import com.division.util.CEUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LoreStrategy implements AmmoStrategy{

    private ConfigParser parser;
    public LoreStrategy(ConfigParser parser) {
        this.parser = parser;
    }


    @Override
    public String getAmmo(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null)
            return null;
        for (String val : lore) {
            if (val.contains("'")) {
                for (String var : ChatColor.stripColor(val).split("'")) {
                    if (CEUtil.isWeaponExist(var)) {
                        return var;
                    }
                }
            }
        }
        return null;
    }
}
