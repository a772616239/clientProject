package server.handler.farmmine;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.farmmine.FarmMineManager;
import protocol.Common.EnumFunction;
import protocol.FarmMine.CS_FarmMineRefOneInfo;
import protocol.FarmMine.SC_FarmMineRefOneInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_FarmMineRefOneInfo_VALUE)
public class FarmMineRefOneHandler extends AbstractBaseHandler<CS_FarmMineRefOneInfo> {
	
	@Override
	protected CS_FarmMineRefOneInfo parse(byte[] bytes) throws Exception {
		return CS_FarmMineRefOneInfo.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_FarmMineRefOneInfo req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		FarmMineManager.getInstance().refMineOneInfo(playerId, req.getIdx());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_FarmMineRefOneInfo_VALUE, SC_FarmMineRefOneInfo.newBuilder().setResult(retCode));
	}
}
