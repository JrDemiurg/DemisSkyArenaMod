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

public class DifficultyLevel20 extends SimpleCriterionTrigger<DifficultyLevel20.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation(SkyArena.MOD_ID, "difficulty_level_20");
    public static final DifficultyLevel20 INSTANCE = new DifficultyLevel20();

    private DifficultyLevel20() {}

    @Override
    public @NotNull ResourceLocation getId() {
        return DifficultyLevel20.ID;
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
            super(DifficultyLevel20.ID, predicate);
        }

        public boolean matches() {
            return true;
        }
    }
}