package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainShopBuy;

@MsgId(msgId = MsgIdEnum.CS_TrainShopBuy_VALUE)
public class TrainShopBuyHandler extends AbstractBaseHandler<CS_TrainShopBuy> {

	@Override
	protected CS_TrainShopBuy parse(byte[] bytes) throws Exception {
		return CS_TrainShopBuy.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainShopBuy req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().buyGoods(playerId, req.getMapId(), req.getGroup(), req.getPos());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
