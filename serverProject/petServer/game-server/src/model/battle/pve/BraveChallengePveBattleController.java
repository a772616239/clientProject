package model.battle.pve;

import cfg.BraveChallengePoint;
import cfg.BraveChallengePointObject;
import common.GlobalData;
import common.SyncExecuteFunction;
import java.util.List;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.battle.PreBattleCheckRet;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.bravechallenge.entity.bravechallengeEntity;
import model.reward.RewardManager;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.StatisticsLogUtil;
import platform.logs.entity.BraveChallengeLog;
import platform.logs.entity.GamePlayLog;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.PlayerBaseInfo;
import protocol.Battle.SC_BattleResult;
import protocol.BraveChallenge.BravePoint;
import protocol.BraveChallenge.SC_BraveChallengeComplete;
import protocol.BraveChallengeDB.DB_ChallengeProgress;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/04/26
 */
public class BraveChallengePveBattleController extends AbstractPveBattleController {

    private final String POINT_PRO = "pointPro";

    private int forwardProgress;


    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (GameUtil.collectionIsEmpty(enterParams)) {
            return false;
        }
        putEnterParam(POINT_PRO, enterParams.get(0));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        bravechallengeEntity entity = bravechallengeCache.getInstance().getEntityByPlayer(getPlayerIdx());
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        forwardProgress = getIntEnterParam(POINT_PRO);
        BraveChallengePointObject pointCfg = BraveChallengePoint.getById(forwardProgress);
        if (pointCfg == null || pointCfg.getPointtype() != 1
                || forwardProgress != (entity.getProgressBuilder().getProgress() + 1)) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        BravePoint bravePoint = entity.getProgressBuilder().getPointCfgMap().get(forwardProgress);
        if (bravePoint == null) {
            LogUtil.error("BreveChallengePveBattleController.initFightInfo, can not get point monster cfg, playerIdx:"
                    + getPlayerIdx() + ", point id:" + forwardProgress);
            return RetCodeEnum.RCE_UnknownError;
        }

        BattlePlayerInfo.Builder battlePlayerInfo = BattlePlayerInfo.newBuilder();
        battlePlayerInfo.setCamp(2);

        List<BattlePetData> battlePetData = BattleUtil.setMonsterRemainHp(bravePoint.getEnemyPetsList(), entity.getProgressBuilder().getBossRemainHpMap());
        if (CollectionUtils.isNotEmpty(battlePetData)) {
            battlePlayerInfo.addAllPetList(battlePetData);
        }
        PlayerBaseInfo monsterBaseInfo = entity.getProgressBuilder().getMonsterInfoMap().get(forwardProgress);
        if (monsterBaseInfo == null) {
            LogUtil.error("BreveChallengePveBattleController.initFightInfo, can not get monsterBaseInfo, playerIdx:"
                    + getPlayerIdx() + ", point id:" + forwardProgress);
            return RetCodeEnum.RCE_UnknownError;
        }

        battlePlayerInfo.setPlayerInfo(monsterBaseInfo);
        battlePlayerInfo.setIsAuto(true);
        addPlayerBattleData(battlePlayerInfo.build());

