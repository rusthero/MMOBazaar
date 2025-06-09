package dev.rusthero.mmobazaar.storage.engine;

import com.zaxxer.hikari.HikariDataSource;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.storage.schema.SQLSchema;
import dev.rusthero.mmobazaar.storage.jdbc.UUIDAdapter;

import java.util.*;
import java.util.logging.Logger;

public class MySQLStorage extends SQLStorage {
    public MySQLStorage(HikariDataSource dataSource, Logger logger) {
        super(dataSource, UUIDAdapter.defaultAdapter(), logger);
    }

    @Override
    public boolean init() {
        return runTransaction(conn -> executeSchema(conn, SQLSchema.MySQL.CREATE_BAZAARS, SQLSchema.MySQL.CREATE_LISTINGS));
    }

    @Override
    public boolean saveBazaar(BazaarData data) {
        return runTransaction(conn -> upsertBazaarWithListings(conn, data, SQLSchema.MySQL.INSERT_BAZAAR, SQLSchema.MySQL.INSERT_LISTING));
    }

    @Override
    public boolean deleteBazaar(UUID bazaarId) {
        return runTransaction(conn -> deleteBazaar(conn, bazaarId, SQLSchema.MySQL.DELETE_BAZAAR, SQLSchema.MySQL.DELETE_LISTINGS));
    }

    @Override
    public boolean saveAllBazaars(Collection<BazaarData> bazaars) {
        return runTransaction(conn -> batchInsertBazaars(conn, bazaars, SQLSchema.MySQL.INSERT_BAZAAR, SQLSchema.MySQL.INSERT_BAZAAR));
    }

    @Override
    public Optional<BazaarData> loadBazaar(UUID bazaarId) {
        return runQuery(conn -> fetchBazaarWithListings(conn, bazaarId, SQLSchema.MySQL.SELECT_BAZAAR, SQLSchema.MySQL.SELECT_LISTINGS).orElse(null));
    }

    @Override
    public Optional<List<BazaarData>> loadAllBazaars() {
        return runQuery(conn -> fetchAllBazaarWithListings(conn, SQLSchema.MySQL.SELECT_ALL_BAZAARS, SQLSchema.MySQL.SELECT_LISTINGS));
    }
}