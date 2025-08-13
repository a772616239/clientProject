package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_ReplyJoinTeamRequest;
import protocol.CpFunction.SC_ReplyJoinTeamRequest;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 队长回复邀请玩家加入
 */
@MsgId(msgId = MsgIdEnum.CS_ReplyJoinTeamRequest_VALUE)
public class ReplyJoinTeamRequestHandler extends AbstractBaseHandler<CS_ReplyJoinTeamRequest> {
    @Override
    protected CS_ReplyJoinTeamRequest parse(byte[] bytes) throws Exception {
        return CS_ReplyJoinTeamRequest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ReplyJoinTeamRequest req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().replyJoinTeamRequest(playerIdx, req.getPlayerIdx(),req.getAccept());
        SC_ReplyJoinTeamRequest.Builder msg = SC_ReplyJoinTeamRequest.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_ReplyJoinTeamRequest_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ReplyJoinTeamRequest_VALUE, SC_ReplyJoinTeamRequest.newBuilder().setRetCode(retCode));
    }
}
