package server.handler.teams;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.team.dbCache.teamCache;
import model.team.entity.teamEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.CS_ClaimTeamsInfo;
import protocol.PrepareWar.SC_ClaimTeamsInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimTeamsInfo_VALUE)
public class ClaimTeamsInfoHandler extends AbstractBaseHandler<CS_ClaimTeamsInfo> {
    @Override
    protected CS_ClaimTeamsInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimTeamsInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimTeamsInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimTeamsInfo.Builder resultBuilder = SC_ClaimTeamsInfo.newBuilder();
        teamEntity teams = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teams == null) {
            LogUtil.info("playerIdx [" + playerIdx + "] teamEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimTeamsInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(teams, entity -> {
//            teams.checkTeamPetIdx();
            teams.sendTeamsInfo();
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimTeamsInfo_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.NullFuntion;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimTeamsInfo_VALUE, SC_ClaimTeamsInfo.newBuilder().setRetCode(retCode));
    }
}
