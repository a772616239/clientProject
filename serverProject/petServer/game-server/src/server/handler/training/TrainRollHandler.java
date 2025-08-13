package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainLuckRoll;

@MsgId(msgId = MsgIdEnum.CS_TrainLuckRoll_VALUE)
public class TrainRollHandler extends AbstractBaseHandler<CS_TrainLuckRoll> {

	@Override
	protected CS_TrainLuckRoll parse(byte[] bytes) throws Exception {
		return CS_TrainLuckRoll.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainLuckRoll req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		int num = 0;
		if (req.getNum() == 0) {
			num = 1;
		} else {
			num = 3;
		}
		//废弃
//		TrainingManager.getInstance().roll(playerId, req.getType(), num);

	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
