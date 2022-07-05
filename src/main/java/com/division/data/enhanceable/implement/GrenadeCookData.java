package com.division.data.enhanceable.implement;

import com.division.CrackShotEnhancer;
import com.division.data.Weapon;
import com.division.data.enhanceable.Interactable;
import com.division.data.enhanceable.Shootable;
import com.division.data.manager.WeaponManager;
import com.division.data.wrapper.WrappedWeaponShootEvent;
import com.division.enums.Priority;
import com.division.hook.ConfigParser;
import com.division.runnable.GrenadeRunnable;
import com.division.util.CEUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class GrenadeCookData implements Interactable, Shootable {

    private Priority priority;
    private boolean isEnabled;
    private GrenadeRunnable runnable;
    private Weapon data;
    private CrackShotEnhancer Plugin;
    private WeaponManager manager;
    private ConfigParser parser;

    public GrenadeCookData(ConfigParser parser, CrackShotEnhancer Plugin, Weapon data, WeaponManager manager) {
        this.isEnabled = true;
        this.priority = Priority.NORMAL;
        this.Plugin = Plugin;
        this.data = data;
        this.manager = manager;
        this.parser = parser;
        runnable = null;
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

    public void removeData() {
        runnable.cancel();
        data.removeEnhance(GrenadeCookData.class);
    }

    public void explodeData(Player p) {
        removeData(); //버리는 경우, GUI 에 옮기는 경우 예외처리
        for (int i = 0; i < 47; i++) {
            ItemStack stack;
            if (i == 46)
                stack = p.getItemOnCursor();
            else
                stack = p.getInventory().getItem(i);
            if (stack != null && !stack.getType().toString().contains("AIR")) {
                Weapon val = manager.getWeapon(stack, data.getWeaponTitle());
                if (val != null && val.equals(data)) {
                    CEUtil.createExplosion(p, val.getWeaponTitle(), getRunnable().getLeftTime());
                    p.getInventory().remove(stack);
                    if (i == 46)
                        p.setItemOnCursor(null);
                    return;
                }
            }
        }

    }
    public GrenadeRunnable getRunnable() {
        return runnable == null ? null : runnable;
    }
    public boolean isRunning() {
        if (runnable == null)
            return false;
        else
            return runnable.isRunning();
    }

    @Override
    public void interact(Player p) {
        if (runnable == null)
            runnable = new GrenadeRunnable(p.getUniqueId(), this, data, parser);
        if (!runnable.isRunning()) {
            runnable.runTaskTimer(Plugin, 0L, 2L);
        }

    }

    @Override
    public void shoot(WrappedWeaponShootEvent event) {
        if (event.isPrepareShoot() && isRunning()) {
            CEUtil.getHandle().csminion.oneTime(event.getPlayer());
            CEUtil.createExplosion(event.getPlayer(), data.getWeaponTitle(), getRunnable().getLeftTime());
            removeData();
        }
    }
}
