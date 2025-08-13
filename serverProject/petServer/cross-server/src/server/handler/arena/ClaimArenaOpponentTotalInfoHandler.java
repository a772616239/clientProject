package server.handler.arena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.arena.ArenaPlayerManager;
import model.arena.util.ArenaUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_ClaimArenaOpponentTotalInfo;
import protocol.ServerTransfer.GS_CS_ClaimArenaOpponentTotalInfo;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/05/19
 */
@MsgId(msgId = MsgIdEnum.GS_CS_ClaimArenaOpponentTotalInfo_VALUE)
public class ClaimArenaOpponentTotalInfoHandler extends AbstractHandler<GS_CS_ClaimArenaOpponentTotalInfo> {
    @Override
    protected GS_CS_ClaimArenaOpponentTotalInfo parse(byte[] bytes) throws Exception {
        return GS_CS_ClaimArenaOpponentTotalInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ClaimArenaOpponentTotalInfo req, int i) {
        String opponentIdx = req.getOpponentIdx();

        CS_GS_ClaimArenaOpponentTotalInfo.Builder resultBuilder = CS_GS_ClaimArenaOpponentTotalInfo.newBuilder();
        resultBuilder.setPlayerIdx(req.getPlayerIdx());

        DB_ArenaPlayerInfo info = ArenaPlayerManager.getInstance().getPlayerBaseInfo(req.getPlayerIdx());
        if (info == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Arena_PlayerIsNotExist));
            gsChn.send(MsgIdEnum.CS_GS_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
            return;
        }

        List<Integer> teams = ArenaUtil.getDanUseDefinedTeams(info.getDan());
        if (CollectionUtils.isEmpty(teams)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.CS_GS_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
            return;
        }

        ArenaOpponentTotalInfo arenaOpponentTotalInfo
                = ArenaPlayerManager.getInstance().buildArenaOpponentTotalInfo(opponentIdx, false, teams);
        if (arenaOpponentTotalInfo == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.CS_GS_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setTotalInfo(arenaOpponentTotalInfo);
        gsChn.send(MsgIdEnum.CS_GS_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
    }
}
