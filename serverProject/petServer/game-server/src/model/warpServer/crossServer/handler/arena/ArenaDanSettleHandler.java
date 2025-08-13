package model.warpServer.crossServer.handler.arena;

import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.player.dbCache.playerCache;
import protocol.Arena.SC_ArenaDanSettle;
import protocol.ArenaDB.DB_Arena.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_ArenaDanSettle;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/05/25
 */
@MsgId(msgId = MsgIdEnum.CS_GS_ArenaDanSettle_VALUE)
public class ArenaDanSettleHandler extends AbstractHandler<CS_GS_ArenaDanSettle> {
    @Override
    protected CS_GS_ArenaDanSettle parse(byte[] bytes) throws Exception {
        return CS_GS_ArenaDanSettle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ArenaDanSettle req, int i) {
        LogUtil.error("receive dan settle msg, content: " + req.toString()
                + "curTime:" + GlobalTick.getInstance().getCurrentTime());
        List<String> allPlayerIdx = playerCache.getInstance().getAllPlayerIdx();
        for (String idx : allPlayerIdx) {
            arenaEntity entity = arenaCache.getByIdx(idx);
            if (entity == null) {
                continue;
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

            if (GlobalData.getInstance().checkPlayerOnline(idx)) {
                GlobalData.getInstance().sendMsg(idx, MsgIdEnum.SC_ArenaDanSettle_VALUE, SC_ArenaDanSettle.newBuilder());
            }
        }
    }
}
