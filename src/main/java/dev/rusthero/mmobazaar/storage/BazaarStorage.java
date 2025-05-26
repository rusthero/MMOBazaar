package dev.rusthero.mmobazaar.storage;

import dev.rusthero.mmobazaar.bazaar.BazaarData;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface BazaarStorage {
    void init();

    boolean saveBazaar(BazaarData data);

    boolean deleteBazaar(UUID bazaarId);

    boolean saveAll(Collection<BazaarData> bazaars);

    Optional<BazaarData> loadBazaar(UUID bazaarId);

    Collection<BazaarData> loadAll();
}