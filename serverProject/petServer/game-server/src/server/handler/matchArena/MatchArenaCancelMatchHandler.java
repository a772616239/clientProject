package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaManager;
import model.player.util.PlayerUtil;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MatchArenaCancelMatch;
import protocol.MatchArena.SC_MatchArenaCancelMatch;
import protocol.MatchArena.SC_MathArenaStartMatch;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_BS_MatchArenaCancelMatch;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/05/18
 */
@MsgId(msgId = MsgIdEnum.CS_MatchArenaCancelMatch_VALUE)
public class MatchArenaCancelMatchHandler extends AbstractBaseHandler<CS_MatchArenaCancelMatch> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MatchArenaCancelMatch.Builder resultBuilder = SC_MatchArenaCancelMatch.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_MatchArenaCancelMatch_VALUE, resultBuilder);
    }

    @Override
    protected CS_MatchArenaCancelMatch parse(byte[] bytes) throws Exception {
        return CS_MatchArenaCancelMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaCancelMatch req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_MathArenaStartMatch.Builder resultBuilder = SC_MathArenaStartMatch.newBuilder();

        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_MatchArena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_MatchArenaCancelMatch_VALUE, resultBuilder);
            return;
        }

        int serverIndex = MatchArenaManager.getInstance().getPlayerServerIndex(playerIdx);
        BaseNettyClient nettyClient = BattleServerManager.getInstance().getActiveNettyClient(serverIndex);
        if (nettyClient == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatchArena_CanNotCancelMatch));
            gsChn.send(MsgIdEnum.SC_MatchArenaCancelMatch_VALUE, resultBuilder);
            return;
        }

        GS_BS_MatchArenaCancelMatch.Builder cancelMatchBuilder = GS_BS_MatchArenaCancelMatch.newBuilder();
        cancelMatchBuilder.setPlayerIdx(playerIdx);
        cancelMatchBuilder.setArenaType(MatchArenaManager.getInstance().getCurArenaType().getCode());
        nettyClient.send(MsgIdEnum.GS_BS_MatchArenaCancelMatch_VALUE, cancelMatchBuilder);
        MatchArenaManager.getInstance().removeMatchingPlayer(playerIdx);
    }
}
