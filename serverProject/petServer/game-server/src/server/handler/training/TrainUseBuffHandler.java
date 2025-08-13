package server.handler.training;

import java.util.HashMap;
import java.util.Map;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.training.TrainingManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Training.CS_TrainUse;
import protocol.Training.SC_TrainUse;
import protocol.Training.TrainKV;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainUse_VALUE)
public class TrainUseBuffHandler extends AbstractBaseHandler<CS_TrainUse> {

	@Override
	protected CS_TrainUse parse(byte[] bytes) throws Exception {
		return CS_TrainUse.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_TrainUse req, int i) {
		Map<Integer, Integer> map = new HashMap<>();
		for(TrainKV k : req.getItemsList()) {
			map.put(k.getKey(), k.getVue());
		}
		TrainingManager.getInstance().useBuff(String.valueOf(gsChn.getPlayerId1()), req.getMapid(),req.getPoint(), map);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_TrainUse_VALUE, SC_TrainUse.newBuilder().setResult(retCode));
	}
}
