package server.handler.rollcard;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.rollcard.RollCardManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RollCard.CS_RollCardChange;

@MsgId(msgId = MsgIdEnum.CS_RollCardChange_VALUE)
public class RollCardChangeHandler extends AbstractBaseHandler<CS_RollCardChange> {

	@Override
	protected CS_RollCardChange parse(byte[] bytes) throws Exception {
		return CS_RollCardChange.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_RollCardChange req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		RollCardManager.getInstance().change(playerId, req.getPool());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
