/*
package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainLimitCardPanel;

@MsgId(msgId = MsgIdEnum.CS_TrainLimitCardPanel_VALUE)
public class TrainLimitCardPanelHandler extends AbstractBaseHandler<CS_TrainLimitCardPanel> {

	@Override
	protected CS_TrainLimitCardPanel parse(byte[] bytes) throws Exception {
		return CS_TrainLimitCardPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainLimitCardPanel req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().sendAllLimitCard(playerId, req.getMapId());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {}
}
*/
