package dev.rusthero.mmobazaar.storage.mapping;

import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.storage.jdbc.UUIDAdapter;
import dev.rusthero.mmobazaar.storage.util.ItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record BazaarMapper(UUIDAdapter uuidAdapter) {
    public BazaarData fromResultSet(ResultSet rs, PreparedStatement psListings) throws SQLException {
        UUID id = uuidAdapter.get(rs, "id");
        UUID owner = uuidAdapter.get(rs, "owner");
        String name = rs.getString("name");

        String worldName = rs.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) throw new IllegalStateException("World not found: " + worldName);

        Location location = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), 0f);
        long createdAt = rs.getLong("created_at");
        long expiresAt = rs.getLong("expires_at");
        boolean closed = rs.getBoolean("closed");
        double bank = rs.getDouble("bank");

        UUID stand = uuidAdapter.get(rs, "stand_uuid");
        UUID nameStand = uuidAdapter.get(rs, "name_uuid");
        UUID ownerStand = uuidAdapter.get(rs, "owner_uuid");

        Map<Integer, BazaarListing> listings = new HashMap<>();
        uuidAdapter.set(psListings, 1, id);
        try (ResultSet lrs = psListings.executeQuery()) {
            while (lrs.next()) {
                int slot = lrs.getInt("slot");
                double price = lrs.getDouble("price");
                byte[] itemBlob = lrs.getBytes("item");
                ItemSerializer.deserializeItem(itemBlob).ifPresent(item -> listings.put(slot, new BazaarListing(item, price, slot)));
            }
        }

        return new BazaarData(id, owner, name, location, createdAt, expiresAt, closed, bank, listings, stand, nameStand, ownerStand);
    }
}
