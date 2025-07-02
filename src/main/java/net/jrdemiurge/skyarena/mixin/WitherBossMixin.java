package net.jrdemiurge.skyarena.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public class WitherBossMixin {

    @Inject(method = "dropCustomDeathLoot", at = @At("HEAD"), cancellable = true)
    private void onDropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit, CallbackInfo ci) {
        WitherBoss entity = (WitherBoss)(Object) this;

        Team team = entity.getTeam();
        String teamName = team != null ? team.getName() : "";
        if ("summonedByArenaWithoutLoot".equals(teamName)) {
            ci.cancel();
        }
    }
}
