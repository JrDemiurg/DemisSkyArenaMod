package net.jrdemiurge.skyarena.mixin;

import com.majruszlibrary.events.OnEntityDied;
import com.majruszsdifficulty.treasurebag.listeners.KillRewarder;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = KillRewarder.class, remap = false)
public class KillRewarderMixin {

    @Inject(method = "giveTreasureBag", at = @At("HEAD"), cancellable = true, require = 0)
    private static void onGiveTreasureBag(OnEntityDied data, CallbackInfo ci) {
        Team team = data.target.getTeam();
        String teamName = team != null ? team.getName() : "";

        if ("summonedByArenaWithoutLoot".equals(teamName)) {
            ci.cancel();
        }
    }
}
