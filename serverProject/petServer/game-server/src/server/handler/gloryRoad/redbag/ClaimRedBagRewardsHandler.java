package server.handler.gloryRoad.redbag;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.gloryroad.GloryRoadManager;
import protocol.Common.EnumFunction;
import protocol.GloryRoad.CS_ClaimRedBagRewards;
import protocol.GloryRoad.SC_ClaimRedBagRewards;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/17
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimRedBagRewards_VALUE)
public class ClaimRedBagRewardsHandler extends AbstractBaseHandler<CS_ClaimRedBagRewards> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_GloryRoad;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_ClaimRedBagRewards.Builder builder = SC_ClaimRedBagRewards.newBuilder();
        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_ClaimRedBagRewards_VALUE, builder);
    }

    @Override
    protected CS_ClaimRedBagRewards parse(byte[] bytes) throws Exception {
        return CS_ClaimRedBagRewards.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimRedBagRewards req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        RetCodeEnum retCodeEnum = GloryRoadManager.getInstance().claimRedBagRewards(playerIdx, req.getRedBagCount());
        SC_ClaimRedBagRewards.Builder resultBuilder = SC_ClaimRedBagRewards.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_ClaimRedBagRewards_VALUE, resultBuilder);
    }
}
