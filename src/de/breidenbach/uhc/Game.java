package de.breidenbach.uhc;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Game {
    public boolean active;
    public boolean started;
    public boolean finished;
    private List<Player> alivePlayers;
    private JavaPlugin plugin;

    public Game(JavaPlugin plugin) {
        this.plugin = plugin;
        this.alivePlayers = new ArrayList<>();
    }

    public void init(Server server){
        server.getWorlds().get(0).getWorldBorder().setCenter(0, 0);
        server.getWorlds().get(0).getWorldBorder().setSize(10);
        server.getWorlds().get(0).setPVP(false);
        server.setWhitelist(false);
        active = true;
    }

    public void start(Server server){
        server.getWorlds().get(0).getWorldBorder().setSize(200);
//        server.getScheduler().runTaskLaterAsynchronously(plugin, () -> {}, )
        started = true;
    }

    public void handlePlayerJoin(Player p){
        if(started) {
            p.teleport(new Location(p.getWorld(), 0, p.getWorld().getHighestBlockYAt(0,0),0));
        }else{
            alivePlayers.add(p);
        }
    }

    public void handlePlayerLeave(Player p){
        if(started){
            if(alivePlayers.remove(p)){
                killPlayer(p, "leaving");
            }
        }
    }

    public void checkForWinner(){
        if(alivePlayers.size() == 1){
            Player winner = alivePlayers.get(0);
            winner.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "You won!");
            for(Player p:winner.getWorld().getPlayers().stream().filter(i -> i != winner).collect(Collectors.toList())){
                p.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + winner.getName() + " won!");
                p.teleport(winner);
            }
            finished = true;
            winner.getServer().broadcastMessage(ChatColor.BOLD + "The Server will shutdown in 30 seconds!");
            winner.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> winner.getServer().shutdown(),600);
        }
    }

    public void killPlayer(Player p, String damageCause){
        p.getWorld().strikeLightningEffect(p.getLocation());
        for(ItemStack stack: Arrays.stream(p.getInventory().getContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).collect(Collectors.toList())){
            p.getWorld().dropItemNaturally(p.getLocation(), stack);
        }
        for(ItemStack stack: Arrays.stream(p.getInventory().getArmorContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).collect(Collectors.toList())){
            p.getWorld().dropItemNaturally(p.getLocation(), stack);
        }
        p.setVelocity(new Vector(0.0f, 10.0f, 0.0f));
        p.setGameMode(GameMode.SPECTATOR);
        p.getInventory().clear();
        p.setHealth(p.getMaxHealth());
        for(Player onlinePlayer : p.getServer().getOnlinePlayers().stream().filter(i -> i != p).collect(Collectors.toList())){
            onlinePlayer.sendMessage(ChatColor.RED + p.getName() + " died!");
        }
        p.sendMessage(ChatColor.RED + "You where killed by " + damageCause + "!");
        if(alivePlayers.remove(p)){
            checkForWinner();
        }
    }
}
