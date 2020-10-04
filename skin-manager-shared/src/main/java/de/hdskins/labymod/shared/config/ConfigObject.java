package de.hdskins.labymod.shared.config;

public interface ConfigObject {

    String getServerUrl();

    void setShowAllSkins(boolean enabled);

    boolean shouldShowAllSkins();

}
