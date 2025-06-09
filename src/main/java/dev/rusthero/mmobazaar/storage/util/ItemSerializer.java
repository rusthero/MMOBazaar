package dev.rusthero.mmobazaar.storage.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public class ItemSerializer {
    private ItemSerializer() {
        // Static class, no instantiation
    }

    public static byte[] serializeItem(ItemStack item) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeObject(item);
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static Optional<ItemStack> deserializeItem(byte[] data) {
        if (data == null || data.length == 0) return Optional.empty();

        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new ByteArrayInputStream(data))) {
            Object obj = in.readObject();
            return (obj instanceof ItemStack) ? Optional.of((ItemStack) obj) : Optional.empty();
        } catch (IOException | ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
