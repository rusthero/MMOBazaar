package dev.rusthero.mmobazaar.gui;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.item.util.ListingLoreUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class BazaarCustomerGUI {
    private final MMOBazaarContext context;
    private final BazaarData data;

    public BazaarCustomerGUI(MMOBazaarContext context, BazaarData data) {
        this.context = context;
        this.data = data;
    }

    public void open(Player player) {
        if (data.isClosed()) {
            player.sendMessage("§cThis bazaar is currently closed.");
            return;
        }

        // Register session for event handling
        context.guiSessions.setCustomerGUI(player.getUniqueId(), this);

        Inventory gui = Bukkit.createInventory(null, 27, data.getName());

        for (Map.Entry<Integer, BazaarListing> entry : data.getListings().entrySet()) {
            int slot = entry.getKey();
            BazaarListing listing = entry.getValue();

            gui.setItem(slot, ListingLoreUtil.withBazaarLore(listing.getItem().clone(), listing.getPrice(), Bukkit.getOfflinePlayer(data.getOwner()).getName()));
        }

        player.openInventory(gui);
    }

    public BazaarData getData() {
        return data;
    }
}