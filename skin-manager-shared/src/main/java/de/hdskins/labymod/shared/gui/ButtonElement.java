package de.hdskins.labymod.shared.gui;

import net.labymod.settings.elements.ControlElement;

public abstract class ButtonElement extends ControlElement {

    public static boolean buttonsEnabledByDefault = true;

    protected Runnable runnable;
    protected boolean enabled;

    public ButtonElement(String displayName, ControlElement.IconData iconData) {
        super(displayName, iconData);
        this.enabled = buttonsEnabledByDefault;
    }

    public abstract String getText();

    public abstract void setText(String text);

    public void setClickListener(Runnable handler) {
        this.runnable = handler;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
