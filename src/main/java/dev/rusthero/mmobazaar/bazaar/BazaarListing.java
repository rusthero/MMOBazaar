package dev.rusthero.mmobazaar.bazaar;

import org.bukkit.inventory.ItemStack;

public class BazaarListing {
    private final ItemStack item;
    private final double price;
    private final long timestamp; // when listed, for sorting later
    private final int slot;

    public BazaarListing(ItemStack item, double price, int slot) {
        this.item = item;
        this.price = price;
        this.slot = slot;
        this.timestamp = System.currentTimeMillis();
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
