package net.jrdemiurge.skyarena.mixin;

import com.Polarice3.Goety.common.entities.boss.Vizier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Vizier.class, remap = false)
public class VizierMixin {

    @Inject(method = "dropCustomDeathLoot", at = @At("HEAD"), cancellable = true, require = 0)
    private void onDropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit, CallbackInfo ci) {
        Vizier entity = (Vizier)(Object) this;

        Team team = entity.getTeam();
        String teamName = team != null ? team.getName() : "";
        if ("summonedByArenaWithoutLoot".equals(teamName)) {
            ci.cancel();
        }
    }
}
