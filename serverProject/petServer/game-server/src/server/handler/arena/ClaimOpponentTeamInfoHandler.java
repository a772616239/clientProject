package server.handler.arena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.Arena.CS_ClaimOpponentTeamInfo;
import protocol.Arena.SC_ClaimOpponentTeamInfo;
import protocol.Arena.SC_ClaimOpponentTeamInfo.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.03.09
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimOpponentTeamInfo_VALUE)
public class ClaimOpponentTeamInfoHandler extends AbstractBaseHandler<CS_ClaimOpponentTeamInfo> {
    @Override
    protected CS_ClaimOpponentTeamInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimOpponentTeamInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimOpponentTeamInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder resultBuilder = SC_ClaimOpponentTeamInfo.newBuilder();
        arenaEntity entity = arenaCache.getInstance().getEntity(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimOpponentTeamInfo_VALUE, resultBuilder);
            return;
        }

        ArenaOpponentTotalInfo teamsInfo = entity.getOpponentInfo(req.getPlayerIdx());
        //直接拿取本地记录发送
        if (teamsInfo == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimOpponentTeamInfo_VALUE, resultBuilder);
            return;
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.addAllTeams(teamsInfo.getTeamsInfoList());
        gsChn.send(MsgIdEnum.SC_ClaimOpponentTeamInfo_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Arena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimOpponentTeamInfo_VALUE, SC_ClaimOpponentTeamInfo.newBuilder().setRetCode(retCode));
    }
}
