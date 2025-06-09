package dev.rusthero.mmobazaar.gui;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ConfirmPurchaseGUI {
    private final MMOBazaarContext context;
    private final BazaarData data;
    private final BazaarListing listing;
    private final int slot;

    public ConfirmPurchaseGUI(MMOBazaarContext context, BazaarData data, BazaarListing listing, int slot) {
        this.context = context;
        this.data = data;
        this.listing = listing;
        this.slot = slot;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§8Buy for §a$" + listing.getPrice());

        // Confirm Button
        ItemStack confirm = new ItemStack(Material.LIME_DYE);
        ItemMeta cMeta = confirm.getItemMeta();
        if (cMeta != null) {
            cMeta.setDisplayName("§aConfirm Purchase");
            cMeta.setLore(List.of(
                    "",
                    "§7Price: §f$" + listing.getPrice(),
                    "§7Seller: §f" + Bukkit.getOfflinePlayer(data.getOwner()).getName(),
                    "",
                    "§eClick to confirm"
            ));
            confirm.setItemMeta(cMeta);
        }
        gui.setItem(3, confirm);

        // Item Preview
        ItemStack preview = getListing().getItem().clone();
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

        player.openInventory(gui);
        context.guiSessions.setConfirmingGUI(player.getUniqueId(), this);
    }

    public BazaarData getData() {
        return data;
    }

    public BazaarListing getListing() {
        return listing;
    }

    public int getSlot() {
        return slot;
    }
}