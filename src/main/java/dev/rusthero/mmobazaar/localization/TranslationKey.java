package dev.rusthero.mmobazaar.localization;

public enum TranslationKey {
    LOG_VAULT_NOT_FOUND("log.vault.not-found"),
    ITEM_MARKET_BAG_NAME("item.market-bag.name");

    private final String path;

    TranslationKey(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
