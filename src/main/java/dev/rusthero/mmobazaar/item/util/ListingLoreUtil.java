package dev.rusthero.mmobazaar.item.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ListingLoreUtil {
    public static ItemStack withBazaarLore(ItemStack item, double price, String sellerName) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() && meta.getLore() != null
                    ? new ArrayList<>(meta.getLore())
                    : new ArrayList<>();

            lore.add("");
            lore.add("§aPrice: §f$" + price);
            lore.add("§7Seller: " + sellerName);
            lore.add("§eClick to buy");

            meta.setLore(lore);
            clone.setItemMeta(meta);
        }

        return clone;
    }

    public static ItemStack withOwnerLore(ItemStack item, double price, String sellerName) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() && meta.getLore() != null
                    ? new ArrayList<>(meta.getLore())
                    : new ArrayList<>();

            lore.add("");
            lore.add("§aPrice: §f$" + price);
            lore.add("§eLEFT CLICK to edit price");
            lore.add("§cRIGHT CLICK to remove");

            meta.setLore(lore);
            clone.setItemMeta(meta);
        }

        return clone;
    }

    public static ItemStack stripListingLore(ItemStack item, int linesToRemove) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();

        if (meta != null && meta.hasLore() && meta.getLore() != null) {
            List<String> lore = new ArrayList<>(meta.getLore());
            int newSize = Math.max(0, lore.size() - linesToRemove);
            lore = lore.subList(0, newSize);
            meta.setLore(lore);
            clone.setItemMeta(meta);
        }

        return clone;
    }
}
