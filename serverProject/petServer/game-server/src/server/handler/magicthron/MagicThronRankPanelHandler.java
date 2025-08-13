package server.handler.magicthron;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.magicthron.MagicThronManager;
import protocol.Common.EnumFunction;
import protocol.MagicThron.CS_MagicThronRank;
import protocol.MessageId.MsgIdEnum;

/**
 * @author luoyun
 * @date 2021/08/04
 */
@MsgId(msgId = MsgIdEnum.CS_MagicThronRank_VALUE)
public class MagicThronRankPanelHandler extends AbstractBaseHandler<CS_MagicThronRank> {
	@Override
	protected CS_MagicThronRank parse(byte[] bytes) throws Exception {
		return CS_MagicThronRank.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_MagicThronRank req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		MagicThronManager.getInstance().getRankPanel(playerIdx, req.getArea());
	}
	@Override
	public EnumFunction belongFunction() {	
		return EnumFunction.MagicThron;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		
	}
}
