package server.handler.offerreward;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.offerreward.OfferRewardManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.OfferReward.CS_OfferRewardGet;

@MsgId(msgId = MsgIdEnum.CS_OfferRewardGet_VALUE)
public class OfferRewardGetHandler extends AbstractBaseHandler<CS_OfferRewardGet> {
	@Override
	protected CS_OfferRewardGet parse(byte[] bytes) throws Exception {
		return CS_OfferRewardGet.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_OfferRewardGet req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		OfferRewardManager.getInstance().getOfferReward(playerId, req.getId());
	}

	@Override
	public EnumFunction belongFunction() {
		return null;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

}
