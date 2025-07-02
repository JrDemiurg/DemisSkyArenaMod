package net.jrdemiurge.skyarena.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Pseudo
@Mixin(value = Ender_Guardian_Entity.class, remap = false)
public class EnderGuardianEntityMixin {

    @Inject(method = "AfterDefeatBoss", at = @At("HEAD"), cancellable = true, require = 0)
    private void onAfterDefeatBoss(@Nullable LivingEntity living, CallbackInfo ci) {
        Ender_Guardian_Entity entity = (Ender_Guardian_Entity)(Object) this;

        Team team = entity.getTeam();
        String teamName = team != null ? team.getName() : "";
        if ("summonedByArena".equals(teamName) || "summonedByArenaWithoutLoot".equals(teamName)) {
            ci.cancel();
        }
    }
}
