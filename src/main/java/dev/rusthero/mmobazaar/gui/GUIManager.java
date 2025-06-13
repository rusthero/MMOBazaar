package dev.rusthero.mmobazaar.gui;

import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.gui.component.ConfirmGUI;
import dev.rusthero.mmobazaar.gui.component.CustomerGUI;
import dev.rusthero.mmobazaar.gui.component.OwnerGUI;
import dev.rusthero.mmobazaar.gui.session.GUISessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class GUIManager {
    private final GUISessionManager sessions;

    public GUIManager() {
        this.sessions = new GUISessionManager();
    }

    public void openOwnerGUI(Player player, OwnerGUI gui) {
        // Close customer and confirm GUIs when owner opens it to prevent price/item mismatch
        closeCustomerAndConfirmGUIsForAllPlayers(gui.getBazaar());

        sessions.owner.set(player.getUniqueId(), gui);
        player.openInventory(gui.getInventory(player));
    }

    public void refreshOwnerGUIWithListing(Player owner, BazaarListing listing) {
        sessions.owner.get(owner.getUniqueId()).ifPresent(ownerGUI -> ownerGUI.refreshListingAndBank(owner, listing.getSlot()));
    }

    public void openCustomerGUI(Player player, CustomerGUI gui) {
        sessions.customer.set(player.getUniqueId(), gui);
        player.openInventory(gui.getInventory(player));
    }

    public void openConfirmGUI(Player player, ConfirmGUI gui) {
        sessions.confirm.set(player.getUniqueId(), gui);
        player.openInventory(gui.getInventory(player));
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        sessions.confirm.get(player.getUniqueId()).ifPresent(gui -> gui.handleClick(player, event));
        sessions.customer.get(player.getUniqueId()).ifPresent(gui -> gui.handleClick(player, event));
        sessions.owner.get(player.getUniqueId()).ifPresent(gui -> gui.handleClick(player, event));
    }

    public void handleDrag(Player player, InventoryDragEvent event) {
        sessions.owner.get(player.getUniqueId()).ifPresent(gui -> gui.handleDrag(player, event));
    }

    public void closeCustomerAndConfirmGUIsForAllPlayers(BazaarData bazaar) {
        sessions.customer.closeForAllPlayers(bazaar.getId());
        sessions.confirm.closeForAllPlayers(bazaar.getId());
    }

    public void closeAllGUIs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInAnyGUI(player)) {
                exitGUI(player);
            }
        }
    }

    public boolean isInAnyGUI(Player player) {
        return sessions.owner.get(player.getUniqueId()).isPresent() || sessions.customer.get(player.getUniqueId()).isPresent() || sessions.confirm.get(player.getUniqueId()).isPresent();
    }

    public void exitGUI(Player player) {
        sessions.owner.remove(player.getUniqueId());
        sessions.customer.remove(player.getUniqueId());
        sessions.confirm.remove(player.getUniqueId());
        player.closeInventory();
    }
}
