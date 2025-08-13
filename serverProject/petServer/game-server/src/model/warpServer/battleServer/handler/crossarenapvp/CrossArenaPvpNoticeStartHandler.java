package model.warpServer.battleServer.handler.crossarenapvp;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaNoticeStart;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaNoticeStart_VALUE)
public class CrossArenaPvpNoticeStartHandler extends AbstractHandler<BS_GS_CrossArenaNoticeStart> {
	@Override
	protected BS_GS_CrossArenaNoticeStart parse(byte[] bytes) throws Exception {
		return BS_GS_CrossArenaNoticeStart.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaNoticeStart req, int i) {
		CrossArenaPvpManager.getInstance().preStart(req.getRoomId(),req.getBattleTime());
	}
}
