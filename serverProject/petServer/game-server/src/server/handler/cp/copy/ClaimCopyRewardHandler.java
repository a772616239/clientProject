package server.handler.cp.copy;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpCopyManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_ClaimCpScoreReward;
import protocol.CpFunction.SC_ClaimCpScoreReward;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 领取副本奖励
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimCpScoreReward_VALUE)
public class ClaimCopyRewardHandler extends AbstractBaseHandler<CS_ClaimCpScoreReward> {
    @Override
    protected CS_ClaimCpScoreReward parse(byte[] bytes) throws Exception {
        return CS_ClaimCpScoreReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimCpScoreReward req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_ClaimCpScoreReward.Builder msg = SC_ClaimCpScoreReward.newBuilder();

        RetCodeEnum codeEnum = CpCopyManger.getInstance().claimCpCopyReward(playerIdx, req.getIndex());

        msg.setRetCode(GameUtil.buildRetCode(codeEnum));

        gsChn.send(MsgIdEnum.SC_ClaimCpScoreReward_VALUE, msg);
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimCpScoreReward_VALUE, SC_ClaimCpScoreReward.newBuilder().setRetCode(retCode));
    }
}
