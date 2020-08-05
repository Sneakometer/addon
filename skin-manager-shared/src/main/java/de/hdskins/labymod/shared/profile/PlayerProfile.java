package de.hdskins.labymod.shared.profile;

import java.util.UUID;

public class PlayerProfile {

    private final String name;
    private final UUID uniqueId;

    public PlayerProfile(String name, UUID uniqueId) {
        this.name = name;
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }
}
