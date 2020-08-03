package de.hdskins.labymod.shared.mappings.v112;

import de.hdskins.labymod.shared.mappings.Mappings;

public class V112Mappings implements Mappings {

    @Override
    public String[] getSkinManagerMappings() {
        return new String[]{"aP", "skinManager", "field_152350_aA"};
    }

    @Override
    public String[] getSkinCacheDirMappings() {
        return new String[]{"skinCacheDir", "field_152796_d", "c"};
    }

    @Override
    public String getVersion() {
        return "1.12.2-14.23.0.2512";
    }

    @Override
    public String getMappingsVersion() {
        return "1.12_snapshot_20171003";
    }
}
