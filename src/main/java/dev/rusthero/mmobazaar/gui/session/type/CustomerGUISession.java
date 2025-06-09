package dev.rusthero.mmobazaar.gui.session.type;

import dev.rusthero.mmobazaar.gui.component.CustomerGUI;
import dev.rusthero.mmobazaar.gui.session.GUISession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class CustomerGUISession extends GUISession<CustomerGUI> {
    public void closeForAllPlayers(UUID bazaarId) {
        var toRemove = sessions.entrySet().stream()
                .filter(e -> e.getValue().getBazaar().getId().equals(bazaarId))
                .map(Map.Entry::getKey)
                .toList();

        for (UUID uuid : toRemove) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage("§eThis bazaar is being updated. Please try again in a moment.");
                p.closeInventory();
            }
            sessions.remove(uuid);
        }
    }
}
