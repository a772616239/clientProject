package server.handler.crossarenapvp;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaNoticeStart;
import protocol.ServerTransfer.GS_BS_CrossArenaNoticeStart;
import protocol.ServerTransfer.ServerTypeEnum;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaNoticeStart_VALUE)
public class CrossArenaPvpNoticeStartHandler extends AbstractHandler<GS_BS_CrossArenaNoticeStart> {
	@Override
	protected GS_BS_CrossArenaNoticeStart parse(byte[] bytes) throws Exception {
		return GS_BS_CrossArenaNoticeStart.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaNoticeStart req, int i) {
		BS_GS_CrossArenaNoticeStart.Builder b = BS_GS_CrossArenaNoticeStart.newBuilder();
		b.setRoomId(req.getRoomId());
		b.setBattleTime(req.getBattleTime());
		int svrIndex = req.getSvrIndex();
		if (svrIndex <= 0) { // 兼容代码
			svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(req.getIp());
		}
		WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, svrIndex, MsgIdEnum.BS_GS_CrossArenaNoticeStart_VALUE, b);
	}
}
