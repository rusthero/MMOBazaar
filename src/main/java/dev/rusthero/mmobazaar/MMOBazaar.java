package dev.rusthero.mmobazaar;

import dev.rusthero.mmobazaar.api.MMOBazaarAPI;
import dev.rusthero.mmobazaar.bazaar.BazaarData;
import dev.rusthero.mmobazaar.bazaar.BazaarManager;
import dev.rusthero.mmobazaar.commands.MMOBazaarCommand;
import dev.rusthero.mmobazaar.config.BazaarConfig;
import dev.rusthero.mmobazaar.config.StorageConfig;
import dev.rusthero.mmobazaar.economy.VaultHook;
import dev.rusthero.mmobazaar.gui.GUISessionManager;
import dev.rusthero.mmobazaar.item.BazaarBagFactory;
import dev.rusthero.mmobazaar.listener.BazaarBagUseListener;
import dev.rusthero.mmobazaar.listener.BazaarGUIListener;
import dev.rusthero.mmobazaar.listener.BazaarInteractionListener;
import dev.rusthero.mmobazaar.storage.api.BazaarStorage;
import dev.rusthero.mmobazaar.storage.util.SQLStorageFactory;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Objects;

public class MMOBazaar extends JavaPlugin {
    public static NamespacedKey BAZAAR_ID_KEY;

    private MMOBazaarContext context;

    @Override
    public void onEnable() {
        BAZAAR_ID_KEY = new NamespacedKey(this, "bazaar-id");

        // Vault Integration
        final VaultHook vaultHook = new VaultHook();
        if (!vaultHook.setup()) {
            getLogger().severe("Vault not found or economy provider missing. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Configuration
        final BazaarConfig config;
        saveDefaultConfig();
        config = new BazaarConfig(getConfig());
        ConfigurationSection storageSection = getConfig().getConfigurationSection("storage");
        if (storageSection == null) {
            getLogger().severe("Storage section not found in config file. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        final StorageConfig storageConfig = new StorageConfig(this, storageSection);

        // Load storage
        SQLStorageFactory storageFactory = new SQLStorageFactory(getLogger());
        BazaarStorage storage = null;
        try {
            storage = storageFactory.create(storageConfig);
        } catch (ClassNotFoundException e) {
            getLogger().severe("Loading JDBC (database) classes failed");
        }
        if (storage == null) {
            getLogger().severe("Disabling plugin due to missing storage backend.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        storage.init();

        // Setup MMOBazaar
        final BazaarManager bazaarManager = new BazaarManager(this, storage);

        // Load bazaars from storage
        storage.loadAllBazaars().ifPresent(loadedBazaars -> {
            getLogger().info("Loaded " + loadedBazaars.size() + " bazaars from database.");
            loadedBazaars.forEach(bazaarManager::registerBazaar);
        });

        final BazaarBagFactory bagFactory = new BazaarBagFactory(config.getCreationFee());
        final MMOBazaarAPI api = new MMOBazaarAPI(bagFactory);
        final GUISessionManager guiSessions = new GUISessionManager();

        // Setup context bundle for easier access to MMOBazaar
        context = new MMOBazaarContext(this, vaultHook, bazaarManager, bagFactory, api, guiSessions, config, storage);

        // Register listeners
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BazaarBagUseListener(context), this);
        pm.registerEvents(new BazaarInteractionListener(context), this);
        pm.registerEvents(new BazaarGUIListener(context), this);

        // Set command executor
        Objects.requireNonNull(getCommand("mmobazaar")).setExecutor(new MMOBazaarCommand(api));

        getLogger().info("MMOBazaar enabled.");
    }

    @Override
    public void onDisable() {
        if (this.context != null) {
            // Close all GUIs in case it is a reload to prevent item dupe. Rare but dangerous one
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (context.guiSessions.getOwnerGUI(player.getUniqueId()).isPresent()
                        || context.guiSessions.getConfirmingGUI(player.getUniqueId()).isPresent()
                        || context.guiSessions.getCustomerGUI(player.getUniqueId()).isPresent()) {
                    player.closeInventory();
                    player.sendMessage("§cBazaar GUI was forcibly closed due to reload.");
                }
            }
        }

        // Save all bazaars in case
        if (this.context != null && this.context.storage != null) {
            try {
                Collection<BazaarData> bazaars = this.context.bazaarManager.getAllBazaars();
                this.context.storage.saveAllBazaars(bazaars);
                getLogger().info("Saved all " + bazaars.size() + " bazaars to storage.");
            } catch (Exception e) {
                getLogger().severe("Failed to save bazaars on shutdown: " + e.getMessage());
            }
        }

        getLogger().info("MMOBazaar disabled.");
    }
}
