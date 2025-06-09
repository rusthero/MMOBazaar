package dev.rusthero.mmobazaar;

import dev.rusthero.mmobazaar.api.MMOBazaarAPI;
import dev.rusthero.mmobazaar.config.BazaarConfig;
import dev.rusthero.mmobazaar.economy.VaultHook;
import dev.rusthero.mmobazaar.bazaar.BazaarManager;
import dev.rusthero.mmobazaar.gui.GUIManager;
import dev.rusthero.mmobazaar.gui.session.GUISessionManager;
import dev.rusthero.mmobazaar.item.BazaarBagFactory;
import dev.rusthero.mmobazaar.storage.api.BazaarStorage;

public class MMOBazaarContext {
    public final MMOBazaar plugin;
    public final VaultHook vaultHook;
    public final BazaarManager bazaarManager;
    public final BazaarBagFactory bagFactory;
    public final MMOBazaarAPI api;
    public final GUISessionManager guiSessions;
    public final GUIManager gui;
    public final BazaarConfig config;
    public final BazaarStorage storage;

    public MMOBazaarContext(MMOBazaar plugin, VaultHook vaultHook, BazaarManager manager, BazaarBagFactory bagFactory, MMOBazaarAPI api, GUISessionManager guiSessions, GUIManager gui, BazaarConfig config, BazaarStorage storage) {
        this.plugin = plugin;
        this.vaultHook = vaultHook;
        this.bazaarManager = manager;
        this.bagFactory = bagFactory;
        this.api = api;
        this.guiSessions = guiSessions;
        this.gui = gui;
        this.config = config;
        this.storage = storage;
    }
}
