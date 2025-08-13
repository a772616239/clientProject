package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainShopRefresh;

@MsgId(msgId = MsgIdEnum.CS_TrainShopRefresh_VALUE)
public class TrainRefreshHandler extends AbstractBaseHandler<CS_TrainShopRefresh> {

	@Override
	protected CS_TrainShopRefresh parse(byte[] bytes) throws Exception {
		return CS_TrainShopRefresh.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainShopRefresh req, int i) {
		TrainingManager.getInstance().handRefreshTrainShop(String.valueOf(gsChn.getPlayerId1()), req.getMapId(),req.getGroupId());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
