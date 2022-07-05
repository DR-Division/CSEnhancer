package com.division.runnable;

import com.division.data.Weapon;
import com.division.data.enhanceable.implement.GrenadeCookData;
import com.division.hook.ConfigParser;
import com.division.message.CEMessage;
import com.division.message.implement.ActionbarMessage;
import com.division.message.implement.ChatMessage;
import com.division.message.implement.TitleMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class GrenadeRunnable extends BukkitRunnable {

    private boolean isRunning;
    private UUID uuid; //Player UUID
    private int MAX_TIME;
    private int count;
    private GrenadeCookData data;
    private CEMessage message;
    private Weapon weapon;
    private ConfigParser parser;

    public GrenadeRunnable(UUID uuid, GrenadeCookData data, Weapon weapon, ConfigParser parser) {
        this.uuid = uuid;
        this.data = data;
        this.parser = parser;
        this.weapon = weapon;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void init() {
        count = 0;
        MAX_TIME = parser.getInt(weapon.getWeaponTitle(), ".Explosions.Explosion_Delay");
        String msg = parser.getString(weapon.getWeaponTitle(), ".Explosions.Grenade_Cook_Message");
        if (msg != null && msg.contains("<>")) {
            String[] splits = msg.split("<>");
            if (splits.length == 2 && (splits[0].equalsIgnoreCase("chat") || splits[0].equalsIgnoreCase("actionbar") || splits[0].equalsIgnoreCase("title"))) {
                if (splits[0].equalsIgnoreCase("chat"))
                    message = new ChatMessage(splits[1]);
                else if (splits[0].equalsIgnoreCase("actionbar"))
                    message = new ActionbarMessage(splits[1]);
                else
                    message = new TitleMessage(splits[1], 25);
            }
        }
    }

    public int getLeftTime() {
        return Math.abs(MAX_TIME - count);
    }

    @Override
    public BukkitTask runTaskTimer(Plugin plugin, long delay, long period) {
        init();
        isRunning = true;
        return super.runTaskTimer(plugin, delay, period);
    }

    @Override
    public void run() {
        if (MAX_TIME <= 0) {
            data.removeData();
            return;
        }
        Player p = Bukkit.getPlayer(uuid);
        if (p == null || !p.isOnline())
            data.removeData();
        else {
            if (count >= MAX_TIME) {
                data.explodeData(p);
            }
            else {
                message.resetOriginMessage().replace("#TIME", String.valueOf((MAX_TIME - count) / 20.0))
                        .replace("#PLAYER", p.getName())
                        .replace("#MAX_TIME", String.valueOf(Math.round(MAX_TIME / 20.0)))
                        .replace("#WEAPON", weapon.getWeaponTitle()).send(p);
                count += 2;
            }
        }
    }
}
