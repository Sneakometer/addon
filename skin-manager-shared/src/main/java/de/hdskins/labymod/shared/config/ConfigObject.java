package de.hdskins.labymod.shared.config;

public interface ConfigObject {

    String getServerUrl();

    String getToken();

    void setServerUrl(String url);

    void setToken(String token);
}
