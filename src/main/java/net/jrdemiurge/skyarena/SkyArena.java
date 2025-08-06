package net.jrdemiurge.skyarena;

import com.mojang.logging.LogUtils;
import net.jrdemiurge.skyarena.block.ModBlocks;
import net.jrdemiurge.skyarena.block.entity.AltarBlockEntity;
import net.jrdemiurge.skyarena.block.entity.ModBlockEntity;
import net.jrdemiurge.skyarena.item.ModCreativeTabs;
import net.jrdemiurge.skyarena.item.ModItems;
import net.jrdemiurge.skyarena.item.custom.RewardKeyItem;
import net.jrdemiurge.skyarena.triggers.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            BlockPos altarPos = AltarBlockEntity.getAltarPosForPlayer(player);

            if (altarPos != null) {
                Level level = player.level();

                BlockEntity blockEntity = level.getBlockEntity(altarPos);
                if (blockEntity instanceof AltarBlockEntity altarEntity) {
                    altarEntity.setPlayerDeath(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        BlockPos pos = event.getPos();
        if (RewardKeyItem.keyedChests.contains(pos)) {
            event.setCanceled(true);
            RewardKeyItem.keyedChests.remove(pos);
        }
    }
}
