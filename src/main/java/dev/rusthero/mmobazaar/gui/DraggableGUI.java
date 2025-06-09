package dev.rusthero.mmobazaar.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;

public interface DraggableGUI {
    void handleDrag(Player player, InventoryDragEvent event);
}
