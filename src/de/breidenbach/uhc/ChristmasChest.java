package de.breidenbach.uhc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

public class ChristmasChest {
    private JavaPlugin plugin;
    private List<Location> activeChests;

    public ChristmasChest(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public void spawnLoot(Location spawnLocation){
        double borderSize = plugin.getServer().getWorlds().get(0).getWorldBorder().getSize();
        spawnLocation = new Location(plugin.getServer().getWorlds().get(0), (int) (Math.random()*borderSize*0.8-(borderSize*0.4)), 255, (int) (Math.random()*borderSize*0.8-(borderSize*0.4)));

    }
}
