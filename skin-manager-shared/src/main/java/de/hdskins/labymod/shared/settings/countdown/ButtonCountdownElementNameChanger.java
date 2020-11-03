package de.hdskins.labymod.shared.settings.countdown;

import com.google.common.primitives.Longs;
import de.hdskins.labymod.shared.settings.element.elements.ButtonElement;
import net.labymod.core.LabyModCore;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@SuppressWarnings("UnstableApiUsage")
public class ButtonCountdownElementNameChanger extends DefaultCountdownElementNameChanger {

    private final ButtonElement buttonElement;
    private String previousName;
    private int stringWidth;

    public ButtonCountdownElementNameChanger(ButtonElement targetElement) {
        super(targetElement);
        this.buttonElement = targetElement;
        this.setPreviousName(this.buttonElement.getText());
    }

    @Override
    public void accept(Long remainingTime) {
        if (Longs.tryParse(this.buttonElement.getText().replace("§c§l", "")) == null) {
            this.setPreviousName(this.buttonElement.getText());
        }

        if (remainingTime <= 0) {
            this.buttonElement.setSettingEnabled(true);
            this.buttonElement.setText(this.previousName);
        } else {
            this.buttonElement.setText("§c§l" + remainingTime, this.stringWidth);
        }
    }

    private void setPreviousName(String previousName) {
        this.previousName = previousName;
        this.stringWidth = LabyModCore.getMinecraft().getFontRenderer().getStringWidth(previousName);
    }
}
