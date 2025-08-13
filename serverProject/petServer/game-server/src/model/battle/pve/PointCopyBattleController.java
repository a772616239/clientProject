package model.battle.pve;

import cfg.PointCopyCfg;
import cfg.PointCopyCfgObject;
import common.SyncExecuteFunction;
import java.util.List;
import model.activity.PointCopyManager;
import model.battle.AbstractPveBattleController;
import model.consume.ConsumeManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.Consume;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TargetSystemDB.DB_PointCopy;
import util.EventUtil;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/04/26
 */
public class PointCopyBattleController extends AbstractPveBattleController {

    private final String MISSION_ID = "missionId";


    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (GameUtil.collectionIsEmpty(enterParams)) {
            return false;
        }
        putEnterParam(MISSION_ID, enterParams.get(0));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        if (!PointCopyManager.getInstance().isOpen()) {
            return RetCodeEnum.RCE_Activity_NotOpen;
        }
        int cfgId = getIntEnterParam(MISSION_ID);
        Consume consume = PointCopyManager.getInstance().getConsumeByCfgId(cfgId);
        if (null == consume) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (!ConsumeManager.getInstance().materialIsEnough(getPlayerIdx(), consume)) {
            return RetCodeEnum.RCE_Itembag_ItemNotEnought;
        }

        setFightMakeId(PointCopyManager.getInstance().getFightMakeIdByCfgId(cfgId));
        return RetCodeEnum.RCE_Success;
    }

//    @Override
//    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
//        if (battleResult.getWinnerCamp() != getCamp()) {
//            return null;
//        }
//
//        PointCopyCfgObject missionCfg = PointCopyCfg.getById(getIntEnterParam(MISSION_ID));
//        if (missionCfg == null) {
//            return null;
//        }
//
//        Reward reward = RewardUtil.parseReward(missionCfg.getPointreward());
//        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PointInstance);
//        RewardManager.getInstance().doReward(getPlayerIdx(), reward, reason, false);
//
//        return Collections.singletonList(reward);
//    }

    @Override
    protected void initSuccess() {
        //目标：累积参加x次积分副本
        EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TEE_PointCopy_CumuJoin, 1, 0);
    }

    @Override
    public int getPointId() {
        return getIntEnterParam(MISSION_ID);
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        if (realResult.getWinnerCamp() != getCamp()) {
            return;
        }
        int missionId = getIntEnterParam(MISSION_ID);
        PointCopyCfgObject cfg = PointCopyCfg.getById(getIntEnterParam(MISSION_ID));
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(getPlayerIdx());
        if (target == null || cfg == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(target, e -> {
            DB_PointCopy.Builder copyBuilder = target.getDb_Builder().getSpecialInfoBuilder().getPointCopyBuilder();

            if (!copyBuilder.getCanSweepIdList().contains(missionId)) {
                copyBuilder.addCanSweepId(missionId);
            }

            //更新新的关卡
            PointCopyCfgObject newPoint = PointCopyCfg.getById(cfg.getWinunlock());
            if (newPoint != null && newPoint.getMissiontype() == 1
                    && !copyBuilder.getUnlockBattleIdList().contains(cfg.getWinunlock())) {
                copyBuilder.addUnlockBattleId(cfg.getWinunlock());
            }

            target.sendPointCopyInfo();
        });

        //消耗道具
        Consume consume = PointCopyManager.getInstance().getConsumeByCfgId(missionId);
        ConsumeManager.getInstance().consumeMaterial(target.getLinkplayeridx(), consume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PointInstance));
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_PointCopy;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_PointInstance;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Common;
    }
}
