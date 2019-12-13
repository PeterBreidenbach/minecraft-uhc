package de.breidenbach.uhc;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Objects;

public class PortableWorkbench implements Listener {

    public static ArrayList<String> LORE = new ArrayList<>();
    static{
        LORE.add("Portable");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(Objects.nonNull(event.getItem()) && event.getItem().getType() == Material.WORKBENCH && Objects.nonNull(event.getItem().getItemMeta().getLore()) && event.getItem().getItemMeta().getLore().get(0).equals(LORE.get(0))){
            event.getPlayer().openWorkbench(null, true);
            event.setCancelled(true);
        }
    }

}
