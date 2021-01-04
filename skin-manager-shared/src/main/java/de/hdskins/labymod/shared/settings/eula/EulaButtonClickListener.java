package de.hdskins.labymod.shared.settings.eula;

import de.hdskins.labymod.shared.addon.AddonContext;
import de.hdskins.labymod.shared.gui.AcceptRejectGuiScreen;
import de.hdskins.labymod.shared.utils.GuidelineUtils;

public class EulaButtonClickListener implements Runnable {

  private final AddonContext addonContext;

  public EulaButtonClickListener(AddonContext addonContext) {
    this.addonContext = addonContext;
  }

  @Override
  public void run() {
    AcceptRejectGuiScreen.newScreen(
      "Accept", "Decline",
      GuidelineUtils.readGuidelines(this.addonContext.getAddonConfig().getGuidelinesUrl()),
      (screen, accepted) -> {
        this.addonContext.getAddonConfig().setGuidelinesAccepted(accepted);
        screen.returnBack();
      }
    ).requestFocus();
  }
}
