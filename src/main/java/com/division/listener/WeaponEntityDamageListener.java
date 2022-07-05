package com.division.listener;

import com.division.CrackShotEnhancer;
import com.division.hook.ConfigParser;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

public class WeaponEntityDamageListener implements Listener {

    private CrackShotEnhancer Plugin;

    public WeaponEntityDamageListener(CrackShotEnhancer Plugin) {
        this.Plugin = Plugin;
    }

    @EventHandler
    public void onExplode(ExplosionPrimeEvent event) {
        if (event.getEntity().hasMetadata("CS_potex")) {
            //Cause by Explode Weapon
            String title = event.getEntity().getMetadata("CS_potex").get(0).asString();
            ConfigParser hook = ConfigParser.getInstance();
            if (hook.getBoolean(title, ".Explosions.Deal_Exact_Damage")) {
                int finalDamage = hook.getInt(title, ".Shooting.Projectile_Damage");
                int multiplier = hook.getInt(title, ".Explosions.Damage_Multiplier");
                if (multiplier == 0)
                    multiplier = 100;
                finalDamage = finalDamage * (multiplier / 100);
                event.getEntity().setMetadata("CS_Exact", new FixedMetadataValue(Plugin, finalDamage));

            }
        }
    }
    @EventHandler
    public void onDamage(WeaponDamageEntityEvent event) {
        if (event.getVictim() instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) event.getVictim();
            ConfigParser hook = ConfigParser.getInstance();
            String title = event.getWeaponTitle();
            boolean isEnable = hook.getBoolean(title, ".Extra_Potion_Effect.Enable");
            if (isEnable) {
                String onHit = hook.getString(title, ".Extra_Potion_Effect.When_Hit"); //When_Hit
                String onHead = hook.getString(title, ".Extra_Potion_Effect.When_Head"); //When_Head
                String onCrit = hook.getString(title, ".Extra_Potion_Effect.When_Crit"); //When_Crit
                String onBack = hook.getString(title, ".Extra_Potion_Effect.When_Back"); //When_Back
                if (onHit != null)
                    //CSDirector.this.csminion.givePotionEffects(player, parentNode, ".Potion_Effects.Potion_Effect_Shooter", "shoot");
                    givePotionEffect(victim, title, ".Extra_Potion_Effect.When_Hit");
                if (onHead != null && event.isHeadshot())
                    givePotionEffect(victim, title, ".Extra_Potion_Effect.When_Head");
                if (onBack != null && event.isBackstab())
                    givePotionEffect(victim, title, ".Extra_Potion_Effect.When_Back");
                if (onCrit != null && event.isCritical())
                    givePotionEffect(victim, title, ".Extra_Potion_Effect.When_Crit");
            }

            if (event.getDamager() != null && event.getDamager().hasMetadata("CS_Exact")) {
                int value = event.getDamager().getMetadata("CS_Exact").get(0).asInt();
                event.setDamage(value);
                event.getDamager().removeMetadata("CS_Exact", Plugin);
            }
        }
    }

    private void givePotionEffect(LivingEntity victim, String parentNode, String childNode) {
        if (ConfigParser.getInstance().getString(parentNode, childNode) != null) {
            String[] effectList = ConfigParser.getInstance().getString(parentNode, childNode).split(",");
            int var8 = effectList.length;

            for (String s : effectList) {
                String potFX = s;
                potFX = potFX.replace(" ", "");
                String[] args = potFX.split("-");
                if (args.length == 3) {
                    try {
                        PotionEffectType potionType = PotionEffectType.getByName(args[0].toUpperCase());
                        int duration = Integer.parseInt(args[1]);
                        if (potionType.getDurationModifier() != 1.0D) {
                            double maths = (double) duration * (1.0D / potionType.getDurationModifier());
                            duration = (int) maths;
                        }

                        victim.removePotionEffect(potionType);
                        victim.addPotionEffect(potionType.createEffect(duration, Integer.parseInt(args[2]) - 1));
                    }
                    catch (Exception var15) {
                        System.out.print("[CrackShot] '" + potFX + "' of weapon '" + parentNode + "' has an incorrect potion type, duration or level!");
                    }
                }
                else {
                    System.out.print("[CrackShot] '" + potFX + "' of weapon '" + parentNode + "' has an invalid format! The correct format is: Potion-Duration-Level!");
                }
            }

        }
    }
}
