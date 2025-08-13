package server.handler.arena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.arena.ArenaManager;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.ArenaRankingInfo;
import protocol.ServerTransfer.CS_GS_ResponseArenaRankInfo;
import protocol.ServerTransfer.GS_CS_RequestArenaRankInfo;

import java.util.List;

/**
 * @author huhan
 * @date 2020/06/28
 */
@MsgId(msgId = MsgIdEnum.GS_CS_RequestArenaRankInfo_VALUE)
public class RequestArenaRankInfoHandler extends AbstractHandler<GS_CS_RequestArenaRankInfo> {
    @Override
    protected GS_CS_RequestArenaRankInfo parse(byte[] bytes) throws Exception {
        return GS_CS_RequestArenaRankInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_RequestArenaRankInfo req, int i) {
        List<ArenaRankingInfo> infoList = ArenaManager.getInstance().queryDanRanking(req.getDan(), req.getLimit());

        CS_GS_ResponseArenaRankInfo.Builder resultBuilder = CS_GS_ResponseArenaRankInfo.newBuilder();
        if (CollectionUtils.isEmpty(infoList)) {
//            LogUtil.error("RequestArenaRankInfoHandler, query dan ranking info failed, result is empty, dan:" + req.getDan());
        } else {
            resultBuilder.addAllRankingInfo(infoList);
        }
        gsChn.send(MsgIdEnum.CS_GS_ResponseArenaRankInfo_VALUE, resultBuilder);
    }
}
