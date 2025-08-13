package server.handler.crossarenapvp;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaPvpNoticeConsume;
import protocol.ServerTransfer.GS_BS_CrossArenaPvpNoticeConsume;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaPvpNoticeConsume_VALUE)
public class CrossArenaPvpNoticeConsumeHandler extends AbstractHandler<GS_BS_CrossArenaPvpNoticeConsume> {
	@Override
	protected GS_BS_CrossArenaPvpNoticeConsume parse(byte[] bytes) throws Exception {
		return GS_BS_CrossArenaPvpNoticeConsume.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaPvpNoticeConsume req, int i) {
		int attSvrIndex = req.getAtterSvrIndex();
		if (attSvrIndex <= 0) { // 兼容代码
			attSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(req.getAtterIp());
		}
		BS_GS_CrossArenaPvpNoticeConsume.Builder builder = BS_GS_CrossArenaPvpNoticeConsume.newBuilder();
		builder.setAtterSvrIndex(attSvrIndex);
		builder.setAtterPlayerId(req.getAtterPlayerId());
		builder.setCostIndex(req.getCostIndex());
		builder.setOwnPlayerId(req.getOwnPlayerId());
		builder.setRoomId(req.getRoomId());
		int ownerSvrIndex = req.getOwnSvrIndex();
		if (ownerSvrIndex <= 0) { // 兼容代码
			ownerSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(req.getOwnIp());
		}
		builder.setOwnSvrIndex(ownerSvrIndex);
		WarpServerManager.getInstance().sendMsgToGSExcept(MsgIdEnum.BS_GS_CrossArenaPvpNoticeConsume_VALUE, builder, attSvrIndex);
	}
}
