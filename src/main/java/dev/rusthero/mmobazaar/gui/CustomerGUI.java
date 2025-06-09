package dev.rusthero.mmobazaar.gui;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.item.util.ListingLoreUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CustomerGUI implements ClickableGUI, BazaarGUI {
    private final MMOBazaarContext context;
    private final BazaarData data;

    public CustomerGUI(MMOBazaarContext context, BazaarData data) {
        this.context = context;
        this.data = data;
    }

    public Inventory getInventory(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, data.getName());

        for (Map.Entry<Integer, BazaarListing> entry : data.getListings().entrySet()) {
            int slot = entry.getKey();
            BazaarListing listing = entry.getValue();

            gui.setItem(slot, ListingLoreUtil.withBazaarLore(listing.getItem().clone(), listing.getPrice(), Bukkit.getOfflinePlayer(data.getOwner()).getName()));
        }

        return gui;
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        BazaarListing listing = data.getListings().get(slot);
        if (listing == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        ItemStack original = listing.getItem();

        // Security check: must match listing item
        if (!ListingLoreUtil.stripListingLore(clicked, 4).isSimilar(original)) {
            player.sendMessage("§cThis item does not match the listing. Please reopen the shop.");
            return;
        }

        // Open confirmation GUI
        context.gui.openConfirmGUI(player, new ConfirmGUI(context, data, listing));
    }

    @Override
    public BazaarData getBazaar() {
        return data;
    }
}