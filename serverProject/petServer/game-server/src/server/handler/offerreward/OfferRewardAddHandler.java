package server.handler.offerreward;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.offerreward.OfferRewardManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.OfferReward.CS_OfferRewardRelease;

@MsgId(msgId = MsgIdEnum.CS_OfferRewardRelease_VALUE)
public class OfferRewardAddHandler extends AbstractBaseHandler<CS_OfferRewardRelease> {
	@Override
	protected CS_OfferRewardRelease parse(byte[] bytes) throws Exception {
		return CS_OfferRewardRelease.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_OfferRewardRelease req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		OfferRewardManager.getInstance().addOneOffer(playerId, req.getGrade(), req.getBossIn(), req.getRewardList(),false);
	}

	@Override
	public EnumFunction belongFunction() {
		return null;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

}
