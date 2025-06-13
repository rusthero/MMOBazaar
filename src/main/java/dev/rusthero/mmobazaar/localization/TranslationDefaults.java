package dev.rusthero.mmobazaar.localization;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import java.util.logging.Level;

public class TranslationDefaults {
    public static FileConfiguration loadFromJar(JavaPlugin plugin, String locale) {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(plugin.getResource("lang/" + locale + ".yml")))) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[MMOBazaar] Could not load default translation file: falling back to en_US.yml");

            // fallback to en_US.yml inside JAR
            try {
                Reader fallbackReader = new InputStreamReader(Objects.requireNonNull(plugin.getResource("lang/en_US.yml")));
                return YamlConfiguration.loadConfiguration(fallbackReader);
            } catch (Exception fallbackError) {
                plugin.getLogger().log(Level.SEVERE, "[MMOBazaar] FATAL: Could not load fallback en_US.yml from JAR!");
                return new YamlConfiguration(); // blank config to avoid crash
            }
        }
    }
}