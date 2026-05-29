package com.tcto.rpg.common.event;

import com.tcto.rpg.common.data.RpgDataManager;
import com.tcto.rpg.client.keybind.ClientKeybinds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

public final class ModEvents {
    private ModEvents() {
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RpgDataManager::onAddReloadListener);
        NeoForge.EVENT_BUS.register(new CommonEvents());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(ClientKeybinds::register);
            NeoForge.EVENT_BUS.register(new ClientEvents());
        }
    }
}

