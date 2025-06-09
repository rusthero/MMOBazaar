package dev.rusthero.mmobazaar.storage.schema;

public final class SQLSchema {

    private SQLSchema() {
        // Utility class; prevent instantiation
    }

    public static final class SQLite {
        public static final String CREATE_BAZAARS = """
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
                    );
                """;

        public static final String CREATE_LISTINGS = """
                    CREATE TABLE IF NOT EXISTS listings (
                        bazaar_id TEXT,
                        slot INTEGER,
                        price DOUBLE,
                        item BLOB,
                        PRIMARY KEY (bazaar_id, slot)
                    );
                """;

        public static final String INSERT_BAZAAR = """
                    INSERT OR REPLACE INTO bazaars (
                        id, owner, name, world, x, y, z, yaw,
                        created_at, expires_at, closed, bank,
                        stand_uuid, name_uuid, owner_uuid
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        public static final String INSERT_LISTING = """
                    INSERT OR REPLACE INTO listings (bazaar_id, slot, price, item)
                    VALUES (?, ?, ?, ?);
                """;

        public static final String SELECT_BAZAAR = "SELECT * FROM bazaars WHERE id = ?";
        public static final String SELECT_ALL_BAZAARS = "SELECT * FROM bazaars";
        public static final String SELECT_LISTINGS = "SELECT * FROM listings WHERE bazaar_id = ?";
        public static final String DELETE_BAZAAR = "DELETE FROM bazaars WHERE id = ?";
        public static final String DELETE_LISTINGS = "DELETE FROM listings WHERE bazaar_id = ?";
    }

    public static final class MySQL {
        public static final String CREATE_BAZAARS = """
                    CREATE TABLE IF NOT EXISTS bazaars (
                        id CHAR(36) PRIMARY KEY,
                        owner CHAR(36),
                        name VARCHAR(64),
                        world VARCHAR(64),
                        x DOUBLE, y DOUBLE, z DOUBLE,
                        yaw FLOAT,
                        created_at BIGINT,
                        expires_at BIGINT,
                        closed TINYINT(1),
                        bank DOUBLE,
                        stand_uuid CHAR(36),
                        name_uuid CHAR(36),
                        owner_uuid CHAR(36)
                    ) ENGINE=InnoDB;
                """;

        public static final String CREATE_LISTINGS = """
                    CREATE TABLE IF NOT EXISTS listings (
                        bazaar_id CHAR(36),
                        slot INT,
                        price DOUBLE,
                        item BLOB,
                        PRIMARY KEY (bazaar_id, slot),
                        FOREIGN KEY (bazaar_id) REFERENCES bazaars(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB;
                """;

        public static final String INSERT_BAZAAR = """
                    INSERT INTO bazaars (
                        id, owner, name, world, x, y, z, yaw,
                        created_at, expires_at, closed, bank,
                        stand_uuid, name_uuid, owner_uuid
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        owner = VALUES(owner),
                        name = VALUES(name),
                        world = VALUES(world),
                        x = VALUES(x),
                        y = VALUES(y),
                        z = VALUES(z),
                        yaw = VALUES(yaw),
                        created_at = VALUES(created_at),
                        expires_at = VALUES(expires_at),
                        closed = VALUES(closed),
                        bank = VALUES(bank),
                        stand_uuid = VALUES(stand_uuid),
                        name_uuid = VALUES(name_uuid),
                        owner_uuid = VALUES(owner_uuid);
                """;

        public static final String INSERT_LISTING = """
                    INSERT INTO listings (bazaar_id, slot, price, item)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        price = VALUES(price),
                        item = VALUES(item);
                """;

        public static final String SELECT_BAZAAR = "SELECT * FROM bazaars WHERE id = ?";
        public static final String SELECT_ALL_BAZAARS = "SELECT * FROM bazaars";
        public static final String SELECT_LISTINGS = "SELECT * FROM listings WHERE bazaar_id = ?";
        public static final String DELETE_BAZAAR = "DELETE FROM bazaars WHERE id = ?";
        public static final String DELETE_LISTINGS = "DELETE FROM listings WHERE bazaar_id = ?";
    }

    public static final class PostgreSQL {
        public static final String CREATE_BAZAARS = """
                    CREATE TABLE IF NOT EXISTS bazaars (
                        id UUID PRIMARY KEY,
                        owner UUID,
                        name VARCHAR(64),
                        world VARCHAR(64),
                        x DOUBLE PRECISION, y DOUBLE PRECISION, z DOUBLE PRECISION,
                        yaw FLOAT,
                        created_at BIGINT,
                        expires_at BIGINT,
                        closed BOOLEAN,
                        bank DOUBLE PRECISION,
                        stand_uuid UUID,
                        name_uuid UUID,
                        owner_uuid UUID
                    );
                """;

        public static final String CREATE_LISTINGS = """
                    CREATE TABLE IF NOT EXISTS listings (
                        bazaar_id UUID,
                        slot INT,
                        price DOUBLE PRECISION,
                        item BYTEA,
                        PRIMARY KEY (bazaar_id, slot),
                        FOREIGN KEY (bazaar_id) REFERENCES bazaars(id) ON DELETE CASCADE
                    );
                """;

        public static final String INSERT_BAZAAR = """
                    INSERT INTO bazaars (
                        id, owner, name, world, x, y, z, yaw,
                        created_at, expires_at, closed, bank,
                        stand_uuid, name_uuid, owner_uuid
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (id) DO UPDATE SET
                        owner = EXCLUDED.owner,
                        name = EXCLUDED.name,
                        world = EXCLUDED.world,
                        x = EXCLUDED.x,
                        y = EXCLUDED.y,
                        z = EXCLUDED.z,
                        yaw = EXCLUDED.yaw,
                        created_at = EXCLUDED.created_at,
                        expires_at = EXCLUDED.expires_at,
                        closed = EXCLUDED.closed,
                        bank = EXCLUDED.bank,
                        stand_uuid = EXCLUDED.stand_uuid,
                        name_uuid = EXCLUDED.name_uuid,
                        owner_uuid = EXCLUDED.owner_uuid;
                """;

        public static final String INSERT_LISTING = """
                    INSERT INTO listings (bazaar_id, slot, price, item)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT (bazaar_id, slot) DO UPDATE SET
                        price = EXCLUDED.price,
                        item = EXCLUDED.item;
                """;

        public static final String SELECT_BAZAAR = "SELECT * FROM bazaars WHERE id = ?";
        public static final String SELECT_ALL_BAZAARS = "SELECT * FROM bazaars";
        public static final String SELECT_LISTINGS = "SELECT * FROM listings WHERE bazaar_id = ?";
        public static final String DELETE_BAZAAR = "DELETE FROM bazaars WHERE id = ?";
        public static final String DELETE_LISTINGS = "DELETE FROM listings WHERE bazaar_id = ?";
    }
}