package de.breidenbach.uhc;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;

public class MinecraftUHC extends JavaPlugin {

    public static final String VERSION = "1.0";
    private UHC uhc;

    @Override
    public void onEnable() {
        uhc = new UHC(this);
        this.getServer().getPluginManager().registerEvents(new EventListener(uhc), this);
        this.getServer().getPluginManager().registerEvents(new PortableWorkbench(), this);
        System.out.println("UHC " + VERSION + " enabled");
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
                                        ItemStack[] loot = ChristmasLoot.generateLoot(value);
                                        Player p = (Player) sender;
                                        Inventory chestView = Bukkit.createInventory(p, InventoryType.CHEST);
                                        chestView.setContents(loot);
                                        p.openInventory(chestView);
                                        ItemStack portableWorkbench = new ItemStack(Material.WORKBENCH, 1);
                                        ItemMeta im = portableWorkbench.getItemMeta();
                                        im.setLore(PortableWorkbench.LORE);
                                        portableWorkbench.setItemMeta(im);
                                        p.getWorld().dropItem(p.getLocation(), portableWorkbench);
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
