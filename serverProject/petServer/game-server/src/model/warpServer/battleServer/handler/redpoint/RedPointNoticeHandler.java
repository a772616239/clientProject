package model.warpServer.battleServer.handler.redpoint;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.redpoint.RedPointManager;
import model.redpoint.RedPointOptionEnum;
import protocol.Common;
import protocol.MessageId;
import protocol.ServerTransfer;

/**
 * 
 */
@MsgId(msgId = MessageId.MsgIdEnum.BS_GS_RedPoint_VALUE)
public class RedPointNoticeHandler extends AbstractBaseHandler<ServerTransfer.BS_GS_RedPoint> {
	@Override
	public Common.EnumFunction belongFunction() {
		return Common.EnumFunction.EF_MatchArena;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}

	@Override
	protected ServerTransfer.BS_GS_RedPoint parse(byte[] bytes) throws Exception {
		return ServerTransfer.BS_GS_RedPoint.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, ServerTransfer.BS_GS_RedPoint req, int i) {
		RedPointManager.getInstance().sendRedPoint(req.getPlayerId(), req.getType(), RedPointOptionEnum.optionOf(req.getState()));

	}
}
