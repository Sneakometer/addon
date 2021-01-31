package de.hdskins.labymod.shared.gui;

import de.hdskins.labymod.shared.Constants;
import de.hdskins.labymod.shared.manager.HDSkinManager;
import de.hdskins.labymod.shared.manager.SkinHashWrapper;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.SkinManager;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class TabRenderer {

  private static final int ICON_SIZE = 8;

  public static void renderTabOverlay(int center, int xOffset) {
    if (LabyMod.getSettings().revealFamiliarUsers && LabyMod.getSettings().revealFamiliarUsersPercentage) {
      final SkinManager manager = Minecraft.getMinecraft().getSkinManager();
      if (manager instanceof HDSkinManager) {
        final HDSkinManager hdManager = (HDSkinManager) manager;
        final NetHandlerPlayClient connection = hdManager.getClientConnection();

        if (connection != null) {
          final int users = countHDSkinUsers(hdManager, connection);
          final int total = connection.getPlayerInfoMap().size();
          final int x = center - xOffset;

          final int percent = (int) (total == 0 ? 0 : Math.round(100D / total * users));
          final String displayString = "§7" + users + "§8/§7" + total + " §a" + percent + "%";
          final int size = LabyMod.getInstance().getDrawUtils().getStringWidth(displayString) + 2 + x;

          if (size < center) {
            Constants.getAddonInfo().getAddonElement().drawIcon(x, 1, ICON_SIZE, ICON_SIZE);
            LabyMod.getInstance().getDrawUtils().drawString(displayString, ICON_SIZE + 2 + x, 3, 0.7);
          }
        }
      }
    }
  }

  private static int countHDSkinUsers(HDSkinManager manager, NetHandlerPlayClient connection) {
    int amount = 0;
    for (NetworkPlayerInfo info : connection.getPlayerInfoMap()) {
      SkinHashWrapper wrapper = manager.getCachedSkins().getIfPresent(info.getGameProfile().getId());
      if (wrapper != null && wrapper.hasSkin()) {
        ++amount;
      }
    }

    return amount;
  }
}
