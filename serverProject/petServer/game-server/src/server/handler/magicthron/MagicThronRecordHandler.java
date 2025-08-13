package server.handler.magicthron;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.magicthron.MagicThronManager;
import protocol.Common.EnumFunction;
import protocol.MagicThron.CS_MagicThronRecord;
import protocol.MessageId.MsgIdEnum;

/**
 * @author luoyun
 * @date 2021/08/04
 */
@MsgId(msgId = MsgIdEnum.CS_MagicThronRecord_VALUE)
public class MagicThronRecordHandler extends AbstractBaseHandler<CS_MagicThronRecord> {
	@Override
	protected CS_MagicThronRecord parse(byte[] bytes) throws Exception {
		return CS_MagicThronRecord.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_MagicThronRecord req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		MagicThronManager.getInstance().magicThronRecord(playerIdx);
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.MagicThron;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
