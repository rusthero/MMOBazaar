package dev.rusthero.mmobazaar.gui.session;

import java.util.*;

public abstract class GUISession<T> {
    protected final Map<UUID, T> sessions = new HashMap<>();

    public void set(UUID playerId, T gui) {
        sessions.put(playerId, gui);
    }

    public Optional<T> get(UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    public void remove(UUID playerId) {
        sessions.remove(playerId);
    }

    public Collection<Map.Entry<UUID, T>> entries() {
        return sessions.entrySet();
    }
}