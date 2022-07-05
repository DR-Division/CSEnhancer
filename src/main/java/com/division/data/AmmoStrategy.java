package com.division.data;

import org.bukkit.inventory.ItemStack;

public interface AmmoStrategy {

    String getAmmo(ItemStack stack); //데이터가 없는경우 null

}
