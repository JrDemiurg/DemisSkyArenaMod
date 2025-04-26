package net.jrdemiurge.skyarena.mixin;

import net.mcreator.borninchaosv.entity.SiameseSkeletonsEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(SiameseSkeletonsEntity.class)
public class MixinSiameseSkeletonsEntity extends Monster {

    public MixinSiameseSkeletonsEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }
    /**
     * @author I
     * @reason Because
     */
    @Overwrite
    public void baseTick() {
        super.baseTick();
        execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
        this.refreshDimensions();
    }

    private static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null) {
            float var10000;
            if (entity instanceof LivingEntity) {
                LivingEntity _livEnt = (LivingEntity)entity;
                var10000 = _livEnt.getHealth();
            } else {
                var10000 = -1.0F;
            }

            if (var10000 <= 10.0F) {
                if (world instanceof ServerLevel) {
                    ServerLevel _level = (ServerLevel)world;
                    _level.sendParticles(ParticleTypes.POOF, x, y, z, 6, 0.2, 0.2, 0.2, 0.1);
                }

                if (world instanceof Level) {
                    Level _level = (Level)world;
                    if (!_level.isClientSide()) {
                        _level.playSound((Player)null, BlockPos.containing(x, y, z), (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.skeleton.hurt")), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    } else {
                        _level.playLocalSound(x, y, z, (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.skeleton.hurt")), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
                    }
                }

                if (world instanceof ServerLevel) {
                    ServerLevel _level = (ServerLevel)world;
                    Entity entityToSpawn = ((EntityType)BornInChaosV1ModEntities.SIAMESE_SKELETONSRIGHT.get()).spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                    if (entityToSpawn != null) {
                        entityToSpawn.setYRot(entity.getYRot());
                        entityToSpawn.setYBodyRot(entity.getYRot());
                        entityToSpawn.setYHeadRot(entity.getYRot());
                        entityToSpawn.setXRot(entity.getXRot());
                    }

                    // new code
                    if (entity.getTeam() != null) {
                        String teamName = entity.getTeam().getName();
                        ((ServerLevel) world).getScoreboard().addPlayerToTeam(entityToSpawn.getScoreboardName(), ((ServerLevel) world).getScoreboard().getPlayerTeam(teamName));
                        if ("summonedByArenaWithoutLoot".equals(teamName)) {
                            CompoundTag tag = entityToSpawn.saveWithoutId(new CompoundTag());
                            tag.putString("DeathLootTable", "minecraft:empty");
                            entityToSpawn.load(tag);
                        }
                    }
                    //
                }

                if (world instanceof ServerLevel) {
                    ServerLevel _level = (ServerLevel)world;
                    Entity entityToSpawn = ((EntityType)BornInChaosV1ModEntities.SIAMESE_SKELETONSLEFT.get()).spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                    if (entityToSpawn != null) {
                        entityToSpawn.setYRot(entity.getYRot());
                        entityToSpawn.setYBodyRot(entity.getYRot());
                        entityToSpawn.setYHeadRot(entity.getYRot());
                        entityToSpawn.setXRot(entity.getXRot());
                    }

                    // new code
                    if (entity.getTeam() != null) {
                        String teamName = entity.getTeam().getName();
                        ((ServerLevel) world).getScoreboard().addPlayerToTeam(entityToSpawn.getScoreboardName(), ((ServerLevel) world).getScoreboard().getPlayerTeam(teamName));
                        if ("summonedByArenaWithoutLoot".equals(teamName)) {
                            CompoundTag tag = entityToSpawn.saveWithoutId(new CompoundTag());
                            tag.putString("DeathLootTable", "minecraft:empty");
                            entityToSpawn.load(tag);
                        }
                    }
                    //
                }

                if (!entity.level().isClientSide()) {
                    entity.discard();
                }
            }

            if (world.canSeeSkyFromBelowWater(BlockPos.containing(x, y + (double)1.0F, z))) {
                if (world instanceof Level) {
                    Level _lvl11 = (Level)world;
                    if (_lvl11.isDay() && !world.getLevelData().isRaining() && !world.getLevelData().isThundering() && !entity.isInWaterRainOrBubble() && !entity.isOnFire() && !world.isClientSide()) {
                        entity.setSecondsOnFire(5);
                    }
                }

                if (entity.isInWaterRainOrBubble()) {
                    entity.clearFire();
                }
            }

        }
    }
}
