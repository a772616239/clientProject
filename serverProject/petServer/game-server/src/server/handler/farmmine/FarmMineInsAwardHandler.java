package server.handler.farmmine;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.farmmine.FarmMineManager;
import protocol.Common.EnumFunction;
import protocol.FarmMine.CS_FarmMineInsAward;
import protocol.FarmMine.SC_FarmMineInsAward;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_FarmMineInsAward_VALUE)
public class FarmMineInsAwardHandler extends AbstractBaseHandler<CS_FarmMineInsAward> {
	
	@Override
	protected CS_FarmMineInsAward parse(byte[] bytes) throws Exception {
		return CS_FarmMineInsAward.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_FarmMineInsAward req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		FarmMineManager.getInstance().getInsAward(playerId, req.getInsTime());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_FarmMineInsAward_VALUE, SC_FarmMineInsAward.newBuilder().setResult(retCode));
	}
}
