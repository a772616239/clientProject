package server.handler.crossarenapvp;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.Common.EnumFunction;
import protocol.CrossArenaPvp.CS_CrossArenaPvpClose;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_CrossArenaPvpClose_VALUE)
public class CrossArenaPvpCloseHandler extends AbstractBaseHandler<CS_CrossArenaPvpClose> {
	@Override
	protected CS_CrossArenaPvpClose parse(byte[] bytes) throws Exception {
		return CS_CrossArenaPvpClose.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaPvpClose req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		CrossArenaPvpManager.getInstance().close(playerIdx, req.getId());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.DrawCard_AncientCall;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}
}
