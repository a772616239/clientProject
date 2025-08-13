package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaWinPanel;
import protocol.MessageId.MsgIdEnum;

/**
 * 连胜奖励领取
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaWinPanel_VALUE)
public class CrossArenaWinTaskPanelHandler extends AbstractBaseHandler<CS_CrossArenaWinPanel> {
	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.CrossArena;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

	@Override
	protected CS_CrossArenaWinPanel parse(byte[] bytes) throws Exception {
		return CS_CrossArenaWinPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaWinPanel req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		CrossArenaManager.getInstance().getWinTaskPanel(playerIdx);
	}
}
