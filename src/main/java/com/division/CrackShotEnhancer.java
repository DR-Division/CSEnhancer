package com.division;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.division.data.manager.InheritanceManager;
import com.division.data.manager.WeaponManager;
import com.division.hook.ConfigParser;
import com.division.listener.*;
import com.division.packet.GlowPacketAdapter;
import com.division.util.CEUtil;
import com.division.util.EnhancerAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CrackShotEnhancer extends JavaPlugin {

    private final InheritanceManager manager;
    private final WeaponManager weaponManager;
    private final ConfigParser parser;

    public CrackShotEnhancer() {
        weaponManager = new WeaponManager();
        manager = new InheritanceManager(this, CEUtil.getHandle(), ConfigParser.getInstance(), weaponManager);
        parser = ConfigParser.getInstance();
    }

    @Override
    public void onEnable() {
        getLogger().info("CrackShotEnhancer Enabled");
        getServer().getPluginManager().registerEvents(new ProjectileInteractListener(), this);
        getServer().getPluginManager().registerEvents(new WeaponScopeListener(this,weaponManager), this);
        getServer().getPluginManager().registerEvents(new WeaponExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new WeaponShootListener(weaponManager), this);
        getServer().getPluginManager().registerEvents(new WeaponReloadListener(weaponManager), this);
        getServer().getPluginManager().registerEvents(new WeaponEntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new WeaponReloadCommandListener(manager, this), this);
        getServer().getPluginManager().registerEvents(new WeaponPrepareShootListener(ConfigParser.getInstance(), this, weaponManager), this);
        getServer().getPluginManager().registerEvents(new PlayerOffhandSwapListener(weaponManager, parser, this), this);
        ProtocolLibrary.getProtocolManager().addPacketListener(new GlowPacketAdapter(this, PacketType.Play.Server.ENTITY_METADATA));
        CEUtil.injectManager(weaponManager);
        EnhancerAPI.inject(this, manager, weaponManager);
        manager.checkInheritance();
    }

    @Override
    public void onDisable() {
        getLogger().info("CrackShotEnhancer Disabled");
    }
}
