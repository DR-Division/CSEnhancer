package com.division.util;

import com.division.hook.ConfigParser;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Raytrace {

    public static List<Entity> getEntities(Player shooter, String weaponTitle, int distance, int maxPenetrate) {
        World world = shooter.getWorld();
        Vector direction = shooter.getLocation().getDirection();
        ConfigParser hook = ConfigParser.getInstance();
        String hookBlocks = hook.getString(weaponTitle, ".Shooting.Ignore_Blocks") == null ? "" : hook.getString(weaponTitle, ".Shooting.Ignore_Blocks");
        ArrayList<Entity> list = new ArrayList<>();
        ArrayList<String> blocks = new ArrayList<>(Arrays.asList(hookBlocks.replace(" ", "").split(",")));
        BlockIterator iterator = new BlockIterator(world, shooter.getLocation().toVector(), direction, 2, distance);
        blocks.add("AIR"); //공기는 통과
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (list.size() >= maxPenetrate || (!blocks.contains(block.getType().toString()) && !blocks.contains("ALL")))
                break;
            for (Entity entity : world.getNearbyEntities(block.getLocation(), 3,3,3)) {
                if (entity instanceof LivingEntity && entity != shooter && !list.contains(entity)) {
                    Vector center = entity.getLocation().clone().add(new Vector(0,1,0)).subtract(shooter.getEyeLocation()).toVector().normalize();
                    Vector head = entity.getLocation().clone().add(new Vector(0,entity.getHeight(),0)).subtract(shooter.getEyeLocation()).toVector().normalize();
                    Vector foot = entity.getLocation().clone().subtract(shooter.getEyeLocation()).toVector().normalize();
                    double accuracy = center.angle(head) + center.angle(foot);
                    accuracy += accuracy * 3 / 20;
                    if (direction.angle(head) + direction.angle(foot) < accuracy)
                        list.add(entity);
                }
            }
        }
        return list;
    }

}
