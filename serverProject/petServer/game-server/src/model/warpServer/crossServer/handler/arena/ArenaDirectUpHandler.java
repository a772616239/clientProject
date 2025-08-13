package model.warpServer.crossServer.handler.arena;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import protocol.Arena.SC_ArenaDirectUp;
import protocol.ArenaDB.DB_Arena.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_ArenaDirectUp;

/**
 * @author huhan
 * @date 2020/05/15
 */
@MsgId(msgId = MsgIdEnum.CS_GS_ArenaDirectUp_VALUE)
public class ArenaDirectUpHandler extends AbstractHandler<CS_GS_ArenaDirectUp> {
    @Override
    protected CS_GS_ArenaDirectUp parse(byte[] bytes) throws Exception {
        return CS_GS_ArenaDirectUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ArenaDirectUp req, int i) {
        arenaEntity entity = arenaCache.getByIdx(req.getPlayerIdx());
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            Builder builder = entity.getDbBuilder();
            if (builder == null) {
                return;
            }

            builder.clearOpponent();
            builder.clearVictoryIdx();
            builder.clearLastRefreshTime();
            builder.clearTempOpponent();
            builder.clearLastClaimRankingTime();
        });

        GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_ArenaDirectUp_VALUE,
                SC_ArenaDirectUp.newBuilder().setDan(req.getNewDan()));
    }
}
