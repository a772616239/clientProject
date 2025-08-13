package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainUseItem;

@MsgId(msgId = MsgIdEnum.CS_TrainUseItem_VALUE)
public class TrainUseItemHandler extends AbstractBaseHandler<CS_TrainUseItem> {

	@Override
	protected CS_TrainUseItem parse(byte[] bytes) throws Exception {
		return CS_TrainUseItem.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainUseItem req, int i) {
		TrainingManager.getInstance().useItem(String.valueOf(gsChn.getPlayerId1()), req.getItemId(), req.getParam());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
