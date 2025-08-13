package model.battle.pve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.GameConst.EventType;
import model.battle.AbstractPveBattleController;
import model.crossarena.CrossArenaManager;
import model.patrol.entity.PatrolBattleResult;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
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

/**
 * @author Hammer
 */
public class CrossArenaBossPveBattleController extends AbstractPveBattleController {

    private final String STAGEID = "stageId";

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
//		if (enterParams == null || enterParams.size() < 2) {
//			return false;
//		}
//		putEnterParam(STAGEID, enterParams.get(0));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {

        int bossFightMakeId = CrossArenaManager.getInstance().getBossFightMakeId(getPlayerIdx());
        if (bossFightMakeId == 0) {
            return RetCodeEnum.RCE_ConfigError;
        }
        PatrolBattleResult result = new PatrolBattleResult();

        result.setMakeId(bossFightMakeId);
        result.setCode(RetCodeEnum.RCE_Success);
        result.setSuccess(true);
//		PatrolBattleResult result = TrainingManager.getInstance().getFightMakeId(getPlayerIdx(), getIntEnterParam(MAPID), getIntEnterParam(POINTID));
        if (result.isSuccess()) {
            if (result.getBuffList() != null) {
                ExtendProperty.Builder buff = ExtendProperty.newBuilder();
                buff.setCamp(1);
                buff.addAllBuffData(result.getBuffList());
                addExtendProp(buff.build());
            }

            // 怪物属性变化
            ExtendProperty.Builder deBuff = ExtendProperty.newBuilder();
            // debuff或者怪物增强阵营为2
            deBuff.setCamp(2);
            if (result.getDebuffList() != null) {
                deBuff.addAllBuffData(result.getDebuffList());
            }
            // 属性加强
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
    public int getPointId() {
        return 0;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        if (realResult.getWinnerCamp() != 1) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_CrossArenaBoss, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(getPlayerIdx());
        EventManager.getInstance().dispatchEvent(event);
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_CrossArenaLeiTaiBoss;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        // TODO BOSS守关怪奖励
        return RewardSourceEnum.RSE_CROSSARENA_GRADE;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_MatchArenaLeiTai;
    }

    @Override
    public EnumFunction getFunctionEnum() {
        return EnumFunction.CrossArena;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        if (battleResult.getWinnerCamp() != 1) {
            return Collections.emptyList();
        }
        List<Reward> totalReward = RewardUtil.getRewardsByFightMakeId(getFightMakeId());
        Reason reason = ReasonManager.getInstance().borrowReason(getRewardSourceType(), getLogExInfo());
        RewardManager.getInstance().doRewardByList(getPlayerIdx(), totalReward, reason, false);
        return totalReward;
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
        if (isSkipBattle()) {
            return true;
        }
        return super.checkRealResultSpeedUp(realResult, realBattleTime);
    }

    @Override
    protected void initSuccess() {

    }
}
