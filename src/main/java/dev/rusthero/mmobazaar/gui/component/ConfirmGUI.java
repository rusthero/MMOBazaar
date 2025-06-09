package dev.rusthero.mmobazaar.gui.component;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.gui.api.BazaarGUI;
import dev.rusthero.mmobazaar.gui.api.ClickableGUI;
import dev.rusthero.mmobazaar.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class ConfirmGUI implements ClickableGUI, BazaarGUI {
    private final MMOBazaarContext context;
    private final BazaarData data;
    private final BazaarListing listing;

    public ConfirmGUI(MMOBazaarContext context, BazaarData data, BazaarListing listing) {
        this.context = context;
        this.data = data;
        this.listing = listing;
    }

    public Inventory getInventory(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§8Buy for §a$" + listing.getPrice());

        // Confirm Button
        ItemStack confirm = new ItemBuilder(Material.LIME_DYE).setName("§aConfirm Purchase").addLore("").addLore("§7Price: §f$" + listing.getPrice()).addLore("§7Seller: §f" + Bukkit.getOfflinePlayer(data.getOwner()).getName()).addLore("").addLore("§eClick to confirm").build();
        gui.setItem(3, confirm);

        // Item Preview
        ItemStack preview = new ItemBuilder(listing.getItem().clone()).addLore("").addLore("§aPrice: §f$" + listing.getPrice()).addLore("§eClick to confirm purchase").build();
        gui.setItem(4, preview);

        // Cancel Button
        ItemStack cancel = new ItemBuilder(Material.RED_DYE).setName("§cCancel").build();
        gui.setItem(5, cancel);

        return gui;
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);

        switch (event.getRawSlot()) {
            case 3 -> { // Confirm purchase
                double price = listing.getPrice();
                if (!context.vaultHook.getEconomy().has(player, price)) {
                    player.sendMessage("§cYou don’t have enough money.");
                    player.closeInventory();
                    return;
                }

                BazaarListing current = data.getListings().get(listing.getSlot());
                if (current == null || !current.getItem().isSimilar(listing.getItem())) {
                    player.sendMessage("§cThis item is no longer available.");
                    player.closeInventory();
                    return;
                }

                if (current.getPrice() != listing.getPrice()) {
                    player.sendMessage("§cThe item's price has changed. Please reopen the bazaar.");
                    player.closeInventory();
                    return;
                }

                ItemStack item = listing.getItem().clone();
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    player.sendMessage("§cNot enough inventory space.");
                    player.closeInventory();
                    return;
                }

                // 1. Remove listing
                data.removeListing(listing.getSlot());

                // 2. Vault transfer
                context.vaultHook.getEconomy().withdrawPlayer(player, price);
                data.deposit(price);

                // 2.5 Refresh GUI if owner looks at it to prevent visual bugs
                UUID ownerId = data.getOwner();
                Player owner = Bukkit.getPlayer(ownerId);
                if (owner != null) context.gui.refreshOwnerGUIWithListing(owner, listing);

                // 3. Notify
                player.sendMessage("§aYou bought the item for §f$" + price + "§a.");
                if (owner != null) owner.sendMessage("§aSomeone bought an item from your bazaar.");

                // 3.5 Update database
                Bukkit.getScheduler().runTaskAsynchronously(context.plugin, () -> context.storage.saveBazaar(data));

                // 4. Close and cleanup
                player.closeInventory();
            }

            case 5 -> { // Cancel
                player.sendMessage("§7Purchase cancelled.");
                context.gui.exitGUI(player);
            }
        }
    }

    @Override
    public BazaarData getBazaar() {
        return data;
    }
}