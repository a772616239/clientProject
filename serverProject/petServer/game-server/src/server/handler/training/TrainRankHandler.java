package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainRank;
import protocol.Training.SC_TrainRank;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainRank_VALUE)
public class TrainRankHandler extends AbstractBaseHandler<CS_TrainRank> {
	
	@Override
	protected CS_TrainRank parse(byte[] bytes) throws Exception {
		return CS_TrainRank.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainRank req, int i) {
		int mapId = req.getMapId();
		// 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().getRankInfo(playerId, mapId);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainRank_VALUE, SC_TrainRank.newBuilder().setResult(retCode));
	}
}
