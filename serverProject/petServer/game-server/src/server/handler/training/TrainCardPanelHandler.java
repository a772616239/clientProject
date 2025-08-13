package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainingCardPanel;
import protocol.Training.SC_TrainAlertBuffChose;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainingCardPanel_VALUE)
public class TrainCardPanelHandler extends AbstractBaseHandler<CS_TrainingCardPanel> {

	@Override
	protected CS_TrainingCardPanel parse(byte[] bytes) throws Exception {
		return CS_TrainingCardPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainingCardPanel req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().sendAllLuckCard(playerId, req.getMapId());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainAlertBuffChose_VALUE, SC_TrainAlertBuffChose.newBuilder().setResult(retCode));
	}
}
