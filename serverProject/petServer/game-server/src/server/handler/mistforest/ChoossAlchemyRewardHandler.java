package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_ChooseAlchemyReward;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_ChooseAlchemyReward;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ChooseAlchemyReward_VALUE)
public class ChoossAlchemyRewardHandler extends AbstractBaseHandler<CS_ChooseAlchemyReward> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ChooseAlchemyReward_VALUE, SC_ChooseAlchemyReward.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }

    @Override
    protected CS_ChooseAlchemyReward parse(byte[] bytes) throws Exception {
        return CS_ChooseAlchemyReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChooseAlchemyReward req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_ChooseAlchemyReward_VALUE, req.toByteString(), true);
    }
}
