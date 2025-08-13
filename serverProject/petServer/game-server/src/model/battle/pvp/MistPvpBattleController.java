package model.battle.pvp;

import common.SyncExecuteFunction;
import java.util.List;
import model.battle.AbstractPvpBattleController;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import platform.logs.LogService;
import platform.logs.entity.MistPvpTimesLog;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PlayerDB.DB_MistForestData;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;

/**
 * @author huhan
 * @date 2020/04/27
 */
public class MistPvpBattleController extends AbstractPvpBattleController {

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    protected void battleLog(int winnerCamp, List<Reward> rewardListList) {

    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        LogService.getInstance().submit(new MistPvpTimesLog(getPlayerIdx()));
        boolean victory = realResult.getWinnerCamp() == getCamp();
        playerEntity entity = playerCache.getByIdx(getPlayerIdx());
        if (entity == null) {
            return;
        }

        boolean canRecordVictory;
        if (victory) {
            final int otherCamp = getCamp() == 1 ? 2 : 1;
            canRecordVictory = targetsystemCache.getInstance().canRecordMistKillPlayerTarget(getPlayerIdx(), getCampPlayerIdx(otherCamp));
        } else {
            canRecordVictory = false;
        }

        //连续击杀处理:玩家在胜利冷却时间内,击败即清空连续击杀标记,连续击杀标记累计有冷却
        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_MistForestData.Builder builder = entity.getDb_data().getMistForestDataBuilder();
            if (victory) {
                if (canRecordVictory) {
//                    builder.setKillPlayerCount(builder.getKillPlayerCount() + 1);
                    builder.setContinuousKillPlayer(builder.getContinuousKillPlayer() + 1);

                    //目标进度
                    EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TTE_CumuMistKillPlayer, 1, 0);
                }
            } else {
                builder.clearContinuousKillPlayer();
            }

            //目标：迷雾深林连续击杀玩家
            if (!victory || canRecordVictory) {
                EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TTE_MistContinuousKillPlayer, builder.getContinuousKillPlayer(), 0);
            }
        });
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_MistForest;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_MistForest;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Common;
    }
}
