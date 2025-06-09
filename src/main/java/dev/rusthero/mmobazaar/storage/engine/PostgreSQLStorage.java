package dev.rusthero.mmobazaar.storage.engine;

import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.storage.schema.SQLSchema;
import dev.rusthero.mmobazaar.storage.jdbc.UUIDAdapter;

import javax.sql.DataSource;
import java.util.*;
import java.util.logging.Logger;

public class PostgreSQLStorage extends SQLStorage {
    public PostgreSQLStorage(DataSource dataSource, Logger logger) {
        super(dataSource, UUIDAdapter.postgresAdapter(), logger);
    }

    @Override
    public boolean init() {
        return runTransaction(conn -> executeSchema(conn, SQLSchema.PostgreSQL.CREATE_BAZAARS, SQLSchema.PostgreSQL.CREATE_LISTINGS));
    }

    @Override
    public boolean saveBazaar(BazaarData data) {
        return runTransaction(conn -> upsertBazaarWithListings(conn, data, SQLSchema.PostgreSQL.INSERT_BAZAAR, SQLSchema.PostgreSQL.INSERT_LISTING));
    }

    @Override
    public boolean deleteBazaar(UUID bazaarId) {
        return runTransaction(conn -> deleteBazaar(conn, bazaarId, SQLSchema.PostgreSQL.DELETE_BAZAAR, SQLSchema.PostgreSQL.DELETE_LISTINGS));
    }

    @Override
    public boolean saveAllBazaars(Collection<BazaarData> bazaars) {
        return runTransaction(conn -> batchInsertBazaars(conn, bazaars, SQLSchema.PostgreSQL.INSERT_BAZAAR, SQLSchema.PostgreSQL.INSERT_BAZAAR));
    }

    @Override
    public Optional<BazaarData> loadBazaar(UUID bazaarId) {
        return runQuery(conn -> fetchBazaarWithListings(conn, bazaarId, SQLSchema.PostgreSQL.SELECT_BAZAAR, SQLSchema.PostgreSQL.SELECT_LISTINGS).orElse(null));
    }

    @Override
    public Optional<List<BazaarData>> loadAllBazaars() {
        return runQuery(conn -> fetchAllBazaarWithListings(conn, SQLSchema.PostgreSQL.SELECT_ALL_BAZAARS, SQLSchema.PostgreSQL.SELECT_LISTINGS));
    }
}