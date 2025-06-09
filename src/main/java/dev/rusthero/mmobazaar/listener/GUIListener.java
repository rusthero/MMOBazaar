package dev.rusthero.mmobazaar.listener;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.EnumSet;

public class GUIListener implements Listener {
    private final MMOBazaarContext context;

    public GUIListener(MMOBazaarContext context) {
        this.context = context;
    }

    private static final EnumSet<InventoryAction> ALLOWED_ACTIONS = EnumSet.of(InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF, InventoryAction.PLACE_ALL, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ONE);

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if player is in a Bazaar GUI session first
        if (!context.gui.isInAnyGUI(player)) return; // Let normal inventory interactions happen

        // Not allow shift click, and check if action is an allowed one to prevent unexpected swaps
        if (!event.isShiftClick() && ALLOWED_ACTIONS.contains(event.getAction())) {
            context.gui.handleClick(player, event);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        context.gui.handleDrag(player, event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) context.gui.exitGUI(player);
    }
}