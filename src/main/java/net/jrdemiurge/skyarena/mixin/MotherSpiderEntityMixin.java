package net.jrdemiurge.skyarena.mixin;

import net.mcreator.borninchaosv.entity.MotherSpiderEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MotherSpiderEntity.class)
public abstract class MotherSpiderEntityMixin extends Monster {

    protected MotherSpiderEntityMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true, require = 0)
    private void onTickDeath(CallbackInfo ci) {
        MotherSpiderEntity entity = (MotherSpiderEntity)(Object)this;

        Team team = entity.getTeam();
        String teamName = team != null ? team.getName() : "";
        if ("summonedByArena".equals(teamName) || "summonedByArenaWithoutLoot".equals(teamName)) {
            ++this.deathTime;
            if (this.deathTime == 30) {
                this.remove(RemovalReason.KILLED);
                this.dropExperience();
            }

            ci.cancel();
        }
    }
}
