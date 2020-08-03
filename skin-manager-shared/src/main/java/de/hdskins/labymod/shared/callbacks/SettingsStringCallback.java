package de.hdskins.labymod.shared.callbacks;

import net.labymod.utils.Consumer;

public class SettingsStringCallback implements Consumer<String> {

    private final Consumer<String> callback;

    public SettingsStringCallback(Consumer<String> callback) {
        this.callback = callback;
    }

    @Override
    public void accept(String s) {
        if (s.trim().isEmpty()) {
            this.callback.accept(null);
        } else {
            this.callback.accept(s.trim());
        }
    }
}
