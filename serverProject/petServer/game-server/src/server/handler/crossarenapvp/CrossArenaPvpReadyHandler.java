package server.handler.crossarenapvp;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.Common.EnumFunction;
import protocol.CrossArenaPvp.CS_CrossArenaPvpReady;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_CrossArenaPvpReady_VALUE)
public class CrossArenaPvpReadyHandler extends AbstractBaseHandler<CS_CrossArenaPvpReady> {
	@Override
	protected CS_CrossArenaPvpReady parse(byte[] bytes) throws Exception {
		return CS_CrossArenaPvpReady.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaPvpReady req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		CrossArenaPvpManager.getInstance().ready(playerIdx, req.getId(), req.getReady());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.DrawCard_AncientCall;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}
}
