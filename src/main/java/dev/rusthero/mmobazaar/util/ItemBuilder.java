package dev.rusthero.mmobazaar.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    private final List<String> lore;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack base) {
        this.item = base.clone();
        this.meta = this.item.getItemMeta();
        this.lore = meta != null && meta.hasLore() && meta.getLore() != null
                ? new ArrayList<>(meta.getLore())
                : new ArrayList<>();
    }

    public ItemBuilder setName(String name) {
        if (meta != null) meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder addLore(String line) {
        if (meta != null) lore.add(line);
        return this;
    }

    public ItemBuilder setModelData(int modelData) {
        if (meta != null) meta.setCustomModelData(modelData);
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}