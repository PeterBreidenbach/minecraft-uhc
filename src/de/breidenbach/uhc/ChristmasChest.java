package de.breidenbach.uhc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ChristmasChest {
    private JavaPlugin plugin;

    public ChristmasChest(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public void spawnLoot(){
        double borderSize = plugin.getServer().getWorlds().get(0).getWorldBorder().getSize();
        Location spawnLocation = new Location(plugin.getServer().getWorlds().get(0), (int) (Math.random()*borderSize*0.8-(borderSize*0.4)), 255, (int) (Math.random()*borderSize*0.8-(borderSize*0.4)));
        ItemStack stack = new ItemStack(Material.COOKED_BEEF, 64);
    }
}
