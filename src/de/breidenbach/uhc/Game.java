package de.breidenbach.uhc;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Game {
    public static boolean active;
    public static boolean started;
    public static List<Player> alivePlayers = new ArrayList<>();

    public static void init(Server server){
        server.getWorlds().get(0).getWorldBorder().setCenter(0,0);
        server.getWorlds().get(0).getWorldBorder().setSize(10);
        server.setWhitelist(false);
        active = true;
    }

    public static void start(Server server){
        server.getWorlds().get(0).getWorldBorder().setSize(10);

    }

    public static void addPlayer(Player p){
        alivePlayers.add(p);
    }

    public static void killPlayer(Player p, String damageCause){
        p.getWorld().strikeLightningEffect(p.getLocation());
        for(ItemStack stack: Arrays.stream(p.getInventory().getContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).collect(Collectors.toList())){
            p.getWorld().dropItemNaturally(p.getLocation(), stack);
        }
        for(ItemStack stack: Arrays.stream(p.getInventory().getArmorContents()).filter(s -> Objects.nonNull(s) && s.getType() != Material.AIR).collect(Collectors.toList())){
            p.getWorld().dropItemNaturally(p.getLocation(), stack);
        }
        p.getInventory().clear();
        p.setGameMode(GameMode.SPECTATOR);
        p.setHealth(p.getMaxHealth());
        p.setVelocity(new Vector(0.0f, 10.0f, 0.0f));
        for(Player onlinePlayer : p.getServer().getOnlinePlayers().stream().filter(i -> i != p).collect(Collectors.toList())){
            onlinePlayer.sendMessage(ChatColor.RED + p.getName() + " died!");
        }
        p.sendMessage(ChatColor.RED + "You where killed by " + damageCause + "!");
    }
}
