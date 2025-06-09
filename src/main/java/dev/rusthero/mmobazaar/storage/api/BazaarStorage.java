package dev.rusthero.mmobazaar.storage.api;

import dev.rusthero.mmobazaar.bazaar.BazaarData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BazaarStorage {
    boolean init();

    boolean saveBazaar(BazaarData data);

    boolean deleteBazaar(UUID bazaarId);

    boolean saveBazaars(Collection<BazaarData> bazaars);

    Optional<BazaarData> loadBazaar(UUID bazaarId);

    Optional<List<BazaarData>> loadAllBazaars();
}