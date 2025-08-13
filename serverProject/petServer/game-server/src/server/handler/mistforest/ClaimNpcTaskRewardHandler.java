package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_ClaimNpcTaskReward;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_ClaimNpcTaskReward;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimNpcTaskReward_VALUE)
public class ClaimNpcTaskRewardHandler extends AbstractBaseHandler<CS_ClaimNpcTaskReward> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ClaimNpcTaskReward_VALUE, SC_ClaimNpcTaskReward.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));

    }

    @Override
    protected CS_ClaimNpcTaskReward parse(byte[] bytes) throws Exception {
        return CS_ClaimNpcTaskReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimNpcTaskReward req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_ClaimNpcTaskReward_VALUE, req.toByteString(), true);
    }
}
