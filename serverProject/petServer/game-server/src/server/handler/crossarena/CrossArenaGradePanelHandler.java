package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaGradePanel;
import protocol.MessageId.MsgIdEnum;

/**
 * 荣誉周积分任务
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaGradePanel_VALUE)
public class CrossArenaGradePanelHandler extends AbstractBaseHandler<CS_CrossArenaGradePanel> {
	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.CrossArena;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

	@Override
	protected CS_CrossArenaGradePanel parse(byte[] bytes) throws Exception {
		return CS_CrossArenaGradePanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaGradePanel req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		CrossArenaManager.getInstance().getGradePanel(playerId);
	}
}
