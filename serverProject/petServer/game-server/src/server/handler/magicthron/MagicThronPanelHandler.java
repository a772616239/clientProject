package server.handler.magicthron;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.magicthron.MagicThronManager;
import protocol.Common.EnumFunction;
import protocol.MagicThron;
import protocol.MagicThron.CS_MagicThronPanel;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import util.GameUtil;

/**
 * @author luoyun
 * @date 2021/08/04
 */
@MsgId(msgId = MsgIdEnum.CS_MagicThronPanel_VALUE)
public class MagicThronPanelHandler extends AbstractBaseHandler<CS_MagicThronPanel> {
	@Override
	protected CS_MagicThronPanel parse(byte[] bytes) throws Exception {
		return CS_MagicThronPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_MagicThronPanel req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		MagicThronManager.getInstance().getPanel(playerIdx);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.MagicThron;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MessageId.MsgIdEnum.SC_MagicThronPanel_VALUE, MagicThron.SC_MagicThronPanel.newBuilder().setResult(retCode));
	}
}
