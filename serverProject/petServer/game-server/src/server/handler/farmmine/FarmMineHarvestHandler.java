package server.handler.farmmine;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.farmmine.FarmMineManager;
import protocol.Common.EnumFunction;
import protocol.FarmMine.CS_FarmMineHarvest;
import protocol.FarmMine.SC_FarmMineHarvest;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_FarmMineHarvest_VALUE)
public class FarmMineHarvestHandler extends AbstractBaseHandler<CS_FarmMineHarvest> {
	
	@Override
	protected CS_FarmMineHarvest parse(byte[] bytes) throws Exception {
		return CS_FarmMineHarvest.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_FarmMineHarvest req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		FarmMineManager.getInstance().harvest(playerId, req.getIdx());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_FarmMineHarvest_VALUE, SC_FarmMineHarvest.newBuilder().setResult(retCode));
	}
}
