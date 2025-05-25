package dev.rusthero.mmobazaar.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BazaarBagFactory {
    private final double creationCost;

    private final String displayName;
    private final List<String> lore;
    private final int customModelData;
    private final Material baseMaterial;

    public BazaarBagFactory(double extensionFee) {
        // TODO Multi-language support in future
        this.creationCost = extensionFee;
        this.displayName = "§6Bazaar Bag";
        this.lore = List.of("§7Right-click to open your shop.");
        this.customModelData = 7001;
        this.baseMaterial = Material.RABBIT_HIDE;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(baseMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);

            List<String> fullLore = new ArrayList<>(lore); // existing base lore
            fullLore.add(""); // spacing line
            fullLore.add("§7Opening a shop costs:");
            fullLore.add("§6$" + creationCost);

            meta.setCustomModelData(customModelData);
            meta.setLore(fullLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isBazaarBag(ItemStack item) {
        if (item == null || item.getType() != baseMaterial || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return false;
        return meta.getCustomModelData() == customModelData;
    }
}
