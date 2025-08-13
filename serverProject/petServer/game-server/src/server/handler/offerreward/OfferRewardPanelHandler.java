package server.handler.offerreward;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.offerreward.OfferRewardManager;
import model.player.util.PlayerUtil;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.OfferReward;
import protocol.OfferReward.CS_OfferRewardPanel;

@MsgId(msgId = MsgIdEnum.CS_OfferRewardPanel_VALUE)
public class OfferRewardPanelHandler extends AbstractBaseHandler<CS_OfferRewardPanel> {
	@Override
	protected CS_OfferRewardPanel parse(byte[] bytes) throws Exception {
		return CS_OfferRewardPanel.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_OfferRewardPanel req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		if (PlayerUtil.queryFunctionLock(playerId, Common.EnumFunction.OfferReward)) {
			OfferReward.SC_OfferRewardPanel.Builder msg = OfferReward.SC_OfferRewardPanel.newBuilder();
			//msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_FunctionIsLock));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardPanel_VALUE, msg);
			return;
		}
		OfferRewardManager.getInstance().getPanel(playerId, req.getSelf(), req.getStar());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.OfferReward;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
	}

}
