package dev.rusthero.mmobazaar.storage;

import com.zaxxer.hikari.HikariDataSource;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarListing;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLiteStorage implements BazaarStorage {
    private final JavaPlugin plugin;
    private final DataSource dataSource;

    public SQLiteStorage(JavaPlugin plugin, HikariDataSource dataSource) {
        this.plugin = plugin;
        this.dataSource = dataSource;
    }

    @Override
    public void init() {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();

            stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS bazaars (
                            id TEXT PRIMARY KEY,
                            owner TEXT,
                            name TEXT,
                            world TEXT,
                            x DOUBLE, y DOUBLE, z DOUBLE,
                            yaw FLOAT,
                            created_at BIGINT,
                            expires_at BIGINT,
                            closed BOOLEAN,
                            bank DOUBLE,
                            stand_uuid TEXT,
                            name_uuid TEXT,
                            owner_uuid TEXT
                        )
                    """);

            stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS listings (
                            bazaar_id TEXT,
                            slot INTEGER,
                            price DOUBLE,
                            item BLOB,
                            PRIMARY KEY (bazaar_id, slot)
                        )
                    """);

            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize SQLite: " + e.getMessage());
        }
    }

    @Override
    public boolean saveBazaar(BazaarData data) {
        String sql = """
                    INSERT OR REPLACE INTO bazaars (
                        id, owner, name, world, x, y, z, yaw,
                        created_at, expires_at, closed, bank,
                        stand_uuid, name_uuid, owner_uuid
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String listingSql = """
                    INSERT OR REPLACE INTO listings (bazaar_id, slot, price, item)
                    VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); PreparedStatement listingStmt = conn.prepareStatement(listingSql)) {
            ps.setString(1, data.getId().toString());
            ps.setString(2, data.getOwner().toString());
            ps.setString(3, data.getName());

            World world = data.getLocation().getWorld();
            if (world == null) {
                throw new IllegalStateException("[MMOBazaar] Cannot save bazaar with null world: " + data.getId());
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

            ps.setString(13, data.getVisualStandId().toString());
            ps.setString(14, data.getNameStandId().toString());
            ps.setString(15, data.getOwnerStandId().toString());

            ps.executeUpdate();

            for (Map.Entry<Integer, BazaarListing> entry : data.getListings().entrySet()) {
                int slot = entry.getKey();
                BazaarListing listing = entry.getValue();

                listingStmt.setString(1, data.getId().toString());
                listingStmt.setInt(2, slot);
                listingStmt.setDouble(3, listing.getPrice());
                listingStmt.setBytes(4, serializeItem(listing.getItem()));

                listingStmt.addBatch();
            }

            listingStmt.executeBatch();

            return true;
        } catch (IllegalStateException | SQLException e) {
            plugin.getLogger().severe("Failed to save bazaar [" + data.getName() + " / " + data.getId() + "]: " + e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) {
                plugin.getLogger().severe("  at " + el.toString());
            }
        }

        return false;
    }

    private byte[] serializeItem(ItemStack item) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeObject(item);
            return baos.toByteArray();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to serialize item: " + e.getMessage());
            return new byte[0];
        }
    }

    @Override
    public boolean deleteBazaar(UUID bazaarId) {
        String deleteListingsSql = "DELETE FROM listings WHERE bazaar_id = ?";
        String deleteBazaarSql = "DELETE FROM bazaars WHERE id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt1 = conn.prepareStatement(deleteListingsSql); PreparedStatement stmt2 = conn.prepareStatement(deleteBazaarSql)) {
            stmt1.setString(1, bazaarId.toString());
            stmt1.executeUpdate();

            stmt2.setString(1, bazaarId.toString());
            stmt2.executeUpdate();

            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete bazaar: " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean saveAll(Collection<BazaarData> bazaars) {
        String sqlBazaar = """
                    INSERT OR REPLACE INTO bazaars (
                        id, owner, name, world, x, y, z, yaw, created_at,
                        expires_at, closed, bank, stand_uuid, name_uuid, owner_uuid
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String sqlListing = """
                    INSERT OR REPLACE INTO listings (bazaar_id, slot, price, item)
                    VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection(); PreparedStatement psBazaar = conn.prepareStatement(sqlBazaar); PreparedStatement psListing = conn.prepareStatement(sqlListing)) {
            for (BazaarData data : bazaars) {
                // Bazaar main data
                psBazaar.setString(1, data.getId().toString());
                psBazaar.setString(2, data.getOwner().toString());
                psBazaar.setString(3, data.getName());

                World world = data.getLocation().getWorld();
                if (world == null) {
                    throw new IllegalStateException("[MMOBazaar] Cannot save bazaar with null world: " + data.getId());
                }

                psBazaar.setString(4, world.getName());
                psBazaar.setDouble(5, data.getLocation().getX());
                psBazaar.setDouble(6, data.getLocation().getY());
                psBazaar.setDouble(7, data.getLocation().getZ());
                psBazaar.setFloat(8, data.getLocation().getYaw());
                psBazaar.setLong(9, data.getCreatedAt());
                psBazaar.setLong(10, data.getExpiresAt());
                psBazaar.setBoolean(11, data.isClosed());
                psBazaar.setDouble(12, data.getBankBalance());

                psBazaar.setString(13, data.getVisualStandId().toString());
                psBazaar.setString(14, data.getNameStandId().toString());
                psBazaar.setString(15, data.getOwnerStandId().toString());

                psBazaar.addBatch();

                // Bazaar listings
                for (Map.Entry<Integer, BazaarListing> entry : data.getListings().entrySet()) {
                    psListing.setString(1, data.getId().toString());
                    psListing.setInt(2, entry.getKey());
                    psListing.setDouble(3, entry.getValue().getPrice());
                    psListing.setBytes(4, serializeItem(entry.getValue().getItem()));
                    psListing.addBatch();
                }
            }

            psBazaar.executeBatch();
            psListing.executeBatch();

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("[MMOBazaar] Failed to bulk-save bazaars: " + e.getMessage());
        }

        return false;
    }

    @Override
    public Optional<BazaarData> loadBazaar(UUID bazaarId) {
        String sqlBazaar = "SELECT * FROM bazaars WHERE id = ?";
        String sqlListings = "SELECT * FROM listings WHERE bazaar_id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement psBazaar = conn.prepareStatement(sqlBazaar); PreparedStatement psListings = conn.prepareStatement(sqlListings)) {
            psBazaar.setString(1, bazaarId.toString());
            try (ResultSet rs = psBazaar.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                UUID owner = UUID.fromString(rs.getString("owner"));
                String name = rs.getString("name");

                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    throw new IllegalStateException("[MMOBazaar] World not found: " + worldName);
                }

                Location location = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), 0f);

                long createdAt = rs.getLong("created_at");
                long expiresAt = rs.getLong("expires_at");
                boolean closed = rs.getBoolean("closed");
                double bank = rs.getDouble("bank");

                UUID stand = UUID.fromString(rs.getString("stand_uuid"));
                UUID nameStand = UUID.fromString(rs.getString("name_uuid"));
                UUID ownerStand = UUID.fromString(rs.getString("owner_uuid"));

                Map<Integer, BazaarListing> listings = new HashMap<>();

                psListings.setString(1, bazaarId.toString());
                try (ResultSet lrs = psListings.executeQuery()) {
                    while (lrs.next()) {
                        int slot = lrs.getInt("slot");
                        double price = lrs.getDouble("price");
                        byte[] itemBlob = lrs.getBytes("item");

                        ItemStack item = deserializeItem(itemBlob);
                        if (item != null) {
                            listings.put(slot, new BazaarListing(item, price, slot));
                        }
                    }
                }

                BazaarData data = new BazaarData(bazaarId, owner, name, location, createdAt, expiresAt, closed, bank, listings, stand, nameStand, ownerStand);

                return Optional.of(data);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[MMOBazaar] Failed to load bazaar: " + e.getMessage());
            return Optional.empty();
        }
    }

    private ItemStack deserializeItem(byte[] data) {
        if (data == null || data.length == 0) return null;

        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new ByteArrayInputStream(data))) {
            Object obj = in.readObject();
            return (obj instanceof ItemStack) ? (ItemStack) obj : null;
        } catch (IOException | ClassNotFoundException e) {
            Bukkit.getLogger().warning("[MMOBazaar] Failed to deserialize item: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Collection<BazaarData> loadAll() {
        String sqlBazaar = "SELECT * FROM bazaars";
        String sqlListings = "SELECT * FROM listings WHERE bazaar_id = ?";

        List<BazaarData> allBazaars = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); PreparedStatement psBazaar = conn.prepareStatement(sqlBazaar); PreparedStatement psListings = conn.prepareStatement(sqlListings)) {
            try (ResultSet rs = psBazaar.executeQuery()) {
                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    UUID owner = UUID.fromString(rs.getString("owner"));
                    String name = rs.getString("name");

                    String worldName = rs.getString("world");
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("[MMOBazaar] Skipping bazaar '" + name + "': world not found (" + worldName + ")");
                        continue;
                    }

                    Location location = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), 0f);

                    long createdAt = rs.getLong("created_at");
                    long expiresAt = rs.getLong("expires_at");
                    boolean closed = rs.getBoolean("closed");
                    double bank = rs.getDouble("bank");

                    UUID stand = UUID.fromString(rs.getString("stand_uuid"));
                    UUID nameStand = UUID.fromString(rs.getString("name_uuid"));
                    UUID ownerStand = UUID.fromString(rs.getString("owner_uuid"));

                    // Load listings
                    Map<Integer, BazaarListing> listings = new HashMap<>();
                    psListings.setString(1, id.toString());
                    try (ResultSet lrs = psListings.executeQuery()) {
                        while (lrs.next()) {
                            int slot = lrs.getInt("slot");
                            double price = lrs.getDouble("price");
                            byte[] itemBlob = lrs.getBytes("item");

                            ItemStack item = deserializeItem(itemBlob);
                            if (item != null) {
                                listings.put(slot, new BazaarListing(item, price, slot));
                            }
                        }
                    }

                    BazaarData data = new BazaarData(id, owner, name, location, createdAt, expiresAt, closed, bank, listings, stand, nameStand, ownerStand);

                    allBazaars.add(data);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[MMOBazaar] Failed to load all bazaars: " + e.getMessage());
        }

        return allBazaars;
    }
}