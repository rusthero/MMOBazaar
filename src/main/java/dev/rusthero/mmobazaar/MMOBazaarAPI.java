package dev.rusthero.mmobazaar;

import dev.rusthero.mmobazaar.item.BazaarBagFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MMOBazaarAPI {
    private final BazaarBagFactory factory;

    public MMOBazaarAPI(BazaarBagFactory factory) {
        this.factory = factory;
    }

    public void giveBazaarBag(Player player) {
        ItemStack bag = factory.create();
        player.getInventory().addItem(bag);
        player.sendMessage("§a[MMOBazaar] Bazaar Bag added to your inventory.");
    }

    public void giveBazaarBag(Player player, int amount) {
        if (amount <= 0) return;

        ItemStack bag = factory.create();
        bag.setAmount(Math.min(amount, bag.getMaxStackSize())); // max 64

        int fullStacks = amount / bag.getMaxStackSize();
        int remainder = amount % bag.getMaxStackSize();

        for (int i = 0; i < fullStacks; i++) {
            player.getInventory().addItem(bag.clone());
        }

        if (remainder > 0) {
            ItemStack last = bag.clone();
            last.setAmount(remainder);
            player.getInventory().addItem(last);
        }

        player.sendMessage("§a[MMOBazaar] " + amount + "x Bazaar Bag added to your inventory.");
    }
}
