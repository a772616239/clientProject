package server.handler.offerreward;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.offerreward.OfferRewardManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.OfferReward.CS_OfferRewardRefresh;

@MsgId(msgId = MsgIdEnum.CS_OfferRewardRefresh_VALUE)
public class OfferRewardRefreshHandler extends AbstractBaseHandler<CS_OfferRewardRefresh> {
	@Override
	protected CS_OfferRewardRefresh parse(byte[] bytes) throws Exception {
		return CS_OfferRewardRefresh.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_OfferRewardRefresh req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		OfferRewardManager.getInstance().refresh(playerId, req.getSelf(),req.getStar(), req.getId());
	}

	@Override
	public EnumFunction belongFunction() {
		return null;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

}
