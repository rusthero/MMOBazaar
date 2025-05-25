package dev.rusthero.mmobazaar.storage;

import com.zaxxer.hikari.HikariDataSource;
import dev.rusthero.mmobazaar.config.StorageConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class StorageFactory {
    private final JavaPlugin plugin;

    public StorageFactory(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public BazaarStorage create(StorageConfig config) {
        switch (config.getEngine()) {
            case SQLITE -> {
                return new SQLiteStorage(plugin, new HikariDataSource(config.getHikariConfig()));
            }
            case MYSQL, MARIADB -> {
                try {
                    Class.forName("org.mariadb.jdbc.Driver");
                } catch (Exception ignored) {
                    plugin.getLogger().severe("MariaDB JDBC driver not found!");
                }
                return new MySQLStorage(plugin, new HikariDataSource(config.getHikariConfig()));
            }
            case POSTGRES -> {
                try {
                    Class.forName("org.postgresql.Driver");
                } catch (Exception ignored) {
                    plugin.getLogger().severe("PostgreSQL JDBC driver not found!");
                }
                return new PostgreSQLStorage(plugin, new HikariDataSource(config.getHikariConfig()));
            }

            default -> {
                Bukkit.getLogger().severe("[MMOBazaar] Unsupported storage engine.");
                return null;
            }
        }
    }
}