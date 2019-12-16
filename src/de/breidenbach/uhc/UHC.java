package de.breidenbach.uhc;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class UHC {
    public boolean active;
    public boolean countdownStarted;
    public boolean started;
    public boolean invulnerable;
    public boolean finished;
    public HashSet<Player> alivePlayers;
    private JavaPlugin plugin;
    private ChristmasChest christmasChest;
    private int countDownTimerAddress;
    private int matchTimerAddress;
    private int countDownTimerValue;
    private int matchTimerValue;
    private int fireworkTimerAddress;
    private int fireworkTimerValue;

    public UHC(JavaPlugin plugin, ChristmasChest christmasChest) {
        this.plugin = plugin;
        this.christmasChest = christmasChest;
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
            p.getInventory().clear();
            p.setTotalExperience(0);
            p.setExp(0);
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
            killPlayer(p, null);
        }
    }

    private void startMatch() {
        matchTimerAddress = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            switch (matchTimerValue) {
                case 0:
                    fillSpawn(plugin.getServer().getWorlds().get(0), Material.AIR);
                    alivePlayers.forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_PLING, 2.0f, 2.0f));
                    alivePlayers.forEach(p -> p.setGameMode(GameMode.SURVIVAL));
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Match started!" + ChatColor.RESET + " You are invulnerable for one minute. PVP will activate in 15 minutes!");
                    plugin.getServer().getWorlds().get(0).getWorldBorder().setSize((300 + Math.log(alivePlayers.size() - 1) * 300), 10);
                    plugin.getServer().getWorlds().get(0).setTime(1000);
                    plugin.getServer().getScheduler().cancelTask(countDownTimerAddress);
                    christmasChest.start(ChristmasChest.SPAWN_DELAY, ChristmasChest.SPAWN_INTERVAL);
                    countdownStarted = false;
                    started = true;
                    break;
                case 60:
                    //MAKE PLAYERS VULNERABLE
                    alivePlayers.forEach(p -> p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are now vulnerable!"));
                    invulnerable = false;
                    break;
                case 5 * 60:
                case 10 * 60:
                case 14 * 60:
                    //SHOW MESSAGE 10/5/1 MINUTE(S) LEFT
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + (15 - matchTimerValue / 60) + ChatColor.RESET + " minute" + ((15 - matchTimerValue / 60) == 1 ? "" : "s") + " until PVP is enabled!");
                    break;
                case 15 * 60:
                    //ENABLE PVP
                    plugin.getServer().getWorlds().get(0).setPVP(true);
                    plugin.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "PVP is now enabled!");
                    break;
                case 20 * 60:
                case 25 * 60:
                case 29 * 60:
                    //SHOW BORDER TIMER
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + (30 - matchTimerValue / 60) + ChatColor.RESET + " minute" + ((30 - matchTimerValue / 60) == 1 ? "" : "s") + " until the border starts shrinking!");
                    break;
                case 30 * 60:
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
            christmasChest.stop();
            Player winner = alivePlayers.toArray(new Player[0])[0];
            winner.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "You won!");
            fireworkTimerAddress = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                Firework fw = winner.getWorld().spawn(winner.getLocation(), Firework.class);
                FireworkMeta meta = fw.getFireworkMeta();
                meta.setPower(2);
                meta.addEffect(FireworkEffect.builder().withFlicker().withColor(Color.fromRGB((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256))).withFade(Color.fromRGB((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256))).build());
                fw.setFireworkMeta(meta);
                fireworkTimerValue++;
                if (fireworkTimerValue == 10) {
                    plugin.getServer().getScheduler().cancelTask(fireworkTimerAddress);
                    plugin.getServer().broadcastMessage(ChatColor.BOLD + "The Server will shutdown in 30 seconds!");
                    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> plugin.getServer().shutdown(), 600);
                }
            }, 0, 5);
            winner.getWorld().getPlayers().stream().filter(i -> i != winner).forEach(p -> {
                p.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + winner.getName() + " won!");
                p.teleport(winner);
            });
            plugin.getServer().getScheduler().cancelTask(matchTimerAddress);
            finished = true;
        }
    }

    public void killPlayer(Player p, Player killer) {
        p.getWorld().strikeLightningEffect(p.getLocation());
        Arrays.stream(p.getInventory().getContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).forEach(x -> p.getWorld().dropItemNaturally(p.getLocation(), x));
        Arrays.stream(p.getInventory().getArmorContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).forEach(x -> p.getWorld().dropItemNaturally(p.getLocation(), x));
        p.setVelocity(new Vector(0.0f, 10.0f, 0.0f));
        p.setGameMode(GameMode.SPECTATOR);
        p.getInventory().clear();
        p.setHealth(p.getMaxHealth());
        p.setSaturation(20);
        p.getServer().getOnlinePlayers().stream().filter(i -> i != p && (!Objects.nonNull(killer) || i != killer)).forEach(x -> x.sendMessage(ChatColor.RED + p.getName() + " died!"));
        p.sendMessage(ChatColor.RED + "You where killed" + (Objects.nonNull(killer) ? " by " + ChatColor.YELLOW + killer.getDisplayName() + ChatColor.RESET + "!" : "!"));
        if (Objects.nonNull(killer)) {
            killer.playSound(killer.getLocation(), Sound.LEVEL_UP, 2.0f, 2.0f);
            killer.sendMessage("You killed " + ChatColor.BOLD + "" + ChatColor.YELLOW + p.getDisplayName() + ChatColor.RESET + "!");
        }
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
