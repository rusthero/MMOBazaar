package dev.rusthero.mmobazaar.config;

import com.zaxxer.hikari.HikariConfig;
import dev.rusthero.mmobazaar.storage.SQLDialect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class StorageConfig {
    private final JavaPlugin plugin;
    private final SQLDialect dialect;
    private final ConfigurationSection config;

    public StorageConfig(JavaPlugin plugin, ConfigurationSection root) {
        String strDialect = root.getString("engine", "sqlite");
        this.plugin = plugin;
        this.dialect = SQLDialect.fromString(strDialect);
        this.config = root.getConfigurationSection(dialect.name().toLowerCase());
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public @NotNull HikariConfig getHikariConfig() {
        if (dialect == SQLDialect.SQLITE) {
            plugin.getDataFolder().mkdirs();
            String filePath = config.getString("file", "data.db");
            File dbFile = new File(plugin.getDataFolder(), filePath);

            HikariConfig hikari = new HikariConfig();
            hikari.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikari.setPoolName("MMOBazaar-SQLite");
            hikari.setMaximumPoolSize(10);
            return hikari;
        }

        String host = config.getString("host", "localhost");
        String database = config.getString("database", "mmobazaar");
        String user = config.getString("username", "root");
        String pass = config.getString("password", "");
        boolean useSSL = config.getBoolean("useSSL", false);

        int defaultPort;
        String jdbcPrefix;

        switch (dialect) {
            case POSTGRES -> {
                defaultPort = 5432;
                jdbcPrefix = "jdbc:postgresql://";
            }
            case MARIADB -> {
                defaultPort = 3306;
                jdbcPrefix = "jdbc:mariadb://";
            }
            case MYSQL -> {
                defaultPort = 3306;
                jdbcPrefix = "jdbc:mysql://";
            }
            default -> throw new IllegalArgumentException("Unsupported engine: " + dialect);
        }

        int port = config.getInt("port", defaultPort);

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(jdbcPrefix + host + ":" + port + "/" + database + "?useSSL=" + useSSL);
        hikari.setUsername(user);
        hikari.setPassword(pass);
        hikari.setPoolName("MMOBazaar-" + dialect.name());
        hikari.setMaximumPoolSize(10);

        return hikari;
    }
}