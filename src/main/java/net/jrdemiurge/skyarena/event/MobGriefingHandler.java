package net.jrdemiurge.skyarena.event;

import net.jrdemiurge.skyarena.SkyArena;
import net.jrdemiurge.skyarena.block.entity.AltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SkyArena.MOD_ID)
public class MobGriefingHandler {

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        Vec3 explosionPos = event.getExplosion().getPosition();
        BlockPos center = BlockPos.containing(explosionPos);

        if (AltarBlockEntity.isNearProtectedAltar(center)) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent
    public static void onMobGriefingCheck(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (!level.isClientSide && AltarBlockEntity.isNearProtectedAltar(entity.blockPosition())) {
            event.setResult(Event.Result.DENY);
        }
    }
}
