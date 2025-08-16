package net.jrdemiurge.skyarena.event;

import net.jrdemiurge.skyarena.SkyArena;
import net.jrdemiurge.skyarena.item.custom.RewardKeyItem;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SkyArena.MOD_ID)
public class RightClickBlockHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        BlockPos pos = event.getPos();
        if (RewardKeyItem.keyedChests.contains(pos)) {
            event.setCanceled(true);
            RewardKeyItem.keyedChests.remove(pos);
        }
    }
}
