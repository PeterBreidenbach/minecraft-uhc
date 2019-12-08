package de.breidenbach.uhc;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class EventListener implements Listener {

    private Game game;

    public EventListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event){
        if(game.active){
            if(game.started){
                event.setMotd(ChatColor.RED + "UHC " + MinecraftUHC.VERSION + " RUNNING" + ChatColor.RESET  + " - " + ChatColor.BOLD + "Join now to spectate!");
            }else{
                event.setMotd(ChatColor.RED + "UHC " + MinecraftUHC.VERSION + " STARTING" + ChatColor.RESET + " - " + ChatColor.BOLD + "Join now to participate!");
            }
        }else {
            if (game.finished) {
                event.setMotd(ChatColor.RED + "UHC " + MinecraftUHC.VERSION + " FINISHED" + ChatColor.RESET + " - " + ChatColor.BOLD + "Please ask an operator to start a new game!");
            } else {
                event.setMotd(ChatColor.RED + "UHC " + MinecraftUHC.VERSION + " INITIALIZING" + ChatColor.RESET + " - " + ChatColor.BOLD + "Initializing, please wait...");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(game.active) {
            game.handlePlayerJoin(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if(game.active) {
            game.handlePlayerLeave(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event){
        if(game.active) {
            if (event.getEntityType() == EntityType.PLAYER) {
                Player p = (Player) event.getEntity();
                if (p.getHealth() - event.getDamage() <= 0) {
                    game.killPlayer(p, event.getCause().name());
                    event.setCancelled(true);
                }
            }
        }
    }
}
