package net.jrdemiurge.skyarena.mixin;

import net.jrdemiurge.skyarena.util.BossBarHideZones;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerBossEvent.class)
public class ServerBossEventMixin {

    @Inject(method = "addPlayer", at = @At("HEAD"), cancellable = true)
    private void cancelIfNearAltar(ServerPlayer pPlayer, CallbackInfo ci) {
        BlockPos playerPos = pPlayer.blockPosition();
        if (BossBarHideZones.isInZone(pPlayer.level() ,playerPos)) {
            ci.cancel();
        }
    }
}
