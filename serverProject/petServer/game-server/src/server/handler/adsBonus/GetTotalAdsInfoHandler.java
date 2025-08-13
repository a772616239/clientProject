package server.handler.adsBonus;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_TotalAdsInfo;
import protocol.PlayerInfo.SC_TotalAdsInfo;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TotalAdsInfo_VALUE)
public class GetTotalAdsInfoHandler extends AbstractBaseHandler<CS_TotalAdsInfo> {
    @Override
    protected CS_TotalAdsInfo parse(byte[] bytes) throws Exception {
        return CS_TotalAdsInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_TotalAdsInfo req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        player.sendTotalAdsInfo();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Ads;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_TotalAdsInfo_VALUE, SC_TotalAdsInfo.newBuilder());
    }
}
