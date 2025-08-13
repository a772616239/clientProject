package model.warpServer.crossServer.handler.arena;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.Collection;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import protocol.Activity.EnumRankingType;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.Arena.SC_ClaimArenaInfo;
import protocol.Arena.SC_ClaimArenaInfo.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_JoinArena;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/05/13
 */
@MsgId(msgId = MsgIdEnum.CS_GS_JoinArena_VALUE)
public class JoinArenaHandler extends AbstractHandler<CS_GS_JoinArena> {
    @Override
    protected CS_GS_JoinArena parse(byte[] bytes) throws Exception {
        return CS_GS_JoinArena.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gaChn, CS_GS_JoinArena req, int i) {
        arenaEntity entity = arenaCache.getInstance().getEntity(req.getPlayerIdx());

        Builder resultBuilder = SC_ClaimArenaInfo.newBuilder();

        if (req.getRetCode().getRetCode() != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(req.getRetCode());
            GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_ClaimArenaInfo_VALUE, resultBuilder);
            return;
        }

        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_ClaimArenaInfo_VALUE, resultBuilder);
            return;
        }

        int beforeDan = entity.getDbBuilder().getDan();
        boolean danUpdate = SyncExecuteFunction.executePredicate(entity, e -> {
            entity.getDbBuilder().setDan(req.getDan());
            entity.getDbBuilder().setScore(req.getScore());
            entity.getDbBuilder().setRanking(req.getRanking());
            entity.getDbBuilder().setRoomId(req.getRoomId());
            entity.getDbBuilder().setFightAbility(req.getFightAbility());

            if (req.getOpponnentInfoCount() > 0) {
                entity.refreshOpponent(req.getOpponnentInfoList(), null);
            }

            entity.doDanReachReward();

            //段位达到, 额外条件排名达到
            for (int j = 1; j <= req.getDan(); j++) {
                //当前段位之前的段位任务全都完成
                int params = j == req.getDan() ? req.getRanking() : 1;
                EventUtil.triggerUpdateTargetProgress(req.getPlayerIdx(), TargetTypeEnum.TEE_Arena_DanReach, j, params);
            }

            return beforeDan != req.getDan();
        });


        resultBuilder.setRetCode(req.getRetCode());
        resultBuilder.setDan(req.getDan());
        resultBuilder.setScore(req.getScore());
        resultBuilder.setTodayFreeChallengeTimes(entity.getDbBuilder().getTodayFreeChallengeTimes());
        Collection<ArenaOpponentTotalInfo> values = entity.getDbBuilder().getOpponentMap().values();
        for (ArenaOpponentTotalInfo value : values) {
            resultBuilder.addOpponnentInfo(value.getOpponnentInfo());
        }
        resultBuilder.addAllVictoryIdx(entity.getDbBuilder().getVictoryIdxList());
        resultBuilder.setNextSettleDanTime(req.getNextSettleDanTime());
        resultBuilder.setDirectUpKillCount(req.getDirectUpCount());
        resultBuilder.setTodayBuyTimes(entity.getDbBuilder().getTodayBuyTicketCount());

        GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_ClaimArenaInfo_VALUE, resultBuilder);

        if (danUpdate) {
            playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
            if (player != null) {
                player.sendRefreshTitleMsg();
            }
            // 清除老段位排行榜上当前玩家的数据
            RankingManager.getInstance().clearPlayerRanking(req.getPlayerIdx(), RankingUtils.getArenaScoreLocalDanRankName(beforeDan));
            // 更新段位排行榜
            RankingManager.getInstance().updatePlayerRankingScore(req.getPlayerIdx(), EnumRankingType.ERT_ArenaScoreLocalDan
                    , RankingUtils.getArenaScoreLocalDanRankName(entity.getDbBuilder().getDan()), entity.getDbBuilder().getDan()
                    , req.getScore());
        }
    }
}
