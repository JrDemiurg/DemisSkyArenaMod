package net.jrdemiurge.skyarena.block.entity;

import net.jrdemiurge.skyarena.Config;
import net.jrdemiurge.skyarena.config.ArenaConfig;
import net.jrdemiurge.skyarena.config.SkyArenaConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class AltarBlockEntity extends BlockEntity {

    private static final Map<Player, BlockPos> activeAltarBlocks = new HashMap<>();
    private Player activatingPlayer;
    public final List<Entity> summonedMobs = new ArrayList<>();
    private boolean battlePhaseActive = false;
    private boolean PlayerDeath = false;
    private int DeathDelay = 0;
    private int BattleDelay = 0;
    private int glowingCounter = 0;
    private boolean firstMessageSent = false;
    // музыка
    private ItemStack recordItem = ItemStack.EMPTY;
    private boolean isPlayingMusic = false;
    private long musicEndTick = 0;
    private long musicTickCount = 0;
    // сложность
    private int difficultyLevel = 1;
    private int battleDifficultyLevel = 1;

    private String arenaType = "sky_arena";
    private int startingPoints;
    private int pointsIncrease;
    private int mobSpawnRadius;
    private int mobCostRatio;
    private int baseScalingThreshold;
    private double mobStatGrowthCoefficient;
    private double squadSpawnChance;
    private int squadSpawnSize;
    private int spawnDistanceFromPlayer;
    private int battleLossDistance;
    private int mobTeleportDistance;
    private int rewardIncreaseInterval;
    private int maxDifficultyLevel;
    private boolean allowDifficultyReset;
    private boolean allowWaterAndAirSpawn;
    private boolean individualPlayerStats;
    private boolean nightTime;
    private boolean enableRain;
    private boolean enableMobItemDrop;
    private String rewardItem;
    private LinkedHashMap<String, Integer> mobValues;

    private final Map<String, Integer> playerDifficulty = new HashMap<>();

    public AltarBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntity.ALTAR_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public void loadArenaConfig(String arenaType) {

        if (level != null && level.isClientSide) {
            return; // Клиент не должен загружать конфиг
        }

        ArenaConfig arenaConfig;

        if (SkyArenaConfig.configData == null) {
            arenaConfig = SkyArenaConfig.DEFAULT_ARENA;
        } else {
            arenaConfig = SkyArenaConfig.configData.arenas.getOrDefault(arenaType, SkyArenaConfig.DEFAULT_ARENA);
        }

        if (arenaConfig != null) {
            this.arenaType = arenaType;
            this.startingPoints = arenaConfig.startingPoints != 0 ? arenaConfig.startingPoints : 500;
            this.mobSpawnRadius = arenaConfig.mobSpawnRadius != 0 ? arenaConfig.mobSpawnRadius : 36;
            this.pointsIncrease = arenaConfig.pointsIncrease;
            this.mobCostRatio = arenaConfig.mobCostRatio != 0 ? arenaConfig.mobCostRatio : 20;
            this.baseScalingThreshold = arenaConfig.baseScalingThreshold != 0 ? arenaConfig.baseScalingThreshold : 120;
            this.mobStatGrowthCoefficient = arenaConfig.mobStatGrowthCoefficient;
            this.squadSpawnChance = arenaConfig.squadSpawnChance;
            this.squadSpawnSize = arenaConfig.squadSpawnSize;
            this.spawnDistanceFromPlayer = arenaConfig.spawnDistanceFromPlayer;
            this.battleLossDistance = arenaConfig.battleLossDistance != 0 ? arenaConfig.battleLossDistance : 60;
            this.mobTeleportDistance = arenaConfig.mobTeleportDistance != 0 ? arenaConfig.mobTeleportDistance : 50;
            this.rewardIncreaseInterval = arenaConfig.rewardIncreaseInterval != 0 ? arenaConfig.rewardIncreaseInterval : 10;
            this.maxDifficultyLevel = arenaConfig.maxDifficultyLevel;
            this.allowDifficultyReset = arenaConfig.allowDifficultyReset;
            this.allowWaterAndAirSpawn = arenaConfig.allowWaterAndAirSpawn;
            this.individualPlayerStats = arenaConfig.individualPlayerStats;
            this.nightTime = arenaConfig.nightTime;
            this.enableRain = arenaConfig.enableRain;
            this.enableMobItemDrop = arenaConfig.enableMobItemDrop;
            // возможно надо проверять есть ли такой предмет или вообще создать таблицу лута с ключом, и выдавать таблицу лута
            this.rewardItem = arenaConfig.reward != null ? arenaConfig.reward : "skyarena:battle_rewards/crimson_key";

            this.mobValues = arenaConfig.mobValues != null
                    ? arenaConfig.mobValues.entrySet().stream()
                    .filter(entry -> ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(entry.getKey()))) // Проверяем, существует ли моб
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new))
                    : new LinkedHashMap<>();

            if (this.level != null) {
                this.level.blockEntityChanged(this.getBlockPos());
            }
        }
    }

    public void switchToNextArena() {

        if (SkyArenaConfig.configData == null) {
            return;
        }

        List<String> arenaTypes = new ArrayList<>(SkyArenaConfig.configData.arenas.keySet()); // Получаем список всех арен

        if (arenaTypes.isEmpty()) {
            return;
        }

        int currentIndex = arenaTypes.indexOf(this.arenaType);
        int nextIndex = (currentIndex + 1) % arenaTypes.size();

        this.arenaType = arenaTypes.get(nextIndex); // Устанавливаем новый тип арены
        loadArenaConfig(this.arenaType); // Перезагружаем настройки

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos()); // Уведомляем игру об изменении блока
        }
    }

    public Map<String, Integer> getMobValues() {
        return mobValues.entrySet().stream()
                .filter(entry -> ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(entry.getKey()))) // Проверяем, существует ли моб
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Integer> getMobValuesUnfiltered() {
        return mobValues;
    }

    public String getArenaType() {
        return arenaType;
    }

    public void addSummonedMob(Entity mob) {
        summonedMobs.add(mob);
    }

    public boolean canSummonMobs() {
        summonedMobs.removeIf(Entity::isRemoved);
        return summonedMobs.isEmpty();
    }

    public void toggleBattlePhase() {
        battlePhaseActive = !battlePhaseActive;
    }

    public boolean isBattlePhaseActive() {
        return battlePhaseActive;
    }

    public void recordAltarActivation(Player player, BlockPos pos) {
        activeAltarBlocks.put(player, pos); // Сохраняем активированный блок для игрока
        activatingPlayer = player;
    }

    public static BlockPos getAltarPosForPlayer(Player player) {
        return activeAltarBlocks.get(player); // Получаем позицию блока для игрока
    }

    public void removeAltarActivationForPlayer() {
        activeAltarBlocks.remove(activatingPlayer); // Удаляем игрока из активных блоков
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        removeSummonedMobs();
        stopMusic();
    }

    public void removeSummonedMobs() {
        for (Entity mob : summonedMobs) {
            if (mob != null && mob.isAlive()) {
                mob.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        summonedMobs.clear();
    }

    public void setRecordItem(ItemStack stack) {
        if (stack.getItem() instanceof RecordItem) {
            this.recordItem = stack.copy();

            if (this.level != null) {
                this.level.blockEntityChanged(this.getBlockPos());
            }
        }
    }

    public void clearRecordItem() {
        this.recordItem = ItemStack.EMPTY;

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public int getPoints(Player player) {
        return startingPoints + (getDifficultyLevel(player) - 1) * pointsIncrease;
    }

    public int getDifficultyLevel(Player player) {
        if (individualPlayerStats) {
            String key = player.getGameProfile().getName() + "_" + this.arenaType; // Уникальный ключ
            return playerDifficulty.getOrDefault(key, 1);
        }
        return difficultyLevel;
    }

    public void increaseDifficultyLevel(Player player) {
        if (individualPlayerStats) {
            String key = player.getGameProfile().getName() + "_" + this.arenaType;
            playerDifficulty.put(key, getDifficultyLevel(player) + 1);
        } else {
            this.difficultyLevel++;
        }

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public void setDifficultyLevel(Player player, int level) {
        if (individualPlayerStats) {
            String key = player.getGameProfile().getName() + "_" + this.arenaType;
            playerDifficulty.put(key, level);
        } else {
            this.difficultyLevel = level;
        }

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }


    public int getBattleDifficultyLevel() {
        return battleDifficultyLevel;
    }

    public void setBattleDifficultyLevel(int battleDifficultyLevel) {
        this.battleDifficultyLevel = battleDifficultyLevel;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        pTag.putInt("DifficultyLevel", this.difficultyLevel);
        pTag.putString("ArenaType", this.arenaType);
        pTag.putInt("StartingPoints", this.startingPoints);
        pTag.putInt("PointsIncrease", this.pointsIncrease);
        pTag.putInt("MobSpawnRadius", this.mobSpawnRadius);
        pTag.putInt("MobCostRatio", this.mobCostRatio);
        pTag.putInt("BaseScalingThreshold", this.baseScalingThreshold);
        pTag.putDouble("MobStatGrowthCoefficient", this.mobStatGrowthCoefficient);
        pTag.putDouble("SquadSpawnChance", this.squadSpawnChance);
        pTag.putInt("SquadSpawnSize", this.squadSpawnSize);
        pTag.putInt("SpawnDistanceFromPlayer", this.spawnDistanceFromPlayer);
        pTag.putInt("BattleLossDistance", this.battleLossDistance);
        pTag.putInt("MobTeleportDistance", this.mobTeleportDistance);
        pTag.putInt("RewardIncreaseInterval", this.rewardIncreaseInterval);
        pTag.putInt("MaxDifficultyLevel", this.maxDifficultyLevel);
        pTag.putBoolean("AllowDifficultyReset", this.allowDifficultyReset);
        pTag.putBoolean("AllowWaterAndAirSpawn", this.allowWaterAndAirSpawn);
        pTag.putBoolean("IndividualPlayerStats", this.individualPlayerStats);
        pTag.putBoolean("NightTime", this.nightTime);
        pTag.putBoolean("EnableRain", this.enableRain);
        pTag.putBoolean("EnableMobItemDrop", this.enableMobItemDrop);
        pTag.putString("RewardItem", this.rewardItem);

        if (!this.recordItem.isEmpty()) {
            pTag.put("RecordItem", this.recordItem.save(new CompoundTag()));
        }

        if (!playerDifficulty.isEmpty()) {
            CompoundTag difficultyTag = new CompoundTag();
            for (Map.Entry<String, Integer> entry : playerDifficulty.entrySet()) {
                difficultyTag.putInt(entry.getKey(), entry.getValue());
            }
            pTag.put("PlayerDifficulty", difficultyTag);
        }

        if (!mobValues.isEmpty()) {
            CompoundTag mobValuesTag = new CompoundTag();
            for (Map.Entry<String, Integer> entry : mobValues.entrySet()) {
                mobValuesTag.putInt(entry.getKey(), entry.getValue());
            }
            pTag.put("MobValues", mobValuesTag);
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        if (pTag.contains("DifficultyLevel")) this.difficultyLevel = pTag.getInt("DifficultyLevel");
        if (pTag.contains("ArenaType")) this.arenaType = pTag.getString("ArenaType");
        if (pTag.contains("StartingPoints")) this.startingPoints = pTag.getInt("StartingPoints");
        if (pTag.contains("PointsIncrease")) this.pointsIncrease = pTag.getInt("PointsIncrease");
        if (pTag.contains("MobSpawnRadius")) this.mobSpawnRadius = pTag.getInt("MobSpawnRadius");
        if (pTag.contains("MobCostRatio")) this.mobCostRatio = pTag.getInt("MobCostRatio");
        if (pTag.contains("BaseScalingThreshold")) this.baseScalingThreshold = pTag.getInt("BaseScalingThreshold");
        if (pTag.contains("MobStatGrowthCoefficient")) this.mobStatGrowthCoefficient = pTag.getDouble("MobStatGrowthCoefficient");
        if (pTag.contains("SquadSpawnChance")) this.squadSpawnChance = pTag.getDouble("SquadSpawnChance");
        if (pTag.contains("SquadSpawnSize")) this.squadSpawnSize = pTag.getInt("SquadSpawnSize");
        if (pTag.contains("SpawnDistanceFromPlayer")) this.spawnDistanceFromPlayer = pTag.getInt("SpawnDistanceFromPlayer");
        if (pTag.contains("BattleLossDistance")) this.battleLossDistance = pTag.getInt("BattleLossDistance");
        if (pTag.contains("MobTeleportDistance")) this.mobTeleportDistance = pTag.getInt("MobTeleportDistance");
        if (pTag.contains("RewardIncreaseInterval")) this.rewardIncreaseInterval = pTag.getInt("RewardIncreaseInterval");
        if (pTag.contains("MaxDifficultyLevel")) this.maxDifficultyLevel = pTag.getInt("MaxDifficultyLevel");
        if (pTag.contains("AllowDifficultyReset")) this.allowDifficultyReset = pTag.getBoolean("AllowDifficultyReset");
        if (pTag.contains("AllowWaterAndAirSpawn")) this.allowWaterAndAirSpawn = pTag.getBoolean("AllowWaterAndAirSpawn");
        if (pTag.contains("IndividualPlayerStats")) this.individualPlayerStats = pTag.getBoolean("IndividualPlayerStats");
        if (pTag.contains("NightTime")) this.nightTime = pTag.getBoolean("NightTime");
        if (pTag.contains("EnableRain")) this.enableRain = pTag.getBoolean("EnableRain");
        if (pTag.contains("EnableMobItemDrop")) this.enableMobItemDrop = pTag.getBoolean("EnableMobItemDrop");
        if (pTag.contains("RewardItem")) this.rewardItem = pTag.getString("RewardItem");

        if (pTag.contains("RecordItem")) {
            this.recordItem = ItemStack.of(pTag.getCompound("RecordItem"));
        }

        if (pTag.contains("PlayerDifficulty")) {
            CompoundTag difficultyTag = pTag.getCompound("PlayerDifficulty");
            for (String key : difficultyTag.getAllKeys()) {
                playerDifficulty.put(key, difficultyTag.getInt(key));
            }
        }

        if (pTag.contains("MobValues")) {
            CompoundTag mobValuesTag = pTag.getCompound("MobValues");
            this.mobValues = new LinkedHashMap<>();
            for (String key : mobValuesTag.getAllKeys()) {
                mobValues.put(key, mobValuesTag.getInt(key));
            }
        }

        if (SkyArenaConfig.configData != null) {
            ArenaConfig arenaConfig = SkyArenaConfig.configData.arenas.getOrDefault(this.arenaType, null);
            if (arenaConfig != null) {
                loadArenaConfig(this.arenaType);
            }
        }
    }

    public void setPlayerDeath(boolean death) {
        PlayerDeath = death;
    }

    public int getBattleDelay() {
        return BattleDelay;
    }

    public void setBattleDelay(int battleDelay) {
        this.BattleDelay = battleDelay;
    }

    public void startMusic() {
        if (this.level != null && !recordItem.isEmpty() && !isPlayingMusic) {
            this.musicTickCount = this.level.getGameTime();
            RecordItem record = (RecordItem) recordItem.getItem();
            this.musicEndTick = this.musicTickCount + record.getLengthInTicks() + 20L;
            this.isPlayingMusic = true;
            this.level.levelEvent(null, 1010, this.getBlockPos(), Item.getId(recordItem.getItem())); // Используем предмет пластинки
            this.setChanged();
        }
    }

    public void stopMusic() {
        if (this.level != null && isPlayingMusic) {
            this.isPlayingMusic = false;
            this.level.levelEvent(1011, this.getBlockPos(), 0); // Останавливаем музыку
            this.setChanged();
        }
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (this.isPlayingMusic && this.level != null && !recordItem.isEmpty()) {

            if (this.musicTickCount >= this.musicEndTick) {
                stopMusic();
                startMusic();
            }
            ++this.musicTickCount;
        }

        if (activatingPlayer != null && battlePhaseActive) {
            double distance = activatingPlayer.distanceToSqr(pPos.getX(), pPos.getY(), pPos.getZ());

            if (distance > battleLossDistance * battleLossDistance) {
                removeSummonedMobs();
                toggleBattlePhase();
                this.stopMusic();
                removeAltarActivationForPlayer(/*player*/);
                PlayerDeath = false;
                DeathDelay = 0;

                if (activatingPlayer instanceof ServerPlayer serverPlayer) {
                    if (Config.enableLossMessageLeave) {
                        Component title = Component.translatable("message.skyarena.battle_failed").withStyle(ChatFormatting.DARK_RED);
                        Component subtitle = Component.translatable("message.skyarena.left_arena").withStyle(ChatFormatting.WHITE);
                        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(title));
                        serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                        serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 10)); // Время появления и исчезновения
                    }
                }
            }
        }

        if (PlayerDeath){
            DeathDelay++;

            if (activatingPlayer != null && DeathDelay > 10) {
                // Проверяем, использовался ли тотем бессмертия
                if (activatingPlayer.getHealth() > 0) {
                    PlayerDeath = false;
                    DeathDelay = 0;
                    return;
                }
                removeSummonedMobs();
                toggleBattlePhase();
                this.stopMusic();
                removeAltarActivationForPlayer(/*player*/);
                PlayerDeath = false;
                DeathDelay = 0;

                if (activatingPlayer instanceof ServerPlayer serverPlayer) {
                    if (Config.enableLossMessageDeath) {
                        Component title = Component.translatable("message.skyarena.wasted").withStyle(ChatFormatting.WHITE);
                        Component subtitle = Component.translatable("message.skyarena.battle_failed").withStyle(ChatFormatting.DARK_RED);
                        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(title)); // Верхняя строка
                        serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(subtitle)); // Нижняя строка
                        serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 10)); // Время появления и исчезновения
                    }
                }
            }
        }

        if (BattleDelay > 0) {
            BattleDelay--;
        }

        if (activatingPlayer != null && battlePhaseActive) {
            summonedMobs.removeIf(Entity::isRemoved);
            for (Entity entity : summonedMobs) {

                if (entity instanceof NeutralMob neutralMob) {
                    if (!(activatingPlayer.isCreative() || activatingPlayer.isSpectator())) {
                        double mobDistance = activatingPlayer.distanceToSqr(entity.getX(), entity.getY(), entity.getZ());
                        if (mobDistance <= 16 * 16) {
                            if (neutralMob.getTarget() == null || !neutralMob.getTarget().isAlive()) {
                                neutralMob.setTarget(activatingPlayer);
                            }
                        }
                    }
                }

                double mobDistanceToAltar = entity.distanceToSqr(pPos.getX(), pPos.getY(), pPos.getZ());
                if (mobDistanceToAltar > mobTeleportDistance * mobTeleportDistance) {
                    List<BlockPos> spawnPositions = findValidSpawnPositions(pLevel, pPos, activatingPlayer);

                    if (!spawnPositions.isEmpty()) {
                        BlockPos teleportPos = spawnPositions.get(pLevel.random.nextInt(spawnPositions.size()));
                        entity.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5);
                    }
                    continue;
                }
            }
        }
    }

    public List<BlockPos> findValidSpawnPositions(Level level, BlockPos center, Player player) {
        List<BlockPos> validPositions = new ArrayList<>();

        if (!allowWaterAndAirSpawn) {
            for (int x = -mobSpawnRadius; x <= mobSpawnRadius; x++) {
                for (int z = -mobSpawnRadius; z <= mobSpawnRadius; z++) {
                    BlockPos currentPos = center.offset(x, 0, z);

                    if (x * x + z * z <= mobSpawnRadius * mobSpawnRadius && //делаем область кругом, чтобы обрезать углы за ареной
                            level.isEmptyBlock(currentPos) &&
                            level.isEmptyBlock(currentPos.above()) &&
                            level.isEmptyBlock(currentPos.above(2)) &&
                            level.isEmptyBlock(currentPos.north()) && level.isEmptyBlock(currentPos.south()) &&
                            level.isEmptyBlock(currentPos.east()) && level.isEmptyBlock(currentPos.west()) &&
                            !level.isEmptyBlock(currentPos.below()) &&
                            player.blockPosition().distSqr(currentPos) > spawnDistanceFromPlayer * spawnDistanceFromPlayer) {
                        validPositions.add(currentPos);
                    }
                }
            }
            return validPositions;
        }else{
            for (int x = -mobSpawnRadius; x <= mobSpawnRadius; x++) {
                for (int z = -mobSpawnRadius; z <= mobSpawnRadius; z++) {
                    BlockPos currentPos = center.offset(x, 0, z);

                    if (x * x + z * z <= mobSpawnRadius * mobSpawnRadius && //делаем область кругом, чтобы обрезать углы за ареной
                            level.getBlockState(currentPos).getCollisionShape(level, currentPos).isEmpty() &&
                            level.getBlockState(currentPos.above()).getCollisionShape(level, currentPos.above()).isEmpty() &&
                            level.getBlockState(currentPos.above(2)).getCollisionShape(level, currentPos.above(2)).isEmpty() &&
                            level.getBlockState(currentPos.north()).getCollisionShape(level, currentPos.north()).isEmpty() &&
                            level.getBlockState(currentPos.south()).getCollisionShape(level, currentPos.south()).isEmpty() &&
                            level.getBlockState(currentPos.east()).getCollisionShape(level, currentPos.east()).isEmpty() &&
                            level.getBlockState(currentPos.west()).getCollisionShape(level, currentPos.west()).isEmpty() &&
                            player.blockPosition().distSqr(currentPos) > spawnDistanceFromPlayer * spawnDistanceFromPlayer) {
                        validPositions.add(currentPos);
                    }
                }
            }
            return validPositions;
        }
    }

    public void setGlowingCounter(int glowingCounter) {
        this.glowingCounter = glowingCounter;
    }

    public void applyGlowEffectToSummonedMobs(Player pPlayer) {
        glowingCounter++;

        if (glowingCounter > 4) {
            int duration = 12000; // Длительность эффекта в тиках (10 минут)
            int amplifier = 0; // Усиление эффекта (0 — базовый уровень)
            glowingCounter = 0;

            int affectedMobs = 0;

            for (Entity mob : summonedMobs) {
                if (mob instanceof LivingEntity livingMob) {
                    livingMob.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, amplifier, false, false));
                    affectedMobs++;
                }
            }
            if (Config.enableUnclaimedRewardMessage) {
                if (!firstMessageSent) {
                    pPlayer.displayClientMessage(Component.translatable("message.skyarena.unclaimed_reward"), false);
                    firstMessageSent = true;
                } else {
                    pPlayer.displayClientMessage(Component.translatable("message.skyarena.mobs_glowing", affectedMobs), true);
                }
            }
        }
    }

    public int getStartingPoints() {
        return startingPoints;
    }

    public int getPointsIncrease() {
        return pointsIncrease;
    }

    public int getSpawnDistanceFromPlayer() {
        return spawnDistanceFromPlayer;
    }

    public int getBattleLossDistance() {
        return battleLossDistance;
    }

    public int getMobTeleportDistance() {
        return mobTeleportDistance;
    }

    public boolean isAllowWaterAndAirSpawn() {
        return allowWaterAndAirSpawn;
    }

    public boolean isIndividualPlayerStats() {
        return individualPlayerStats;
    }

    public int getMobSpawnRadius() {
        return mobSpawnRadius;
    }

    public int getMobCostRatio() {
        return mobCostRatio;
    }

    public int getBaseScalingThreshold() {
        return baseScalingThreshold;
    }

    public double getMobStatGrowthCoefficient() {
        return mobStatGrowthCoefficient;
    }

    public double getSquadSpawnChance() {
        return squadSpawnChance;
    }

    public int getSquadSpawnSize() {
        return squadSpawnSize;
    }

    public boolean isNightTime() {
        return nightTime;
    }

    public boolean isEnableRain() {
        return enableRain;
    }

    public boolean isEnableMobItemDrop() {
        return enableMobItemDrop;
    }

    public String getRewardItem() {
        return rewardItem;
    }

    public int getRewardIncreaseInterval() {
        return rewardIncreaseInterval;
    }

    public int getMaxDifficultyLevel() {
        return maxDifficultyLevel;
    }

    public boolean isAllowDifficultyReset() {
        return allowDifficultyReset;
    }
}
