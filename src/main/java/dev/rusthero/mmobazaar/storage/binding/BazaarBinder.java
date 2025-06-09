package dev.rusthero.mmobazaar.storage.binding;

import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import dev.rusthero.mmobazaar.storage.jdbc.UUIDAdapter;
import dev.rusthero.mmobazaar.storage.util.ItemSerializer;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public record BazaarBinder(UUIDAdapter uuidAdapter) {
    public void bindBazaarInsert(PreparedStatement ps, BazaarData data) throws SQLException {
        uuidAdapter.set(ps, 1, data.getId());
        uuidAdapter.set(ps, 2, data.getOwner());

        ps.setString(3, data.getName());

        World world = data.getLocation().getWorld();
        if (world == null) {
            throw new IllegalStateException("Cannot save bazaar with null world: " + data.getId());
        }

        ps.setString(4, world.getName());
        ps.setDouble(5, data.getLocation().getX());
        ps.setDouble(6, data.getLocation().getY());
        ps.setDouble(7, data.getLocation().getZ());
        ps.setFloat(8, data.getLocation().getYaw());
        ps.setLong(9, data.getCreatedAt());
        ps.setLong(10, data.getExpiresAt());
        ps.setBoolean(11, data.isClosed());
        ps.setDouble(12, data.getBankBalance());

        uuidAdapter.set(ps, 13, data.getVisualStandId());
        uuidAdapter.set(ps, 14, data.getNameStandId());
        uuidAdapter.set(ps, 15, data.getOwnerStandId());
    }

    public void bindListingInsert(PreparedStatement stmt, UUID bazaarId, int slot, BazaarListing listing) throws SQLException {
        uuidAdapter.set(stmt, 1, bazaarId);
        stmt.setInt(2, slot);
        stmt.setDouble(3, listing.getPrice());
        stmt.setBytes(4, ItemSerializer.serializeItem(listing.getItem()));
        stmt.addBatch();
    }

    public void bindListingsInsert(PreparedStatement stmt, UUID bazaarId, Map<Integer, BazaarListing> listings) throws SQLException {
        for (Map.Entry<Integer, BazaarListing> entry : listings.entrySet()) {
            bindListingInsert(stmt, bazaarId, entry.getKey(), entry.getValue());
        }
    }
}
