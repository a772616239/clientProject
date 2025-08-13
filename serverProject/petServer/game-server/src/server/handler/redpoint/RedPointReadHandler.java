package server.handler.redpoint;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.redpoint.RedPointManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RedPoint.CS_RedPointRead;

@MsgId(msgId = MsgIdEnum.CS_RedPointRead_VALUE)
public class RedPointReadHandler extends AbstractBaseHandler<CS_RedPointRead> {

	@Override
	protected CS_RedPointRead parse(byte[] bytes) throws Exception {
		return CS_RedPointRead.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_RedPointRead req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		RedPointManager.getInstance().redPointRead(playerId, req.getIdValue());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
