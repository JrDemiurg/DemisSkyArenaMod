package net.jrdemiurge.skyarena.triggers;


import com.google.gson.JsonObject;
import net.jrdemiurge.skyarena.SkyArena;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public class UseAltarBattle extends SimpleCriterionTrigger<UseAltarBattle.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation(SkyArena.MOD_ID, "use_altar_battle");
    public static final UseAltarBattle INSTANCE = new UseAltarBattle();

    private UseAltarBattle() {}

    @Override
    public @NotNull ResourceLocation getId() {
        return UseAltarBattle.ID;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected @NotNull TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate predicate, DeserializationContext ctx) {
        return new TriggerInstance(predicate);
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer, TriggerInstance::matches);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ContextAwarePredicate predicate) {
            super(UseAltarBattle.ID, predicate);
        }

        public boolean matches() {
            return true;
        }
    }
}