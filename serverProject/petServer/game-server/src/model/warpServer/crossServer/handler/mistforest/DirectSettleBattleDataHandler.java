package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.MistConst;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import platform.logs.LogService;
import platform.logs.entity.FightWithMistBossLog;
import platform.logs.entity.MistPvpTimesLog;
import protocol.Battle.BattleTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_MistForestData;
import protocol.ServerTransfer.CS_GS_MistDirectSettleBattleData;
import protocol.ServerTransfer.EnumMistPveBattleType;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_MistDirectSettleBattleData_VALUE)
public class DirectSettleBattleDataHandler extends AbstractHandler<CS_GS_MistDirectSettleBattleData> {
    @Override
    protected CS_GS_MistDirectSettleBattleData parse(byte[] bytes) throws Exception {
        return CS_GS_MistDirectSettleBattleData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_MistDirectSettleBattleData ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        if (ret.getBattleType() == BattleTypeEnum.BTE_PVE) {
            directSettlePveBattle(ret);
        } else {
            directSettlePvpBattle(player, ret);
        }
    }

    protected void directSettlePveBattle(CS_GS_MistDirectSettleBattleData ret) {
        //目标：迷雾森林胜利
        if (ret.getIsWinner()) {
            EventUtil.triggerUpdateTargetProgress(ret.getPlayerIdx(), TargetTypeEnum.TTE_CumuMistBattleVictory, 1, 0);
            //目标：累积击杀x次迷雾森林boss
            if (ret.getSubPveType() == EnumMistPveBattleType.EMPBT_BossBattle) {
                EventUtil.triggerUpdateTargetProgress(ret.getPlayerIdx(), TargetTypeEnum.TTE_CumuMistKillBoss, 1, 0);
                EventUtil.triggerUpdateTargetProgress(ret.getPlayerIdx(), TargetTypeEnum.TTE_MistSeasonTask_KillBossCount, 1, 0);
            }
            if (ret.getSubPveType() == EnumMistPveBattleType.EMPBT_MonsterBattle) {
                EventUtil.triggerUpdateTargetProgress(ret.getPlayerIdx(), TargetTypeEnum.TTE_Mist_CumuKillMonster, 1,
                        MistConst.getMonsterType(ret.getFightCfgId()));
            }

        }
        if (ret.getSubPveType() == EnumMistPveBattleType.EMPBT_BossBattle) {
            LogService.getInstance().submit(new FightWithMistBossLog(ret.getPlayerIdx(), ret.getIsWinner()));
        }
    }

    protected void directSettlePvpBattle(playerEntity player, CS_GS_MistDirectSettleBattleData ret) {
        LogService.getInstance().submit(new MistPvpTimesLog(player.getIdx()));

        boolean canRecordVictory;
        if (ret.getIsWinner()) {
            canRecordVictory = targetsystemCache.getInstance().canRecordMistKillPlayerTarget(player.getIdx(), ret.getTargetPlayerIdx());
        } else {
            canRecordVictory = false;
        }

        //连续击杀处理:玩家在胜利冷却时间内,击败即清空连续击杀标记,连续击杀标记累计有冷却
        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_MistForestData.Builder builder = entity.getDb_data().getMistForestDataBuilder();
            if (ret.getIsWinner()) {
                if (canRecordVictory) {
//                    builder.setKillPlayerCount(builder.getKillPlayerCount() + 1);
                    builder.setContinuousKillPlayer(builder.getContinuousKillPlayer() + 1);

                    //目标进度
                    EventUtil.triggerUpdateTargetProgress(entity.getIdx(), TargetTypeEnum.TTE_CumuMistKillPlayer, 1, 0);
                }
            } else {
                builder.clearContinuousKillPlayer();
            }

            //目标：迷雾深林连续击杀玩家
            if (!ret.getIsWinner() || canRecordVictory) {
                EventUtil.triggerUpdateTargetProgress(entity.getIdx(), TargetTypeEnum.TTE_MistContinuousKillPlayer, builder.getContinuousKillPlayer(), 0);
            }
        });
    }
}
