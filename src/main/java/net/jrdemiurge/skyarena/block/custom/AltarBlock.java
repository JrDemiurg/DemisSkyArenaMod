package net.jrdemiurge.skyarena.block.custom;

import net.jrdemiurge.skyarena.SkyArena;
import net.jrdemiurge.skyarena.block.ModBlocks;
import net.jrdemiurge.skyarena.block.entity.AltarBlockEntity;
import net.jrdemiurge.skyarena.block.entity.ModBlockEntity;
import net.jrdemiurge.skyarena.config.*;
import net.jrdemiurge.skyarena.item.ModItems;
import net.jrdemiurge.skyarena.triggers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AltarBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public AltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

            if (!(blockEntity instanceof AltarBlockEntity altarBlockEntity)) return InteractionResult.PASS;

            if (pPlayer.getItemInHand(pHand).getItem() == Blocks.BEDROCK.asItem()) {
                handleBedrockUse(pLevel, pPos, pPlayer, pHand, altarBlockEntity);
                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() == ModItems.MOB_ANALYZER.get()) {
                showArenaInfo(pPlayer, altarBlockEntity);
                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() == Items.STICK) {
                handleStickUse(pLevel, pPos, pPlayer, pHand, altarBlockEntity);
                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() instanceof RecordItem) {
                handleRecordUse(pLevel, pPos, pPlayer, pHand, altarBlockEntity);
                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() == Items.NETHERITE_INGOT) {
                return handleNetheriteIngotUse(pLevel, pPos, pPlayer, pHand, altarBlockEntity);
            }

            if (pLevel.getDifficulty() == Difficulty.PEACEFUL) {
                Component message = Component.translatable("message.skyarena.peaceful_disabled");
                pPlayer.displayClientMessage(message, true);
                altarBlockEntity.putPlayerMessageTimestamps(pPlayer);
                return InteractionResult.SUCCESS;
            }

            // выдача награды
            if (altarBlockEntity.isBattlePhaseActive() && altarBlockEntity.canSummonMobs()) {
                handleGiveReward(altarBlockEntity, pLevel, pPos, pState, pPlayer);
                return InteractionResult.SUCCESS;
            }
            // начало боя
            if (!(altarBlockEntity.isBattlePhaseActive())) {

                int difficultyLevel = altarBlockEntity.getDifficultyLevel(pPlayer);

                if (!altarBlockEntity.isBattleOngoing(difficultyLevel)) {
                    handleMaxDifficultyLevel(pPlayer, altarBlockEntity);
                    return InteractionResult.SUCCESS;
                }

                if (pLevel.getGameTime() - altarBlockEntity.getBattleEndTime() < 30) {
                    return InteractionResult.SUCCESS;
                }

                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    UseAltarBattle.INSTANCE.trigger(serverPlayer);
                }

                List<BlockPos> validPositions = altarBlockEntity.findValidSpawnPositions(pLevel, pPos, pPlayer);
                if (validPositions.isEmpty()) {
                    Component message = Component.translatable("message.skyarena.no_spawn_position");
                    pPlayer.displayClientMessage(message, true);
                    altarBlockEntity.putPlayerMessageTimestamps(pPlayer);
                    return InteractionResult.SUCCESS;
                }

                altarBlockEntity.recordAltarActivation(pPlayer, pPos);

                altarBlockEntity.startMusic();

                if (!altarBlockEntity.isAutoWaveRun()) {
                    pLevel.playSound(null, pPos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F);
                } else {
                    pLevel.playSound(null, pPlayer, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F);
                }

                setEnvironment(pLevel, pPlayer, altarBlockEntity.isSetNight(), altarBlockEntity.isSetRain());

                altarBlockEntity.setGlowingCounter(0);

                altarBlockEntity.setBattleDifficultyLevel(difficultyLevel);

                Component message = Component.translatable("message.skyarena.difficult_level")
                        .append(Component.literal(String.valueOf(difficultyLevel)));
                pPlayer.displayClientMessage(message, true);
                altarBlockEntity.putPlayerMessageTimestamps(pPlayer);

                String teamName = !altarBlockEntity.isDisableMobItemDrop() ? "summonedByArena" : "summonedByArenaWithoutLoot";
                PlayerTeam summonedMobsTeam = (PlayerTeam) pLevel.getScoreboard().getPlayerTeam(teamName);
                if (summonedMobsTeam == null) {
                    summonedMobsTeam = pLevel.getScoreboard().addPlayerTeam(teamName);
                    summonedMobsTeam.setAllowFriendlyFire(false);
                    summonedMobsTeam.setCollisionRule(Team.CollisionRule.NEVER);
                }

                double statMultiplier = altarBlockEntity.getStatMultiplier(pPlayer);
                double statCoefPerBlocks = altarBlockEntity.getStatMultiplierCoefficientPerBlocks();

                // Preset Wave
                Map<Integer, PresetWave> presetWaves = altarBlockEntity.getPresetWaves();
                if (presetWaves.containsKey(difficultyLevel) && presetWaves.get(difficultyLevel).mobStatMultiplier != 0) {
                    statMultiplier = presetWaves.get(difficultyLevel).mobStatMultiplier;
                }

                if (statCoefPerBlocks != 0) {
                    double distance = Math.sqrt(pPos.distSqr(BlockPos.ZERO));
                    double thousands = Math.floor(distance / 1000.0);
                    statMultiplier *= (1 + statCoefPerBlocks * thousands);
                }

                if (presetWaves.containsKey(difficultyLevel) && presetWaves.get(difficultyLevel).mobs != null) {
                    PresetWave wave = presetWaves.get(difficultyLevel);

                    for (WaveMob waveMob : wave.mobs) {
                        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(waveMob.type));
                        if (entityType == null) continue;

                        for (int i = 0; i < waveMob.count; i++) {
                            spawnArenaMob(pLevel, altarBlockEntity, entityType, validPositions, statMultiplier, waveMob.type, summonedMobsTeam);
                        }
                    }

                    if (!(altarBlockEntity.canSummonMobs())) {
                        altarBlockEntity.setBattlePhaseActive(true);
                    }

                    return InteractionResult.SUCCESS;
                }

                // Random Wave
                int remainingPoints = altarBlockEntity.getPoints(pPlayer);

                double pointsCoefPerBlocks = altarBlockEntity.getPointsCoefficientPerBlocks();
                if (pointsCoefPerBlocks != 0) {
                    double distance = Math.sqrt(pPos.distSqr(BlockPos.ZERO));
                    double thousands = Math.floor(distance / 1000.0);
                    remainingPoints = (int) (remainingPoints * (1 + pointsCoefPerBlocks * thousands));
                }

                int mobCostRatio = altarBlockEntity.getMobCostRatio();

                List<ExpandedMobInfo> availableMobs = altarBlockEntity.getAvailableMobs(altarBlockEntity.getBattleDifficultyLevel());

                if (availableMobs.isEmpty()) return InteractionResult.SUCCESS;

                int minMobValue = availableMobs.stream()
                        .mapToInt(mob -> mob.cost)
                        .min()
                        .orElse(Integer.MAX_VALUE);

                int skipCount = 0;

                while (remainingPoints >= minMobValue) {
                    ExpandedMobInfo mobInfo = availableMobs.get(ThreadLocalRandom.current().nextInt(availableMobs.size()));

                    // шанс не заспавниться
                    if (mobInfo.mobSpawnChance < 1.0 && ThreadLocalRandom.current().nextDouble() > mobInfo.mobSpawnChance) {
                        continue;
                    }

                    int mobValue = mobInfo.cost;

                    // пропускаем дешёвых мобов до 5 раз
                    if (mobValue < remainingPoints / mobCostRatio && skipCount < 6) {
                        skipCount++;
                        continue;
                    }
                    skipCount = 0;

                    if (remainingPoints >= mobValue) {
                        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mobInfo.mobId));
                        if (entityType == null) continue;

                        boolean spawnSquad = ThreadLocalRandom.current().nextDouble() < mobInfo.squadSpawnChance;
                        int mobsToSpawn = spawnSquad ? mobInfo.squadSpawnSize : 1;

                        double actualMultiplier = statMultiplier * mobInfo.statMultiplierCoefficient;

                        for (int i = 0; i < mobsToSpawn; i++) {
                            if (remainingPoints < mobValue) break;

                            boolean success = spawnArenaMob(pLevel, altarBlockEntity, entityType, validPositions, actualMultiplier, mobInfo.mobId, summonedMobsTeam);
                            if (success) remainingPoints -= mobValue;
                        }
                    }
                }

                if (!(altarBlockEntity.canSummonMobs())) {
                    altarBlockEntity.setBattlePhaseActive(true);
                }

                return InteractionResult.SUCCESS;
            }

            altarBlockEntity.applyGlowEffectToSummonedMobs(pPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean spawnArenaMob(Level pLevel, AltarBlockEntity altarBlockEntity, EntityType<?> entityType, List<BlockPos> validPositions, double statMultiplier, String mobTypeString, PlayerTeam summonedMobsTeam) {
        Entity mob = entityType.create(pLevel);
        if (mob == null) return false;

        BlockPos spawnPos = validPositions.get(ThreadLocalRandom.current().nextInt(validPositions.size()));
        mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

        if (mob instanceof Mob mobEntity) {
            if (altarBlockEntity.isDisableMobItemDrop()) {
                CompoundTag entityData = mobEntity.saveWithoutId(new CompoundTag());
                entityData.putString("DeathLootTable", "minecraft:empty");
                mobEntity.load(entityData);
            }

            mobEntity.setPersistenceRequired();

            AttributeInstance healthAttribute = mobEntity.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttribute != null) {
                double baseHealth = healthAttribute.getBaseValue();
                healthAttribute.setBaseValue(baseHealth * statMultiplier);
                mobEntity.setHealth(mobEntity.getMaxHealth());
            }

            AttributeInstance attackAttribute = mobEntity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttribute != null) {
                double baseDamage = attackAttribute.getBaseValue();
                attackAttribute.setBaseValue(baseDamage * statMultiplier);
            }

            mobEntity.finalizeSpawn(
                    (ServerLevel) pLevel,
                    pLevel.getCurrentDifficultyAt(mobEntity.blockPosition()),
                    MobSpawnType.NATURAL,
                    null,
                    null
            );

            if (!mobTypeString.equals("born_in_chaos_v1:spiritof_chaos")) {
                pLevel.getScoreboard().addPlayerToTeam(mob.getStringUUID(), summonedMobsTeam);
            }
        }

        pLevel.addFreshEntity(mob);
        altarBlockEntity.addSummonedMob(mob);
        return true;
    }
    
    public void showArenaInfo(Player pPlayer, AltarBlockEntity altarBlockEntity) {
        MutableComponent message = Component.literal("§4=== Arena Info ===\n");
        StringBuilder logMessage = new StringBuilder("=== Arena Info ===\n");

        int points = altarBlockEntity.getPoints(pPlayer);
        double pointsCoefPerBlocks = altarBlockEntity.getPointsCoefficientPerBlocks();
        if (pointsCoefPerBlocks != 0) {
            BlockPos pPos = altarBlockEntity.getBlockPos();
            double distance = Math.sqrt(pPos.distSqr(BlockPos.ZERO));
            double thousands = Math.floor(distance / 1000.0);
            points = (int) (points * (1 + pointsCoefPerBlocks * thousands));
        }

        double statMultiplier = altarBlockEntity.getStatMultiplier(pPlayer);
        double statCoefPerBlocks = altarBlockEntity.getStatMultiplierCoefficientPerBlocks();
        if (statCoefPerBlocks != 0) {
            BlockPos pPos = altarBlockEntity.getBlockPos();
            double distance = Math.sqrt(pPos.distSqr(BlockPos.ZERO));
            double thousands = Math.floor(distance / 1000.0);
            statMultiplier *= (1 + statCoefPerBlocks * thousands);
        }

        String[] lines = {
                "Arena Type: " + altarBlockEntity.getArenaType(),
                "Difficulty Level: " + altarBlockEntity.getDifficultyLevel(pPlayer),
                "Points: " + points,
                "Stat Multiplier: " + statMultiplier,
                "Starting Points: " + altarBlockEntity.getStartingPoints(),
                "Starting Stat Multiplier: " + altarBlockEntity.getStartingStatMultiplier(),
                "Mob Spawn Radius: " + altarBlockEntity.getMobSpawnRadius(),
                "Spawn Distance From Player: " + altarBlockEntity.getSpawnDistanceFromPlayer(),
                "Battle Loss Distance: " + altarBlockEntity.getBattleLossDistance(),
                "Mob Teleport Distance: " + altarBlockEntity.getMobTeleportDistance(),
                "Mob Griefing Protection Radius: " + altarBlockEntity.getMobGriefingProtectionRadius(),
                "Full Protection Radius: " + altarBlockEntity.getFullProtectionRadius(),
                "Boss Bar Hide Radius: " + altarBlockEntity.getBossBarHideRadius(),
                "Mob Cost Ratio: " + altarBlockEntity.getMobCostRatio(),
                "Allow Difficulty Reset: " + altarBlockEntity.isAllowDifficultyReset(),
                "Allow Water And Air Spawn: " + altarBlockEntity.isAllowWaterAndAirSpawn(),
                "Individual Player Stats: " + altarBlockEntity.isIndividualPlayerStats(),
                "Set Night: " + altarBlockEntity.isSetNight(),
                "Set Rain: " + altarBlockEntity.isSetRain(),
                "Disable Mob Item Drop: " + altarBlockEntity.isDisableMobItemDrop(),
                "Reset Difficulty On Defeat: " + altarBlockEntity.isResetDifficultyOnDefeat(),
                "Auto Wave Run: " + altarBlockEntity.isAutoWaveRun(),
                "Points Coefficient Per 1000 Blocks: " + altarBlockEntity.getPointsCoefficientPerBlocks(),
                "Stat Multiplier Coefficient Per 1000 Blocks: " + altarBlockEntity.getStatMultiplierCoefficientPerBlocks(),
                "Loot Table Count Coefficient Per 1000 Blocks: " + altarBlockEntity.getLootTableCountCoefficientPerBlocks()
        };

        for (String line : lines) {
            message = message.append(Component.literal("§6" + line.split(":")[0] + ": §a" + line.split(": ")[1] + "\n"));
            logMessage.append(line).append("\n");
        }
        
        // === Difficulty Level Ranges ===
        List<DifficultyLevelRange> ranges = altarBlockEntity.getDifficultyLevelRanges();
        if (!ranges.isEmpty()) {
            message = message.append(Component.literal("§4=== Difficulty Ranges ===\n"));
            logMessage.append("=== Difficulty Ranges ===\n");

            for (DifficultyLevelRange range : ranges) {
                String rangeStr = range.range.get(0) + "–" + range.range.get(1);
                message = message.append(Component.literal("§6Range: §a" + rangeStr + "\n"));
                logMessage.append("Range: ").append(rangeStr).append("\n");

                message = message.append(Component.literal("§7  Points Increase: §a" + range.pointsIncrease + "\n"));
                message = message.append(Component.literal("§7  Stat Multiplier Increase: §a" + range.statMultiplierIncrease + "\n"));
                message = message.append(Component.literal("§7  Reward Loot Table: §a" + range.rewardLootTable + "\n"));
                message = message.append(Component.literal("§7  Reward Count: §a" + range.rewardCount + "\n"));

                logMessage.append("  Points Increase: ").append(range.pointsIncrease).append("\n");
                logMessage.append("  Stat Multiplier Increase: ").append(range.statMultiplierIncrease).append("\n");
                logMessage.append("  Reward Loot Table: ").append(range.rewardLootTable).append("\n");
                logMessage.append("  Reward Count: ").append(range.rewardCount).append("\n");

                if (!range.mobGroupsUsed.isEmpty()) {
                    String joinedGroups = String.join(", ", range.mobGroupsUsed);
                    message = message.append(Component.literal("§7  Mob Groups: §e" + joinedGroups + "\n"));
                    logMessage.append("  Mob Groups: ").append(joinedGroups).append("\n");
                }
            }
        }

        // === Mob Groups ===
        Map<String, MobGroup> mobGroups = altarBlockEntity.getMobGroups();
        if (!mobGroups.isEmpty()) {
            message = message.append(Component.literal("§4=== Mob Groups ===\n"));
            logMessage.append("=== Mob Groups ===\n");

            for (Map.Entry<String, MobGroup> entry : mobGroups.entrySet()) {
                String groupId = entry.getKey();
                MobGroup group = entry.getValue();

                message = message.append(Component.literal("§6Group: §a" + groupId + "\n"));
                logMessage.append("Group: ").append(groupId).append("\n");

                message = message.append(Component.literal("§7  Squad Chance: §a" + group.squadSpawnChance + "\n"));
                message = message.append(Component.literal("§7  Squad Size: §a" + group.squadSpawnSize + "\n"));
                message = message.append(Component.literal("§7  Stat Multiplier Coefficient: §a" + group.statMultiplierCoefficient + "\n"));
                message = message.append(Component.literal("§7  Mob Spawn Chance: §a" + group.mobSpawnChance + "\n"));
                message = message.append(Component.literal("§7  Mobs: §8[list printed to console]\n"));

                logMessage.append("  Squad Chance: ").append(group.squadSpawnChance).append("\n");
                logMessage.append("  Squad Size: ").append(group.squadSpawnSize).append("\n");
                logMessage.append("  Stat Multiplier Coefficient: ").append(group.statMultiplierCoefficient).append("\n");
                logMessage.append("  Mob Spawn Chance: ").append(group.mobSpawnChance).append("\n");

                for (Map.Entry<String, Integer> mobEntry : group.mobValues.entrySet()) {
                    String mobId = mobEntry.getKey();
                    Integer mobValue = mobEntry.getValue();

                    logMessage.append("    - ").append(mobId).append(": ").append(mobValue).append("\n");
                }
            }
        }

        // === Preset Waves ===
        Map<Integer, PresetWave> presetWaves = altarBlockEntity.getPresetWaves();
        if (presetWaves != null && !presetWaves.isEmpty()) {
            message = message.append(Component.literal("§4=== Preset Waves ===\n"));
            logMessage.append("=== Preset Waves ===\n");

            for (Map.Entry<Integer, PresetWave> waveEntry : presetWaves.entrySet()) {
                int waveNumber = waveEntry.getKey();
                PresetWave wave = waveEntry.getValue();

                message = message.append(Component.literal("§6Wave " + waveNumber + ":\n"));
                logMessage.append("Wave ").append(waveNumber).append(":\n");

                message = message.append(Component.literal("§7  Stat Multiplier: §a" + wave.mobStatMultiplier + "\n"));
                logMessage.append("  Stat Multiplier: ").append(wave.mobStatMultiplier).append("\n");

                message = message.append(Component.literal("§7  Reward Loot Table: §a" + wave.rewardLootTable + "\n"));
                logMessage.append("  Reward Loot Table: ").append(wave.rewardLootTable).append("\n");

                message = message.append(Component.literal("§7  Reward Count: §a" + wave.rewardCount + "\n"));
                logMessage.append("  Reward Count: ").append(wave.rewardCount).append("\n");

                for (WaveMob mob : wave.mobs) {
                    message = message.append(Component.literal("§7    - §6" + mob.type + "§7 x§a" + mob.count + "\n"));
                    logMessage.append("    - ").append(mob.type).append(" x").append(mob.count).append("\n");
                }
            }
        }

        int linesCount = message.getString().split("\n").length;
        if (linesCount > 90) {
            message = message.append(Component.literal("§cToo much info! Check console for full details."));
        }

        pPlayer.displayClientMessage(message, false);
        SkyArena.LOGGER.info(logMessage.toString());
    }

    private void handleBedrockUse(Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, AltarBlockEntity altarBlockEntity) {
        if (altarBlockEntity.isBattlePhaseActive()) {
            Component message = Component.translatable("message.skyarena.cannot_do_during_battle");
            pPlayer.displayClientMessage(message, true);
            altarBlockEntity.putPlayerMessageTimestamps(pPlayer);
            return;
        }

        altarBlockEntity.switchToNextArena();

        Component message = Component.literal(altarBlockEntity.getArenaType());
        pPlayer.displayClientMessage(message, true);
        altarBlockEntity.putPlayerMessageTimestamps(pPlayer);

        pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void handleStickUse(Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, AltarBlockEntity altarBlockEntity) {
        altarBlockEntity.clearRecordItem();
        altarBlockEntity.stopMusic();
        pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            UseStick.INSTANCE.trigger(serverPlayer);
        }
    }

    private void handleRecordUse(Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, AltarBlockEntity altarBlockEntity) {
        altarBlockEntity.setRecordItem(pPlayer.getItemInHand(pHand));
        pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (altarBlockEntity.isBattlePhaseActive()) {
            altarBlockEntity.stopMusic();
            altarBlockEntity.startMusic();
        }

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            UseMusicDisk.INSTANCE.trigger(serverPlayer);
        }
    }

    private InteractionResult handleNetheriteIngotUse(Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, AltarBlockEntity altarBlockEntity) {
        if (!altarBlockEntity.isAllowDifficultyReset()) {
            Component message = Component.translatable("message.skyarena.cannot_reset_difficulty");
            pPlayer.displayClientMessage(message, true);
            altarBlockEntity.putPlayerMessageTimestamps(pPlayer);
            return InteractionResult.PASS;
        }

        if (altarBlockEntity.isBattlePhaseActive()) {
            Component message = Component.translatable("message.skyarena.cannot_do_during_battle");
            pPlayer.displayClientMessage(message, true);
            altarBlockEntity.putPlayerMessageTimestamps(pPlayer);
            return InteractionResult.PASS;
        }

        if (pPlayer.getCooldowns().isOnCooldown(Items.NETHERITE_INGOT)) {
            return InteractionResult.PASS;
        }

        altarBlockEntity.setDifficultyLevel(pPlayer, 1);

        pPlayer.getItemInHand(pHand).shrink(1);

        pPlayer.getCooldowns().addCooldown(Items.NETHERITE_INGOT, 40);

        pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

        Component message = Component.translatable("message.skyarena.points_reset");
        pPlayer.displayClientMessage(message, true);
        altarBlockEntity.putPlayerMessageTimestamps(pPlayer);

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            UseNetheriteIngot.INSTANCE.trigger(serverPlayer);
        }

        return InteractionResult.SUCCESS;
    }

    private void handleMaxDifficultyLevel(Player pPlayer, AltarBlockEntity altarBlockEntity) {
        Component message;

        if (ThreadLocalRandom.current().nextBoolean()) {
            message = Component.translatable("message.skyarena.max_difficult_level_1");
        } else {
            message = Component.translatable("message.skyarena.max_difficult_level_2");
        }

        pPlayer.displayClientMessage(message, true);
        altarBlockEntity.putPlayerMessageTimestamps(pPlayer);
    }

    private void handleGiveReward(AltarBlockEntity altarBlockEntity, Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (!altarBlockEntity.isAutoWaveRun())
            altarBlockEntity.removeAltarActivationForPlayer();

        handleVictoryTriggers(altarBlockEntity, pPlayer);

        if (altarBlockEntity.isBattleOngoing(altarBlockEntity.getDifficultyLevel(pPlayer) + 1)) {
            pPlayer.displayClientMessage(Component.translatable("message.skyarena.victory"), true);
            altarBlockEntity.putPlayerMessageTimestamps(pPlayer);
        } else {
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                Component title = Component.translatable("message.skyarena.victory");
                serverPlayer.connection.send(new ClientboundSetTitleTextPacket(title));
                serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 10));
            }
        }

        int difficultyLevel = altarBlockEntity.getBattleDifficultyLevel(); // Получаем текущий уровень сложности
        int keyCount = 1;
        String rewardLootTableId = "minecraft:empty";

        Map<Integer, PresetWave> presetWaves = altarBlockEntity.getPresetWaves();

        AltarBlockEntity.LootReward reward = altarBlockEntity.getRewardFromDifficultyRanges(difficultyLevel);
        if (reward != null) {
            rewardLootTableId = reward.rewardLootTable();
            keyCount = reward.rewardCount();
        }

        if (presetWaves.containsKey(difficultyLevel)) {
            PresetWave wave = presetWaves.get(difficultyLevel);
            rewardLootTableId = wave.rewardLootTable;
            keyCount = wave.rewardCount;
        }

        double lootCountCoefPerBlocks = altarBlockEntity.getLootTableCountCoefficientPerBlocks();
        if (lootCountCoefPerBlocks != 0) {
            double distance = Math.sqrt(pPos.distSqr(BlockPos.ZERO));
            double thousands = Math.floor(distance / 1000.0);

            double newKeyCount = keyCount * (1 + lootCountCoefPerBlocks * thousands);
            int guaranteed = (int) Math.floor(newKeyCount);
            double fraction = newKeyCount - guaranteed;
            if (pLevel.random.nextDouble() < fraction) {
                guaranteed++;
            }

            keyCount = guaranteed;
        }


        if (pLevel instanceof ServerLevel serverLevel) {
            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(new ResourceLocation(rewardLootTableId));

            if (lootTable == LootTable.EMPTY && !rewardLootTableId.equals("minecraft:empty")) {
                rewardLootTableId = "skyarena:battle_rewards/crimson_key";
                lootTable = serverLevel.getServer().getLootData().getLootTable(new ResourceLocation(rewardLootTableId));
            }

            if (lootTable != LootTable.EMPTY) {
                LootParams lootParams = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, pPlayer.position())
                        .withParameter(LootContextParams.THIS_ENTITY, pPlayer)
                        .create(LootContextParamSets.GIFT);

                for (int i = 0; i < keyCount; i++) {
                    List<ItemStack> lootItems = lootTable.getRandomItems(lootParams);
                    for (ItemStack stack : lootItems) {
                        ItemEntity rewardEntity = new ItemEntity(
                                pLevel,
                                pPlayer.getX(),
                                pPlayer.getY(),
                                pPlayer.getZ(),
                                stack
                        );
                        pLevel.addFreshEntity(rewardEntity);
                    }
                }
            }
        }

        if (altarBlockEntity.getBattleDifficultyLevel() >= altarBlockEntity.getDifficultyLevel(pPlayer)) {
            altarBlockEntity.increaseDifficultyLevel(pPlayer);
        }
        // Воспроизведение звука
        if (!altarBlockEntity.isAutoWaveRun()) {
            pLevel.playSound(null, pPos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            pLevel.playSound(null, pPlayer, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        // Переключаем фазу боя
        altarBlockEntity.setBattlePhaseActive(false);
        altarBlockEntity.stopMusic();
        altarBlockEntity.setBattleEndTime(pLevel.getGameTime());
    }

    private void handleVictoryTriggers(AltarBlockEntity altarBlockEntity, Player pPlayer) {
        int difficultyLevel = altarBlockEntity.getDifficultyLevel(pPlayer); // Получаем текущий уровень сложности

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            DifficultyLevel1.INSTANCE.trigger(serverPlayer);
            if (difficultyLevel >= 1) {
                DifficultyLevel1.INSTANCE.trigger(serverPlayer);
            }
            if (difficultyLevel >= 5) {
                DifficultyLevel5.INSTANCE.trigger(serverPlayer);
            }
            if (difficultyLevel >= 10) {
                DifficultyLevel10.INSTANCE.trigger(serverPlayer);
            }
            if (difficultyLevel >= 20) {
                DifficultyLevel20.INSTANCE.trigger(serverPlayer);
            }
            if (difficultyLevel >= 50) {
                DifficultyLevel50.INSTANCE.trigger(serverPlayer);
            }
            if (difficultyLevel >= 100) {
                DifficultyLevel100.INSTANCE.trigger(serverPlayer);
            }
        }
    }

    private void setEnvironment(Level pLevel, Player pPlayer,boolean isNight, boolean isRain) {
        if (isNight) {
            setNightTime(pLevel, pPlayer);
        }

        if (isRain) {
            setRain(pLevel);
        }
    }

    private void setNightTime(Level pLevel, Player pPlayer) {
        if (!(pLevel instanceof ServerLevel serverLevel)) return;

        long currentTime = serverLevel.getDayTime();
        long dayProgress = currentTime % 24000; // Время внутри текущего дня

        long newTime = currentTime - dayProgress + 18000; // Переносим время на 18000 в пределах текущего дня
        if (dayProgress <= 18000 && currentTime > 24000) {
            newTime -= 24000; // Если уже ночь, переносим на предыдущую
        }

        for(ServerLevel serverlevel : pPlayer.getServer().getAllLevels()) {
            serverlevel.setDayTime(newTime);
        }
    }

    private void setRain(Level pLevel) {
        ServerLevel serverLevel = (ServerLevel) pLevel;
        serverLevel.setWeatherParameters(0, 6000, true, false); // Дождь на 5 минут
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AltarBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private static final VoxelShape SHAPE_NORTH = Block.box(2, 0, 5, 14, 16, 11);
    private static final VoxelShape SHAPE_WEST = Block.box(5, 0, 2, 11, 16, 14);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);

        return switch (facing) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_NORTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        } else {
            return null; // Нет места для размещения двух блоков
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.getValue(WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            level.setBlock(pos, state.setValue(WATERLOGGED, true), 3);
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            return true;
        }
        return false;
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return !state.getValue(WATERLOGGED) && fluid == Fluids.WATER;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, placer, itemStack);

        Direction facing = state.getValue(FACING);
        boolean isWaterAbove = level.getBlockState(pos.above()).getBlock() == Blocks.WATER;

        BlockPos posAbove = pos.above();
        BlockState topBlockState = ModBlocks.ALTAR_BATTLE_TOP.get().defaultBlockState()
                .setValue(AltarBlockTop.FACING, facing)
                .setValue(AltarBlockTop.WATERLOGGED, isWaterAbove);
        level.setBlock(posAbove, topBlockState, 3);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AltarBlockEntity altarBlockEntity) {
            altarBlockEntity.loadArenaConfig(altarBlockEntity.getArenaType());
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockPos abovePos = pos.above();
            if (level.getBlockState(abovePos).getBlock() == ModBlocks.ALTAR_BATTLE_TOP.get()) {
                level.destroyBlock(abovePos, false);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntity.ALTAR_BLOCK_ENTITY.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
}
