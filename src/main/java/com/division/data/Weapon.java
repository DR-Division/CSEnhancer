package com.division.data;

import com.division.data.enhanceable.Enhanceable;
import com.division.data.enhanceable.Interactable;
import com.division.data.enhanceable.Reloadable;
import com.division.data.enhanceable.Shootable;
import com.division.data.wrapper.WrappedWeaponReloadEvent;
import com.division.data.wrapper.WrappedWeaponShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Weapon implements Linkable<Weapon> {

    private final List<Enhanceable> enhances;
    private final String weaponTitle;
    private final UUID creator;
    private Weapon previous;
    private Weapon next;

    public Weapon(@Nullable UUID creator, String weaponTitle) {
        this.creator = creator;
        this.weaponTitle = weaponTitle;
        this.previous = null;
        this.next = null;
        this.enhances = new ArrayList<>();
    }

    @Override
    public Weapon getPrevious() {
        return this.previous;
    }

    @Override
    public Weapon getNext() {
        return this.next;
    }

    @Override
    public void setPrevious(Weapon item) {
        this.previous = item;
    }

    @Override
    public void setNext(Weapon item) {
        this.next = item;
    }

    public String getWeaponTitle() {
        return this.weaponTitle;
    }

    public UUID getCreator() {
        return this.creator;
    }

    public void addEnhance(Enhanceable data) {
        for (Enhanceable val : enhances) {
            if (val.equals(data))
                return;
        }
        enhances.add(data);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getEnhance(Class<T> val) {
        return enhances.stream().filter(data -> {
            if (data.getClass().equals(val) || data.getClass().isAssignableFrom(val))
                return true;
            for (Class<?> clz : data.getClass().getInterfaces()) {
                if (clz.equals(val)) {
                    return true;
                }
            }
            return false;
        }).map(data -> (T) data).collect(Collectors.toList());
    }

    public <T> void removeEnhance(Class<T> val) {
        List<T> item = getEnhance(val);
        for (T data : item) {
            enhances.removeIf(param -> param.equals(data));
        }
    }

    public String shoot(WrappedWeaponShootEvent event) {
        List<Shootable> data = getEnhance(Shootable.class);
        if (data.size() != 0) {
            data.sort(Comparator.comparingInt(a -> a.getPriority().getValue()));
            data.forEach(param -> {
                if (param.isEnabled()) {
                    param.shoot(event);
                }
            });
        }
        return event.getReturnValue();
    }

    public void reload(WrappedWeaponReloadEvent event) {
        List<Reloadable> data = getEnhance(Reloadable.class);
        if (data.size() != 0) {
            data.sort(Comparator.comparingInt(a -> a.getPriority().getValue()));
            data.forEach(param -> {
                if (param.isEnabled())
                    param.reload(event);
            });
        }
    }

    public void interact(Player p) {
        List<Interactable> data = getEnhance(Interactable.class);
        if (data.size() != 0) {
            data.sort(Comparator.comparingInt(a -> a.getPriority().getValue()));
            data.forEach(param -> {
                if (param.isEnabled())
                    param.interact(p);
            });
        }
    }

}
