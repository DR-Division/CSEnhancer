package com.division.data.enhanceable.implement;

import com.division.data.enhanceable.Interactable;
import com.division.data.enhanceable.Shootable;
import com.division.data.wrapper.WrappedWeaponShootEvent;
import com.division.enums.Priority;
import com.division.util.CEUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteExplosionData implements Interactable, Shootable {

    private List<Projectile> projList;
    private boolean isOptimizeEnabled;
    private Priority priority;
    private boolean isEnabled;

    public RemoteExplosionData() {
        isEnabled = true;
        isOptimizeEnabled = true;
        priority = Priority.NORMAL;
        projList = new ArrayList<>();
    }

    public boolean isOptimizeEnabled() {
        return isOptimizeEnabled;
    }

    public void setOptimizeEnabled(boolean optimizeEnabled) {
        isOptimizeEnabled = optimizeEnabled;
    }

    public void addData(Projectile proj) {
        if (isOptimizeEnabled())
            checkAndOptimize();
        projList.add(proj);
    }

    public List<Projectile> getAll() {
        List<Projectile> ret  = projList.stream().filter(data -> !data.isDead() && data.getLocation().getWorld().getBlockAt(data.getLocation()).getType().toString().contains("AIR")).collect(Collectors.toList());
        projList.clear();
        return ret;
    }

    public void checkAndOptimize() {
        if (projList.size() > 1000)
            projList.removeIf(data -> data.isDead() || data.getLocation().getWorld().getBlockAt(data.getLocation()).getType().toString().contains("AIR"));
    }

    @Override
    public void shoot(WrappedWeaponShootEvent event) {
        if (event.getProjectile() != null && event.getProjectile() instanceof Projectile)
            addData((Projectile) event.getProjectile());
    }

    @Override
    public void interact(Player p) {
        CEUtil.explodeProjectile(getAll());
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Priority value) {
        priority = value;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

}
