package dev.rusthero.mmobazaar.gui;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.logic.BazaarCreationValidator;
import net.milkbowl.vault.economy.EconomyResponse;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BazaarCreateGUI {
    private final MMOBazaarContext context;
    private final Set<UUID> completed = ConcurrentHashMap.newKeySet();
    private final ItemStack bag;

    public BazaarCreateGUI(MMOBazaarContext context, ItemStack bag) {
        this.context = context;
        this.bag = bag;
    }

    public void open(Player player) {
        completed.remove(player.getUniqueId()); // Reset just in case

        new AnvilGUI.Builder().plugin(context.plugin).title("Bazaar Name").text("Enter Name").itemLeft(new ItemStack(Material.NAME_TAG)).onClick((slot, state) -> {
            if (slot != AnvilGUI.Slot.OUTPUT) return List.of(); // only act on confirmation

            String name = state.getText().trim();
            if (name.isEmpty()) {
                player.sendMessage("§cBazaar name cannot be empty.");
                return List.of(AnvilGUI.ResponseAction.close());
            }

            BazaarCreationValidator.Result result = BazaarCreationValidator.canCreate(context, player);
            switch (result) {
                case MISSING_BAZAAR_BAG -> {
                    player.sendMessage("§cYou need a bazaar bag to open bazaar");
                    return List.of(AnvilGUI.ResponseAction.close());
                }
                case INSUFFICIENT_FUNDS -> {
                    player.sendMessage("§cYou need at least §f$" + context.config.getCreationFee() + " §cto open a bazaar.");
                    return List.of(AnvilGUI.ResponseAction.close());
                }
                case BAZAAR_LIMIT_REACHED -> {
                    player.sendMessage("§cYou cannot have more than " + context.config.getMaxBazaarsPerPlayer() + " bazaars.");
                    return List.of(AnvilGUI.ResponseAction.close());
                }
            }

            EconomyResponse withdraw = context.vaultHook.getEconomy().withdrawPlayer(player, context.config.getCreationFee());
            if (!withdraw.transactionSuccess()) {
                player.sendMessage("§cTransaction failed, check your funds");
                return List.of(AnvilGUI.ResponseAction.close());
            }

            return context.bazaarManager.createBazaar(player, name).map(data -> List.of(AnvilGUI.ResponseAction.run(() -> {
                completed.add(player.getUniqueId());
                bag.setAmount(bag.getAmount() - 1);
                player.sendMessage("§aBazaar created and §f$" + context.config.getCreationFee() + " §awithdrawn.");
            }), AnvilGUI.ResponseAction.close())).orElseGet(() -> {
                player.sendMessage("§cFailed to create bazaar.");
                return List.of(AnvilGUI.ResponseAction.close());
            });
        }).onClose(state -> {
            if (!completed.contains(player.getUniqueId())) {
                player.sendMessage("§eBazaar creation cancelled.");
            }
        }).open(player);
    }
}