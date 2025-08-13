/*
package server.handler.crazyDuel;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crazyDuel.CrazyDuelManager;
import model.crazyDuel.CrazyDuelOpenManager;
import protocol.Common;
import protocol.CrayzeDuel.CS_PublishCrazyDuelTeams;
import protocol.CrayzeDuel.SC_PublishCrazyDuelTeams;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_PublishCrazyDuelTeams_VALUE)
public class PublishCrazyDuelTeamsHandler extends AbstractBaseHandler<CS_PublishCrazyDuelTeams> {
    @Override
    protected CS_PublishCrazyDuelTeams parse(byte[] bytes) throws Exception {
        return CS_PublishCrazyDuelTeams.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PublishCrazyDuelTeams req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        SC_PublishCrazyDuelTeams.Builder msg = SC_PublishCrazyDuelTeams.newBuilder();
        if (!CrazyDuelOpenManager.getInstance().isOpen()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_PublishCrazyDuelTeams_VALUE, msg);
            return;
        }

      //  RetCodeId.RetCodeEnum codeEnum = CrazyDuelManager.getInstance().checkAndPublishTeams(playerIdx);
        //msg.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MessageId.MsgIdEnum.SC_PublishCrazyDuelTeams_VALUE, msg);
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_PublishCrazyDuelTeams_VALUE, SC_PublishCrazyDuelTeams.newBuilder().setRetCode(retCode));
    }
}
*/
