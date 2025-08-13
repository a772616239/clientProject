package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import model.cp.entity.CpTeamPublish;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_CreateCpTeam;
import protocol.CpFunction.SC_CreateCpTeam;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.GameUtil;

/**
 * 创建编队
 */
@MsgId(msgId = MsgIdEnum.CS_CreateCpTeam_VALUE)
public class CreateCpTeamHandler extends AbstractBaseHandler<CS_CreateCpTeam> {
    @Override
    protected CS_CreateCpTeam parse(byte[] bytes) throws Exception {
        return CS_CreateCpTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CreateCpTeam req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeId.RetCodeEnum retCodeEnum = CpTeamManger.getInstance().checkCreateTeam(playerIdx, req);
        SC_CreateCpTeam.Builder msg = SC_CreateCpTeam.newBuilder();
        if (retCodeEnum != RetCodeId.RetCodeEnum.RCE_Success) {
            msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
            gsChn.send(MsgIdEnum.SC_CreateCpTeam_VALUE, msg);
            return;
        }
        CpTeamPublish team = CpTeamManger.getInstance().createTeam(playerIdx, req);
        if (team == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_CP_PleaseReUploadTeam));
            gsChn.send(MsgIdEnum.SC_CreateCpTeam_VALUE, msg);
            return;
        }
        buildMsg(playerIdx, retCodeEnum, msg, team);
        gsChn.send(MsgIdEnum.SC_CreateCpTeam_VALUE, msg);
    }

    private void buildMsg(String playerIdx, RetCodeEnum retCodeEnum, SC_CreateCpTeam.Builder msg, CpTeamPublish team) {
        msg.setTeamId(team.getTeamId());
        msg.setTeamName(team.getTeamName());
        msg.setNeedAbility(team.getNeedAbility());
        msg.setPlayer(CpFunctionUtil.queryCPTeamPlayer(playerIdx));
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CreateCpTeam_VALUE, SC_CreateCpTeam.newBuilder().setRetCode(retCode));
    }
}
