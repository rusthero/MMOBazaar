package dev.rusthero.mmobazaar.gui.session.type;

import dev.rusthero.mmobazaar.gui.component.ConfirmGUI;
import dev.rusthero.mmobazaar.gui.session.GUISession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfirmGUISession extends GUISession<ConfirmGUI> {
    public void closeForAllPlayers(UUID bazaarId) {
        Set<UUID> toRemove = sessions.entrySet().stream().filter(entry -> entry.getValue().getBazaar().getId().equals(bazaarId)).map(Map.Entry::getKey).collect(Collectors.toSet());

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
