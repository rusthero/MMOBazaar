package dev.rusthero.mmobazaar.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface ClickableGUI {
    void handleClick(Player player, InventoryClickEvent event);
}
