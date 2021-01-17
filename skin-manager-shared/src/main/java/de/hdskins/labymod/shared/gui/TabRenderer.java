package de.hdskins.labymod.shared.gui;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.manager.HDSkinManager;
import de.hdskins.labymod.shared.manager.SkinHashWrapper;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.SkinManager;

import java.util.Map;
import java.util.UUID;

public class TabRenderer {

  private static final int ICON_SIZE = 8;

  public static void renderTabOverlay(int center, int xOffset) {
    if (LabyMod.getSettings().revealFamiliarUsers && LabyMod.getSettings().revealFamiliarUsersPercentage) {
      SkinManager manager = Minecraft.getMinecraft().getSkinManager();
      if (!(manager instanceof HDSkinManager)) {
        return;
      }
      HDSkinManager hdManager = (HDSkinManager) manager;

      NetHandlerPlayClient connection = hdManager.getClientConnection();
      if (connection == null) {
        return;
      }

      int users = countHDSkinUsers(hdManager, connection);
      int total = connection.getPlayerInfoMap().size();

      int x = center - xOffset;

      Constants.getAddonInfo().getAddonElement().drawIcon(x, 1, ICON_SIZE, ICON_SIZE);

      int percent = (int) (total == 0 ? 0 : Math.round(100D / total * users));
      String displayString = "§7" + users + "§8/§7" + total + " §a" + percent + "%";
      LabyMod.getInstance().getDrawUtils().drawString(displayString, ICON_SIZE + 2 + x, 3, 0.7);
    }
  }

  private static int countHDSkinUsers(HDSkinManager manager, NetHandlerPlayClient connection) {
    int amount = 0;

    for (Map.Entry<UUID, SkinHashWrapper> entry : manager.getCachedSkins().entrySet()) {
      if (entry.getValue().hasSkin() && connection.getPlayerInfo(entry.getKey()) != null) {
        ++amount;
      }
    }

    return amount;
  }

}
