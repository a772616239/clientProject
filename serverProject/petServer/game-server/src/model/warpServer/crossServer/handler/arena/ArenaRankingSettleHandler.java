package model.warpServer.crossServer.handler.arena;

import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.arena.ArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_ArenaRankingSettle;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/05/15
 */
@MsgId(msgId = MsgIdEnum.CS_GS_ArenaRankingSettle_VALUE)
public class ArenaRankingSettleHandler extends AbstractHandler<CS_GS_ArenaRankingSettle> {
    @Override
    protected CS_GS_ArenaRankingSettle parse(byte[] bytes) throws Exception {
        return CS_GS_ArenaRankingSettle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ArenaRankingSettle req, int i) {
        LogUtil.info("receive arena ranking settle info, curTime:" + GlobalTick.getInstance().getCurrentTime()
                + ",detail:" + req.toString());
        ArenaManager.getInstance().doRankingReward(req);
    }
}
