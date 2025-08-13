package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainGoing;
import protocol.Training.SC_TrainGoing;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainGoing_VALUE)
public class TrainGoingHandler extends AbstractBaseHandler<CS_TrainGoing> {
	
	@Override
	protected CS_TrainGoing parse(byte[] bytes) throws Exception {
		return CS_TrainGoing.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainGoing req, int i) {
		int mapId = req.getMapId();
		int pointId = req.getNextId();
		int param = req.getParam();
		// 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().going(playerId, mapId, pointId, param, true,true,0);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainGoing_VALUE, SC_TrainGoing.newBuilder().setResult(retCode));
	}
}
