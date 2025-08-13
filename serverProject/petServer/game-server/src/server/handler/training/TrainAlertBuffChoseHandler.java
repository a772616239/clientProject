package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainAlertBuffChose;
import protocol.Training.SC_TrainAlertBuffChose;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainAlertBuffChose_VALUE)
public class TrainAlertBuffChoseHandler extends AbstractBaseHandler<CS_TrainAlertBuffChose> {
	
	@Override
	protected CS_TrainAlertBuffChose parse(byte[] bytes) throws Exception {
		return CS_TrainAlertBuffChose.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainAlertBuffChose req, int i) {
		int mapId = req.getMapId();
		int bid = req.getBuffId();
		// 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().choseBuff(playerId, mapId, bid,req.getType());
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
