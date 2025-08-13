package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_KickOutFromTeam;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_KickOutFromTeam;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_KickOutFromTeam_VALUE)
public class KickOutMistTeamHandler extends AbstractBaseHandler<CS_KickOutFromTeam> {
    @Override
    protected CS_KickOutFromTeam parse(byte[] bytes) throws Exception {
        return CS_KickOutFromTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_KickOutFromTeam req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_KickOutFromTeam_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_KickOutFromTeam_VALUE,
                SC_KickOutFromTeam.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }
}
