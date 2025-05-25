package dev.rusthero.mmobazaar.listener;

import dev.rusthero.mmobazaar.MMOBazaar;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.gui.BazaarCustomerGUI;
import dev.rusthero.mmobazaar.gui.BazaarOwnerGUI;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class BazaarInteractionListener implements Listener {
    private final MMOBazaarContext context;

    public BazaarInteractionListener(MMOBazaarContext context) {
        this.context = context;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;
        if (stand.getEquipment() == null) return; // Interact only with the chest

        PersistentDataContainer pdc = stand.getPersistentDataContainer();
        String rawId = pdc.get(MMOBazaar.BAZAAR_ID_KEY, PersistentDataType.STRING);
        if (rawId == null) return;
        UUID bazaarId = UUID.fromString(rawId);
        BazaarData bazaar = context.bazaarManager.getBazaar(bazaarId);

        if (bazaar == null) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        if (bazaar.getOwner().equals(player.getUniqueId())) {
            // Owner GUI
            BazaarOwnerGUI gui = new BazaarOwnerGUI(context, bazaar);
            gui.open(player);
        } else {
            // Customer GUI
            BazaarCustomerGUI gui = new BazaarCustomerGUI(context, bazaar);
            gui.open(player);
        }
    }
}
