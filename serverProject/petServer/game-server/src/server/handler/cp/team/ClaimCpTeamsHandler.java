package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Collection;
import model.cp.CpTeamManger;
import model.cp.entity.CpTeamPublish;
import protocol.Common.EnumFunction;
import protocol.CpFunction;
import protocol.CpFunction.CS_ClaimCPTeams;
import protocol.CpFunction.SC_ClaimCPTeams;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.GameUtil;

/**
 * 大厅拉取组队信息
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimCPTeams_VALUE)
public class ClaimCpTeamsHandler extends AbstractBaseHandler<CS_ClaimCPTeams> {

    private static final int teamSize = 20;

    @Override
    protected CS_ClaimCPTeams parse(byte[] bytes) throws Exception {
        return CS_ClaimCPTeams.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimCPTeams req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        Collection<CpTeamPublish> cpTeamPublishes = CpTeamManger.getInstance().randomTeamsForPlayer(playerIdx, teamSize);

        SC_ClaimCPTeams.Builder msg = SC_ClaimCPTeams.newBuilder();
        for (CpTeamPublish cpTeamPublish : cpTeamPublishes) {
            msg.addTeams(publishTeamsToClientTeams(cpTeamPublish));
        }

        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimCPTeams_VALUE, msg);
    }

    private CpFunction.CPTeam.Builder publishTeamsToClientTeams(CpTeamPublish cpTeamPublish) {
        CpFunction.CPTeam.Builder builder = CpFunction.CPTeam.newBuilder();
        builder.setTeamName(cpTeamPublish.getTeamName());
        builder.setNeedAbility(cpTeamPublish.getNeedAbility());
        builder.setTeamId(cpTeamPublish.getTeamId());
        for (String playerIdx : cpTeamPublish.getMembers()) {
            CpFunction.CPTeamPlayer.Builder player = CpFunctionUtil.queryCPTeamPlayer(playerIdx);
            if (player != null) {
                builder.addPlayers(player);
            }
        }
        return builder;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimCPTeams_VALUE, SC_ClaimCPTeams.newBuilder().setRetCode(retCode));
    }
}
