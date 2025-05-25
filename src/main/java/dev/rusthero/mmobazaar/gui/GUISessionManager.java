package dev.rusthero.mmobazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GUISessionManager {
    private final Map<UUID, BazaarOwnerGUI> ownerGuis = new HashMap<>();

    public void setOwnerGUI(UUID playerId, BazaarOwnerGUI gui) {
        ownerGuis.put(playerId, gui);
    }

    public void removeOwnerGUI(UUID playerId) {
        ownerGuis.remove(playerId);
    }

    public Optional<BazaarOwnerGUI> getOwnerGUI(UUID playerId) {
        return Optional.ofNullable(ownerGuis.get(playerId));
    }

    private final Map<UUID, BazaarCustomerGUI> customerGUIs = new HashMap<>();

    public void setCustomerGUI(UUID playerId, BazaarCustomerGUI gui) {
        customerGUIs.put(playerId, gui);
    }

    public Optional<BazaarCustomerGUI> getCustomerGUI(UUID playerId) {
        return Optional.ofNullable(customerGUIs.get(playerId));
    }

    public void removeCustomerGUI(UUID playerId) {
        customerGUIs.remove(playerId);
    }

    private final Map<UUID, ConfirmPurchaseGUI> confirming = new HashMap<>();

    public void setConfirmingGUI(UUID playerId, ConfirmPurchaseGUI gui) {
        confirming.put(playerId, gui);
    }

    public Optional<ConfirmPurchaseGUI> getConfirmingGUI(UUID playerId) {
        return Optional.ofNullable(confirming.get(playerId));
    }

    public void removeConfirmingGUI(UUID playerId) {
        confirming.remove(playerId);
    }

    public void closeCustomerGUIsFor(UUID bazaarId) {
        Set<UUID> toRemove = customerGUIs.entrySet().stream()
                .filter(entry -> entry.getValue().getData().getId().equals(bazaarId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        for (UUID uuid : toRemove) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage("§eThis bazaar is being updated. Please try again in a moment.");
                p.closeInventory();
            }
            customerGUIs.remove(uuid);
        }
    }

    public void closeConfirmingGUIsFor(UUID bazaarId) {
        Set<UUID> toRemove = confirming.entrySet().stream()
                .filter(entry -> entry.getValue().getData().getId().equals(bazaarId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        for (UUID uuid : toRemove) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage("§eThis bazaar is being updated. Please try again in a moment.");
                p.closeInventory();
            }
            confirming.remove(uuid);
        }
    }
}
