package com.division.listener;

import com.division.CrackShotEnhancer;
import com.division.data.manager.InheritanceManager;
import com.division.util.CEUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeaponReloadCommandListener implements Listener {

    private InheritanceManager manager;
    private CrackShotEnhancer Plugin;
    private List<String> prefix;
    private List<String> CSPrefix;

    public WeaponReloadCommandListener(InheritanceManager manager, CrackShotEnhancer Plugin) {
        this.manager = manager;
        this.Plugin = Plugin;
        prefix = new ArrayList<>(Arrays.asList("cs", "shot", "cra", "crackshot"));
        CSPrefix = new ArrayList<>(Arrays.asList("csp", "csplus", "shotplus", "crackshotplus"));
    }

    @EventHandler
    public void onWeaponReload(ServerCommandEvent event) {
        checkCommand(event.getCommand().toLowerCase());
    }

    @EventHandler
    public void onWeaponReload(PlayerCommandPreprocessEvent event) {
        checkCommand(event.getMessage().toLowerCase());
    }

    private void checkCommand(String value) {

        for (String pref : prefix) {
            if (value.contains(pref) && value.contains("config reload")) {
                Bukkit.getScheduler().runTaskLater(Plugin, manager::checkInheritance, 5L);
                break;
            }
        }
        for (String pref : CSPrefix) {
            if (value.contains(pref) && value.contains("reload")) {
                Bukkit.getScheduler().runTaskLater(Plugin, manager::checkInheritance, 5L);
                break;
            }
        }
    }


}
