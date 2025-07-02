package net.jrdemiurge.skyarena.mixin;

import net.mcreator.borninchaosv.entity.BonescallerEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(BonescallerEntity.class)
public class BonescallerEntityMixin extends Monster {

    public BonescallerEntityMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }
    /**
     * @author I
     * @reason Because
     */
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
        if (source.is(DamageTypes.FALL)) {
            return false;
        } else {
            return source.is(DamageTypes.DROWN) ? false : super.hurt(source, amount);
        }
    }

    private static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null) {
            if (entity instanceof LivingEntity) {
                LivingEntity _livEnt0 = (LivingEntity)entity;
                if (_livEnt0.hasEffect((MobEffect)BornInChaosV1ModMobEffects.MAGIC_DEPLETION.get())) {
                    return;
                }
            }

            if (!entity.isOnFire()) {
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

                if (var10000 <= var10001 - 2.0F) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity _entity = (LivingEntity)entity;
                        if (!_entity.level().isClientSide()) {
                            _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1, false, false));
                        }
                    }

                    if (entity instanceof LivingEntity) {
                        LivingEntity _entity = (LivingEntity)entity;
                        if (!_entity.level().isClientSide()) {
                            _entity.addEffect(new MobEffectInstance((MobEffect)BornInChaosV1ModMobEffects.MAGIC_DEPLETION.get(), 160, 0, false, false));
                        }
                    }

                    if (!world.isClientSide()) {
                        if (world instanceof Level) {
                            Level _level = (Level)world;
                            if (!_level.isClientSide()) {
                                _level.playSound((Player)null, BlockPos.containing(x, y, z), (SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.skeleton.ambient")), SoundSource.NEUTRAL, 1.0F, 1.0F);
                            } else {
                                _level.playLocalSound(x, y, z, (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.skeleton.ambient")), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
                            }
                        }

                        if (world instanceof Level) {
                            Level _level = (Level)world;
                            if (!_level.isClientSide()) {
                                _level.playSound((Player)null, BlockPos.containing(x, y, z), (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.evoker.prepare_summon")), SoundSource.NEUTRAL, 0.4F, 1.0F);
                            } else {
                                _level.playLocalSound(x, y, z, (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.evoker.prepare_summon")), SoundSource.NEUTRAL, 0.4F, 1.0F, false);
                            }
                        }
                    }

                    if (!world.getBlockState(BlockPos.containing(x - (double)2.0F, y, z + (double)0.5F)).canOcclude() || world.getBlockState(BlockPos.containing(x - (double)2.0F, y, z + (double)0.5F)).getBlock() == Blocks.SNOW) {
                        if (world instanceof ServerLevel) {
                            ServerLevel _level = (ServerLevel)world;
                            Entity entityToSpawn = ((EntityType)BornInChaosV1ModEntities.BABY_SKELETON_MINION.get()).spawn(_level, BlockPos.containing(x - (double)2.0F, y, z + (double)0.5F), MobSpawnType.MOB_SUMMONED);
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
                            _level.sendParticles(ParticleTypes.POOF, x - (double)2.0F, y, z + (double)0.5F, 5, 0.3, 0.3, 0.3, 0.1);
                        }

                        if (world instanceof ServerLevel) {
                            ServerLevel _level = (ServerLevel)world;
                            _level.sendParticles((SimpleParticleType) BornInChaosV1ModParticleTypes.RITUAL.get(), x - (double)2.0F, y, z + (double)0.5F, 5, 0.3, 0.3, 0.3, 0.1);
                        }
                    }

                    if (!world.getBlockState(BlockPos.containing(x + (double)2.0F, y, z + (double)0.5F)).canOcclude() || world.getBlockState(BlockPos.containing(x + (double)2.0F, y, z + (double)0.5F)).getBlock() == Blocks.SNOW) {
                        if (world instanceof ServerLevel) {
                            ServerLevel _level = (ServerLevel)world;
                            Entity entityToSpawn = ((EntityType)BornInChaosV1ModEntities.BABY_SKELETON_MINION.get()).spawn(_level, BlockPos.containing(x + (double)2.0F, y, z + (double)0.5F), MobSpawnType.MOB_SUMMONED);
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
                            _level.sendParticles(ParticleTypes.POOF, x + (double)2.0F, y, z + (double)0.5F, 5, 0.3, 0.3, 0.3, 0.1);
                        }

                        if (world instanceof ServerLevel) {
                            ServerLevel _level = (ServerLevel)world;
                            _level.sendParticles((SimpleParticleType)BornInChaosV1ModParticleTypes.RITUAL.get(), x + (double)2.0F, y, z + (double)0.5F, 5, 0.3, 0.3, 0.3, 0.1);
                        }
                    }
                }
            }

        }
    }
}
