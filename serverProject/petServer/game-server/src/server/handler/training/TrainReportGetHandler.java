package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainingReportGet;
import protocol.Training.SC_TrainAlertBuffChose;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainingReportGet_VALUE)
public class TrainReportGetHandler extends AbstractBaseHandler<CS_TrainingReportGet> {
	
	@Override
	protected CS_TrainingReportGet parse(byte[] bytes) throws Exception {
		return CS_TrainingReportGet.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainingReportGet req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().trainReportGet(playerId);
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
