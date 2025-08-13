package server.handler.crossarenapvp;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.Common.EnumFunction;
import protocol.CrossArenaPvp.CS_CrossArenaPvpViewFight;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_CrossArenaPvpViewFight_VALUE)
public class CrossArenaPvpViewFightHandler extends AbstractBaseHandler<CS_CrossArenaPvpViewFight> {
	@Override
	protected CS_CrossArenaPvpViewFight parse(byte[] bytes) throws Exception {
		return CS_CrossArenaPvpViewFight.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaPvpViewFight req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		CrossArenaPvpManager.getInstance().reqViewFight(playerIdx, req.getRoomId());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.DrawCard_AncientCall;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}
}
