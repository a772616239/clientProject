package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Set;
import model.cp.CpTeamManger;
import org.springframework.util.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.CpFunction;
import protocol.CpFunction.CS_ReceiveCpInviteList;
import protocol.CpFunction.SC_ReceiveCpInviteList;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.GameUtil;

/**
 * 拉取邀请列表
 */
@MsgId(msgId = MsgIdEnum.CS_ReceiveCpInviteList_VALUE)
public class ClaimPlayerInviteListHandler extends AbstractBaseHandler<CS_ReceiveCpInviteList> {
    @Override
    protected CS_ReceiveCpInviteList parse(byte[] bytes) throws Exception {
        return CS_ReceiveCpInviteList.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ReceiveCpInviteList req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        Set<String> playerIds = CpTeamManger.getInstance().findAllPlayerInvite(playerIdx);

        SC_ReceiveCpInviteList.Builder msg = SC_ReceiveCpInviteList.newBuilder();
        addInvitePlayers(playerIds, msg);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ReceiveCpInviteList_VALUE, msg);

    }

    private void addInvitePlayers(Set<String> playerIds, SC_ReceiveCpInviteList.Builder msg) {
        if (CollectionUtils.isEmpty(playerIds)) {
            return;
        }
        CpFunction.InviteCpPlayer.Builder inviteCpPlayer;
        for (String playerId : playerIds) {
            inviteCpPlayer = CpFunctionUtil.queryInviteCpPlayer(playerId);
            if (inviteCpPlayer != null) {
                msg.addPlayers(inviteCpPlayer);
            }
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ReceiveCpInviteList_VALUE, SC_ReceiveCpInviteList.newBuilder().setRetCode(retCode));
    }
}
