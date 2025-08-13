package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaWeekBoxReward;
import protocol.MessageId.MsgIdEnum;

/**
 * 周宝箱任务
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaWeekBoxReward_VALUE)
public class CrossArenaWeekBoxRewardHandler extends AbstractBaseHandler<CS_CrossArenaWeekBoxReward> {
	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.CrossArena;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

	@Override
	protected CS_CrossArenaWeekBoxReward parse(byte[] bytes) throws Exception {
		return CS_CrossArenaWeekBoxReward.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaWeekBoxReward req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		CrossArenaManager.getInstance().getWeekBoxTaskReward(playerId,req.getPos());
	}
}
