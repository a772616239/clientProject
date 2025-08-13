package server.handler.farmmine;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.farmmine.FarmMineManager;
import protocol.Common.EnumFunction;
import protocol.FarmMine.CS_FarmMineOfferPriceAdd;
import protocol.FarmMine.SC_FarmMineOfferPriceAdd;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_FarmMineOfferPriceAdd_VALUE)
public class FarmMineOfferPriceAddHandler extends AbstractBaseHandler<CS_FarmMineOfferPriceAdd> {
	
	@Override
	protected CS_FarmMineOfferPriceAdd parse(byte[] bytes) throws Exception {
		return CS_FarmMineOfferPriceAdd.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_FarmMineOfferPriceAdd req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
		FarmMineManager.getInstance().offerPriceAdd(playerId, req.getIdx());
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Training;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_FarmMineOfferPriceAdd_VALUE, SC_FarmMineOfferPriceAdd.newBuilder().setResult(retCode));
	}
}
