package dev.rusthero.mmobazaar.logic;

import dev.rusthero.mmobazaar.MMOBazaarContext;
import dev.rusthero.mmobazaar.item.BazaarBagFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BazaarCreationValidator {
    public enum Result {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        BAZAAR_LIMIT_REACHED,
        MISSING_BAZAAR_BAG
    }

    public static Result canCreate(MMOBazaarContext context, Player player) {
        double balance = context.vaultHook.getEconomy().getBalance(player);
        double fee = context.config.getCreationFee();

        int existing = context.bazaarManager.getBazaarsByOwner(player.getUniqueId()).size();
        int limit = context.config.getMaxBazaarsPerPlayer();

        if (!hasBazaarBag(player, context.bagFactory)) return Result.MISSING_BAZAAR_BAG;
        if (balance < fee) return Result.INSUFFICIENT_FUNDS;
        if (existing >= limit) return Result.BAZAAR_LIMIT_REACHED;

        return Result.SUCCESS;
    }

    private static boolean hasBazaarBag(Player player, BazaarBagFactory factory) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (factory.isBazaarBag(item)) return true;
        }
        return false;
    }
}
