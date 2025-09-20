package net.jrdemiurge.skyarena.mixin;

import net.jrdemiurge.skyarena.util.FullProtectionZones;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void onSetBlock(BlockPos pos, BlockState state, int flags, int recursion, CallbackInfoReturnable<Boolean> cir) {
        if(!forge_sky_arena_1_20_1_47_3_0_mdk$canSetBlock(pos,state,((Level)(Object)this))) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Unique
    private static boolean forge_sky_arena_1_20_1_47_3_0_mdk$canSetBlock(BlockPos pos, BlockState state, Level level){
        if(FullProtectionZones.isInZone(level, pos)) {
            return level.getBlockState(pos).is(state.getBlock());
        }
        return true;
    }

    @Inject(
            method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void onDestroyBlock(BlockPos pos, boolean drop, Entity entity, int recursion, CallbackInfoReturnable<Boolean> cir) {
        if(FullProtectionZones.isInZone((Level)(Object)this, pos)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
