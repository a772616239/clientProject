package server.handler.crossarenapvp;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpRoomManager;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaPvpConsumeBack;
import protocol.ServerTransfer.GS_BS_CrossArenaPvpConsumeResult;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaPvpConsumeResult_VALUE)
public class CrossArenaPvpConsumeResultHandler extends AbstractHandler<GS_BS_CrossArenaPvpConsumeResult> {
	@Override
	protected GS_BS_CrossArenaPvpConsumeResult parse(byte[] bytes) throws Exception {
		return GS_BS_CrossArenaPvpConsumeResult.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaPvpConsumeResult req, int i) {
		if (req.getResult() == 0) {
			BS_GS_CrossArenaPvpConsumeBack.Builder builder = BS_GS_CrossArenaPvpConsumeBack.newBuilder();
			builder.setPlayerId(req.getOwnPlayerId());
			builder.setCostIndex(req.getCostIndex());
			WarpServerManager.getInstance().sendMsgToGSExcept(MsgIdEnum.BS_GS_CrossArenaPvpConsumeBack_VALUE, builder, req.getOwnSvrIndex());
			return;
		}
		CrossArenaPvpRoomManager.getInstance().start(req.getRoomId());

	}
}
