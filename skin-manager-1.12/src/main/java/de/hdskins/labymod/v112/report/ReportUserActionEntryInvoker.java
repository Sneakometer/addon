package de.hdskins.labymod.v112.report;

import de.hdskins.labymod.shared.ReflectionUtils;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.role.UserRole;
import de.hdskins.labymod.shared.utils.ServerHelper;
import net.labymod.main.LabyMod;
import net.labymod.user.gui.UserActionGui;
import net.labymod.user.util.UserActionEntry;

import java.util.List;

public class ReportUserActionEntryInvoker {

    private ReportUserActionEntryInvoker() {
        throw new UnsupportedOperationException();
    }

    public static void addUserAction(MinecraftAdapter minecraftAdapter, ConfigObject configObject) {
        List<UserActionEntry> defaultEntries = ReflectionUtils.get(List.class, UserActionGui.class, LabyMod.getInstance().getUserManager().getUserActionGui(), "defaultEntries");
        if (defaultEntries != null) {
            defaultEntries.add(new ReportUserActionEntry(minecraftAdapter, configObject));

            UserRole userRole = ServerHelper.getSelfRank(configObject, minecraftAdapter);
            if (userRole.isHigherOrEqualThan(UserRole.STAFF)) {
                defaultEntries.add(new DeleteUserSkinActionEntry(minecraftAdapter));
            }
        }
    }
}
