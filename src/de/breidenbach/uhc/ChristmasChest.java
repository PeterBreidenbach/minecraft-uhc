package de.breidenbach.uhc;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ChristmasChest implements Listener {

    public static final int LOOT_VALUE = 500;
    public static final int SPAWN_DELAY = 6000; //5 Minutes
    public static final int SPAWN_INTERVAL = 12000; //10 Minutes

    private JavaPlugin plugin;
    private int fireworkTimerAddress;
    private int spawnTimerAddress;
    private boolean spawningActive;
    private List<Location> activeChests;

    public ChristmasChest(JavaPlugin plugin) {
        this.plugin = plugin;
        activeChests = new ArrayList<>();
        fireworkTimerAddress = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::fireworkTick, 0, 20);
    }

    public void start(int delay, int interval){
        spawnTimerAddress = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            double borderSize = plugin.getServer().getWorlds().get(0).getWorldBorder().getSize();
            int x = (int) (Math.random()*borderSize*0.8-(borderSize*0.4));
            int z = (int) (Math.random()*borderSize*0.8-(borderSize*0.4));
            spawnLoot(new Location(plugin.getServer().getWorlds().get(0), x, plugin.getServer().getWorlds().get(0).getHighestBlockYAt(x, z), z));
        }, delay, interval);
        spawningActive = true;
    }

    public void stop(){
        plugin.getServer().getScheduler().cancelTask(spawnTimerAddress);
        activeChests.clear();
        spawningActive = false;
    }

    public void spawnLoot(Location spawnLocation) {
        spawnLocation = spawnLocation.getBlock().getLocation();
        plugin.getServer().broadcastMessage(ChatColor.BOLD + "A chest spawned at " + ChatColor.GOLD + spawnLocation.getBlockX() + " " + spawnLocation.getBlockY() + " " + spawnLocation.getBlockZ());
        activeChests.add(spawnLocation);
        plugin.getServer().getWorlds().get(0).getBlockAt(spawnLocation).setType(Material.CHEST);
        Chest chest = (Chest) spawnLocation.getBlock().getState();
        ArrayList<ItemStack> chestFilling = new ArrayList<>(Arrays.asList(ChristmasLoot.generateLoot(LOOT_VALUE)));
        chestFilling.addAll(Arrays.asList(new ItemStack[chest.getInventory().getSize()-chestFilling.size()]));
        Collections.shuffle(chestFilling);
        chest.getInventory().setContents(chestFilling.toArray(new ItemStack[0]));
    }

    @EventHandler
    public void onChestClicked(PlayerInteractEvent event){
        if(Objects.nonNull(event.getClickedBlock())){
            activeChests.remove(event.getClickedBlock().getLocation());
        }
    }

    public void fireworkTick(){
        activeChests.forEach(chest -> {
            Firework fw = chest.getWorld().spawn(chest, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.setPower(1);
            meta.addEffect(FireworkEffect.builder().withColor(Color.fromRGB((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256))).withFade(Color.fromRGB((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256))).build());
            fw.setFireworkMeta(meta);
            plugin.getServer().getOnlinePlayers().forEach(p -> p.playSound(chest, Sound.NOTE_PLING, 2.0f, 2.0f));
        });
    }

    public void cleanUp(){
        if(spawningActive){
            stop();
        }
        plugin.getServer().getScheduler().cancelTask(fireworkTimerAddress);
    }

    public boolean isSpawningActive() {
        return spawningActive;
    }
}
