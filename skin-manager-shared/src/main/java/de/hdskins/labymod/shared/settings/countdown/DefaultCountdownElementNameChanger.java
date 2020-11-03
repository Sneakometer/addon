package de.hdskins.labymod.shared.settings.countdown;

import net.labymod.settings.elements.ControlElement;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault
public class DefaultCountdownElementNameChanger implements Consumer<Long> {

    protected static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\(§c§l([0-9]+)§f\\)");
    protected final ControlElement targetElement;

    public DefaultCountdownElementNameChanger(ControlElement targetElement) {
        this.targetElement = targetElement;
        this.targetElement.setSettingEnabled(false);
    }

    @Override
    public void accept(Long remainingTime) {
        Matcher matcher = NUMBER_PATTERN.matcher(this.targetElement.getDisplayName());
        if (matcher.matches()) {
            if (remainingTime <= 0) {
                this.targetElement.setSettingEnabled(true);
                this.targetElement.setDisplayName(this.targetElement.getDisplayName().replaceFirst(
                    "(?s)(.*)\\(§c§l" + matcher.group(1) + "§f\\)",
                    "$1"
                ));
            } else {
                this.targetElement.setDisplayName(this.targetElement.getDisplayName().replaceFirst(
                    "(?s)(.*)" + matcher.group(1),
                    "$1" + remainingTime
                ));
            }
        } else {
            if (remainingTime <= 0) {
                this.targetElement.setSettingEnabled(true);
            } else {
                final String displayName = this.targetElement.getDisplayName();
                this.targetElement.setDisplayName((displayName.endsWith(" ") ? displayName : displayName + " ") + "(§c§l" + remainingTime + "§f)");
            }
        }
    }
}
