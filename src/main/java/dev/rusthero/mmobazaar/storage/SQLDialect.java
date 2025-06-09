package dev.rusthero.mmobazaar.storage;

public enum SQLDialect {
    SQLITE, MYSQL, MARIADB, POSTGRES;

    public static SQLDialect fromString(String raw) {
        return switch (raw.toLowerCase()) {
            case "sqlite" -> SQLITE;
            case "mysql" -> MYSQL;
            case "mariadb" -> MARIADB;
            case "postgres" -> POSTGRES;
            default -> throw new IllegalArgumentException("Unknown storage dialect: " + raw);
        };
    }
}
