package net.jrdemiurge.skyarena.config;

import net.jrdemiurge.skyarena.SkyArena;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SkyArena.MOD_ID)
public class ConfigHandler {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        SkyArenaConfig.loadConfig();
    }
}
