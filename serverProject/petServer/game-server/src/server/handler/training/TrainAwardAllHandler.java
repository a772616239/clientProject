package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainAwardAll;
import protocol.Training.SC_TrainAwardAll;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainAwardAll_VALUE)
public class TrainAwardAllHandler extends AbstractBaseHandler<CS_TrainAwardAll> {
	
	@Override
	protected CS_TrainAwardAll parse(byte[] bytes) throws Exception {
		return CS_TrainAwardAll.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainAwardAll req, int i) {
		int mapId = req.getMapId();
		// 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().getAwardAll(playerId, mapId);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainAwardAll_VALUE, SC_TrainAwardAll.newBuilder().setResult(retCode));
	}
}
