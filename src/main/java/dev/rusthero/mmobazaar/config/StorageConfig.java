package dev.rusthero.mmobazaar.config;

import com.zaxxer.hikari.HikariConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static dev.rusthero.mmobazaar.config.StorageConfig.Engine.SQLITE;

public class StorageConfig {
    public enum Engine {
        SQLITE, MYSQL, MARIADB, POSTGRES;

        public static Engine fromString(String raw) {
            return switch (raw.toLowerCase()) {
                case "sqlite" -> SQLITE;
                case "mysql" -> MYSQL;
                case "mariadb" -> MARIADB;
                case "postgres" -> POSTGRES;
                default -> throw new IllegalArgumentException("Unknown storage engine: " + raw);
            };
        }
    }

    private final JavaPlugin plugin;
    private final Engine engine;
    private final ConfigurationSection section;

    public StorageConfig(JavaPlugin plugin, ConfigurationSection root) {
        String rawEngine = root.getString("engine", "sqlite");
        this.plugin = plugin;
        this.engine = Engine.fromString(rawEngine);
        this.section = root;
    }

    public Engine getEngine() {
        return engine;
    }

    public ConfigurationSection getSubSection() {
        return section.getConfigurationSection(engine.name().toLowerCase());
    }

    public @NotNull HikariConfig getHikariConfig() {
        if (engine == SQLITE) {
            plugin.getDataFolder().mkdirs();
            String filePath = section.getString("file", "data.db");
            File dbFile = new File(plugin.getDataFolder(), filePath);

            HikariConfig hikari = new HikariConfig();
            hikari.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikari.setPoolName("MMOBazaar-SQLite");
            hikari.setMaximumPoolSize(10);
            return hikari;
        }

        var section = getSubSection();
        String host = section.getString("host", "localhost");
        String database = section.getString("database", "mmobazaar");
        String user = section.getString("username", "root");
        String pass = section.getString("password", "");
        boolean useSSL = section.getBoolean("useSSL", false);

        int defaultPort;
        String jdbcPrefix;

        switch (engine) {
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
            default -> throw new IllegalArgumentException("Unsupported engine: " + engine);
        }

        int port = section.getInt("port", defaultPort);

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(jdbcPrefix + host + ":" + port + "/" + database + "?useSSL=" + useSSL);
        hikari.setUsername(user);
        hikari.setPassword(pass);
        hikari.setPoolName("MMOBazaar-" + engine.name());
        hikari.setMaximumPoolSize(10);

        return hikari;
    }
}