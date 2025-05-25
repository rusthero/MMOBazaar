package dev.rusthero.mmobazaar.listener;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.gui.BazaarCreateGUI;
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
    public void onUse(PlayerInteractEvent event) {
        // Ignore offhand and empty interacts, right click only
        if (event.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (context.bagFactory.isBazaarBag(item)) {
            event.setCancelled(true); // Cancel in case player does actually interact with something

            double balance = context.vaultHook.getEconomy().getBalance(player);
            double creationFee = context.config.getCreationFee();
            if (balance < creationFee) {
                player.sendMessage("§cYou need at least §f$" + creationFee + " §cto open a bazaar.");
                return;
            }

            int bazaarAmount = context.bazaarManager.getBazaarsByOwner(player.getUniqueId()).size();
            if (bazaarAmount >= context.config.getMaxBazaarsPerPlayer()) {
                player.sendMessage("§cYou cannot have more than " + bazaarAmount + " bazaars.");
                return;
            }

            player.sendMessage("§e[MMOBazaar] Starting market setup...");
            new BazaarCreateGUI(context, item).open(player);
        }
    }
}
