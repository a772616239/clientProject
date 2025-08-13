package server.handler.crossarenapvp;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.Common.EnumFunction;
import protocol.CrossArenaPvp.CS_CrossArenaPvpJoin;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_CrossArenaPvpJoin_VALUE)
public class CrossArenaPvpJoinHandler extends AbstractBaseHandler<CS_CrossArenaPvpJoin> {
	@Override
	protected CS_CrossArenaPvpJoin parse(byte[] bytes) throws Exception {
		return CS_CrossArenaPvpJoin.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaPvpJoin req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		CrossArenaPvpManager.getInstance().join(playerIdx,req.getId(),req.getJoin(),req.getMapsList(),req.getSkillMapList());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.DrawCard_AncientCall;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}
}
