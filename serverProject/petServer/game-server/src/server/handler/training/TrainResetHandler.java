package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainReset;
import protocol.Training.SC_TrainReset;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainReset_VALUE)
public class TrainResetHandler extends AbstractBaseHandler<CS_TrainReset> {
	
	@Override
	protected CS_TrainReset parse(byte[] bytes) throws Exception {
		return CS_TrainReset.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainReset req, int i) {
		int mapId = req.getMapId();
		int isagree = req.getIsAgree();
		int pid = req.getPointId();
		// 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().reset(playerId, mapId, isagree, pid);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainReset_VALUE, SC_TrainReset.newBuilder().setResult(retCode));
	}
}
