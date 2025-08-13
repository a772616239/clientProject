package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_ChangeAcceptTeamInvite;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ChangeAcceptTeamInvite_VALUE)
public class ChangeAcceptTeamInviteHandler extends AbstractBaseHandler<CS_ChangeAcceptTeamInvite> {
    @Override
    protected CS_ChangeAcceptTeamInvite parse(byte[] bytes) throws Exception {
        return CS_ChangeAcceptTeamInvite.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChangeAcceptTeamInvite req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_ChangeAcceptTeamInvite_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.CS_ChangeAcceptTeamInvite_VALUE, CS_ChangeAcceptTeamInvite.newBuilder());
    }
}
