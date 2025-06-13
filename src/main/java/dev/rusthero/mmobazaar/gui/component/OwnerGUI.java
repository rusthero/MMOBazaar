package dev.rusthero.mmobazaar.gui.component;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.gui.GUIItemFactory;
import dev.rusthero.mmobazaar.gui.api.BazaarGUI;
import dev.rusthero.mmobazaar.gui.api.ClickableGUI;
import dev.rusthero.mmobazaar.gui.api.DraggableGUI;
import dev.rusthero.mmobazaar.item.util.ListingLoreUtil;
import dev.rusthero.mmobazaar.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class OwnerGUI implements ClickableGUI, DraggableGUI, BazaarGUI {
    private final MMOBazaarContext context;
    private final BazaarData data;
    private final ListingPrompts prompts;

    public OwnerGUI(MMOBazaarContext context, BazaarData data) {
        this.context = context;
        this.data = data;
        this.prompts = new ListingPrompts(context, data, this);
    }

    public Inventory getInventory(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, data.getName());

        // Listings 0–26
        for (int slot = 0; slot <= 26; slot++) {
            updateSlot(gui, slot);
        }

        // Filler glass panes on final row
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 27; i < 36; i++) {
            gui.setItem(i, glass);
        }

        // Buttons
        updateBankButton(gui);
        gui.setItem(31, GUIItemFactory.simpleButton(Material.BARRIER, "§cDelete Bazaar", "§7Removes the bazaar and refunds items"));
        updateTimeLeftButton(gui);
        gui.setItem(35, GUIItemFactory.simpleButton(Material.COMPASS, "§bRotate Bazaar", "§7Click to rotate stand 15°"));

        return gui;
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        if (clicked == event.getView().getBottomInventory()) return;

        // Soft check in case another inventory
        if (isMismatchedInventory(player, event.getView())) return;

        int slot = event.getRawSlot();

        // Modify listings: between 0–26 slots
        if (slot >= 0 && slot <= 26 && clicked.equals(event.getView().getTopInventory())) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && !cursor.getType().isAir()) {
                event.setCancelled(true);

                ItemStack droppedItem = cursor.clone();
                player.setItemOnCursor(null); // Remove item on cursor to prevent duplication

                prompts.openListingPrompt(player, clicked, droppedItem, slot);
                return;
            }

            // If there is an item in slot: Edit Price or Remove Listing
            BazaarListing existing = data.getListings().get(slot);
            if (existing != null) {
                event.setCancelled(true);

                if (event.getClick().isLeftClick()) {
                    prompts.openEditPrompt(player, slot, existing);
                } else if (event.getClick().isRightClick()) {
                    event.getClickedInventory().setItem(slot, new ItemStack(Material.AIR));
                    data.removeListing(slot);

                    Bukkit.getScheduler().runTaskAsynchronously(context.plugin, () -> context.storage.saveBazaar(data));

                    context.gui.closeCustomerAndConfirmGUIsForAllPlayers(data);

                    player.sendMessage("§cListing removed.");

                    HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(existing.getItem());
                    for (ItemStack item : leftovers.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }
                return;
            }
        }

        event.setCancelled(true);

        switch (slot) {
            case 30 -> {
                double withdrawn = data.withdrawAll();
                context.vaultHook.getEconomy().depositPlayer(player, withdrawn);
                updateBankButton(event.getClickedInventory());

                Bukkit.getScheduler().runTaskAsynchronously(context.plugin, () -> context.storage.saveBazaar(data));

                player.sendMessage("§aWithdrawn §f$" + withdrawn + "§a to your balance.");
            }
            case 31 -> {
                // This is to prevent visual bugs as a safeguard, shouldn't matter
                data.setClosed(true);

                // Refund items
                for (BazaarListing listing : data.getListings().values()) {
                    HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(listing.getItem());
                    for (ItemStack item : leftovers.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }

                // Refund bank
                double withdrawn = data.withdrawAll();
                context.vaultHook.getEconomy().depositPlayer(player, withdrawn);

                // Remove stand & unregister
                context.bazaarManager.removeBazaar(data.getId());

                // Notify
                player.sendMessage("§cYour bazaar has been closed.");
                player.sendMessage("§7Items and §f$" + withdrawn + "§7 returned.");

                // Close inventory
                player.closeInventory();

                // Delete from database
                Bukkit.getScheduler().runTaskAsynchronously(context.plugin, () -> context.storage.deleteBazaar(data.getId()));

                // Exit GUI
                context.gui.exitGUI(player);
            }
            case 32 -> {
                double balance = context.vaultHook.getEconomy().getBalance(player);
                double extensionFee = context.config.getExtensionFee();
                if (balance < extensionFee) {
                    player.sendMessage("§cYou don’t have enough money to extend your bazaar.");
                    return;
                }

                boolean extended = data.extendExpiration(86400000);
                if (extended) {
                    context.vaultHook.getEconomy().withdrawPlayer(player, extensionFee);
                    updateTimeLeftButton(event.getClickedInventory());

                    Bukkit.getScheduler().runTaskAsynchronously(context.plugin, () -> context.storage.saveBazaar(data));

                    player.sendMessage("§aExtended bazaar by 1 day for §f" + extensionFee);
                } else {
                    player.sendMessage("§eYou can't extend beyond 2 days from now.");
                }
            }
            case 35 -> {
                if (context.bazaarManager.rotateBazaar(data, 45.0f)) {
                    Bukkit.getScheduler().runTaskAsynchronously(context.plugin, () -> context.storage.saveBazaar(data));
                    player.sendMessage("§bBazaar rotated!");
                }
            }
        }
    }

    public boolean isMismatchedInventory(Player player, InventoryView view) {
        if (!ChatColor.stripColor(view.getTitle()).equalsIgnoreCase(data.getName())) {
            context.gui.exitGUI(player);
            return true;
        } else return false;
    }

    public void refreshListingAndBank(Player owner, int slot) {
        if (owner == null) return;

        updateBankButton(owner.getOpenInventory().getTopInventory());
        updateSlot(owner.getOpenInventory().getTopInventory(), slot);
    }

    private void updateBankButton(Inventory gui) {
        gui.setItem(30, GUIItemFactory.bankButton(data.getBankBalance()));
    }

    private void updateTimeLeftButton(Inventory gui) {
        long remaining = data.getExpiresAt() - System.currentTimeMillis();
        gui.setItem(32, GUIItemFactory.timeLeftButton(remaining, context.config.getExtensionFee()));
    }

    public void updateSlot(Inventory gui, int slot) {
        BazaarListing listing = data.getListings().get(slot);
        if (listing != null) {
            ItemStack item = ListingLoreUtil.withOwnerLore(listing.getItem(), listing.getPrice(), Bukkit.getOfflinePlayer(data.getOwner()).getName());
            gui.setItem(slot, item);
        } else {
            gui.setItem(slot, new ItemStack(Material.AIR));
        }
    }

    @Override
    public void handleDrag(Player player, InventoryDragEvent event) {
        // Soft check in case another inventory event
        if (isMismatchedInventory(player, event.getView())) return;

        int topSize = event.getView().getTopInventory().getSize();

        // Cancel only if drag STARTED from top inventory (listing area)
        if (event.getInventorySlots().stream().anyMatch(slot -> slot < topSize)) event.setCancelled(true);
    }

    @Override
    public BazaarData getBazaar() {
        return data;
    }
}
