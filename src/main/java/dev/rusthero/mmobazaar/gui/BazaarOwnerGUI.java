package dev.rusthero.mmobazaar.gui;

import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.item.util.ListingLoreUtil;
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

public class BazaarOwnerGUI {
    private final MMOBazaarContext context;
    private final BazaarData data;

    public BazaarOwnerGUI(MMOBazaarContext context, BazaarData data) {
        this.context = context;
        this.data = data;
    }

    public void open(Player player) {
        // Register session for event handling
        context.guiSessions.setOwnerGUI(player.getUniqueId(), this);

        // Lock the bazaar and close customer GUIs for this bazaar
        context.guiSessions.closeCustomerGUIsFor(data.getId());
        context.guiSessions.closeConfirmingGUIsFor(data.getId());

        Inventory gui = Bukkit.createInventory(null, 36, data.getName());

        // Listings 0–26
        for (int slot = 0; slot <= 26; slot++) {
            updateSlot(gui, slot);
        }

        // Filler glass panes on final row
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        for (int i = 27; i < 36; i++) {
            gui.setItem(i, glass);
        }

        // Buttons
        updateBankButton(gui);
        gui.setItem(31, makeButton(Material.BARRIER, "§cDelete Bazaar", "§7Removes the bazaar and refunds items"));
        updateTimeLeftButton(gui);
        gui.setItem(35, makeButton(Material.COMPASS, "§bRotate Bazaar", "§7Click to rotate stand 15°"));

        player.openInventory(gui);
    }

    public void handleClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

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

                // Clear GUI session
                context.guiSessions.removeOwnerGUI(player.getUniqueId());
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

    private ItemStack makeButton(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(List.of(loreLines));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = (seconds / 60) % 60;
        long hours = (seconds / 3600) % 24;
        long days = seconds / 86400;
        return days + "d " + hours + "h " + minutes + "m";
    }

    public void updateBankButton(Inventory gui) {
        ItemStack bank = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = bank.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Bazaar Bank");
            List<String> lore = new ArrayList<>();
            lore.add("§7Currently: §f$" + data.getBankBalance());
            lore.add("§eClick to withdraw money from your bazaar");
            meta.setLore(lore);
            bank.setItemMeta(meta);
        }
        gui.setItem(30, bank);
    }

    private void updateTimeLeftButton(Inventory gui) {
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Time Left");
            List<String> lore = new ArrayList<>();
            lore.add("§7" + formatTime(data.getExpiresAt() - System.currentTimeMillis()));
            lore.add("§eClick to extend for §f$" + context.config.getExtensionFee());
            meta.setLore(lore);
            clock.setItemMeta(meta);
        }
        gui.setItem(32, clock);
    }

    public void updateSlot(Inventory gui, int slot) {
        BazaarListing listing = data.getListings().get(slot);
        if (listing != null) {
            ItemStack item = ListingLoreUtil.withOwnerLore(
                    listing.getItem(),
                    listing.getPrice(),
                    Bukkit.getOfflinePlayer(data.getOwner()).getName()
            );
            gui.setItem(slot, item);
        } else {
            gui.setItem(slot, new ItemStack(Material.AIR));
        }
    }

    public BazaarData getData() {
        return data;
    }
}
