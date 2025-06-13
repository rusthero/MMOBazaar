package dev.rusthero.mmobazaar.commands;

import dev.rusthero.mmobazaar.item.BazaarBagFactory;
import dev.rusthero.mmobazaar.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MMOBazaarCommand implements CommandExecutor {
    private final ItemUtils itemUtils;

    public MMOBazaarCommand(double creationFee) {
        this.itemUtils = new ItemUtils(new BazaarBagFactory(creationFee));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            int amount = 1;
            if (args.length >= 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cAmount must be a positive number.");
                    return true;
                }
            }

            itemUtils.giveBazaarBag(target, amount);
            sender.sendMessage("§a[MMOBazaar] Gave " + amount + "x Market Bag to " + target.getName());
            return true;
        }

        sender.sendMessage("§cUsage: /mmobazaar give <player> [amount]");
        return true;
    }
}
