package server.handler.rollcard;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.rollcard.RollCardManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RollCard.CS_RollCardPanel;

@MsgId(msgId = MsgIdEnum.CS_RollCardPanel_VALUE)
public class RollCardPanelHandler extends AbstractBaseHandler<CS_RollCardPanel> {

	@Override
	protected CS_RollCardPanel parse(byte[] bytes) throws Exception {
		return CS_RollCardPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_RollCardPanel req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		RollCardManager.getInstance().getPanel(playerId, req.getType());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
