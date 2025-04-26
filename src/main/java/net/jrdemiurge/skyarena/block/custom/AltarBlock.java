package net.jrdemiurge.skyarena.block.custom;

import com.mojang.logging.LogUtils;
import net.jrdemiurge.skyarena.SkyArena;
import net.jrdemiurge.skyarena.block.ModBlocks;
import net.jrdemiurge.skyarena.block.entity.AltarBlockEntity;
import net.jrdemiurge.skyarena.block.entity.ModBlockEntity;
import net.jrdemiurge.skyarena.item.ModItems;
import net.jrdemiurge.skyarena.triggers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

import java.util.ArrayList;
import java.util.Collections;
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
            // получаем кординаты нижней части и определяем ентити блок
            BlockPos altarPos = pPos;
            BlockEntity blockEntity = pLevel.getBlockEntity(altarPos);

            if (!(blockEntity instanceof AltarBlockEntity altarBlockEntity)) return InteractionResult.PASS;

            if (pPlayer.getItemInHand(pHand).getItem() == Blocks.BEDROCK.asItem()) {
                if (altarBlockEntity.isBattlePhaseActive()) {
                    Component message = Component.translatable("message.skyarena.cannot_do_during_battle");
                    pPlayer.displayClientMessage(message, true);
                    return InteractionResult.SUCCESS;
                }

                altarBlockEntity.switchToNextArena();

                Component message = Component.literal(altarBlockEntity.getArenaType());
                pPlayer.displayClientMessage(message, true);

                pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() == ModItems.MOB_ANALYZER.get()) {
                MutableComponent message = Component.literal("§4=== Arena Info ===\n")
                        .append(Component.literal("§6Arena Type: §a" + altarBlockEntity.getArenaType() + "\n"))
                        .append(Component.literal("§6Difficulty Level: §a" + altarBlockEntity.getDifficultyLevel(pPlayer) + "\n"))
                        .append(Component.literal("§6Points: §a" + altarBlockEntity.getPoints(pPlayer) + "\n"))
                        .append(Component.literal("§6Starting Points: §a" + altarBlockEntity.getStartingPoints() + "\n"))
                        .append(Component.literal("§6Points Increase: §a" + altarBlockEntity.getPointsIncrease() + "\n"))
                        .append(Component.literal("§6Mob Spawn Radius: §a" + altarBlockEntity.getMobSpawnRadius() + "\n"))
                        .append(Component.literal("§6Spawn Distance From Player: §a" + altarBlockEntity.getSpawnDistanceFromPlayer() + "\n"))
                        .append(Component.literal("§6Mob Cost Ratio: §a" + altarBlockEntity.getMobCostRatio() + "\n"))
                        .append(Component.literal("§6Base Scaling Threshold: §a" + altarBlockEntity.getBaseScalingThreshold() + "\n"))
                        .append(Component.literal("§6Mob Stat Growth Coefficient: §a" + altarBlockEntity.getMobStatGrowthCoefficient() + "\n"))
                        .append(Component.literal("§6Squad Spawn Chance: §a" + altarBlockEntity.getSquadSpawnChance() + "\n"))
                        .append(Component.literal("§6Squad Spawn Size: §a" + altarBlockEntity.getSquadSpawnSize() + "\n"))
                        .append(Component.literal("§6Battle Loss Distance: §a" + altarBlockEntity.getBattleLossDistance() + "\n"))
                        .append(Component.literal("§6Mob Teleport Distance: §a" + altarBlockEntity.getMobTeleportDistance() + "\n"))
                        .append(Component.literal("§6Max Difficulty Level: §a" + altarBlockEntity.getMaxDifficultyLevel() + "\n"))
                        .append(Component.literal("§6Allow Difficulty Reset: §a" + altarBlockEntity.isAllowDifficultyReset() + "\n"))
                        .append(Component.literal("§6Allow Water And Air Spawn: §a" + altarBlockEntity.isAllowWaterAndAirSpawn() + "\n"))
                        .append(Component.literal("§6Individual Player Stats: §a" + altarBlockEntity.isIndividualPlayerStats() + "\n"))
                        .append(Component.literal("§6Night Time: §a" + altarBlockEntity.isNightTime() + "\n"))
                        .append(Component.literal("§6Enable Rain: §a" + altarBlockEntity.isEnableRain() + "\n"))
                        .append(Component.literal("§6Enable Mob Item Drop: §a" + altarBlockEntity.isEnableMobItemDrop() + "\n"))
                        .append(Component.literal("§6Reward Item: §a" + altarBlockEntity.getRewardItem() + "\n"))
                        .append(Component.literal("§6Reward Increase Interval: §a" + altarBlockEntity.getRewardIncreaseInterval() + "\n"));

                Map<String, Integer> mobValues = altarBlockEntity.getMobValuesUnfiltered();
                if (mobValues != null && !mobValues.isEmpty()) {
                    message = message.append(Component.literal("§4=== Mob Values ===\n"));
                    if (mobValues.size() > 60) {
                        message = message.append(Component.literal("§6Too many mobs in the list! Check the console for the full list.\n"));
                        SkyArena.LOGGER.info("Full list of mobs (Total: " + mobValues.size() + "):");
                        mobValues.forEach((mob, value) -> SkyArena.LOGGER.info(mob + ": " + value));
                    } else {
                        for (Map.Entry<String, Integer> entry : mobValues.entrySet()) {
                            message = message.append(Component.literal("§6" + entry.getKey() + ": §a" + entry.getValue() + "\n"));
                        }
                    }
                }

                pPlayer.displayClientMessage(message, false);
                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() == Items.STICK) {
                altarBlockEntity.clearRecordItem(); // Удаляем пластинку
                altarBlockEntity.stopMusic();
                pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    UseStick.INSTANCE.trigger(serverPlayer);
                }

                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() instanceof RecordItem) {
                altarBlockEntity.setRecordItem(pPlayer.getItemInHand(pHand)); // Сохраняем пластинку
                pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (altarBlockEntity.isBattlePhaseActive()) {
                    altarBlockEntity.stopMusic();
                    altarBlockEntity.startMusic();
                }

                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    UseMusicDisk.INSTANCE.trigger(serverPlayer);
                }

                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() == Items.BLAZE_ROD) {
                Component message = Component.translatable("message.skyarena.current_points")
                        .append(Component.literal(String.valueOf(altarBlockEntity.getPoints(pPlayer))));

                pPlayer.displayClientMessage(message, true); // true для отображения над горячей панелью
                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() == Items.NETHERITE_INGOT) {
                if (!altarBlockEntity.isAllowDifficultyReset()){
                    Component message = Component.translatable("message.skyarena.cannot_reset_difficulty");
                    pPlayer.displayClientMessage(message, true);
                    return InteractionResult.PASS;
                }

                if (altarBlockEntity.isBattlePhaseActive()) {
                    Component message = Component.translatable("message.skyarena.cannot_do_during_battle");
                    pPlayer.displayClientMessage(message, true);
                    return InteractionResult.PASS;
                }

                if (pPlayer.getCooldowns().isOnCooldown(Items.NETHERITE_INGOT)) {
                    return InteractionResult.PASS;
                }


                altarBlockEntity.setDifficultyLevel(pPlayer, 1);

                pPlayer.getItemInHand(pHand).shrink(1);

                pPlayer.getCooldowns().addCooldown(Items.NETHERITE_INGOT, 40); // 40 тиков = 2 секунды

                pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

                Component message = Component.translatable("message.skyarena.points_reset");
                pPlayer.displayClientMessage(message, true);

                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    UseNetheriteIngot.INSTANCE.trigger(serverPlayer);
                }

                return InteractionResult.SUCCESS;
            }

            if (pLevel.getDifficulty() == Difficulty.PEACEFUL) {
                Component message = Component.translatable("message.skyarena.peaceful_disabled");
                pPlayer.displayClientMessage(message, true);
                return InteractionResult.SUCCESS;
            }

            // выдача награды
            if (altarBlockEntity.isBattlePhaseActive() && altarBlockEntity.canSummonMobs()) {
                handleGiveReward(altarBlockEntity, pLevel, altarPos, pState, pPlayer);
                return InteractionResult.SUCCESS;
            }
            // начало боя
            if (!(altarBlockEntity.isBattlePhaseActive())) {

                if (altarBlockEntity.getMaxDifficultyLevel() != 0 && altarBlockEntity.getDifficultyLevel(pPlayer) > altarBlockEntity.getMaxDifficultyLevel()) {
                    Component message;

                    if (ThreadLocalRandom.current().nextBoolean()) {
                        message = Component.translatable("message.skyarena.max_difficult_level_1");
                    } else {
                        message = Component.translatable("message.skyarena.max_difficult_level_2");
                    }

                    pPlayer.displayClientMessage(message, true);

                    return InteractionResult.SUCCESS;
                }

                if (altarBlockEntity.getBattleDelay() != 0){
                    return InteractionResult.SUCCESS;
                }

                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    // Вызов триггера
                    UseAltarBattle.INSTANCE.trigger(serverPlayer);
                }

                List<BlockPos> validPositions = altarBlockEntity.findValidSpawnPositions(pLevel, altarPos, pPlayer);
                /*Component messagee = Component.literal(String.valueOf(validPositions.size()));
                pPlayer.displayClientMessage(messagee, false);
                for (BlockPos pos : validPositions) {
                    pLevel.setBlock(pos, Blocks.GLOWSTONE.defaultBlockState(), 3);

                    // Планируем возврат исходного состояния через 5 секунд
                    pLevel.scheduleTick(pos, Blocks.GLOWSTONE, 100);
                }*/

                if (validPositions.isEmpty()){
                    Component message = Component.translatable("message.skyarena.no_spawn_position");
                    pPlayer.displayClientMessage(message, true);
                    return InteractionResult.SUCCESS;
                }

                // Отслеживаем активацию алтаря
                altarBlockEntity.recordAltarActivation(pPlayer, altarPos);

                altarBlockEntity.startMusic();
                // Отображаем текущие очки игроку в клиентской части
                Component message = Component.translatable("message.skyarena.difficult_level")
                        .append(Component.literal(String.valueOf(altarBlockEntity.getDifficultyLevel(pPlayer))));
                pPlayer.displayClientMessage(message, true); // true для отображения над горячей панелью

                // Устанавливаем окружение
                boolean isNight = altarBlockEntity.isNightTime();
                boolean isRain = altarBlockEntity.isEnableRain();
                setEnvironment(pLevel, isNight, isRain);

                /*if (pLevel instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            altarPos.getX() + 0.5,
                            altarPos.getY() + 0.8,
                            altarPos.getZ() + 0.5,
                            100, 0.25, 0.5, 0.25, 0);
                }*/

                pLevel.playSound(null, altarPos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F);

                altarBlockEntity.setGlowingCounter(0);

                int remainingPoints = altarBlockEntity.getPoints(pPlayer);

                altarBlockEntity.setBattleDifficultyLevel(altarBlockEntity.getDifficultyLevel(pPlayer));

                int mobCostRatio = altarBlockEntity.getMobCostRatio();
                int baseScalingThreshold = altarBlockEntity.getBaseScalingThreshold();
                double costCoefficient = calculateCostCoefficient(remainingPoints, mobCostRatio, baseScalingThreshold);
                double statsCoefficient = (costCoefficient - 1) * altarBlockEntity.getMobStatGrowthCoefficient() + 1;
                // Component message1 = Component.literal(costCoefficient + " " +  statsCoefficient);
                // pPlayer.displayClientMessage(message1, false);

                Map<String, Integer> mobValues = altarBlockEntity.getMobValues();

                int minMobValue = mobValues.isEmpty()
                        ? 100000 // Если список пуст, возвращаем большое значение
                        : Collections.min(mobValues.values());

                minMobValue = (int) (minMobValue * costCoefficient);

                String teamName = altarBlockEntity.isEnableMobItemDrop() ? "summonedByArena" : "summonedByArenaWithoutLoot";

                PlayerTeam summonedMobsTeam = (PlayerTeam) pLevel.getScoreboard().getPlayerTeam(teamName);
                if (summonedMobsTeam == null) {
                    summonedMobsTeam = pLevel.getScoreboard().addPlayerTeam(teamName);
                    summonedMobsTeam.setAllowFriendlyFire(false); // Запрещаем союзникам атаковать друг друга
                    summonedMobsTeam.setCollisionRule(Team.CollisionRule.NEVER); // Избегаем столкновений между союзниками
                }

                int skipCount = 0;

                double squadSpawnChance = altarBlockEntity.getSquadSpawnChance();
                int squadSpawnSize = altarBlockEntity.getSquadSpawnSize();

                while (remainingPoints >= minMobValue) {

                    int randomIndex = ThreadLocalRandom.current().nextInt(mobValues.size());
                    List<String> keys = new ArrayList<>(mobValues.keySet());
                    String mobTypeString = keys.get(randomIndex);
                    int mobValue = mobValues.get(mobTypeString);

                    mobValue = (int) (mobValue * costCoefficient);

                    // пропускаем дешёвых мобов до 5 раз
                    if (mobValue < remainingPoints / mobCostRatio && skipCount < 6) {
                        skipCount++;
                        continue;
                    }
                    skipCount = 0;

                    if (remainingPoints >= mobValue) {
                        boolean spawnTriple = ThreadLocalRandom.current().nextDouble() < squadSpawnChance;

                        // Спавним одного или трёх мобов
                        int mobsToSpawn = spawnTriple ? squadSpawnSize : 1;

                        for (int i = 0; i < mobsToSpawn; i++) {
                            // Проверяем, хватает ли очков для следующего моба
                            if (remainingPoints < mobValue) {
                                break; // Останавливаемся, если очков больше не хватает
                            }

                            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mobTypeString));

                            Entity mob = entityType.create(pLevel);
                            if (mob != null) {

                                BlockPos spawnPos = validPositions.get(ThreadLocalRandom.current().nextInt(validPositions.size()));

                                mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

                                // Чтобы моб спавнился в стандартной комплектации. К примеру скелет будет появляться с луком.
                                if (mob instanceof Mob) {
                                    Mob mobEntity = (Mob) mob;

                                    /*mobEntity.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(16.0);*/

                                    if (!(altarBlockEntity.isEnableMobItemDrop())) {
                                        CompoundTag entityData = mobEntity.saveWithoutId(new CompoundTag());
                                        entityData.putString("DeathLootTable", "minecraft:empty");
                                        mobEntity.load(entityData);
                                    }

                                    mobEntity.setPersistenceRequired();

                                    AttributeInstance healthAttribute = mobEntity.getAttribute(Attributes.MAX_HEALTH);
                                    if (healthAttribute != null) {
                                        double baseHealth = healthAttribute.getBaseValue();
                                        healthAttribute.setBaseValue(baseHealth * statsCoefficient);
                                        mobEntity.setHealth((float) (baseHealth * statsCoefficient));
                                    }

                                    AttributeInstance attackAttribute = mobEntity.getAttribute(Attributes.ATTACK_DAMAGE);
                                    if (attackAttribute != null) {
                                        double baseDamage = attackAttribute.getBaseValue();
                                        attackAttribute.setBaseValue(baseDamage * statsCoefficient);
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

                                /*pPlayer.displayClientMessage(Component.literal(mobTypeString + mobValue), false);*/
                                pLevel.addFreshEntity(mob);
                                altarBlockEntity.addSummonedMob(mob); // записываем призванного моба
                                remainingPoints -= mobValue;
                            }
                        }
                    }
                }

                if (!(altarBlockEntity.canSummonMobs())){
                    altarBlockEntity.toggleBattlePhase(); // переключаем фазу в фазу лута
                }

                return InteractionResult.SUCCESS;
            }

            altarBlockEntity.applyGlowEffectToSummonedMobs(pPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    public double calculateCostCoefficient(int remainingPoints, int mobCostRatio, int baseScalingThreshold) {
        double costCoefficient = 1.0;

        while (true) {
            double cost = (double) baseScalingThreshold * costCoefficient;
            if (cost < (double) remainingPoints / mobCostRatio) {
                costCoefficient += 0.1;
                continue;
            }
            break;
        }

        return Math.round(costCoefficient * 10) / 10.0; // Возвращаем новый коэффициент
    }

    private void handleGiveReward(AltarBlockEntity altarBlockEntity, Level pLevel, BlockPos altarPos, BlockState pState, Player pPlayer) {
        // Удаляем игрока из списка и добавляем очки
        altarBlockEntity.removeAltarActivationForPlayer(/*pPlayer*/);

        /*if (pLevel instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    altarPos.getX() + 0.5,
                    altarPos.getY() + 0.8,
                    altarPos.getZ() + 0.5,
                    100, 0.25, 0.5, 0.25, 0);
        }*/

        handleVictoryTriggers(altarBlockEntity, pPlayer);

        pPlayer.displayClientMessage(Component.translatable("message.skyarena.victory"), true);

        int difficultyLevel = altarBlockEntity.getBattleDifficultyLevel(); // Получаем текущий уровень сложности
        int keyCount = (difficultyLevel-1) / altarBlockEntity.getRewardIncreaseInterval() + 1;

        String rewardLootTableId = altarBlockEntity.getRewardItem();

        if (pLevel instanceof ServerLevel serverLevel) {
            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(new ResourceLocation(rewardLootTableId));

            if (lootTable == LootTable.EMPTY) {
                rewardLootTableId = "skyarena:battle_rewards/crimson_key";
                lootTable = serverLevel.getServer().getLootData().getLootTable(new ResourceLocation(rewardLootTableId));
            }

            if (lootTable != LootTable.EMPTY) {
                LootParams lootParams = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, pPlayer.position()) // Позиция игрока
                        .withParameter(LootContextParams.THIS_ENTITY, pPlayer) // Сам игрок
                        .create(LootContextParamSets.GIFT); // Используемый набор параметров (GIFT подойдёт для выдачи)

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
        pLevel.playSound(null, altarPos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

        // Переключаем фазу боя
        altarBlockEntity.toggleBattlePhase();
        altarBlockEntity.stopMusic();
        altarBlockEntity.setBattleDelay(30);
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

    private void setEnvironment(Level pLevel,boolean isNight, boolean isRain) {
        if (isNight) {
            setNightTime(pLevel);
        }

        if (isRain) {
            setRain(pLevel);
        }
    }

    private void setNightTime(Level pLevel) {
        if (!(pLevel instanceof ServerLevel serverLevel)) return;

        long currentTime = serverLevel.getDayTime();
        long dayProgress = currentTime % 24000; // Время внутри текущего дня

        long newTime = currentTime - dayProgress + 18000; // Переносим время на 18000 в пределах текущего дня
        if (dayProgress <= 18000 && currentTime > 24000) {
            newTime -= 24000; // Если уже ночь, переносим на предыдущую
        }

        serverLevel.setDayTime(newTime);
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
