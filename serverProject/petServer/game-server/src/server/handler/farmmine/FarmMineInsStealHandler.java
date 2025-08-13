package server.handler.farmmine;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.farmmine.FarmMineManager;
import protocol.Common.EnumFunction;
import protocol.FarmMine.CS_FarmMineSteal;
import protocol.FarmMine.SC_FarmMineSteal;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_FarmMineSteal_VALUE)
public class FarmMineInsStealHandler extends AbstractBaseHandler<CS_FarmMineSteal> {
	
	@Override
	protected CS_FarmMineSteal parse(byte[] bytes) throws Exception {
		return CS_FarmMineSteal.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_FarmMineSteal req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		FarmMineManager.getInstance().stealAward(playerId, req.getIdx());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_FarmMineSteal_VALUE, SC_FarmMineSteal.newBuilder().setResult(retCode));
	}
}
