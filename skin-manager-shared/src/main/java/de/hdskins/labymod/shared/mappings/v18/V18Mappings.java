package de.hdskins.labymod.shared.mappings.v18;

import de.hdskins.labymod.shared.mappings.Mappings;

public class V18Mappings implements Mappings {

    @Override
    public String[] getSkinManagerMappings() {
        return new String[]{"aL", "skinManager", "field_152350_aA"};
    }

    @Override
    public String[] getSkinCacheDirMappings() {
        return new String[]{"c", "skinCacheDir", "field_152796_d"};
    }

    @Override
    public String getVersion() {
        return "1.8.9-11.15.1.1855";
    }

    @Override
    public String getMappingsVersion() {
        return "1.8.8_stable_20";
    }
}
