package dev.rusthero.mmobazaar.listener;

import dev.rusthero.mmobazaar.MMOBazaar;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.gui.CustomerGUI;
import dev.rusthero.mmobazaar.gui.OwnerGUI;
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
            context.gui.openOwnerGUI(player, new OwnerGUI(context, bazaar));
        } else {
            context.gui.openCustomerGUI(player, new CustomerGUI(context, bazaar));
        }
    }
}
