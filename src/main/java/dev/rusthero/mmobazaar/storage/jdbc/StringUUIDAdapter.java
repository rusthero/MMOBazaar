package dev.rusthero.mmobazaar.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class StringUUIDAdapter implements UUIDAdapter {
    public void set(PreparedStatement ps, int index, UUID uuid) throws SQLException {
        ps.setString(index, uuid.toString());
    }

    public UUID get(ResultSet rs, String column) throws SQLException {
        String str = rs.getString(column);
        return str != null ? UUID.fromString(str) : null;
    }
}

