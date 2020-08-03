package de.hdskins.labymod.shared.mappings;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.mappings.v112.V112Mappings;
import de.hdskins.labymod.shared.mappings.v18.V18Mappings;

public enum HandledMappings {

    V1_8(
            Constants.aboutMcVersion.startsWith("1.8"),
            new V18Mappings()
    ),

    V1_12(
            Constants.aboutMcVersion.startsWith("1.12"),
            new V112Mappings()
    );

    private final boolean isLoaded;
    private final Mappings mappings;

    HandledMappings(boolean isLoaded, Mappings mappings) {
        this.isLoaded = isLoaded;
        this.mappings = mappings;
    }

    public Mappings getMappings() {
        return mappings;
    }

    public static HandledMappings getLoadedMappings() {
        for (HandledMappings value : values()) {
            if (value.isLoaded) {
                return value;
            }
        }

        return null;
    }
}
