package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaReadyFight;
import protocol.MessageId.MsgIdEnum;

/**
 * 擂台提前准备
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaReadyFight_VALUE)
public class CrossArenaPlayerReadyFightHandler extends AbstractBaseHandler<CS_CrossArenaReadyFight> {
	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.CrossArena;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}

	@Override
	protected CS_CrossArenaReadyFight parse(byte[] bytes) throws Exception {
		return CS_CrossArenaReadyFight.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaReadyFight req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		CrossArenaManager.getInstance().readyFight(playerIdx, req.getState());
	}
}
