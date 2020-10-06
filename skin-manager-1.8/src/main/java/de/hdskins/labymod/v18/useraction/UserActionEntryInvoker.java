package de.hdskins.labymod.v18.useraction;

import de.hdskins.labymod.test.ReflectionUtils;
import de.hdskins.labymod.test.config.ConfigObject;
import de.hdskins.labymod.test.minecraft.MinecraftAdapter;
import de.hdskins.labymod.test.role.UserRole;
import de.hdskins.labymod.test.utils.ServerHelper;
import net.labymod.main.LabyMod;
import net.labymod.user.gui.UserActionGui;
import net.labymod.user.util.UserActionEntry;

import java.util.List;

public class UserActionEntryInvoker {

    private UserActionEntryInvoker() {
        throw new UnsupportedOperationException();
    }

    public static void addUserAction(MinecraftAdapter minecraftAdapter, ConfigObject configObject) {
        List<UserActionEntry> defaultEntries = ReflectionUtils.get(List.class, UserActionGui.class, LabyMod.getInstance().getUserManager().getUserActionGui(), "defaultEntries");
        if (defaultEntries != null) {
            defaultEntries.add(new ReportUserActionEntry(minecraftAdapter, configObject));
            defaultEntries.add(new ReloadUserActionEntry(minecraftAdapter));

            UserRole userRole = ServerHelper.getSelfRank(configObject, minecraftAdapter);
            if (userRole.isHigherOrEqualThan(UserRole.STAFF)) {
                defaultEntries.add(new DeleteUserSkinActionEntry(minecraftAdapter));
            }
        }
    }
}