package dev.rusthero.mmobazaar.localization;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LocalizationManager {
    private final FileConfiguration translations;

    public LocalizationManager(JavaPlugin plugin, String locale) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang/" + locale + ".yml", false);
        }
        translations = YamlConfiguration.loadConfiguration(langFile);

        // Load default translations from JAR in case of missing/corrupted language file
        FileConfiguration defaultConfig = TranslationDefaults.loadFromJar(plugin, locale);
        translations.setDefaults(defaultConfig);
        translations.options().copyDefaults(true);
    }

    public String get(TranslationKey key, Object... args) {
        String message = translations.getString(key.getPath(), "§cMissing translation key: " + key.name());
        return format(message, args);
    }

    private String format(String message, Object... args) {
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i].toString());
        }
        return ChatColor.translateAlternateColorCodes('§', message);
    }
}
