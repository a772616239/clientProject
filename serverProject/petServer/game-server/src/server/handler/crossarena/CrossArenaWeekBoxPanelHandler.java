package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaWeekBoxPanel;
import protocol.MessageId.MsgIdEnum;

/**
 * 周宝箱任务
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaWeekBoxPanel_VALUE)
public class CrossArenaWeekBoxPanelHandler extends AbstractBaseHandler<CS_CrossArenaWeekBoxPanel> {
	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.CrossArena;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

	@Override
	protected CS_CrossArenaWeekBoxPanel parse(byte[] bytes) throws Exception {
		return CS_CrossArenaWeekBoxPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaWeekBoxPanel req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		CrossArenaManager.getInstance().getWeekBoxTaskPanel(playerId);
	}
}
