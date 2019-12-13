package de.breidenbach.uhc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class EventListener implements Listener {

    private UHC uhc;

    public EventListener(UHC uhc) {
        this.uhc = uhc;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        if (uhc.active) {
            if (uhc.started) {
                event.setMotd(ChatColor.RED + "UHC " + MinecraftUHC.VERSION + " RUNNING" + ChatColor.RESET + " - " + ChatColor.BOLD + "Join now to spectate!");
            } else {
                event.setMotd(ChatColor.RED + "UHC " + MinecraftUHC.VERSION + " STARTING" + ChatColor.RESET + " - " + ChatColor.BOLD + "Join now to participate!");
            }
        } else {
            if (uhc.finished) {
                event.setMotd(ChatColor.RED + "UHC " + MinecraftUHC.VERSION + " FINISHED" + ChatColor.RESET + " - " + ChatColor.BOLD + "Please ask an operator to start a new game!");
            } else {
                event.setMotd(ChatColor.RED + "UHC " + MinecraftUHC.VERSION + " INITIALIZING" + ChatColor.RESET + " - " + ChatColor.BOLD + "Initializing, please wait...");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (uhc.active) {
            uhc.handlePlayerJoin(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (uhc.active) {
            uhc.handlePlayerLeave(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (uhc.active) {
            if (uhc.invulnerable && event.getEntityType() == EntityType.PLAYER) {
                event.setCancelled(true);
            } else {
                if (event.getEntityType() == EntityType.PLAYER) {
                    Player p = (Player) event.getEntity();
                    if (p.getHealth() - event.getDamage() <= 0) {
                        uhc.killPlayer(p, ((Player) event.getEntity()).getKiller());
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (uhc.active && !uhc.started) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(uhc.active && event.getBlock().getType() == Material.TNT){
            event.getBlock().setType(Material.AIR);
            event.getBlock().getLocation().getWorld().spawnEntity(event.getBlock().getLocation(), EntityType.PRIMED_TNT);
        }
    }

}
