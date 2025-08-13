package model.battle.pve;

import cfg.NewForeignInvasionBuildingsConfig;
import cfg.NewForeignInvasionWaveConfig;
import cfg.NewForeignInvasionWaveConfigObject;
import common.GameConst;
import common.SyncExecuteFunction;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.foreignInvasion.dbCache.foreigninvasionCache;
import model.foreignInvasion.entity.foreigninvasionEntity;
import model.foreignInvasion.newVersion.NewForeignInvasionManager;
import model.mainLine.dbCache.mainlineCache;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.GamePlayLog;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleRemainPet;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.NewForeignInvasion.NewForeignInvasionPlayerBuildingInfo;
import protocol.NewForeignInvasion.NewForeignInvasionStatusEnum;
import protocol.NewForeignInvasionDB.DB_NewForeignInvasionBuildingMonsterBaseInfo;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2020.11.11
 */
public class NewForeignInvasionPveBattleController extends AbstractPveBattleController {

    public static final String BUILDING_ID = "buildingId";

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (CollectionUtils.size(enterParams) < 1) {
            return false;
        }
        putEnterParam(BUILDING_ID, enterParams.get(0));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        if (PlayerUtil.queryFunctionLock(getPlayerIdx(), EnumFunction.NewForeignInvasion)) {
            return RetCodeEnum.RCE_FunctionNotUnLock;
        }

        if (NewForeignInvasionManager.getInstance().getCurStatus() != NewForeignInvasionStatusEnum.NFISE_OPEN) {
            return RetCodeEnum.RCE_ForInv_StatusMismatching;
        }

        int buildingId = getIntEnterParam(BUILDING_ID);
        if (NewForeignInvasionManager.getInstance().getRemainWave(buildingId) <= 0) {
            return RetCodeEnum.RCE_ForInv_BuildingFreed;
        }

        foreigninvasionEntity entity = foreigninvasionCache.getInstance().getEntity(getPlayerIdx());
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        NewForeignInvasionPlayerBuildingInfo.Builder builder = entity.getBuildingBuilder(buildingId);
        DB_NewForeignInvasionBuildingMonsterBaseInfo monsterBaseInfo = entity.getMonsterBaseInfo(buildingId);

        BattlePlayerInfo.Builder battlePlayerInfo = BattlePlayerInfo.newBuilder();
        battlePlayerInfo.setCamp(2);

        List<BattlePetData> battlePetData = BattleUtil.setMonsterRemainHp(builder.getCurWaveMonsterInfoList()
                , builder.getMonsterRemainHpList().stream().collect(Collectors.toMap(BattleRemainPet::getPetId, e -> e)));

        if (CollectionUtils.isNotEmpty(battlePetData)) {
            battlePlayerInfo.addAllPetList(battlePetData);
        }
        battlePlayerInfo.setPlayerInfo(monsterBaseInfo.getBaseInfo());
        battlePlayerInfo.setIsAuto(true);
        addPlayerBattleData(battlePlayerInfo.build());

        setFightMakeId(NewForeignInvasionBuildingsConfig.getByBuildingid(buildingId).getFightmake());
        if (monsterBaseInfo.hasExProperty()) {
            addExtendProp(monsterBaseInfo.getExProperty());
        }
        return RetCodeEnum.RCE_Success;
    }

    @Override
    protected void initSuccess() {
        //目标：累积参加外敌入侵(战斗即算,不论胜负)
        EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TEE_Foreign_CumuJoin, 1, 0);
        LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), EnumFunction.ForeignInvasion));
    }

    @Override
    public int getPointId() {
        return getIntEnterParam(BUILDING_ID);
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        boolean win = battleResult.getWinnerCamp() == getCamp();
        if (!win) {
            return null;
        }

        List<Reward> rewards = mainlineCache.getInstance().calculateOnHookRewards(getPlayerIdx(), getRewardValidTime(getPlayerIdx()));

        //战斗只发放金币
        List<Reward> goldRewards = null;
        if (CollectionUtils.isNotEmpty(rewards)) {
            goldRewards = rewards.stream().filter(this::battleReward).collect(Collectors.toList());
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ForeignInvasion);
        RewardManager.getInstance().doRewardByList(getPlayerIdx(), goldRewards, reason, false);

        return goldRewards;
    }

    /**
     * 只发放生命石金币
     * @param e
     * @return
     */
    private boolean battleReward(Reward e) {
        return e.getRewardType() == RewardTypeEnum.RTE_Gold ||
                (e.getRewardType() == RewardTypeEnum.RTE_Item && e.getId() == GameConst.ITEM_ID_LIFE_STONE);
    }

    private long getRewardValidTime(String playerIdx) {
        foreigninvasionEntity entity = foreigninvasionCache.getInstance().getEntity(getPlayerIdx());
        if (entity == null) {
            return 0L;
        }

        int buildingId = getIntEnterParam(BUILDING_ID);
        int curWave = entity.getBuildingBuilder(getIntEnterParam(BUILDING_ID)).getCurWave();

        NewForeignInvasionWaveConfigObject waveCfg = NewForeignInvasionWaveConfig.getInstance().getBuildingWaveCfg(buildingId, curWave);
        if (waveCfg == null) {
            return 0L;
        }

        return waveCfg.getBattlerewards() * TimeUtil.MS_IN_A_S;
    }


    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, Builder resultBuilder) {
        foreigninvasionEntity entity = foreigninvasionCache.getInstance().getEntity(getPlayerIdx());
        if (entity == null) {
            return;
        }

        boolean win = realResult.getWinnerCamp() == getCamp();
        int buildingId = getIntEnterParam(BUILDING_ID);

        int newKillMonsterCount = SyncExecuteFunction.executeFunction(entity, e -> {
            //计算新击杀的怪物数量
            NewForeignInvasionPlayerBuildingInfo.Builder buildingBuilder = entity.getBuildingBuilder(buildingId);
            int newKillCount = (int) realResult.getRemainPetList().stream()
                    .filter(hpRemain -> hpRemain.getCamp() == 2 && hpRemain.getRemainHpRate() <= 0)
                    .count();

            //更新积分
            entity.updateScore(buildingId, win, newKillCount);
            entity.updateRemainHp(getIntEnterParam(BUILDING_ID), win, realResult.getRemainPetList());

            if (win) {
                entity.initNextWaveMonsterAndBaseInfo(buildingId);
            }

            entity.sendPlayerBuildingsInfo(Collections.singletonList(buildingId));
            return newKillCount;
        });

        //移除阵亡的宠物
        EventUtil.removeDeadPetFromTeam(getPlayerIdx(), getUseTeamType());

        NewForeignInvasionManager.getInstance().killMonster(getPlayerIdx(), buildingId, newKillMonsterCount);
        NewForeignInvasionManager.getInstance().sendPlayerRankingMarquee(getPlayerIdx());
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_NewForeignInvasion;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_ForeignInvasion;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_ForeignInvasion;
    }
}
