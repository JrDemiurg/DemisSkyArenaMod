package net.jrdemiurge.skyarena.mixin;

import net.mcreator.borninchaosv.entity.DireHoundLeaderEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(DireHoundLeaderEntity.class)
public class MixinDireHoundLeaderEntity extends Monster {

    public MixinDireHoundLeaderEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }
    /**
     * @author I
     * @reason Because
     */
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        execute(this.level(), this.getY(), this);
        return super.hurt(source, amount);
    }

    private static void execute(LevelAccessor world, double y, Entity entity) {
        if (entity != null) {
            if (entity instanceof LivingEntity) {
                LivingEntity _livEnt0 = (LivingEntity)entity;
                if (_livEnt0.hasEffect((MobEffect) BornInChaosV1ModMobEffects.BLOCK_BREAK.get())) {
                    return;
                }
            }

            if (!entity.isInLava()) {
                float var10000;
                if (entity instanceof LivingEntity) {
                    LivingEntity _livEnt = (LivingEntity)entity;
                    var10000 = _livEnt.getHealth();
                } else {
                    var10000 = -1.0F;
                }

                float var10001;
                if (entity instanceof LivingEntity) {
                    LivingEntity _livEnt = (LivingEntity)entity;
                    var10001 = _livEnt.getMaxHealth();
                } else {
                    var10001 = -1.0F;
                }

                if (var10000 <= var10001 - 20.0F) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity _entity = (LivingEntity)entity;
                        if (!_entity.level().isClientSide()) {
                            _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 140, 0, false, false));
                        }
                    }

                    if (entity instanceof LivingEntity) {
                        LivingEntity _entity = (LivingEntity)entity;
                        if (!_entity.level().isClientSide()) {
                            _entity.addEffect(new MobEffectInstance((MobEffect)BornInChaosV1ModMobEffects.BLOCK_BREAK.get(), 140, 0, false, false));
                        }
                    }

                    if (!world.getBlockState(BlockPos.containing(entity.getX() - (double)2.0F, y, entity.getZ() + (double)0.5F)).canOcclude() || world.getBlockState(BlockPos.containing(entity.getX() - (double)2.0F, y, entity.getZ() + (double)0.5F)).getBlock() == Blocks.SNOW) {
                        if (world instanceof ServerLevel) {
                            ServerLevel _level = (ServerLevel)world;
                            Entity entityToSpawn = ((EntityType)BornInChaosV1ModEntities.DREAD_HOUND.get()).spawn(_level, BlockPos.containing(entity.getX() - (double)2.0F, y, entity.getZ() + (double)0.5F), MobSpawnType.MOB_SUMMONED);
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
                            _level.sendParticles(ParticleTypes.POOF, entity.getX() - (double)2.0F, y, entity.getZ() + (double)0.5F, 5, 0.3, 0.3, 0.3, 0.1);
                        }
                    }

                    if (!world.getBlockState(BlockPos.containing(entity.getX() + (double)2.0F, y, entity.getZ() + (double)0.5F)).canOcclude() || world.getBlockState(BlockPos.containing(entity.getX() + (double)2.0F, y, entity.getZ() + (double)0.5F)).getBlock() == Blocks.SNOW) {
                        if (world instanceof ServerLevel) {
                            ServerLevel _level = (ServerLevel)world;
                            Entity entityToSpawn = ((EntityType)BornInChaosV1ModEntities.DREAD_HOUND.get()).spawn(_level, BlockPos.containing(entity.getX() + (double)2.0F, y, entity.getZ() + (double)0.5F), MobSpawnType.MOB_SUMMONED);
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
                            _level.sendParticles(ParticleTypes.POOF, entity.getX() + (double)2.0F, y, entity.getZ() + (double)0.5F, 5, 0.3, 0.3, 0.3, 0.1);
                        }
                    }
                }
            }

        }
    }
}
