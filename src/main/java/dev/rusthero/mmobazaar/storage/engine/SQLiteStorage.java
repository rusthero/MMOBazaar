package dev.rusthero.mmobazaar.storage.engine;

import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.storage.schema.SQLSchema;
import dev.rusthero.mmobazaar.storage.jdbc.UUIDAdapter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class SQLiteStorage extends SQLStorage {
    public SQLiteStorage(DataSource dataSource, Logger logger) {
        super(dataSource, UUIDAdapter.defaultAdapter(), logger);
    }

    @Override
    public boolean init() {
        return runTransaction(conn -> executeSchema(conn, SQLSchema.SQLite.CREATE_BAZAARS, SQLSchema.SQLite.CREATE_LISTINGS));
    }

    @Override
    public boolean saveBazaar(BazaarData data) {
        return runTransaction(conn -> upsertBazaarWithListings(conn, data, SQLSchema.SQLite.INSERT_BAZAAR, SQLSchema.SQLite.INSERT_LISTING));
    }

    @Override
    public boolean deleteBazaar(UUID bazaarId) {
        return runTransaction(conn -> deleteBazaar(conn, bazaarId, SQLSchema.SQLite.DELETE_BAZAAR, SQLSchema.SQLite.DELETE_LISTINGS));
    }

    @Override
    public boolean saveAllBazaars(Collection<BazaarData> bazaars) {
        return runTransaction(conn -> batchInsertBazaars(conn, bazaars, SQLSchema.SQLite.INSERT_BAZAAR, SQLSchema.SQLite.INSERT_LISTING));
    }

    @Override
    public Optional<BazaarData> loadBazaar(UUID bazaarId) {
        return runQuery(conn -> fetchBazaarWithListings(conn, bazaarId, SQLSchema.SQLite.SELECT_BAZAAR, SQLSchema.SQLite.SELECT_LISTINGS).orElse(null));
    }

    @Override
    public Optional<List<BazaarData>> loadAllBazaars() {
        return runQuery(conn -> fetchAllBazaarWithListings(conn, SQLSchema.SQLite.SELECT_ALL_BAZAARS, SQLSchema.SQLite.SELECT_LISTINGS));
    }
}