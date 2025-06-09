package dev.rusthero.mmobazaar.storage.util;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SQLRunnable {
    void run(Connection conn) throws SQLException;
}
