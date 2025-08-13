/**
 * created by tool DAOGenerate
 */
package model.foreignInvasion.entity;


import cfg.Head;
import cfg.MonsterDifficulty;
import cfg.MonsterDifficultyObject;
import cfg.NewForeignInvasionBuildingsConfig;
import cfg.NewForeignInvasionWaveConfig;
import cfg.NewForeignInvasionWaveConfigObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetRarityConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.RankingName;
import common.GlobalData;
import common.IdGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import model.battle.BattleUtil;
import model.foreignInvasion.dbCache.foreigninvasionCache;
import model.foreignInvasion.newVersion.NewForeignInvasionManager;
import model.obj.BaseObj;
import model.pet.dbCache.petCache;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Activity.EnumRankingType;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleRemainPet;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PlayerBaseInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.NewForeignInvasion.NewForeignInvasionPlayerBuildingInfo;
import protocol.NewForeignInvasion.SC_RefreshNewForeignInvasionPlayerBuildingInfo;
import protocol.NewForeignInvasionDB.DB_NewForeignInvasionBuildingMonsterBaseInfo;
import protocol.NewForeignInvasionDB.DB_NewForeignInvasionPlayerInfo;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Pet.Builder;
import util.LogUtil;
import util.ObjUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class foreigninvasionEntity extends BaseObj {

    public String getClassType() {
        return "foreigninvasionEntity";
    }

    @Override
    public void putToCache() {
        foreigninvasionCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.info = getDbBuilder().build().toByteArray();
    }

    /**
     *
     */
    private String playeridx;

    /**
     *
     */
    private byte[] info;


    /**
     * 获得
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得
     */
    public byte[] getInfo() {
        return info;
    }

    /**
     * 设置
     */
    public void setInfo(byte[] info) {
        this.info = info;
    }


    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return playeridx;
    }

    public foreigninvasionEntity(){}

    public foreigninvasionEntity(String playerIdx) {
        this.playeridx = playerIdx;
    }

    private DB_NewForeignInvasionPlayerInfo.Builder dbBuilder;

    public DB_NewForeignInvasionPlayerInfo.Builder getDbBuilder() {
        if (this.dbBuilder == null) {
            if (this.info != null) {
                try {
                    this.dbBuilder = DB_NewForeignInvasionPlayerInfo.parseFrom(this.info).toBuilder();
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.printStackTrace(e);
                }
            }
        }

        if (this.dbBuilder == null) {
            this.dbBuilder = DB_NewForeignInvasionPlayerInfo.newBuilder();
        }

        return dbBuilder;
    }

    /**
     * 更新剩余血量
     *
     * @param win
     * @param remainHp
     */
    public void updateRemainHp(int buildingId, boolean win, Collection<BattleRemainPet> remainHp) {
        if (NewForeignInvasionBuildingsConfig.getByBuildingid(buildingId) == null
                || CollectionUtils.isEmpty(remainHp)) {
            return;
        }

        //根据阵营分类
        Map<Integer, List<BattleRemainPet>> collect = remainHp.stream().collect(Collectors.groupingBy(BattleRemainPet::getCamp));
        if (MapUtils.isEmpty(collect)) {
            return;
        }

        //宠物血量
        List<BattleRemainPet> remainPets = collect.get(1);
        if (CollectionUtils.isNotEmpty(remainPets)) {
            remainPets.forEach(e -> getDbBuilder().putPetsRemainHp(e.getPetId(), e));
        }

        //怪物血量
        NewForeignInvasionPlayerBuildingInfo.Builder buildingBuilder = getBuildingBuilder(buildingId);
        if (!win) {
            List<BattleRemainPet> newRemainMonsters = collect.get(2);
            if (CollectionUtils.isNotEmpty(newRemainMonsters)) {
                Set<String> update = newRemainMonsters.stream()
                        .map(BattleRemainPet::getPetId)
                        .collect(Collectors.toSet());

                List<BattleRemainPet> unUpdate = buildingBuilder.getMonsterRemainHpList().stream()
                        .filter(e -> !update.contains(e.getPetId()))
                        .collect(Collectors.toList());

                newRemainMonsters.addAll(unUpdate);
                buildingBuilder.clearMonsterRemainHp();
                buildingBuilder.addAllMonsterRemainHp(newRemainMonsters);
            }
        }
        putBuildingBuilder(buildingBuilder);
    }

    public void putBuildingBuilder(NewForeignInvasionPlayerBuildingInfo.Builder builder) {
        if (builder == null) {
            return;
        }

        getDbBuilder().putBuildingsInfo(builder.getBuildingId(), builder.build());
    }

    public NewForeignInvasionPlayerBuildingInfo.Builder getBuildingBuilder(int buildId) {
        NewForeignInvasionPlayerBuildingInfo info = getDbBuilder().getBuildingsInfoMap().get(buildId);
        return info == null ? initMonsterAndBaseInfo(buildId, 1) : info.toBuilder();
    }

    /**
     * 初始化建筑的怪物不用发送到客户端的消息信息,此方法会将初始化的信息存入DBBuilder
     *
     * @param waveCfg
     */
    private DB_NewForeignInvasionBuildingMonsterBaseInfo.Builder initMonsterBaseInfo(MonsterDifficultyObject diffCfg, NewForeignInvasionWaveConfigObject waveCfg) {
        if (waveCfg == null || diffCfg == null) {
            LogUtil.error("foreigninvasionEntity.initMonsterBaseInfo, cfg is null");
            return null;
        }
        DB_NewForeignInvasionBuildingMonsterBaseInfo.Builder builder = DB_NewForeignInvasionBuildingMonsterBaseInfo.newBuilder();
        builder.setBuildingId(waveCfg.getLinkbuildingid());

        int baseLv = calculateLv(diffCfg.getNewforeigninvasionmonsterbaselv(), waveCfg.getLvaddition());
        builder.setBaseInfo(initMonsterBaseInfo(baseLv));
        ExtendProperty.Builder exPropertyAdjust = BattleUtil.builderMonsterExtendProperty(2,
                BattleUtil.getMonsterMainLineAdjustByNodeId(NewForeignInvasionManager.getInstance().getMainlineNode()), waveCfg.getExpropertyaddition());

        if (exPropertyAdjust != null) {
            builder.setExProperty(exPropertyAdjust);
        }
        return builder;
    }

    private PlayerBaseInfo.Builder initMonsterBaseInfo(int baseLv) {
        PlayerBaseInfo.Builder resultBuilder = PlayerBaseInfo.newBuilder();
        resultBuilder.setPlayerId(IdGenerator.getInstance().generateId());
        String playerName = ObjUtil.createRandomName(PlayerUtil.queryPlayerLanguage(getPlayeridx()));
        resultBuilder.setPlayerName(playerName);
        resultBuilder.setAvatar(Head.randomGetAvatar());
        resultBuilder.setLevel(baseLv);
        return resultBuilder;
    }

    private List<BattlePetData> initMonsterPets(MonsterDifficultyObject diffCfg, NewForeignInvasionWaveConfigObject cfg) {
        if (diffCfg == null || cfg == null) {
            return null;
        }

        int petLv = calculateLv(NewForeignInvasionManager.getInstance().getPetLv(), cfg.getLvaddition());
        List<Pet.Builder> petList = initPointPetDate(diffCfg.getNewforeigninvasionmonster(), petLv, diffCfg.getNewforeigninvasionmonsterrarity());

        return petCache.getInstance().buildPetBattleData(null,
                petList.stream().map(Builder::build).collect(Collectors.toList()), BattleSubTypeEnum.BSTE_NewForeignInvasion,false);
    }

    /**
     * 计算宠物最终等级
     *
     * @param baseLv
     * @param lvAddition
     */
    private int calculateLv(int baseLv, int lvAddition) {
        int newLv = (baseLv * lvAddition) / 100;
        return Math.max(Math.min(PetRarityConfig.maxLv, newLv), 1);
    }

    public List<Pet.Builder> initPointPetDate(int[][] petRarityCount, int petLv, int petRarity) {
        if (petRarityCount == null) {
            LogUtil.error("bravechallengeEntity.initPointPetDate, petRarityCount is null");
            return null;
        }

        List<Pet.Builder> petList = new ArrayList<>();
        for (int[] petCount : petRarityCount) {
            if (petCount.length < 2) {
                LogUtil.error("bravechallengeEntity.initPoint, pet count cfg length is not enough");
                continue;
            }

            for (int i = 0; i < petCount[1]; i++) {
                PetBasePropertiesObject randomPet = PetBaseProperties.randomBravePetByStartRarity(petCount[0]);
                if (randomPet == null) {
                    LogUtil.error("bravechallengeEntity.initPoint, random pet by rarity failed, rarity:" + petCount[0]);
                    continue;
                }

                Pet.Builder petBuilder = petCache.getInstance().getPetBuilder(randomPet, 0);
                if (petBuilder == null) {
                    continue;
                }

                petBuilder.setPetLvl(Math.max(1, petLv));
                petBuilder.setPetRarity(petRarity);
                Pet.Builder newPetBuilder = petCache.getInstance().refreshPetData(petBuilder, null);
                petList.add(newPetBuilder);
            }
        }
        return petList;
    }


    public void sendBuildingsInfo() {
        sendPlayerBuildingsInfo(NewForeignInvasionBuildingsConfig._ix_buildingid.keySet());
    }

    public void sendPlayerBuildingsInfo(Collection<Integer> buildingIds) {
        SC_RefreshNewForeignInvasionPlayerBuildingInfo.Builder builder = SC_RefreshNewForeignInvasionPlayerBuildingInfo.newBuilder();
        builder.setScore(getDbBuilder().getTotalScore());
        for (Integer buildingId : buildingIds) {
            NewForeignInvasionPlayerBuildingInfo.Builder buildingBuilder = getBuildingBuilder(buildingId);
            if (buildingBuilder != null) {
                builder.addPlayerBuildingInfo(buildingBuilder);
            }

        }
        builder.addAllPetsRemainHp(getDbBuilder().getPetsRemainHpMap().values());
        builder.setRestoreTimes(getDbBuilder().getRestoreTimes());
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_RefreshNewForeignInvasionPlayerBuildingInfo_VALUE, builder);
    }

    public int getPetRemainHp(String petId) {
        if (StringUtils.isEmpty(petId)) {
            return 0;
        }
        BattleRemainPet remainPet = getDbBuilder().getPetsRemainHpMap().get(petId);
        return remainPet == null ? GameConst.PetMaxHpRate : remainPet.getRemainHpRate();
    }

    public DB_NewForeignInvasionBuildingMonsterBaseInfo getMonsterBaseInfo(int buildingId) {
        DB_NewForeignInvasionBuildingMonsterBaseInfo info = getDbBuilder().getMonsterBaseInfoMap().get(buildingId);
        if (info != null) {
            return info;
        }

        NewForeignInvasionPlayerBuildingInfo.Builder builder = getBuildingBuilder(buildingId);
        MonsterDifficultyObject diffCfg = MonsterDifficulty.getById(NewForeignInvasionManager.getInstance().getMainlineNode());
        NewForeignInvasionWaveConfigObject waveCfg = NewForeignInvasionWaveConfig.getInstance().getBuildingWaveCfg(buildingId, builder.getCurWave());
        DB_NewForeignInvasionBuildingMonsterBaseInfo.Builder baseInfoBuilder = initMonsterBaseInfo(diffCfg, waveCfg);
        if (baseInfoBuilder == null) {
            LogUtil.error("foreigninvasionEntity.getMonsterBaseInfo, can not init monsterBaseInfo," +
                    " playerIdx:" + getPlayeridx() + ", buildingId:" + buildingId + ", wave:" + builder.getCurWave());
            return null;
        }

        DB_NewForeignInvasionBuildingMonsterBaseInfo result = baseInfoBuilder.build();
        getDbBuilder().putMonsterBaseInfo(buildingId, result);
        return result;
    }

    public void initNextWaveMonsterAndBaseInfo(int buildingId) {
        NewForeignInvasionPlayerBuildingInfo.Builder builder = getBuildingBuilder(buildingId);
        initMonsterAndBaseInfo(buildingId, builder.getCurWave() + 1);
    }

    public NewForeignInvasionPlayerBuildingInfo.Builder initMonsterAndBaseInfo(int buildingId, int nextWave) {
        MonsterDifficultyObject diffCfg = MonsterDifficulty.getById(NewForeignInvasionManager.getInstance().getMainlineNode());
        NewForeignInvasionWaveConfigObject waveCfg
                = NewForeignInvasionWaveConfig.getInstance().getBuildingWaveCfg(buildingId, nextWave);
        return initMonsterAndBaseInfo(diffCfg, waveCfg);
    }

    /**
     * @return 返回怪物信息
     */
    public NewForeignInvasionPlayerBuildingInfo.Builder initMonsterAndBaseInfo(MonsterDifficultyObject diffCfg, NewForeignInvasionWaveConfigObject waveCfg) {
        if (diffCfg == null || waveCfg == null) {
            return null;
        }

        //初始化基础信息
        DB_NewForeignInvasionBuildingMonsterBaseInfo.Builder baseInfoBuilder = initMonsterBaseInfo(diffCfg, waveCfg);
        if (baseInfoBuilder == null) {
            LogUtil.error("foreigninvasionEntity.initMonsterAndBaseInfo, playerIdx:" + getPlayeridx()
                    + ", buildingId:" + waveCfg.getLinkbuildingid() + ", wave:" + waveCfg.getWave());
            return null;
        }
        getDbBuilder().putMonsterBaseInfo(waveCfg.getLinkbuildingid(), baseInfoBuilder.build());


        //初始化怪物消息
        NewForeignInvasionPlayerBuildingInfo.Builder resultBuilder = NewForeignInvasionPlayerBuildingInfo.newBuilder();
        resultBuilder.setBuildingId(waveCfg.getLinkbuildingid());
        resultBuilder.setCurWave(waveCfg.getWave());

        List<BattlePetData> monster = initMonsterPets(diffCfg, waveCfg);
        if (CollectionUtils.isNotEmpty(monster)) {
            resultBuilder.addAllCurWaveMonsterInfo(monster);
        }

        getDbBuilder().putBuildingsInfo(resultBuilder.getBuildingId(), resultBuilder.build());
        return resultBuilder;
    }

    /**
     * 单个建筑积分(全量)：当前波次*150+玩家等级*2+累计击杀怪物数*7
     *
     * @param buildingId
     * @param newAdd     新增的击杀个数
     */
    public void updateScore(int buildingId, boolean win, int newAdd) {
        NewForeignInvasionPlayerBuildingInfo.Builder builder = getBuildingBuilder(buildingId);
        int scoreWave = builder.getCurWave();
        if (!win) {
            scoreWave -= 1;
        }

        Integer oldKillCount = getDbBuilder().getKillMonsterCountMap().get(buildingId);
        int newKillCount = oldKillCount == null ? newAdd : oldKillCount + newAdd;
        getDbBuilder().putKillMonsterCount(buildingId, newKillCount);

        int playerLv = PlayerUtil.queryPlayerLv(getPlayeridx());

        int buildingNewScore = scoreWave * 150 + playerLv * 2 + newKillCount * 7;

        getDbBuilder().putBuildingScore(buildingId, buildingNewScore);

        int newTotalScore = getDbBuilder().getBuildingScoreMap().values().stream().reduce(Integer::sum).orElse(0);
        getDbBuilder().setTotalScore(newTotalScore);

        //更新排行榜
        RankingManager.getInstance().updatePlayerRankingScore(getPlayeridx(),
                EnumRankingType.ERT_NewForeignInvasion, RankingName.RN_New_ForInv_Score, newTotalScore);
    }
}