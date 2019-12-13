package de.breidenbach.uhc;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.stream.Collectors;

public enum ChristmasLoot {
    GOLDEN_APPLE(Material.GOLDEN_APPLE, 1, 2, 80),
    IRON(Material.IRON_INGOT, 10, 20, 5),
    GOLD(Material.GOLD_INGOT, 5, 10, 10),
    DIAMONDS(Material.DIAMOND, 2, 5, 50),
    SHARPNESS(Enchantment.DAMAGE_ALL, 2, 1, 1, 100),
    PROTECTION(Enchantment.PROTECTION_ENVIRONMENTAL, 2, 1, 1, 100),
    POWER(Enchantment.ARROW_DAMAGE, 1, 2, 1, 100),
    TNT(Material.TNT, 3, 6, 5),
    OBSIDIAN(Material.OBSIDIAN, 3, 6, 10),
    COOKED_BEEF(Material.COOKED_BEEF, 16, 32, 1),
    BOW(Material.BOW, 1, 1, 20),
    SPEED(PotionType.SPEED, 1, false, 1, 2, 30),
    HEAL(PotionType.INSTANT_HEAL, 2, true, 2, 3, 30),
    ARROWS(Material.ARROW, 20, 40, 3),
    COBWEB(Material.WEB, 1, 3, 40),
    XP(Material.EXP_BOTTLE, 5, 10, 15),
    PORTABLE_WORKBENCH(Material.WORKBENCH, 1, 1, 100);

    private Material material;
    private int minCount;
    private int maxCount;
    private int valuePerItem;

    private Enchantment enchantment;
    private int enchantmentIntensity;

    private PotionType potionType;
    private int duration;
    private boolean splash;

    ChristmasLoot(Material material, int minCount, int maxCount, int valuePerItem) {
        this.material = material;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.valuePerItem = valuePerItem;
    }

    ChristmasLoot(Enchantment enchantment, int enchantmentIntensity, int minCount, int maxCount, int valuePerItem) {
        this.material = Material.ENCHANTED_BOOK;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.valuePerItem = valuePerItem;
        this.enchantment = enchantment;
        this.enchantmentIntensity = enchantmentIntensity;
    }

    ChristmasLoot(PotionType potionType, int duration, boolean splash, int minCount, int maxCount, int valuePerItem) {
        this.material = Material.POTION;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.valuePerItem = valuePerItem;
        this.potionType = potionType;
        this.duration = duration;
        this.splash = splash;
    }

    public static ItemStack[] generateLoot(int lootValue){
        int value = 0;
        HashSet<ChristmasLoot> usedLoot = new HashSet<>();
        ArrayList<ItemStack> generatedLoot = new ArrayList<>();
        while(value < lootValue && usedLoot.size() < ChristmasLoot.values().length){
            int finalValue = value;
            List<ChristmasLoot> unusedLoot = Arrays.stream(ChristmasLoot.values()).filter(l -> !usedLoot.contains(l)).collect(Collectors.toList());
            Collections.shuffle(unusedLoot);
            ChristmasLoot currentLoot = unusedLoot.get(0);
            int maxCount = (lootValue-finalValue)/currentLoot.valuePerItem;
            usedLoot.add(currentLoot);
            if(maxCount >= currentLoot.minCount){
                int count = currentLoot.minCount + (int) (Math.random()*(Math.min(maxCount, currentLoot.maxCount)-currentLoot.minCount+1));
                switch (currentLoot.material){
                    case ENCHANTED_BOOK:
                        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
                        EnchantmentStorageMeta esm = (EnchantmentStorageMeta) book.getItemMeta();
                        esm.addStoredEnchant(currentLoot.enchantment, currentLoot.enchantmentIntensity, true);
                        book.setItemMeta(esm);
                        generatedLoot.add(book);
                        break;
                    case POTION:
                        generatedLoot.add(new Potion(currentLoot.potionType, currentLoot.duration, currentLoot.splash).toItemStack(count));
                        break;
                    case WORKBENCH:
                        ItemStack portableWorkbench = new ItemStack(Material.WORKBENCH, 1);
                        ItemMeta im = portableWorkbench.getItemMeta();
                        im.setLore(PortableWorkbench.LORE);
                        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        im.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
                        portableWorkbench.setItemMeta(im);
                        generatedLoot.add(portableWorkbench);
                        break;
                    default:
                        generatedLoot.add(new ItemStack(currentLoot.material, count));
                }
                value += count*currentLoot.valuePerItem;
            }
        }
        return generatedLoot.toArray(new ItemStack[0]);
    }
}
