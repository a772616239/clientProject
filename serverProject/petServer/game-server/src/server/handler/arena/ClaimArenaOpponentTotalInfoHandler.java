package server.handler.arena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.Arena.CS_ClaimArenaOpponentTotalInfo;
import protocol.Arena.SC_ClaimArenaOpponentTotalInfo;
import protocol.Arena.SC_ClaimArenaOpponentTotalInfo.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_ClaimArenaOpponentTotalInfo;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/05/19
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimArenaOpponentTotalInfo_VALUE)
public class ClaimArenaOpponentTotalInfoHandler extends AbstractBaseHandler<CS_ClaimArenaOpponentTotalInfo> {
    @Override
    protected CS_ClaimArenaOpponentTotalInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimArenaOpponentTotalInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimArenaOpponentTotalInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        arenaEntity entity = arenaCache.getInstance().getEntity(playerIdx);

        Builder resultBuilder = SC_ClaimArenaOpponentTotalInfo.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
            return;
        }

        ArenaOpponentTotalInfo opponentInfo = entity.getOpponentInfo(req.getPlayerIdx());
        if (opponentInfo != null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setTotalInfo(opponentInfo);
            gsChn.send(MsgIdEnum.SC_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
            return;
        }

        GS_CS_ClaimArenaOpponentTotalInfo.Builder builder = GS_CS_ClaimArenaOpponentTotalInfo.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setOpponentIdx(req.getPlayerIdx());
        if (!CrossServerManager.getInstance().sendMsgToArena(playerIdx, MsgIdEnum.GS_CS_ClaimArenaOpponentTotalInfo_VALUE, builder, false)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Arena_CanNotFindServer));
            gsChn.send(MsgIdEnum.SC_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Arena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimArenaOpponentTotalInfo_VALUE, SC_ClaimArenaOpponentTotalInfo.newBuilder().setRetCode(retCode));
    }
}
