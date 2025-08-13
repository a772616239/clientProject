package model.warpServer.crossServer.handler.arena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.arena.ArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_ResponseArenaRankInfo;

/**
 * @author huhan
 * @date 2020/06/28
 */
@MsgId(msgId = MsgIdEnum.CS_GS_ResponseArenaRankInfo_VALUE)
public class ResponseArenaRankInfoHandler extends AbstractHandler<CS_GS_ResponseArenaRankInfo> {
    @Override
    protected CS_GS_ResponseArenaRankInfo parse(byte[] bytes) throws Exception {
        return CS_GS_ResponseArenaRankInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ResponseArenaRankInfo req, int i) {
        ArenaManager.getInstance().updateRankingInfo(req.getRankingInfoList());
    }
}
