package server.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_OFFERNOTICE;
import protocol.ServerTransfer.GS_BS_OFFERNOTICE;
import protocol.ServerTransfer.ServerTypeEnum;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_BS_OFFERNOTICE_VALUE)
public class OfferRewardNoticeHandler extends AbstractHandler<GS_BS_OFFERNOTICE> {
	@Override
	protected GS_BS_OFFERNOTICE parse(byte[] bytes) throws Exception {
		return GS_BS_OFFERNOTICE.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, GS_BS_OFFERNOTICE req, int i) {
		try {
			BS_GS_OFFERNOTICE.Builder b = BS_GS_OFFERNOTICE.newBuilder();
			b.setData(req.getData());
			WarpServerManager.getInstance().sendMsgToAllServer(ServerTypeEnum.STE_GameServer_VALUE, MsgIdEnum.BS_GS_OFFERNOTICE_VALUE, b);
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
		}
	}
}
