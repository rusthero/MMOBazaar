package dev.rusthero.mmobazaar.gui;

import dev.rusthero.mmobazaar.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GUIItemFactory {
    private GUIItemFactory() {

    }

    public static ItemStack simpleButton(Material mat, String name, String... loreLines) {
        ItemBuilder builder = new ItemBuilder(mat).setName(name);
        for (String line : loreLines) {
            builder.addLore(line);
        }
        return builder.build();
    }

    public static ItemStack bankButton(double balance) {
        return new ItemBuilder(Material.GOLD_INGOT)
                .setName("§6Bazaar Bank")
                .addLore("§7Currently: §f$" + balance)
                .addLore("§eClick to withdraw money from your bazaar")
                .build();
    }

    public static ItemStack timeLeftButton(long millisLeft, double extensionFee) {
        return new ItemBuilder(Material.CLOCK)
                .setName("§6Time Left")
                .addLore("§7" + formatTime(millisLeft))
                .addLore("§eClick to extend for §f$" + extensionFee)
                .build();
    }

    private static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = (seconds / 60) % 60;
        long hours = (seconds / 3600) % 24;
        long days = seconds / 86400;
        return days + "d " + hours + "h " + minutes + "m";
    }
}
