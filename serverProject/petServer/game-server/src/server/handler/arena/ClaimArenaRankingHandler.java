package server.handler.arena;

import cfg.ArenaConfig;
import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.player.util.PlayerUtil;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Arena.CS_ClaimArenaRanking;
import protocol.Arena.SC_ClaimArenaRanking;
import protocol.Arena.SC_ClaimArenaRanking.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_ClaimArenaRanking;
import util.GameUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2020.03.11
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimArenaRanking_VALUE)
public class ClaimArenaRankingHandler extends AbstractBaseHandler<CS_ClaimArenaRanking> {
    @Override
    protected CS_ClaimArenaRanking parse(byte[] bytes) throws Exception {
        return CS_ClaimArenaRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimArenaRanking req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder resultBuilder = SC_ClaimArenaRanking.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.Arena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_ClaimArenaRanking_VALUE, resultBuilder);
            return;
        }

        arenaEntity entity = arenaCache.getByIdx(playerIdx);
        if(entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimArenaRanking_VALUE, resultBuilder);
            return;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        //TODO 暂时屏蔽排行榜刷新限制
        if ((currentTime - entity.getDbBuilder().getLastClaimRankingTime()) <=
                (ArenaConfig.getById(GameConst.CONFIG_ID).getRankrefrshtime() - 5) * TimeUtil.MS_IN_A_S) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Arena_RefreshFrequently));
            gsChn.send(MsgIdEnum.SC_ClaimArenaRanking_VALUE, resultBuilder);
            return;
        }

        if (!CrossServerManager.getInstance().sendMsgToArena(playerIdx, MsgIdEnum.GS_CS_ClaimArenaRanking_VALUE,
                GS_CS_ClaimArenaRanking.newBuilder().setPlayerIdx(playerIdx), false)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Arena_CanNotFindServer));
            gsChn.send(MsgIdEnum.SC_ClaimArenaRanking_VALUE, resultBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Arena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimArenaRanking_VALUE, SC_ClaimArenaRanking.newBuilder().setRetCode(retCode));
    }
}
