package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainingMapPanel;

@MsgId(msgId = MsgIdEnum.CS_TrainingMapPanel_VALUE)
public class TrainMapPanelHandler extends AbstractBaseHandler<CS_TrainingMapPanel> {

	@Override
	protected CS_TrainingMapPanel parse(byte[] bytes) throws Exception {
		return CS_TrainingMapPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainingMapPanel req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().sendMapAll(playerId);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
