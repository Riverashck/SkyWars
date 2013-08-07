/*
 * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.daboross.bukkitdev.skywars.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.daboross.bukkitdev.skywars.SkyWarsPlugin;
import net.daboross.bukkitdev.skywars.game.KillBroadcaster;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 *
 * @author daboross
 */
public class DeathListener implements Listener {

    private final SkyWarsPlugin plugin;
    private Map<String, String> lastHit = new HashMap<String, String>();
    private Set<String> causedVoid = new HashSet<String>();

    public DeathListener(SkyWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent evt) {
        String name = evt.getPlayer().getName().toLowerCase();
        lastHit.remove(name);
        causedVoid.remove(name);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent evt) {
        if (evt.getEntity() instanceof Player) {
            Player p = (Player) evt.getEntity();
            String name = p.getName().toLowerCase();
            Entity damager = evt.getDamager();
            if (damager instanceof HumanEntity) {
                lastHit.put(name, ((HumanEntity) damager).getName().toLowerCase());
            } else if (damager instanceof Projectile) {
                LivingEntity shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Player) {
                    lastHit.put(name, ((Player) shooter).getName());
                } else {
                    String customName = shooter.getCustomName();
                    lastHit.put(name, customName == null ? shooter.getType().getName() : customName);
                }
            } else if (damager instanceof LivingEntity) {
                String customName = ((LivingEntity) damager).getCustomName();
                lastHit.put(name, customName == null ? damager.getType().getName() : customName);
            } else {
                lastHit.put(name, evt.getDamager().getType().getName());
            }
            System.out.println("Damage! Last hit: " + lastHit.get(name));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent evt) {
        if (evt.getEntity() instanceof Player) {
            String name = ((Player) evt.getEntity()).getName().toLowerCase();
            if (evt.getCause() == EntityDamageEvent.DamageCause.VOID) {
                causedVoid.add(name);
            } else {
                causedVoid.remove(name);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent evt) {
        String name = evt.getEntity().getName().toLowerCase();
        Integer id = plugin.getCurrentGames().getGameID(name);
        if (id != null) {
            evt.setDeathMessage(KillBroadcaster.getMessage(name, lastHit.get(name), causedVoid.contains(name)));
            plugin.getGameHandler().removePlayerFromGame(name, false, false);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent evt) {
        evt.setRespawnLocation(plugin.getLocationStore().getLobbyPosition().toLocation());

    }
}