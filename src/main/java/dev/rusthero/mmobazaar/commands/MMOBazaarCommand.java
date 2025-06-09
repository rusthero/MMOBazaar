package dev.rusthero.mmobazaar.commands;

import dev.rusthero.mmobazaar.MMOBazaarAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MMOBazaarCommand implements CommandExecutor {
    private final MMOBazaarAPI api;

    public MMOBazaarCommand(MMOBazaarAPI api) {
        this.api = api;
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

            api.giveBazaarBag(target, amount);
            sender.sendMessage("§a[MMOBazaar] Gave " + amount + "x Market Bag to " + target.getName());
            return true;
        }

        sender.sendMessage("§cUsage: /mmobazaar give <player> [amount]");
        return true;
    }
}
