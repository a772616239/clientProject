package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_MistForestExitTeam;
import protocol.MistForest.SC_MistForestTeamInfo;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MistForestExitTeam_VALUE)
public class ExitMistTeamHandler extends AbstractBaseHandler<CS_MistForestExitTeam> {
    @Override
    protected CS_MistForestExitTeam parse(byte[] bytes) throws Exception {
        return CS_MistForestExitTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MistForestExitTeam req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_MistForestExitTeam_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_MistForestTeamInfo_VALUE, SC_MistForestTeamInfo.newBuilder());
    }
}
