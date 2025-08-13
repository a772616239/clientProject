package model.battle.pve;

import common.GameConst.RankingName;
import common.SyncExecuteFunction;
import java.util.List;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.RankingManager;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Activity.EnumRankingType;
import protocol.Battle.BattleRemainPet;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_TheWarData;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_TheWarBattleResult;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.LogUtil;

public class TheWarPveBattleController extends AbstractPveBattleController {
    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        return RetCodeEnum.RCE_Success;
    }

    @Override
    protected void initSuccess() {

    }

    @Override
    public int getPointId() {
        return 0;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, Builder resultBuilder) {
        playerEntity player = playerCache.getByIdx(getPlayerIdx());
        if (player == null) {
            return;
        }
        int newKillMonsterCount = updatePlayerTheWarRanking(player, realResult);
        try {
            GS_CS_TheWarBattleResult.Builder result = GS_CS_TheWarBattleResult.newBuilder();
            result.setPlayerIdx(getPlayerIdx());
            int posX = Integer.parseInt(getEnterParam("posX"));
            int posY = Integer.parseInt(getEnterParam("posY"));
            result.getBattleGridPosBuilder().setX(posX).setY(posY);
            result.setNewKillPetCount(newKillMonsterCount);
            //直接胜利判定为三星
            int fightStar = directVictory() ? 3 : (int) BattleUtil.getFightParamsValue(realResult.getFightParamsList(), FightParamTypeEnum.FPTE_FightStar);

            if (realResult.getWinnerCamp() == 3) {
                fightStar = -2; // 投降为-2
            } else {
                if (realResult.getWinnerCamp() != getCamp()) {
                    fightStar = -1; // 普通失败为-1
                }
                result.addAllRemainMonsters(realResult.getRemainPetList());
            }
            result.setFightStar(fightStar);
            CrossServerManager.getInstance().sendMsgToWarRoom(player.getDb_data().getTheWarRoomIdx(),
                    MsgIdEnum.GS_CS_TheWarBattleResult_VALUE, result);
        } catch (NumberFormatException e) {
            LogUtil.printStackTrace(e);
        }
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_TheWar;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_TheWar;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Null;
    }

    private int updatePlayerTheWarRanking(playerEntity player, CS_BattleResult realResult) {
        if (player == null || realResult == null) {
            return 0;
        }
        //计算玩家更新排行榜
        int newKillCount = (int) realResult.getRemainPetList().stream().filter(e -> e.getCamp() == 2 && e.getRemainHpRate() <= 0).count();

        if (newKillCount > 0) {
            int totalCount = SyncExecuteFunction.executeFunction(player, p -> {
                DB_TheWarData.Builder theWarDataBuilder = player.getDb_data().getTheWarDataBuilder();
                theWarDataBuilder.setKillMonsterCount(theWarDataBuilder.getKillMonsterCount() + newKillCount);
                return theWarDataBuilder.getKillMonsterCount();
            });
            RankingManager.getInstance().updatePlayerRankingScore(player.getIdx(), EnumRankingType.ERT_TheWar_KillMonster,
                    RankingName.RN_TheWar_KillMonsterCount, totalCount);

            EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetTypeEnum.TTE_TheWar_KillMonsterCount, totalCount, 0);
        }

        return newKillCount;
    }

    @Override
    protected boolean directVictory() {
        if (CollectionUtils.isEmpty(getRemainMonsters())) {
            return false;
        }
        for (BattleRemainPet monsterRemainHp : getRemainMonsters()){
            if (monsterRemainHp.getCamp() != getCamp() && monsterRemainHp.getRemainHpRate() > 0) {
                return false;
            }
        }
        return true;
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
