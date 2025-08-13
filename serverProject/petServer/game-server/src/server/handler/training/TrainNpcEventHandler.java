package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainNpcEventResult;

@MsgId(msgId = MsgIdEnum.CS_TrainNpcEventResult_VALUE)
public class TrainNpcEventHandler extends AbstractBaseHandler<CS_TrainNpcEventResult> {
	
	@Override
	protected CS_TrainNpcEventResult parse(byte[] bytes) throws Exception {
		return CS_TrainNpcEventResult.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainNpcEventResult req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().getNpcReward(playerId, req.getChoiceList());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}
}
