package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_CreateMistTeam;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_CreateMistTeam_VALUE)
public class CreateMistTeamHandler extends AbstractBaseHandler<CS_CreateMistTeam> {
    @Override
    protected CS_CreateMistTeam parse(byte[] bytes) throws Exception {
        return CS_CreateMistTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CreateMistTeam req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_CreateMistTeam_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
//        gsChn.send(MsgIdEnum.SC_MistForestPlayerInfo_VALUE, SC_MistForestPlayerInfo.newBuilder());
    }
}
