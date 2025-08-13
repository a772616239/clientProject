package server.handler.offerreward;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.offerreward.OfferRewardManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.OfferReward.CS_OfferRewardOneCall;

@MsgId(msgId = MsgIdEnum.CS_OfferRewardOneCall_VALUE)
public class OfferRewardNeedNoticeHandler extends AbstractBaseHandler<CS_OfferRewardOneCall> {
	@Override
	protected CS_OfferRewardOneCall parse(byte[] bytes) throws Exception {
		return CS_OfferRewardOneCall.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_OfferRewardOneCall req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		OfferRewardManager.getInstance().addNotice(playerId);
	}

	@Override
	public EnumFunction belongFunction() {
		return null;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

}