        setFightMakeId(pointCfg.getFightmake());
        if (entity.getProgressBuilder().hasMonsterExProperty()) {
            addExtendProp(entity.getProgressBuilder().getMonsterExProperty());
        }
        return RetCodeEnum.RCE_Success;
    }

    @Override
    protected void initSuccess() {
        //目标：累积参加勇气试炼战斗
        EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TTE_CumuJoinCourageBattle, 1, 0);
        LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), EnumFunction.CourageTrial));
    }

    @Override
    public int getPointId() {
        return getIntEnterParam(POINT_PRO);
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardList, SC_BattleResult.Builder resultBuilder) {
        bravechallengeEntity entity = bravechallengeCache.getInstance().getEntityByPlayer(getPlayerIdx());
        if (entity == null) {
            LogUtil.error("tailSettle error,braveChallenge is null by playerId[" + getPlayerIdx() + "]");
            return;
        }

        saveBraveChallengeLog(realResult, rewardList, entity);

        //前后进度不一致视为服务器每日重置进度更新了,这里仅给玩家奖励
        if (forwardProgress != entity.getProgressBuilder().getProgress() + 1) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_ChallengeProgress.Builder progressBuilder = entity.getProgressBuilder();

            //首次战败
            if (bravechallengeCache.isFirstFail(realResult.getWinnerCamp(), progressBuilder)) {
                progressBuilder.setGiftFirstBlood(true);
                GlobalData.getInstance().sendMsg(getPlayerIdx(), protocol.MessageId.MsgIdEnum.SC_BraveChallengeFirstFail_VALUE,
                        protocol.BraveChallenge.SC_BraveChallengeFirstFail.newBuilder());
            }

            //投降
            if (realResult.getWinnerCamp() != 1 && realResult.getWinnerCamp() != 2) {
                return;
            }

            boolean win = realResult.getWinnerCamp() == getCamp();
            entity.updateRemainHp(win, realResult.getRemainPetList());
            if (win) {
                int newProgress = getIntEnterParam(POINT_PRO);
                entity.getProgressBuilder().setProgress(newProgress);


                //完成所有关卡
                if (newProgress >= BraveChallengePoint.maxPoint) {
                    GlobalData.getInstance().sendMsg(getPlayerIdx(), MsgIdEnum.SC_BraveChallengeComplete_VALUE, SC_BraveChallengeComplete.newBuilder());
                    EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TTE_CumuPassCouragePoint, 1, 0);
                }
            }
            //今日已挑战
            entity.getProgressBuilder().setTodayChallenge(true);

            entity.sendProgress();
        });


        //非投降情况下清除勇气试炼小队已阵亡的宠物
        if (realResult.getWinnerCamp() == 1 || realResult.getWinnerCamp() == 2) {
            EventUtil.removeDeadPetFromTeam(getPlayerIdx(), getUseTeamType());
        }
    }

    private void saveBraveChallengeLog(CS_BattleResult realResult, List<Reward> rewardList, bravechallengeEntity entity) {
        BraveChallengePointObject pointCfg = BraveChallengePoint.getById(forwardProgress);
        if (pointCfg == null) {
            LogUtil.error("error in BraveChallengeServiceImpl, method battleSettle(), playerId = " + getPlayerIdx() +
                    ", progress = " + forwardProgress);
            return;
        }

        //日志
        LogService.getInstance().submit(new BraveChallengeLog(getPlayerIdx(), entity.getClientProgress(), rewardList,
                StatisticsLogUtil.getBattleResultByWinCamp(realResult.getWinnerCamp()), pointCfg.getFightmake()));
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_BreaveChallenge;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_BraveChallenge;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_CourageTrial;
    }

    @Override
    public PreBattleCheckRet preBaseCheck(CS_BattleResult clientResult) {
        PreBattleCheckRet preRet = super.preBaseCheck(clientResult);
        preRet.setMustCheck(true);
        return preRet;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        if (battleResult.getWinnerCamp() != getCamp()) {
            return null;
        }

        bravechallengeEntity braveEntity = bravechallengeCache.getInstance().getEntityByPlayer(getPlayerIdx());
        if (braveEntity == null) {
            return null;
        }

        return SyncExecuteFunction.executeFunction(braveEntity, e -> {
            BravePoint point = braveEntity.getProgressBuilder().getPointCfgMap().get(getIntEnterParam(POINT_PRO));
            if (point == null) {
                return null;
            }
            List<Reward> rewardsList = point.getRewardsList();
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_BraveChallenge);
            RewardManager.getInstance().doRewardByList(getPlayerIdx(), rewardsList, reason, false);
            return rewardsList;
        });
    }
}
