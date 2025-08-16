package net.jrdemiurge.skyarena.event;

import net.jrdemiurge.skyarena.SkyArena;
import net.jrdemiurge.skyarena.block.entity.AltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SkyArena.MOD_ID)
public class PlayerDeathHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            BlockPos altarPos = AltarBlockEntity.getAltarPosForPlayer(player);

            if (altarPos != null) {
                Level level = player.level();

                BlockEntity blockEntity = level.getBlockEntity(altarPos);
                if (blockEntity instanceof AltarBlockEntity altarEntity &&
                        altarEntity.getActivatingPlayer() == player) {
                    altarEntity.setPlayerDeath(true);
                }
            }
        }
    }
}
