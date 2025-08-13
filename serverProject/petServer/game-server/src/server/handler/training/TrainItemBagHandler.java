package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainBagInfo;

@MsgId(msgId = MsgIdEnum.CS_TrainBagInfo_VALUE)
public class TrainItemBagHandler extends AbstractBaseHandler<CS_TrainBagInfo> {
	
	@Override
	protected CS_TrainBagInfo parse(byte[] bytes) throws Exception {
		return CS_TrainBagInfo.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainBagInfo req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().sendItemBag(playerId);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		
	}
}
