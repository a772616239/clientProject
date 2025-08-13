package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_MistForestJoinTeam;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MistForestJoinTeam_VALUE)
public class ApplyJoinTeamHandler extends AbstractBaseHandler<CS_MistForestJoinTeam> {
    @Override
    protected CS_MistForestJoinTeam parse(byte[] bytes) throws Exception {
        return CS_MistForestJoinTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MistForestJoinTeam req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_MistForestJoinTeam_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
//        gsChn.send(MsgIdEnum.SC_mistfore,
//                SC_ApplyExchangeMistRoom.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }
}
