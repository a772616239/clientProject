package model.warpServer.battleServer.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.offerreward.OfferRewardManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_OFFERNOTICE;

@MsgId(msgId = MsgIdEnum.BS_GS_OFFERNOTICE_VALUE)
public class OfferRewardNoticeHandler extends AbstractHandler<BS_GS_OFFERNOTICE> {
    @Override
    protected BS_GS_OFFERNOTICE parse(byte[] bytes) throws Exception {
        return BS_GS_OFFERNOTICE.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_OFFERNOTICE req, int i) {
    	OfferRewardManager.getInstance().sendInfoOne(req.getData());
    }
}
