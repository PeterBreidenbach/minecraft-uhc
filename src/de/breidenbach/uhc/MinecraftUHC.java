package de.breidenbach.uhc;

import javafx.print.PageLayout;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MinecraftUHC extends JavaPlugin {

    public static final String VERSION = "1.0";
    private UHC uhc;
    private ChristmasChest christmasChest;

    @Override
    public void onEnable() {
        christmasChest = new ChristmasChest(this);
        uhc = new UHC(this, christmasChest);
        this.getServer().getPluginManager().registerEvents(new EventListener(uhc), this);
        this.getServer().getPluginManager().registerEvents(new PortableWorkbench(), this);
        this.getServer().getPluginManager().registerEvents(christmasChest, this);
        System.out.println("UHC " + VERSION + " enabled");
    }

    @Override
    public void onDisable() {
        christmasChest.cleanUp();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("uhc")) {
            if (sender.isOp()) {
                if (args.length > 0) {
                    switch (args[0]) {
                        case "init":
                            if (!uhc.active) {
                                sender.sendMessage("Initializing match...");
                                uhc.init();
                                sender.sendMessage("Done.");
                            } else {
                                sender.sendMessage("Game already initialized!");
                            }
                            return true;
                        case "start":
                            if (uhc.active) {
                                if (!uhc.countdownStarted && !uhc.started) {
                                    if (uhc.alivePlayers.size() > 1) {
                                        sender.sendMessage("Starting countdown...");
                                        uhc.start();
                                    } else {
                                        sender.sendMessage("At least 2 players are needed to start!");
                                    }
                                } else {
                                    sender.sendMessage("Game already started!");
                                }
                            } else {
                                sender.sendMessage("Please run \"/uhc init\" first!");
                            }
                            return true;
                        case "spawn":
                            if (sender instanceof Player) {
                                Player p = (Player) sender;
                                p.setGameMode(GameMode.CREATIVE);
                                p.teleport(new Location(p.getWorld(), 0, p.getWorld().getHighestBlockYAt(0, 0), 0));
                            } else {
                                sender.sendMessage("This command is only for players!");
                            }
                            return true;
                        case "loot":
                            if (sender instanceof Player) {
                                if (args.length == 2) {
                                    try {
                                        int value = Integer.parseInt(args[1]);
                                        Player p = (Player) sender;
                                        Inventory chestView = Bukkit.createInventory(p, InventoryType.CHEST);
                                        ArrayList<ItemStack> chestFilling = new ArrayList<>(Arrays.asList(ChristmasLoot.generateLoot(value)));
                                        chestFilling.addAll(Arrays.asList(new ItemStack[chestView.getSize() - chestFilling.size()]));
                                        Collections.shuffle(chestFilling);
                                        chestView.setContents(chestFilling.toArray(new ItemStack[0]));
                                        p.openInventory(chestView);
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                } else {
                                    return false;
                                }
                            } else {
                                sender.sendMessage("This command is only for players!");
                            }
                            return true;
                        case "test":
                            if (christmasChest.isSpawningActive()) {
                                sender.sendMessage("Stopped chest spawning");
                                christmasChest.stop();
                            } else {
                                sender.sendMessage("Started chest spawning");
                                christmasChest.start(ChristmasChest.SPAWN_DELAY, ChristmasChest.SPAWN_INTERVAL);
                            }
                            return true;
                        default:
                            return false;
                    }
                } else {
                    sender.sendMessage("UHC " + VERSION + " loaded.");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "UHC-Settings can only be accessed by an operator!");
                return true;
            }
        }
        return false;
    }
}
