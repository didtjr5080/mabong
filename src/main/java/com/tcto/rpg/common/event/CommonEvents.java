package com.tcto.rpg.common.event;

import com.tcto.rpg.common.command.TctorpgCommand;
import com.tcto.rpg.common.config.ServerCommonConfig;
import com.tcto.rpg.server.combat.CombatService;
import com.tcto.rpg.server.combat.RpgRuntimeStateManager;
import com.tcto.rpg.server.player.HealthSyncService;
import com.tcto.rpg.server.save.RpgSyncService;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class CommonEvents {
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TctorpgCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        RpgRuntimeStateManager.tickServer(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            HealthSyncService.sync(player);
            RpgSyncService.syncAll(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            RpgRuntimeStateManager.clearFor(player);
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!ServerCommonConfig.ENABLE_CUSTOM_DAMAGE.get()) {
            return;
        }
        CombatService.applyCustomDamage(event);
    }
}

