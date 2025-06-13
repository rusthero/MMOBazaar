package dev.rusthero.mmobazaar.gui.component;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.item.util.ListingLoreUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class ListingPrompts {
    private final MMOBazaarContext context;
    private final BazaarData data;
    private final OwnerGUI ownerGui;

    public ListingPrompts(MMOBazaarContext context, BazaarData data, OwnerGUI ownerGui) {
        this.context = context;
        this.data = data;
        this.ownerGui = ownerGui;
    }

    public void openListingPrompt(Player player, Inventory inventory, ItemStack item, int slot) {
        new AnvilGUI.Builder().onClick((_s, stateSnapshot) -> {
            try {
                double price = Double.parseDouble(stateSnapshot.getText());
                if (price <= 0) throw new NumberFormatException();

                data.addListing(slot, item.clone(), price);
                inventory.setItem(slot, ListingLoreUtil.withOwnerLore(item, price, Bukkit.getOfflinePlayer(data.getOwner()).getName()));
                Bukkit.getScheduler().runTaskAsynchronously(context.plugin, () -> context.storage.saveBazaar(data));
                context.gui.closeCustomerAndConfirmGUIsForAllPlayers(data);

                player.sendMessage("§aItem listed for §f$" + price);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid price.");
                // Item given back in onClose as slot will be empty
            }
            return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> ownerGui.getInventory(player)));
        }).onClose(stateSnapshot -> {
            if (!data.getListings().containsKey(slot)) {
                returnItem(player, item);
            }
        }).text("10.0").itemLeft(new ItemStack(Material.NAME_TAG)).title("Enter Price").plugin(context.plugin).open(player);
    }

    public void openEditPrompt(Player player, int slot, BazaarListing listing) {
        new AnvilGUI.Builder().plugin(context.plugin).title("Edit Price").text(String.valueOf(listing.getPrice())).itemLeft(new ItemStack(Material.NAME_TAG)).onClick((clickedSlot, state) -> {
            if (clickedSlot != AnvilGUI.Slot.OUTPUT) return List.of();

            try {
                double newPrice = Double.parseDouble(state.getText());
                if (newPrice <= 0) throw new NumberFormatException();

                boolean updated = data.changeListingPrice(slot, newPrice);
                if (updated) {
                    Bukkit.getScheduler().runTaskAsynchronously(context.plugin, () -> context.storage.saveBazaar(data));
                    context.gui.closeCustomerAndConfirmGUIsForAllPlayers(data);

                    player.sendMessage("§aPrice updated to §f$" + newPrice);
                } else {
                    player.sendMessage("§cFailed to update price: listing not found.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid price.");
            }

            return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> ownerGui.getInventory(player)));
        }).open(player);
    }

    private void returnItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }
}