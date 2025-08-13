package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_ApplyToMistTeam;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_ReplyApplyToMistTeamRet;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ApplyToMistTeam_VALUE)
public class ApplyToMistTeamHandler extends AbstractBaseHandler<CS_ApplyToMistTeam> {
    @Override
    protected CS_ApplyToMistTeam parse(byte[] bytes) throws Exception {
        return CS_ApplyToMistTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ApplyToMistTeam req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_ApplyToMistTeam_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE,
                SC_ReplyApplyToMistTeamRet.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }
}
