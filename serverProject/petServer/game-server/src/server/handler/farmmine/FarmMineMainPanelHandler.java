package server.handler.farmmine;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.farmmine.FarmMineManager;
import protocol.Common.EnumFunction;
import protocol.FarmMine.CS_FarmMineMainPanel;
import protocol.FarmMine.SC_FarmMineMainPanel;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_FarmMineMainPanel_VALUE)
public class FarmMineMainPanelHandler extends AbstractBaseHandler<CS_FarmMineMainPanel> {
	
	@Override
	protected CS_FarmMineMainPanel parse(byte[] bytes) throws Exception {
		return CS_FarmMineMainPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_FarmMineMainPanel req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		FarmMineManager.getInstance().sendMainPanel(playerId);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_FarmMineMainPanel_VALUE, SC_FarmMineMainPanel.newBuilder().setResult(retCode));
	}
}
