package dev.rusthero.mmobazaar.storage.util.functional;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SQLRunnable {
    void run(Connection conn) throws SQLException;
}
