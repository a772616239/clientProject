package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_InviteJoinMistTeam;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_ReplyInviteJoinTeamRet;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_InviteJoinMistTeam_VALUE)
public class InviteJoinMistTeamHandler extends AbstractBaseHandler<CS_InviteJoinMistTeam> {
    @Override
    protected CS_InviteJoinMistTeam parse(byte[] bytes) throws Exception {
        return CS_InviteJoinMistTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_InviteJoinMistTeam req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_InviteJoinMistTeam_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ReplyInviteJoinTeamRet_VALUE,
                SC_ReplyInviteJoinTeamRet.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }
}
