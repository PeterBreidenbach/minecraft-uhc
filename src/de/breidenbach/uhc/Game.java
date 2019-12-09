package de.breidenbach.uhc;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Game {
    public boolean active;
    public boolean countdownStarted;
    public boolean started;
    public boolean finished;
    public List<Player> alivePlayers;
    private JavaPlugin plugin;
    private int countDownTimerAddress;
    private int matchTimerAddress;
    private int countDownTimerValue;
    private int matchTimerValue;

    public Game(JavaPlugin plugin) {
        this.plugin = plugin;
        this.alivePlayers = new ArrayList<>();
    }

    public void init(){
        World overworld = plugin.getServer().getWorlds().get(0);
        fillSpawn(overworld, Material.GLASS);
        overworld.getWorldBorder().setCenter(0, 0);
        overworld.getWorldBorder().setSize(10);
        overworld.setPVP(false);
        plugin.getServer().setWhitelist(false);
        active = true;
    }

    public void start(){
        countdownStarted = true;
        countDownTimerValue = 60;
        countDownTimerAddress = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            switch (countDownTimerValue){
                case 60:
                case 30:
                case 10:
                case 5:
                case 4:
                case 3:
                case 2:
                case 1:
                    plugin.getServer().getWorlds().get(0).getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_BASS, 1.0f, 2.0f));
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + countDownTimerValue + ChatColor.RESET + " seconds until the match begins");
                    break;
                case 0:
                    fillSpawn(plugin.getServer().getWorlds().get(0), Material.AIR);
                    plugin.getServer().getWorlds().get(0).getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_PLING, 2.0f, 2.0f));
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Match started!" + ChatColor.RESET + " You are invulnerable for one minute. PVP will activate in 15 minutes!");
                    plugin.getServer().getWorlds().get(0).getWorldBorder().setSize(300, 10);
                    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                        plugin.getServer().getWorlds().get(0).setPVP(true);
                        plugin.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "PVP is now enabled!");
                        plugin.getServer().getWorlds().get(0).getWorldBorder().setSize(20, 10*60);
                    }, 15*60*20);
                    plugin.getServer().getScheduler().cancelTask(countDownTimerAddress);
                    countdownStarted = false;
                    started = true;
                    break;
            }
            countDownTimerValue--;
        }, 0, 20);
    }

    public void handlePlayerJoin(Player p){
        p.teleport(new Location(p.getWorld(), 0, p.getWorld().getHighestBlockYAt(0,0),0));
        if(!started) {
            alivePlayers.add(p);
        }
    }

    public void handlePlayerLeave(Player p){
        if(countdownStarted){
            alivePlayers.remove(p);
            if(alivePlayers.size() < 2) {
                plugin.getServer().getScheduler().cancelTask(countDownTimerAddress);
                plugin.getServer().broadcastMessage(ChatColor.RED + "Start abort, not enough players!");
                countdownStarted = false;
            }
            }else {
            if (started) {
                killPlayer(p, "leaving");
            }
        }
    }

    private void startMatch(){
        matchTimerAddress = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, ()->{
            switch(matchTimerValue){
                case 0:
                    //INIT
                    break;
                case 1*60:
                    //MAKE PLAYERS VULNERABLE
                    break;
                case 5*60:
                    //SHOW MESSAGE 10 MINUTES LEFT
                    break;
                case 10*60:
                    //SHOW MESSAGE 5 MINUTES LEFT
                    break;
                case 14*60:
                    //SHOW MESSAGE 1 MINUTE LEFT
                    break;
                case 15*60:
                    //ENABLE PVP
                    break;
                case 30*60:
                    //GIVE PLAYER A TRACKER AND BORDER SHRINKS
                    break;
            }
        }, 0, 20);
    }

    private void checkForWinner(){
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
        Arrays.stream(p.getInventory().getContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).forEach(x -> p.getWorld().dropItemNaturally(p.getLocation(), x));
        Arrays.stream(p.getInventory().getArmorContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).forEach(x -> p.getWorld().dropItemNaturally(p.getLocation(), x));
        p.setVelocity(new Vector(0.0f, 10.0f, 0.0f));
        p.setGameMode(GameMode.SPECTATOR);
        p.getInventory().clear();
        p.setHealth(p.getMaxHealth());
        p.getServer().getOnlinePlayers().stream().filter(i -> i != p).forEach(x -> x.sendMessage(ChatColor.RED + p.getName() + " died!"));
        p.sendMessage(ChatColor.RED + "You where killed by " + damageCause + "!");
        if(alivePlayers.remove(p)){
            checkForWinner();
        }
    }

    private void fillSpawn(World w, Material m){
        for(int i = -11; i <= 11; i++){
            for(int j = -11; j <= 11; j++){
                new Location(w, i, 200, j).getBlock().setType(m);
            }
        }
    }
}
