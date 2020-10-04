package de.hdskins.labymod.v18.listener;

import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import net.labymod.main.LabyMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.UUID;

public class TickListener {

    public TickListener(MinecraftAdapter minecraftAdapter) {
        this.minecraftAdapter = minecraftAdapter;
    }

    private final MinecraftAdapter minecraftAdapter;

    private UUID currentUniqueId;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (this.currentUniqueId == null) {
            this.currentUniqueId = LabyMod.getInstance().getPlayerUUID();
            return;
        }

        if (LabyMod.getInstance().getPlayerUUID().equals(this.currentUniqueId)) {
            return;
        }
        // account changed using the account manager

        this.minecraftAdapter.updateSlimState();
        this.currentUniqueId = LabyMod.getInstance().getPlayerUUID();
    }
}