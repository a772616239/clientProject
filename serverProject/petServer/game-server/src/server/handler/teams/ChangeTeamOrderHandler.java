package server.handler.teams;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import model.team.util.TeamsUtil;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.CS_ChangeTeamOrder;
import protocol.PrepareWar.SC_ChangeTeamOrder;
import protocol.PrepareWar.SC_ChangeTeamOrder.Builder;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/05/18
 */
@MsgId(msgId = MsgIdEnum.CS_ChangeTeamOrder_VALUE)
public class ChangeTeamOrderHandler extends AbstractBaseHandler<CS_ChangeTeamOrder> {
    @Override
    protected CS_ChangeTeamOrder parse(byte[] bytes) throws Exception {
        return CS_ChangeTeamOrder.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChangeTeamOrder req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder resultBuilder = SC_ChangeTeamOrder.newBuilder();
        //必须为竞技场小队且都为进攻小队
        if (TeamsUtil.getTeamType(req.getFirst()) != TeamTypeEnum.TTE_Arena
                || !TeamsUtil.isArenaAttack(req.getFirst())
                || TeamsUtil.getTeamType(req.getSeconed()) != TeamTypeEnum.TTE_Arena
                || !TeamsUtil.isArenaAttack(req.getSeconed())
                || req.getFirst() == req.getSeconed()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ChangeTeamOrder_VALUE, resultBuilder);
            return;
        }

        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ChangeTeamOrder_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            Team firstTeam = entity.getDBTeam(req.getFirst());
            Team secondTeam = entity.getDBTeam(req.getSeconed());

            if (firstTeam == null || secondTeam == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ChangeTeamOrder_VALUE, resultBuilder);
                return;
            }

            firstTeam.setTeamNum(req.getSeconed());
            secondTeam.setTeamNum(req.getFirst());

            entity.putDBTeam(firstTeam);
            entity.putDBTeam(secondTeam);

            //刷新队伍
            entity.sendRefreshTeamsMsg(req.getFirst());
            entity.sendRefreshTeamsMsg(req.getSeconed());

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ChangeTeamOrder_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Teams;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ChangeTeamOrder_VALUE, SC_ChangeTeamOrder.newBuilder().setRetCode(retCode));
    }
}
