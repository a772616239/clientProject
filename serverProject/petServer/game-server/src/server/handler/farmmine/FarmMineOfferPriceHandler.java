package server.handler.farmmine;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.farmmine.FarmMineManager;
import protocol.Common.EnumFunction;
import protocol.FarmMine.CS_FarmMineOfferPrice;
import protocol.FarmMine.SC_FarmMineOfferPrice;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_FarmMineOfferPrice_VALUE)
public class FarmMineOfferPriceHandler extends AbstractBaseHandler<CS_FarmMineOfferPrice> {
	
	@Override
	protected CS_FarmMineOfferPrice parse(byte[] bytes) throws Exception {
		return CS_FarmMineOfferPrice.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_FarmMineOfferPrice req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		FarmMineManager.getInstance().offerPrice(playerId, req.getIdx(), req.getConsume());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_FarmMineOfferPrice_VALUE, SC_FarmMineOfferPrice.newBuilder().setResult(retCode));
	}
}
