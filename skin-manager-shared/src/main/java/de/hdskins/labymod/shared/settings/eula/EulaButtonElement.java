package de.hdskins.labymod.shared.settings.eula;

import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class EulaButtonElement extends ControlElement {

  public EulaButtonElement(String displayName, IconData iconData) {
    super(displayName, iconData);
  }

  @Override
  public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
    final int height = LabyMod.getInstance().getDrawUtils().getHeight() - 20;
    this.mouseOver = mouseX > 5 && mouseX < 20 && mouseY > height && mouseY < height + 15;
    Minecraft.getMinecraft().getTextureManager().bindTexture(ModTextures.BUTTON_ACCEPT);
    GlStateManager.color(1, 1, 1, 1);
    LabyMod.getInstance().getDrawUtils().drawTexture(5, height, 255, 255, 15, 15, 1);
  }
}
