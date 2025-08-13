package server.handler.offerreward;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.offerreward.OfferRewardManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.OfferReward.CS_OfferRewardPrepare;

@MsgId(msgId = MsgIdEnum.CS_OfferRewardPrepare_VALUE)
public class OfferRewardPrepareHandler extends AbstractBaseHandler<CS_OfferRewardPrepare> {
	@Override
	protected CS_OfferRewardPrepare parse(byte[] bytes) throws Exception {
		return CS_OfferRewardPrepare.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_OfferRewardPrepare req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		OfferRewardManager.getInstance().getPanelPrepare(playerId, req.getGrade());
	}

	@Override
	public EnumFunction belongFunction() {
		return null;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

}
