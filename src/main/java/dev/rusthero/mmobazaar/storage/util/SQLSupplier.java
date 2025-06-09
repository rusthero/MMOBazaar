package dev.rusthero.mmobazaar.storage.util;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SQLSupplier<T> {
    T run(Connection conn) throws SQLException;
}