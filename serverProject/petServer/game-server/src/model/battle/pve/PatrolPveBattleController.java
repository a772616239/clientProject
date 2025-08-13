package model.battle.pve;

import common.GameConst.EventType;
import java.util.Collections;
import java.util.List;
import model.battle.AbstractPveBattleController;
import model.patrol.dbCache.patrolCache;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolBattleResult;
import model.patrol.entity.PatrolTree;
import model.patrol.entity.patrolEntity;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.ExtendProperty;
import protocol.Battle.SC_BattleResult;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;
import util.PatrolUtil;

/**
 * @author huhan
 * @date 2020/04/26
 */
public class PatrolPveBattleController extends AbstractPveBattleController {

    private final String X = "x";
    private final String Y = "y";
    private final String IS_ANGER = "isAnger";


    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (enterParams == null || enterParams.size() < 3) {
            return false;
        }
        putEnterParam(X, enterParams.get(0));
        putEnterParam(Y, enterParams.get(1));
        putEnterParam(IS_ANGER, enterParams.get(2));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        PatrolBattleResult result = PatrolServiceImpl.getInstance().getFightMakeId(getPlayerIdx(),
                getIntEnterParam(X), getIntEnterParam(Y), getIntEnterParam(IS_ANGER));
        if (result.isSuccess()) {
            if (result.getBuffList() != null) {
                ExtendProperty.Builder buff = ExtendProperty.newBuilder();
                buff.setCamp(1);
                buff.addAllBuffData(result.getBuffList());
                addExtendProp(buff.build());
            }

            //怪物属性变化
            ExtendProperty.Builder deBuff = ExtendProperty.newBuilder();
            // debuff或者怪物增强阵营为2
            deBuff.setCamp(2);
            if (result.getDebuffList() != null) {
                deBuff.addAllBuffData(result.getDebuffList());
            }
            //属性加强
            ExtendProperty monsterExProperty = result.getMonsterExProperty();
            if (monsterExProperty != null) {
                deBuff.setPropDict(monsterExProperty.getPropDict());
                deBuff.addAllBuffData(monsterExProperty.getBuffDataList());
            }
            addExtendProp(deBuff.build());

            setFightMakeId(result.getMakeId());
            return RetCodeEnum.RCE_Success;
        }
        return result.getCode();
    }

    @Override
    protected void initSuccess() {

    }

    @Override
    public int getPointId() {
        return 0;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        patrolEntity entity = patrolCache.getInstance().getCacheByPlayer(getPlayerIdx());
        if (entity == null || entity.gameFailed()) {
            LogUtil.error("settlePatrolBattle error,patrol empty or game failed ,playerIdx:" + getPlayerIdx());
            return;
        }
        PatrolTree location = PatrolUtil.preOrderByLocation(entity.getPatrolTree(), new PatrolTree(entity.getPatrolStatusEntity().getBattlePoint().getX(), entity.getPatrolStatusEntity().getBattlePoint().getY()));
        if (location == null || !location.ifBattlePoint() || location.ifExplored()) {
            LogUtil.error("settlePatrolBattle,playerIdx[" + getPlayerIdx() + "] location data error");
            return;
        }
        Event event = Event.valueOf(EventType.ET_PATROL_BATTLE_SETTLE, GameUtil.getDefaultEventSource(),
                GameUtil.getDefaultEventSource());
        event.pushParam(getPlayerIdx(), realResult.getWinnerCamp(), rewardListList, location);
        EventManager.getInstance().dispatchEvent(event);
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_Patrol;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_Patrol;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Patrol;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
            return null;
        }

        // 推送奖励更新消息
        String playerId = getPlayerIdx();
        patrolEntity patrol = patrolCache.getInstance().getCacheByPlayer(playerId);
        if (patrol == null) {
            return Collections.emptyList();
        }
        List<Reward> battleReward = PatrolServiceImpl.getInstance().getBattleReward(playerId);
        patrol.sendRewardRefreshMsg();
        return battleReward;
    }

    @Override
    public EnumFunction getFunctionEnum() {
        return EnumFunction.Patrol;
    }

    @Override
    public boolean checkSpeedUp(CS_BattleResult clientResult, long realBattleTime) {
        if (isSkipBattle()) {
            return true;
        }
        return super.checkSpeedUp(clientResult, realBattleTime);
    }

    @Override
    public boolean checkRealResultSpeedUp(CS_BattleResult realResult, long realBattleTime) {
        if(isSkipBattle()) {
            return true;
        }
        return super.checkRealResultSpeedUp(realResult, realBattleTime);
    }
}
