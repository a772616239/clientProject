package server.handler.crossarenapvp;

import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerConst;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaPvpInfoOne;
import protocol.ServerTransfer.GS_BS_CrossArenaPvpInfoOne;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaPvpInfoOne_VALUE)
public class CrossArenaPvpInfoOneHandler extends AbstractHandler<GS_BS_CrossArenaPvpInfoOne> {
	@Override
	protected GS_BS_CrossArenaPvpInfoOne parse(byte[] bytes) throws Exception {
		return GS_BS_CrossArenaPvpInfoOne.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaPvpInfoOne req, int i) {
		int serverIndex = StringHelper.stringToInt(gsChn.getPlayerId(), 0);
		BS_GS_CrossArenaPvpInfoOne.Builder b = BS_GS_CrossArenaPvpInfoOne.newBuilder();
		b.setRoom(req.getRoom());
		b.setType(req.getType());
		WarpServerManager.getInstance().sendMsgToGSExcept(MsgIdEnum.BS_GS_CrossArenaPvpInfoOne_VALUE, b, serverIndex);
	}
}
