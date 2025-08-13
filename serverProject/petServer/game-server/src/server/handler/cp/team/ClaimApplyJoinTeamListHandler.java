package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Set;
import model.cp.CpTeamManger;
import org.springframework.util.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.CpFunction;
import protocol.CpFunction.CS_ApplyJoinTeamList;
import protocol.CpFunction.SC_ApplyJoinTeamList;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.GameUtil;

/**
 * 拉取申请加入组队列表
 */
@MsgId(msgId = MsgIdEnum.CS_ApplyJoinTeamList_VALUE)
public class ClaimApplyJoinTeamListHandler extends AbstractBaseHandler<CS_ApplyJoinTeamList> {
    @Override
    protected CS_ApplyJoinTeamList parse(byte[] bytes) throws Exception {
        return CS_ApplyJoinTeamList.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ApplyJoinTeamList req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        Set<String> playerIds = CpTeamManger.getInstance().findAllApplyJoinPlayer(playerIdx);

        SC_ApplyJoinTeamList.Builder msg = SC_ApplyJoinTeamList.newBuilder();
        addInvitePlayers(playerIds, msg);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ApplyJoinTeamList_VALUE, msg);

    }

    private void addInvitePlayers(Set<String> playerIds, SC_ApplyJoinTeamList.Builder msg) {
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
        gsChn.send(MsgIdEnum.SC_ApplyJoinTeamList_VALUE, SC_ApplyJoinTeamList.newBuilder().setRetCode(retCode));
    }
}
