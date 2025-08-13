package server.handler.matchArena;

import common.AbstractBaseHandler;
import common.GameConst;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.matcharena.MatchArenaLTManager;
import model.matcharena.MatchArenaManager;
import model.matcharena.dbCache.matcharenaCache;
import model.player.util.PlayerUtil;
import model.team.dbCache.teamCache;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MathArenaStartMatch;
import protocol.MatchArena.SC_MathArenaStartMatch;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer;
import util.GameUtil;

import java.util.List;


@MsgId(msgId = MsgIdEnum.CS_MathArenaStartMatch_VALUE)
public class MathArenaStartMatchHandler extends AbstractBaseHandler<CS_MathArenaStartMatch> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MathArenaStartMatch.Builder resultBuilder = SC_MathArenaStartMatch.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_MathArenaStartMatch_VALUE, resultBuilder);
    }

    @Override
    protected CS_MathArenaStartMatch parse(byte[] bytes) throws Exception {
        return CS_MathArenaStartMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MathArenaStartMatch req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_MathArenaStartMatch.Builder resultBuilder = SC_MathArenaStartMatch.newBuilder();

        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_MatchArena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_MathArenaStartMatch_VALUE, resultBuilder);
            return;
        }

        if (MatchArenaLTManager.getInstance().checkPlayerAtLeitai(playerIdx)){
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatchArenaLT_LTING));
            gsChn.send(MsgIdEnum.SC_MathArenaStartMatch_VALUE, resultBuilder);
            return;
        }

        if (BattleManager.getInstance().isInBattle(playerIdx)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Battle_Batting));
            gsChn.send(MsgIdEnum.SC_MathArenaStartMatch_VALUE, resultBuilder);
            return;
        }

        GameConst.ArenaType curArenaType = MatchArenaManager.getInstance().getCurArenaType();
        if (curArenaType == null || curArenaType == GameConst.ArenaType.Null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_MathArenaStartMatch_VALUE, resultBuilder);
            return;
        }

        List<String> petList = teamCache.getInstance().getCurUsedTeamPetIdxList(playerIdx, getTeamType(curArenaType));
        if (CollectionUtils.isEmpty(petList)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Battle_UsedTeamNotHavePet));
            gsChn.send(MsgIdEnum.SC_MathArenaStartMatch_VALUE, resultBuilder);
            return;
        }

        BaseNettyClient nettyClient = BattleServerManager.getInstance().getAvailableBattleServer();
        if (nettyClient == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MineFight_NotFoundBattleSrv));
            gsChn.send(MsgIdEnum.SC_MathArenaStartMatch_VALUE, resultBuilder);
            return;
        }

        addPlayerToMatch(playerIdx, nettyClient, curArenaType);
    }

    private TeamTypeEnum getTeamType(GameConst.ArenaType curArenaType) {
        return curArenaType == GameConst.ArenaType.Normal ? TeamTypeEnum.TTE_MatchArenaNormal : TeamTypeEnum.TTE_MatchArenaRank;
    }

    private void addPlayerToMatch(String playerIdx, BaseNettyClient nettyClient, GameConst.ArenaType curArenaType) {
        matcharenaCache.getInstance().updatePlayerInfoToRedis(playerIdx, curArenaType);
        switch (curArenaType) {
            case Normal:
                sendJoinNormalArenaMatch(playerIdx, nettyClient);
                break;
            case Rank:
                sendJoinRankArenaMatch(playerIdx, nettyClient);
                break;
            default:
                return;
        }
        MatchArenaManager.getInstance().savePlayerServerIndex(playerIdx, nettyClient.getServerIndex());
    }

    private void sendJoinRankArenaMatch(String playerIdx, BaseNettyClient nettyClient) {
        ServerTransfer.GS_BS_MatchArenaStartRankMatch.Builder startMatchBuilder = ServerTransfer.GS_BS_MatchArenaStartRankMatch.newBuilder();
        startMatchBuilder.setPlayerIdx(playerIdx);
        nettyClient.send(MsgIdEnum.GS_BS_MatchArenaStartRankMatch_VALUE, startMatchBuilder);
    }

    private void sendJoinNormalArenaMatch(String playerIdx, BaseNettyClient nettyClient) {
        ServerTransfer.GS_BS_MatchArenaStartNormalMatch.Builder startMatchBuilder = ServerTransfer.GS_BS_MatchArenaStartNormalMatch.newBuilder();
        startMatchBuilder.setPlayerId(playerIdx);
        nettyClient.send(MsgIdEnum.GS_BS_MatchArenaStartNormalMatch_VALUE, startMatchBuilder);
    }
}
