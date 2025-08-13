package server.handler.crossarenapvp;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.Common.EnumFunction;
import protocol.CrossArenaPvp.CS_CrossArenaOpen;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_CrossArenaOpen_VALUE)
public class CrossArenaPvpOpenHandler extends AbstractBaseHandler<CS_CrossArenaOpen> {
	@Override
	protected CS_CrossArenaOpen parse(byte[] bytes) throws Exception {
		return CS_CrossArenaOpen.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaOpen req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		CrossArenaPvpManager.getInstance().addOne(playerIdx, req.getCostIndex(),req.getMaxLv(), req.getBlackpetList(), req.getId(),req.getMapsList(),req.getSkillMapList());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.DrawCard_AncientCall;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}
}
