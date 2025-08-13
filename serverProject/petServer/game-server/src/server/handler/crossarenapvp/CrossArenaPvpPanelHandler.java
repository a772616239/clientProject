package server.handler.crossarenapvp;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.Common.EnumFunction;
import protocol.CrossArenaPvp.CS_CrossArenaPvpPanel;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_CrossArenaPvpPanel_VALUE)
public class CrossArenaPvpPanelHandler extends AbstractBaseHandler<CS_CrossArenaPvpPanel> {
	@Override
	protected CS_CrossArenaPvpPanel parse(byte[] bytes) throws Exception {
		return CS_CrossArenaPvpPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaPvpPanel req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		CrossArenaPvpManager.getInstance().getPanel(playerIdx);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.DrawCard_AncientCall;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}
}
