package model.warpServer.battleServer.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.matcharena.MatchArenaManager;
import protocol.Common;
import protocol.MessageId;
import protocol.ServerTransfer;

import static protocol.MessageId.MsgIdEnum.GS_BS_BuildMatchArenaPet_VALUE;

/**
 * 构建匹配竞技场宠物
 */
@MsgId(msgId = MessageId.MsgIdEnum.BS_GS_BuildMatchArenaPet_VALUE)
public class BuildMatchArenaPetHandler extends AbstractBaseHandler<ServerTransfer.BS_GS_BuildMatchArenaPet> {
    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        ServerTransfer.GS_BS_BuildMatchArenaPet.Builder resultBuilder = ServerTransfer.GS_BS_BuildMatchArenaPet.newBuilder();
        gsChn.send(GS_BS_BuildMatchArenaPet_VALUE, resultBuilder);
    }

    @Override
    protected ServerTransfer.BS_GS_BuildMatchArenaPet parse(byte[] bytes) throws Exception {
        return ServerTransfer.BS_GS_BuildMatchArenaPet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, ServerTransfer.BS_GS_BuildMatchArenaPet req, int i) {
        String battleId = req.getBattleId();
        List<Integer> playerPetCfgIdList = req.getPlayerPetCfgIdList();
        List<Integer> opponentPetCfgIdList = req.getOpponentPetCfgIdList();
        String playerId = req.getPlayerId();

        ServerTransfer.GS_BS_BuildMatchArenaPet.Builder msg = ServerTransfer.GS_BS_BuildMatchArenaPet.newBuilder();
        msg.addAllPlayerPets(MatchArenaManager.getInstance().recreateBattlePets(playerId, playerPetCfgIdList));
        msg.addAllOpponentPets(MatchArenaManager.getInstance().recreateBattlePets(playerId, opponentPetCfgIdList));
        msg.setBattleId(battleId);
        gsChn.send(GS_BS_BuildMatchArenaPet_VALUE, msg);
    }
}
