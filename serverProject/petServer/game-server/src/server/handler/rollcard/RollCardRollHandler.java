package server.handler.rollcard;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.RollCard.CS_RollCard;
import protocol.RollCard.SC_RollCard;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_RollCard_VALUE)
public class RollCardRollHandler extends AbstractBaseHandler<CS_RollCard> {

	@Override
	protected CS_RollCard parse(byte[] bytes) throws Exception {
		return CS_RollCard.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_RollCard req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		SC_RollCard.Builder builder = SC_RollCard.newBuilder();
		builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCard_VALUE, builder);
		// 暂时屏蔽
//		RollCardManager.getInstance().roll(playerId, req.getType(), req.getNum(), req.getCost());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
