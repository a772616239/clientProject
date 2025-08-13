package model.battle.pve;

import cfg.GloryRoadConfig;
import common.GameConst;
import java.util.List;
import model.battle.AbstractPveBattleController;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;

/**
 * @author huhan
 * @date 2021/4/2
 */
public class GloryRoadPveBattleController extends AbstractPveBattleController {

    public static final String OPPONENT_IDX = "opponentIdx";

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, Builder resultBuilder) {
        EventUtil.gloryRoadBattleResult(getPlayerIdx(), getCampPlayerIdx(2), realResult.getWinnerCamp(), String.valueOf(getBattleId()));
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_GloryRoad;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_GloryRoad;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_GloryRoad;
    }

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (CollectionUtils.isEmpty(enterParams)) {
            return false;
        }
        putEnterParam(OPPONENT_IDX, enterParams.get(0));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        setFightMakeId(GloryRoadConfig.getById(GameConst.CONFIG_ID).getPvefightmakeid());
        return addPlayerBattleData(getEnterParam(OPPONENT_IDX), getUseTeamType(), 2, true);
    }

    @Override
    protected void initSuccess() {

    }

    @Override
    public int getPointId() {
        return 0;
    }
}
