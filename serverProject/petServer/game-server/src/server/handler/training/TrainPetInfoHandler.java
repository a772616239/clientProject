package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainPetInfo;
import protocol.Training.SC_TrainPetInfo;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainPetInfo_VALUE)
public class TrainPetInfoHandler extends AbstractBaseHandler<CS_TrainPetInfo> {
	
	@Override
	protected CS_TrainPetInfo parse(byte[] bytes) throws Exception {
		return CS_TrainPetInfo.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainPetInfo req, int i) {
		int mapId = req.getMapId();
		int pointId = req.getPointId();
		int itemId = req.getItemId();
		// 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().getPointPetInfo(playerId, mapId, pointId,itemId);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainPetInfo_VALUE, SC_TrainPetInfo.newBuilder().setResult(retCode));
	}
}
