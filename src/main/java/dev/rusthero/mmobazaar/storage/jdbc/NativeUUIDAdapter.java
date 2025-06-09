package dev.rusthero.mmobazaar.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NativeUUIDAdapter implements UUIDAdapter {
    public void set(PreparedStatement ps, int index, UUID uuid) throws SQLException {
        ps.setObject(index, uuid);
    }

    public UUID get(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, UUID.class);
    }
}

