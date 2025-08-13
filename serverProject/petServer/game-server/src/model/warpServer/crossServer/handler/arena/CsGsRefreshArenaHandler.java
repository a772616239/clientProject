package model.warpServer.crossServer.handler.arena;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import protocol.Activity.EnumRankingType;
import protocol.Arena.SC_RefreshArena;
import protocol.Arena.SC_RefreshArena.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_RefreshArena;

/**
 * @author huhan
 * @date 2020/05/19
 */
@MsgId(msgId = MsgIdEnum.CS_GS_RefreshArena_VALUE)
public class CsGsRefreshArenaHandler extends AbstractHandler<CS_GS_RefreshArena> {
    @Override
    protected CS_GS_RefreshArena parse(byte[] bytes) throws Exception {
        return CS_GS_RefreshArena.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_RefreshArena req, int i) {
        arenaEntity entity = arenaCache.getByIdx(req.getPlayerIdx());
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDbBuilder().setScore(req.getNewScore());
            if (req.hasRecord()) {
                entity.addBattleRecord(req.getRecord());
            }
        });

        if (GlobalData.getInstance().checkPlayerOnline(req.getPlayerIdx())) {
            Builder resultBuilder = SC_RefreshArena.newBuilder();
            resultBuilder.setDirectUpKillCount(req.getDirectUpKillCount());
            resultBuilder.addAllDefeatPlayerIdx(entity.getDbBuilder().getVictoryIdxList());
            resultBuilder.setNewScore(req.getNewScore());
            resultBuilder.setTodayFreeChallenageTimes(entity.getDbBuilder().getTodayFreeChallengeTimes());
            GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_RefreshArena_VALUE, resultBuilder);
        }

        //更新活动排行榜
        RankingManager.getInstance().updatePlayerRankingScore(req.getPlayerIdx(), EnumRankingType.ERT_ArenaScoreLocal
                , entity.getDbBuilder().getDan(), req.getNewScore());
        RankingManager.getInstance().updatePlayerRankingScore(req.getPlayerIdx(), EnumRankingType.ERT_ArenaScoreCross
                , entity.getDbBuilder().getDan(), req.getNewScore());
        RankingManager.getInstance().updatePlayerRankingScore(req.getPlayerIdx(), EnumRankingType.ERT_ArenaScoreLocalDan
                , RankingUtils.getArenaScoreLocalDanRankName(entity.getDbBuilder().getDan()), entity.getDbBuilder().getDan()
                , req.getNewScore());
    }
}
