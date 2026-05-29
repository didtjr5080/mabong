package com.tcto.rpg;

import com.tcto.rpg.common.config.ModConfigs;
import com.tcto.rpg.common.event.ModEvents;
import com.tcto.rpg.common.network.ModNetwork;
import com.tcto.rpg.admin.AdminPermissionService;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(TCToRPG.MODID)
public class TCToRPG {
    public static final String MODID = "tctorpg";

    public TCToRPG(IEventBus modBus, ModContainer modContainer) {
        ModConfigs.register(modContainer);
        AdminPermissionService.load();
        ModNetwork.register(modBus);
        ModEvents.register(modBus);
    }
}

