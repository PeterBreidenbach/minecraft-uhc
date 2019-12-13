package de.breidenbach.uhc;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class UHC {
    public boolean active;
    public boolean countdownStarted;
    public boolean started;
    public boolean invulnerable;
    public boolean finished;
    public HashSet<Player> alivePlayers;
    private JavaPlugin plugin;
    private int countDownTimerAddress;
    private int matchTimerAddress;
    private int countDownTimerValue;
    private int matchTimerValue;

    public UHC(JavaPlugin plugin) {
        this.plugin = plugin;
        this.alivePlayers = new HashSet<>();
    }

    public void init() {
        World overworld = plugin.getServer().getWorlds().get(0);
        fillSpawn(overworld, Material.GLASS);
        overworld.getWorldBorder().setCenter(0, 0);
        overworld.getWorldBorder().setSize(10);
        overworld.setPVP(false);
        plugin.getServer().setWhitelist(false);
        plugin.getServer().getOnlinePlayers().forEach(p -> p.kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "UHC is now enabled!" + ChatColor.RESET + " Please rejoin to participate!"));
        invulnerable = true;
        active = true;
    }

    public void start() {
        countdownStarted = true;
        countDownTimerValue = 60;
        countDownTimerAddress = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            switch (countDownTimerValue) {
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
                    startMatch();
                    break;
            }
            countDownTimerValue--;
        }, 0, 20);
    }

    public void handlePlayerJoin(Player p) {
        p.teleport(new Location(p.getWorld(), 0, 201, 0));
        if (!started) {
            alivePlayers.add(p);
            p.setFoodLevel(20);
            p.setGameMode(GameMode.ADVENTURE);
        } else {
            p.setGameMode(GameMode.SPECTATOR);
        }
    }

    public void handlePlayerLeave(Player p) {
        if (!started) {
            alivePlayers.remove(p);
            if (countdownStarted && alivePlayers.size() < 2) {
                plugin.getServer().getScheduler().cancelTask(countDownTimerAddress);
                plugin.getServer().broadcastMessage(ChatColor.RED + "Start abort, not enough players!");
                countdownStarted = false;
            }
        } else {
            killPlayer(p, "leaving");
        }
    }

    private void startMatch() {
        matchTimerAddress = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            switch (matchTimerValue) {
                case 0:
                    fillSpawn(plugin.getServer().getWorlds().get(0), Material.AIR);
                    alivePlayers.forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_PLING, 2.0f, 2.0f));
                    alivePlayers.forEach(p -> p.setGameMode(GameMode.SURVIVAL));
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Match started!" + ChatColor.RESET + " You are invulnerable for 30 seconds. PVP will activate in 15 minutes!");
                    plugin.getServer().getWorlds().get(0).getWorldBorder().setSize((300 + Math.log(alivePlayers.size() - 1) * 300), 10);
                    plugin.getServer().getScheduler().cancelTask(countDownTimerAddress);
                    countdownStarted = false;
                    started = true;
                    break;
                case 30:
                    //MAKE PLAYERS VULNERABLE
                    alivePlayers.forEach(p -> p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are now vulnerable!"));
                    invulnerable = false;
                    break;
                case 5*60:
                case 10*60:
                case 14*60:
                    //SHOW MESSAGE 10/5/1 MINUTE(S) LEFT
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + (15 - matchTimerValue / 60) + ChatColor.RESET + " minute" + ((15 - matchTimerValue / 60) == 1 ? "" : "s") + " until PVP is enabled!");
                    break;
                case 15*60:
                    //ENABLE PVP
                    plugin.getServer().getWorlds().get(0).setPVP(true);
                    plugin.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "PVP is now enabled!");
                    break;
                case 20*60:
                case 25*60:
                case 29*60:
                    //SHOW BORDER TIMER
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + (30 - matchTimerValue / 60) + ChatColor.RESET + " minute" + ((30 - matchTimerValue / 60) == 1 ? "" : "s") + " until the border starts shrinking!");
                    break;
                case 30*60:
                    // START BORDER SHRINKING
                    plugin.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "The border will now start shrinking!");
                    plugin.getServer().getWorlds().get(0).getWorldBorder().setSize(50, 30 * 60);
                    break;
            }
            matchTimerValue++;
        }, 0, 20);
    }

    private void checkForWinner() {
        if (alivePlayers.size() == 1) {
            Player winner = alivePlayers.toArray(new Player[0])[0];
            winner.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "You won!");
            winner.getWorld().getPlayers().stream().filter(i -> i != winner).forEach(p -> {
                p.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + winner.getName() + " won!");
                p.teleport(winner);
            });
            plugin.getServer().getScheduler().cancelTask(matchTimerAddress);
            finished = true;
            plugin.getServer().broadcastMessage(ChatColor.BOLD + "The Server will shutdown in 30 seconds!");
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> plugin.getServer().shutdown(), 600);
        }
    }

    public void killPlayer(Player p, String damageCause) {
        p.getWorld().strikeLightningEffect(p.getLocation());
        Arrays.stream(p.getInventory().getContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).forEach(x -> p.getWorld().dropItemNaturally(p.getLocation(), x));
        Arrays.stream(p.getInventory().getArmorContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).forEach(x -> p.getWorld().dropItemNaturally(p.getLocation(), x));
        p.setVelocity(new Vector(0.0f, 10.0f, 0.0f));
        p.setGameMode(GameMode.SPECTATOR);
        p.getInventory().clear();
        p.setHealth(p.getMaxHealth());
        p.getServer().getOnlinePlayers().stream().filter(i -> i != p).forEach(x -> x.sendMessage(ChatColor.RED + p.getName() + " died!"));
        p.sendMessage(ChatColor.RED + "You where killed by " + damageCause + "!");
        if (alivePlayers.remove(p)) {
            checkForWinner();
        }
    }

    private void fillSpawn(World w, Material m) {
        for (int i = -5; i < 5; i++) {
            for (int j = -5; j < 5; j++) {
                new Location(w, i, 200, j).getBlock().setType(m);
            }
        }
    }
}
