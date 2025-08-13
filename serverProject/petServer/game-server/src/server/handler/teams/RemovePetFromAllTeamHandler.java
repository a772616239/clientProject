package server.handler.teams;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.HashSet;
import model.team.dbCache.teamCache;
import model.team.entity.teamEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.CS_RemovePetFromAllTeam;
import protocol.PrepareWar.SC_RemovePetFromAllTeam;
import protocol.PrepareWar.SC_RemovePetFromAllTeam.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/07/02
 */
@MsgId(msgId = MsgIdEnum.CS_RemovePetFromAllTeam_VALUE)
public class RemovePetFromAllTeamHandler extends AbstractBaseHandler<CS_RemovePetFromAllTeam> {
    @Override
    protected CS_RemovePetFromAllTeam parse(byte[] bytes) throws Exception {
        return CS_RemovePetFromAllTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RemovePetFromAllTeam req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        Builder resultBuilder = SC_RemovePetFromAllTeam.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_RemovePetFromAllTeam_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.removePetFromTeam(new HashSet<>(req.getPetIdxList()));
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_RemovePetFromAllTeam_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Teams;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_RemovePetFromAllTeam_VALUE, SC_RemovePetFromAllTeam.newBuilder().setRetCode(retCode));
    }
}
