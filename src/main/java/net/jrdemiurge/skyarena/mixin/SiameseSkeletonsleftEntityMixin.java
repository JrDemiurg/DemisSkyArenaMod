package net.jrdemiurge.skyarena.mixin;

import net.mcreator.borninchaosv.entity.SiameseSkeletonsleftEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(SiameseSkeletonsleftEntity.class)
public class SiameseSkeletonsleftEntityMixin extends Monster {

    public SiameseSkeletonsleftEntityMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "dropCustomDeathLoot", at = @At("HEAD"), cancellable = true, require = 0)
    private void cancelLootIfInArenaTeam(DamageSource source, int looting, boolean recentlyHitIn, CallbackInfo ci) {
        if (this.getTeam() != null && "summonedByArenaWithoutLoot".equals(this.getTeam().getName())) {
            ci.cancel();
        }
    }
}
