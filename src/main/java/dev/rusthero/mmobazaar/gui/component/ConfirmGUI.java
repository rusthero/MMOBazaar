package dev.rusthero.mmobazaar.gui.component;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.gui.api.BazaarGUI;
import dev.rusthero.mmobazaar.gui.api.ClickableGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        ItemStack confirm = new ItemStack(Material.LIME_DYE);
        ItemMeta cMeta = confirm.getItemMeta();
        if (cMeta != null) {
            cMeta.setDisplayName("§aConfirm Purchase");
            cMeta.setLore(List.of("", "§7Price: §f$" + listing.getPrice(), "§7Seller: §f" + Bukkit.getOfflinePlayer(data.getOwner()).getName(), "", "§eClick to confirm"));
            confirm.setItemMeta(cMeta);
        }
        gui.setItem(3, confirm);

        // Item Preview
        ItemStack preview = listing.getItem().clone();
        ItemMeta pMeta = preview.getItemMeta();

        if (pMeta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("§aPrice: §f$" + listing.getPrice());
            lore.add("§eClick to confirm purchase");
            pMeta.setLore(lore);

            // Apply the customized meta to the preview item
            preview.setItemMeta(pMeta);
        }
        gui.setItem(4, preview);

        // Cancel Button
        ItemStack cancel = new ItemStack(Material.RED_DYE);
        ItemMeta xMeta = cancel.getItemMeta();
        if (xMeta != null) {
            xMeta.setDisplayName("§cCancel");
            cancel.setItemMeta(xMeta);
        }
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