package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import model.team.dbCache.teamCache;
import org.springframework.util.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_CpUseLastTeamInfo;
import protocol.CpFunction.SC_CpUseLastTeamInfo;
import protocol.CpFunction.SC_CpUseLastTeamInfo.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import java.util.List;

/**
 * 组队使用上次的编队
 */
@MsgId(msgId = MsgIdEnum.CS_CpUseLastTeamInfo_VALUE)
public class CpUseLastTeamHandler extends AbstractBaseHandler<CS_CpUseLastTeamInfo> {
    @Override
    protected CS_CpUseLastTeamInfo parse(byte[] bytes) throws Exception {
        return CS_CpUseLastTeamInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CpUseLastTeamInfo req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        RetCodeEnum codeEnum = useLastTeam(playerIdx);
        Builder msg = SC_CpUseLastTeamInfo.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MsgIdEnum.SC_CpUseLastTeamInfo_VALUE, msg);
    }

    private RetCodeEnum useLastTeam(String playerIdx) {
        List<String> teamPetIdxList =
                teamCache.getInstance().getTeamPetIdxList(playerIdx, TeamNumEnum.TNE_LtCP_1);
        if (CollectionUtils.isEmpty(teamPetIdxList)) {
            return RetCodeEnum.RCE_TheWar_EmptyPetTeam;
        }

        CpTeamManger.getInstance().uploadPlayerInfo(playerIdx);

        return RetCodeEnum.RCE_Success;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CpUseLastTeamInfo_VALUE, SC_CpUseLastTeamInfo.newBuilder().setRetCode(retCode));
    }
}
