package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainJoin;
import protocol.Training.SC_TrainJoin;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainJoin_VALUE)
public class TrainJionHandler extends AbstractBaseHandler<CS_TrainJoin> {
	
	@Override
	protected CS_TrainJoin parse(byte[] bytes) throws Exception {
		return CS_TrainJoin.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainJoin req, int i) {
		int mapId = req.getMapId();
		// 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().joinTrain(playerId, mapId);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainJoin_VALUE, SC_TrainJoin.newBuilder().setResult(retCode));
	}
}
