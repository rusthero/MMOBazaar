package dev.rusthero.mmobazaar.listener;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.gui.component.CreatePrompt;
import dev.rusthero.mmobazaar.logic.BazaarCreationValidator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BazaarBagUseListener implements Listener {
    private final MMOBazaarContext context;

    public BazaarBagUseListener(MMOBazaarContext context) {
        this.context = context;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Ignore offhand and empty interacts, right click only
        if (event.getHand() != EquipmentSlot.HAND) return;
        final ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        // Check if item is bazaar bag
        if (!context.bagFactory.isBazaarBag(item)) return;

        event.setCancelled(true); // Cancel in case player does actually interact with something

        final Player player = event.getPlayer();
        switch (BazaarCreationValidator.canCreate(context, player)) {
            case MISSING_BAZAAR_BAG -> player.sendMessage("§cYou need a bazaar bag to open bazaar");
            case INSUFFICIENT_FUNDS ->
                    player.sendMessage("§cYou need at least §f$" + context.config.getCreationFee() + " §cto open a bazaar.");
            case BAZAAR_LIMIT_REACHED ->
                    player.sendMessage("§cYou cannot have more than " + context.config.getMaxBazaarsPerPlayer() + " bazaars.");
            case SUCCESS -> {
                player.sendMessage("§e[MMOBazaar] Starting market setup...");
                new CreatePrompt(context, item).open(player);
            }
        }
    }
}