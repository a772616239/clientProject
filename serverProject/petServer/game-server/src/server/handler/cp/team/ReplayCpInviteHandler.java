package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_ReplyCpInvite;
import protocol.CpFunction.SC_ReplyCpInvite;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 玩家回复邀请其他玩家邀请入队
 */
@MsgId(msgId = MsgIdEnum.CS_ReplyCpInvite_VALUE)
public class ReplayCpInviteHandler extends AbstractBaseHandler<CS_ReplyCpInvite> {
    @Override
    protected CS_ReplyCpInvite parse(byte[] bytes) throws Exception {
        return CS_ReplyCpInvite.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ReplyCpInvite req, int i) {
        String playerIdx = GameUtil
                .longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().replyCpInvite(playerIdx, req.getInvitePlayerId(),req.getAccept());
        SC_ReplyCpInvite.Builder msg = SC_ReplyCpInvite.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_ReplyCpInvite_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ReplyCpInvite_VALUE, SC_ReplyCpInvite.newBuilder().setRetCode(retCode));
    }
}
