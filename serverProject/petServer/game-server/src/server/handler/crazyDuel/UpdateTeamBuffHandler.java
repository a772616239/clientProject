package server.handler.crazyDuel;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crazyDuel.CrazyDuelManager;
import model.crazyDuel.CrazyDuelOpenManager;
import org.springframework.util.StringUtils;
import protocol.Common;
import protocol.CrayzeDuel.CS_UpdateCrazyDuelTeamBuff;
import protocol.CrayzeDuel.SC_UpdateCrazyDuelTeamBuff;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_UpdateCrazyDuelTeamBuff_VALUE)
public class UpdateTeamBuffHandler extends AbstractBaseHandler<CS_UpdateCrazyDuelTeamBuff> {
    @Override
    protected CS_UpdateCrazyDuelTeamBuff parse(byte[] bytes) throws Exception {
        return CS_UpdateCrazyDuelTeamBuff.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateCrazyDuelTeamBuff req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        SC_UpdateCrazyDuelTeamBuff.Builder msg = SC_UpdateCrazyDuelTeamBuff.newBuilder();
        if (!CrazyDuelOpenManager.getInstance().isOpen()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_UpdateCrazyDuelTeamBuff_VALUE, msg);
            return;
        }
        RetCodeId.RetCodeEnum retCodeEnum = CrazyDuelManager.getInstance().updateSettingBuff(playerIdx, req);

        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));

        gsChn.send(MessageId.MsgIdEnum.SC_UpdateCrazyDuelTeamBuff_VALUE, msg);
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_UpdateCrazyDuelTeamBuff_VALUE, SC_UpdateCrazyDuelTeamBuff.newBuilder().setRetCode(retCode));
    }
}
