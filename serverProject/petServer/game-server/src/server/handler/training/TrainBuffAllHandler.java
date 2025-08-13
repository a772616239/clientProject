package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainBuffAll;
import protocol.Training.SC_TrainBuffAll;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainBuffAll_VALUE)
public class TrainBuffAllHandler extends AbstractBaseHandler<CS_TrainBuffAll> {
	
	@Override
	protected CS_TrainBuffAll parse(byte[] bytes) throws Exception {
		return CS_TrainBuffAll.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainBuffAll req, int i) {
		int mapId = req.getMapId();
		// 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().getBuffAll(playerId, mapId);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainBuffAll_VALUE, SC_TrainBuffAll.newBuilder().setResult(retCode));
	}
}
