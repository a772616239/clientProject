package server.handler.training;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Training.CS_TrainShop;

@MsgId(msgId = MsgIdEnum.CS_TrainShop_VALUE)
public class TrainShopHandler extends AbstractBaseHandler<CS_TrainShop> {
	
	@Override
	protected CS_TrainShop parse(byte[] bytes) throws Exception {
		return CS_TrainShop.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainShop req, int i) {
		int mapId = req.getMapId();
		// 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
		TrainingManager.getInstance().getShopInfo(playerId, mapId,req.getGroupId());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {/*TODO
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainShop_VALUE, SC_TrainShop.newBuilder().setResult(retCode));
	*/}
}
