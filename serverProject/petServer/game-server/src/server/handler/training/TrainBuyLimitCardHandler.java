/*
package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainLimitCard;

@MsgId(msgId = MsgIdEnum.CS_TrainLimitCard_VALUE)
public class TrainBuyLimitCardHandler extends AbstractBaseHandler<CS_TrainLimitCard> {

	@Override
	protected CS_TrainLimitCard parse(byte[] bytes) throws Exception {
		return CS_TrainLimitCard.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainLimitCard req, int i) {
		int mapId = req.getMapId();
		int cardId = req.getId();
		// 获取当前channel对应playerId
		String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().buyLimitCard(playerId, mapId, cardId);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
*/
