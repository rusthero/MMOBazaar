package dev.rusthero.mmobazaar.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public interface UUIDAdapter {
    void set(PreparedStatement ps, int index, UUID uuid) throws SQLException;
    UUID get(ResultSet rs, String column) throws SQLException;

    static UUIDAdapter defaultAdapter() {
        return new StringUUIDAdapter(); // fallback for SQLite/MySQL
    }

    static UUIDAdapter postgresAdapter() {
        return new NativeUUIDAdapter(); // for PostgreSQL
    }
}
