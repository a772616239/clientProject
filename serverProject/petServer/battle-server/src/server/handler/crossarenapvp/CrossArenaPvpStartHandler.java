package server.handler.crossarenapvp;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpRoomManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_CrossArenaPvpStartbattle;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaPvpStartbattle_VALUE)
public class CrossArenaPvpStartHandler extends AbstractHandler<GS_BS_CrossArenaPvpStartbattle> {
	@Override
	protected GS_BS_CrossArenaPvpStartbattle parse(byte[] bytes) throws Exception {
		return GS_BS_CrossArenaPvpStartbattle.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaPvpStartbattle req, int i) {
		CrossArenaPvpRoomManager.getInstance().start(req.getRoomId());
	}
}
