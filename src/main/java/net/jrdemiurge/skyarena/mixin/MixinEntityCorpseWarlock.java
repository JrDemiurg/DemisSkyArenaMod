package net.jrdemiurge.skyarena.mixin;

import com.eeeab.eeeabsmobs.sever.entity.corpse.EntityCorpseWarlock;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EntityCorpseWarlock.class)
public class MixinEntityCorpseWarlock {

    @Inject(method = "dropCustomDeathLoot", at = @At("HEAD"), cancellable = true, require = 0)
    private void cancelLootIfSummoned(DamageSource source, int looting, boolean recentlyHit, CallbackInfo ci) {
        EntityCorpseWarlock entity = (EntityCorpseWarlock) (Object) this;

        System.out.println(entity.getTeam() != null && "summonedByArenaWithoutLoot".equals(entity.getTeam().getName()));
        if (entity.getTeam() != null && "summonedByArenaWithoutLoot".equals(entity.getTeam().getName())) {
            ci.cancel();
        }
    }
}
