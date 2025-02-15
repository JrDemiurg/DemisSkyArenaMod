package net.jrdemiurge.skyarena.block.custom;

import net.jrdemiurge.skyarena.Config;
import net.jrdemiurge.skyarena.block.ModBlocks;
import net.jrdemiurge.skyarena.block.entity.AltarBlockEntity;
import net.jrdemiurge.skyarena.block.entity.ModBlockEntity;
import net.jrdemiurge.skyarena.item.ModItems;
import net.jrdemiurge.skyarena.triggers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AltarBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public AltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            // получаем кординаты нижней части и определяем ентити блок
            BlockPos altarPos = pState.getValue(HALF) == DoubleBlockHalf.UPPER ? pPos.below() : pPos;
            BlockEntity blockEntity = pLevel.getBlockEntity(altarPos);

            if (!(blockEntity instanceof AltarBlockEntity altarBlockEntity)) return InteractionResult.PASS;

            if (pPlayer instanceof ServerPlayer serverPlayer) {
                // Вызов триггера
                UseAltarBattle.INSTANCE.trigger(serverPlayer);
            }

            if (pLevel.getDifficulty() == Difficulty.PEACEFUL) {
                Component message = Component.translatable("message.skyarena.peaceful_disabled");
                pPlayer.displayClientMessage(message, true);
                return InteractionResult.PASS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() == Blocks.BEDROCK.asItem()) {
                // Переключаем тип арены
                String currentType = altarBlockEntity.getArenaType();
                String newType;

                switch (currentType) {
                    case "sky_arena":
                        newType = "ice_arena";
                        break;
                    case "ice_arena":
                        newType = "sky_arena";
                        break;
                    default:
                        newType = "sky_arena";
                        break;
                }

                altarBlockEntity.setArenaType(newType);

                // Отображаем сообщение игроку
                Component message = Component.literal(newType);
                pPlayer.displayClientMessage(message, true);

                pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

                return InteractionResult.SUCCESS;
            }

            // Если это новый блок, устанавливаем начальные очки
            if (altarBlockEntity.isNewBlock()) {
                altarBlockEntity.setPoints(Config.StartingPoints); // Устанавливаем стартовое количество очков
                altarBlockEntity.setNewBlock(false); // Устанавливаем флаг в false, так как блок уже был использован
            }

            if (pPlayer.getItemInHand(pHand).getItem() == Items.BLAZE_ROD) {
                Component message = Component.translatable("message.skyarena.current_points")
                        .append(Component.literal(String.valueOf(altarBlockEntity.getRemainingPoints(pPlayer))));

                pPlayer.displayClientMessage(message, true); // true для отображения над горячей панелью
                return InteractionResult.SUCCESS;
            }

            if (pPlayer.getItemInHand(pHand).getItem() == Items.STICK) {
                altarBlockEntity.clearRecordItem(); // Удаляем пластинку
                altarBlockEntity.stopMusic();
                pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
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

            if (pPlayer.getItemInHand(pHand).getItem() == Items.NETHERITE_INGOT) {
                if (pPlayer.getCooldowns().isOnCooldown(Items.NETHERITE_INGOT) || altarBlockEntity.isBattlePhaseActive()) {
                    return InteractionResult.PASS;
                }

                altarBlockEntity.setPoints(pPlayer, Config.StartingPoints);
                altarBlockEntity.setDifficultyLevel(pPlayer, 1);

                pPlayer.getItemInHand(pHand).shrink(1);

                pPlayer.getCooldowns().addCooldown(Items.NETHERITE_INGOT, 40); // 40 тиков = 2 секунды

                pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

                Component message = Component.translatable("message.skyarena.points_reset");
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

                if (altarBlockEntity.getBattleDelay() != 0){
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
                setEnvironment(pLevel);

                /*if (pLevel instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            altarPos.getX() + 0.5,
                            altarPos.getY() + 0.8,
                            altarPos.getZ() + 0.5,
                            100, 0.25, 0.5, 0.25, 0);
                }*/

                pLevel.playSound(null, altarPos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F);

                int remainingPoints = altarBlockEntity.getRemainingPoints(pPlayer); // Начальное количество очков

                double costCoefficient = calculateCostCoefficient(remainingPoints);

                int minMobValue = Config.mobValues.stream()
                        .map(mob -> (Integer) mob.get(1)) // Получаем стоимость каждого моба
                        .min(Integer::compareTo) // Находим минимальную стоимость
                        .orElse(100000); // Если список пуст, стоимость = 0

                minMobValue = (int) (minMobValue * costCoefficient);

                PlayerTeam summonedMobsTeam = (PlayerTeam) pLevel.getScoreboard().getPlayerTeam("summonedMobs");
                if (summonedMobsTeam == null) {
                    summonedMobsTeam = pLevel.getScoreboard().addPlayerTeam("summonedMobs");
                    summonedMobsTeam.setAllowFriendlyFire(false); // Запрещаем союзникам атаковать друг друга
                    summonedMobsTeam.setCollisionRule(Team.CollisionRule.NEVER); // Избегаем столкновений между союзниками
                }

                int skipCount = 0;

                int mobCostRatio = Config.mobCostRatio;
                double squadSpawnChance = Config.SquadSpawnChance;
                int squadSpawnSize = Config.SquadSpawnSize;

                List<BlockPos> validPositions = altarBlockEntity.findValidSpawnPositions(pLevel, altarPos, altarBlockEntity.getSpawnRadius(), pPlayer);
                /*Component messagee = Component.literal(String.valueOf(validPositions.size()));
                pPlayer.displayClientMessage(messagee, false);
                for (BlockPos pos : validPositions) {
                    pLevel.setBlock(pos, Blocks.GLOWSTONE.defaultBlockState(), 3);

                    // Планируем возврат исходного состояния через 5 секунд
                    pLevel.scheduleTick(pos, Blocks.GLOWSTONE, 100);
                }*/

                if (validPositions.isEmpty()) return InteractionResult.FAIL;

                // Цикл для призыва мобов, пока есть очки или не получится призвать больше
                while (remainingPoints >= minMobValue) {
                // выбираем рандомного моба
                    int randomIndex = ThreadLocalRandom.current().nextInt(Config.mobValues.size());
                    List<Object> selectedMob = Config.mobValues.get(randomIndex);
                    String mobTypeString = (String) selectedMob.get(0); // ID моба как строка
                    int mobValue = (Integer) selectedMob.get(1); // Значение для моба

                    mobValue = (int) (mobValue * costCoefficient);

                    // пропускаем дешёвых мобов до 5 раз
                    if (mobValue <= remainingPoints / mobCostRatio && skipCount < 6) {
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

                                    if (!(Config.enableMobItemDrop)) {
                                        CompoundTag entityData = mobEntity.saveWithoutId(new CompoundTag());
                                        entityData.putString("DeathLootTable", "minecraft:empty");
                                        mobEntity.load(entityData);
                                    }

                                    mobEntity.setPersistenceRequired();

                                    AttributeInstance healthAttribute = mobEntity.getAttribute(Attributes.MAX_HEALTH);
                                    if (healthAttribute != null) {
                                        double baseHealth = healthAttribute.getBaseValue();
                                        healthAttribute.setBaseValue(baseHealth * costCoefficient);
                                        mobEntity.setHealth((float) (baseHealth * costCoefficient));
                                    }

                                    AttributeInstance attackAttribute = mobEntity.getAttribute(Attributes.ATTACK_DAMAGE);
                                    if (attackAttribute != null) {
                                        double baseDamage = attackAttribute.getBaseValue();
                                        attackAttribute.setBaseValue(baseDamage * costCoefficient);
                                    }

                                    mobEntity.finalizeSpawn(
                                            (ServerLevel) pLevel,
                                            pLevel.getCurrentDifficultyAt(mobEntity.blockPosition()),
                                            MobSpawnType.NATURAL,
                                            null,
                                            null
                                    );

                                    pLevel.getScoreboard().addPlayerToTeam(mob.getStringUUID(), summonedMobsTeam);
                                }
                                /*pPlayer.displayClientMessage(Component.literal(mobTypeString + mobValue), false);*/
                                pLevel.addFreshEntity(mob);
                                altarBlockEntity.addSummonedMob(mob); // записываем призванного моба
                                remainingPoints -= mobValue;
                            }
                        }
                    }
                }
                if (!(altarBlockEntity.canSummonMobs())) altarBlockEntity.toggleBattlePhase(); // переключаем фазу в фазу лута
            }

            altarBlockEntity.applyGlowEffectToSummonedMobs(pPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    public double calculateCostCoefficient(int remainingPoints) {
        double costCoefficient = 1.0;
        double startcost = Config.baseScalingThreshold;

        while (true) {
            double cost = startcost * costCoefficient;
            if (cost < remainingPoints / Config.mobCostRatio) {
                costCoefficient += 0.1;
                continue;
            }
            break;
        }

        return costCoefficient; // Возвращаем новый коэффициент
    }

    private void handleGiveReward(AltarBlockEntity altarBlockEntity, Level pLevel, BlockPos altarPos, BlockState pState, Player pPlayer) {
        // Удаляем игрока из списка и добавляем очки
        altarBlockEntity.removeAltarActivationForPlayer(/*pPlayer*/);
        altarBlockEntity.addPoints(pPlayer ,Config.PointsIncrease);

        /*if (pLevel instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    altarPos.getX() + 0.5,
                    altarPos.getY() + 0.8,
                    altarPos.getZ() + 0.5,
                    100, 0.25, 0.5, 0.25, 0);
        }*/

        handleVictoryTriggers(altarBlockEntity, pPlayer);

        pPlayer.displayClientMessage(Component.translatable("message.skyarena.victory"), true);

        int difficultyLevel = altarBlockEntity.getDifficultyLevel(pPlayer); // Получаем текущий уровень сложности
        int keyCount = difficultyLevel / 10 + 1;

        ItemStack keyStack = switch (altarBlockEntity.getArenaType()) {
            case "sky_arena" -> new ItemStack(ModItems.CRIMSON_KEY.get(), keyCount);
            case "ice_arena" -> new ItemStack(ModItems.ICE_KEY.get(), keyCount);
            default -> new ItemStack(ModItems.CRIMSON_KEY.get(), keyCount);
        };
        ItemEntity keyEntity = new ItemEntity(
                pLevel,
                pPlayer.getX(),
                pPlayer.getY(),
                pPlayer.getZ(),
                keyStack
        );
        pLevel.addFreshEntity(keyEntity);

        altarBlockEntity.increaseDifficultyLevel(pPlayer);

        // Воспроизведение звука
        pLevel.playSound(null, altarPos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

        // Переключаем фазу боя
        altarBlockEntity.toggleBattlePhase();
        altarBlockEntity.stopMusic();
        altarBlockEntity.setBattleDelay(40);
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

    private void setEnvironment(Level pLevel) {
        if (Config.isNightTime) {
            setNightTime(pLevel);
        }

        if (Config.enableRain) {
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
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? RenderShape.MODEL : RenderShape.INVISIBLE;
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

        // Проверка, что есть место для верхней части блока
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null; // Нет места для размещения двух блоков
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, placer, itemStack);

        // Получаем направление нижнего блока
        Direction facing = state.getValue(FACING);

        // Устанавливаем верхний блок над нижним с тем же направлением
        BlockPos posAbove = pos.above();
        BlockState topBlockState = ModBlocks.ALTAR_BATTLE_TOP.get().defaultBlockState().setValue(AltarBlockTop.FACING, facing);
        level.setBlock(posAbove, topBlockState, 3);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Удаление верхнего блока, если удаляется нижний
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
        builder.add(FACING, HALF);
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
