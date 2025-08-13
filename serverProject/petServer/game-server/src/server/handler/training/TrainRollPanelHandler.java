package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainLuckCardPanel;

@MsgId(msgId = MsgIdEnum.CS_TrainLuckCardPanel_VALUE)
public class TrainRollPanelHandler extends AbstractBaseHandler<CS_TrainLuckCardPanel> {

	@Override
	protected CS_TrainLuckCardPanel parse(byte[] bytes) throws Exception {
		return CS_TrainLuckCardPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainLuckCardPanel req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		//废弃
//		TrainingManager.getInstance().rollPanel(playerId);

	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
