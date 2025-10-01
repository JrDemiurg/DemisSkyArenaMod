package net.jrdemiurge.skyarena;

import com.mojang.logging.LogUtils;
import net.jrdemiurge.skyarena.block.ModBlocks;
import net.jrdemiurge.skyarena.block.entity.ModBlockEntity;
import net.jrdemiurge.skyarena.item.ModCreativeTabs;
import net.jrdemiurge.skyarena.item.ModItems;
import net.jrdemiurge.skyarena.triggers.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SkyArena.MOD_ID)
public class SkyArena {
    public static final String MOD_ID = "skyarena";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SkyArena() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntity.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Registering triggers...");
        CriteriaTriggers.register(UseAltarBattle.INSTANCE);
        CriteriaTriggers.register(UseMusicDisk.INSTANCE);
        CriteriaTriggers.register(UseStick.INSTANCE);
        CriteriaTriggers.register(UseNetheriteIngot.INSTANCE);
        CriteriaTriggers.register(DifficultyLevel1.INSTANCE);
        CriteriaTriggers.register(DifficultyLevel5.INSTANCE);
        CriteriaTriggers.register(DifficultyLevel10.INSTANCE);
        CriteriaTriggers.register(DifficultyLevel20.INSTANCE);
        CriteriaTriggers.register(DifficultyLevel50.INSTANCE);
        CriteriaTriggers.register(DifficultyLevel100.INSTANCE);
    }
}
