package server.handler.arena;

import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.arena.ArenaManager;
import model.arena.ArenaPlayerManager;
import model.timer.TimerConst.TimerIdx;
import model.timer.dbCache.timerCache;
import model.warpServer.WarpServerConst;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_JoinArena;
import protocol.ServerTransfer.GS_CS_JoinArena;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/05/12
 */
@MsgId(msgId = MsgIdEnum.GS_CS_JoinArena_VALUE)
public class JoinArenaHandler extends AbstractHandler<GS_CS_JoinArena> {
    @Override
    protected GS_CS_JoinArena parse(byte[] bytes) throws Exception {
        return GS_CS_JoinArena.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_JoinArena req, int i) {
        String playerIdx = req.getBaseInfo().getPlayerIdx();

        CS_GS_JoinArena.Builder resultBuilder = CS_GS_JoinArena.newBuilder();
        if (StringUtils.isBlank(playerIdx)) {
            LogUtil.error("JoinArenaHandler, playerIdx is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.CS_GS_JoinArena_VALUE, resultBuilder);
            return;
        }

        String newIp = WarpServerConst.parseIp(gsChn.channel.remoteAddress().toString().substring(1));
        int serverIndex = StringHelper.stringToInt(gsChn.getPlayerId(), 0);

        //更新玩家信息
        ArenaPlayerManager.getInstance().syncMergePlayerInfoToRedis(req, serverIndex);

        //检查房间是否存在不存在需要重新分配房间
        DB_ArenaPlayerInfo.Builder baseInfoBuilder = ArenaPlayerManager.getInstance().getPlayerBaseInfoBuilder(playerIdx);
        if (baseInfoBuilder == null) {
            LogUtil.error("server.handler.arena.JoinArenaHandler.execute, player base info is null, player:" + playerIdx);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Arena_PlayerIsNotExist));
            gsChn.send(MsgIdEnum.CS_GS_JoinArena_VALUE, resultBuilder);
            return;
        }

        if (!ArenaManager.getInstance().roomIsExist(baseInfoBuilder.getRoomId())) {
            LogUtil.info("JoinArenaHandler, player room is not exist, need allocation a new room, playerIdx:"
                    + playerIdx + ", old roomId:" + baseInfoBuilder.getRoomId());

            String newRoomId = ArenaManager.getInstance().allocationRoom(playerIdx, baseInfoBuilder.getDan());
            if (newRoomId == null) {
                LogUtil.error("JoinArenaHandler, player allocation room failed, playerIdx:" + playerIdx);
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Arena_AllocationRoomFailed));
                gsChn.send(MsgIdEnum.CS_GS_JoinArena_VALUE, resultBuilder);
                return;
            }

            //设置新的房间
            baseInfoBuilder.setRoomId(newRoomId);
            if (!ArenaPlayerManager.getInstance().syncUpdatePlayerInfoToRedis(baseInfoBuilder.build())) {
                LogUtil.error("JoinArenaHandler, player update failed, playerIdx:" + playerIdx);
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.CS_GS_JoinArena_VALUE, resultBuilder);
                return;
            }
        }

        if (req.getNeedRefreshOpponent()) {
            List<ArenaOpponentTotalInfo> opponents =
                    ArenaManager.getInstance().randomOpponent(baseInfoBuilder.getRoomId(), baseInfoBuilder.getBaseInfo().getPlayerIdx());
            if (CollectionUtils.isEmpty(opponents)) {
                LogUtil.error("JoinArenaHandler, player random opponent failed, playerIdx:" + playerIdx);
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.CS_GS_JoinArena_VALUE, resultBuilder);
                return;
            }

            resultBuilder.addAllOpponnentInfo(opponents);
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setRoomId(baseInfoBuilder.getRoomId());
        resultBuilder.setPlayerIdx(playerIdx);
        resultBuilder.setDan(baseInfoBuilder.getDan());
        resultBuilder.setScore(baseInfoBuilder.getScore());
        resultBuilder.setFightAbility(baseInfoBuilder.getBaseInfo().getFightAbility());
        resultBuilder.setRanking(ArenaManager.getInstance().queryPlayerRanking(baseInfoBuilder.getRoomId(), baseInfoBuilder.getBaseInfo().getPlayerIdx()));
        resultBuilder.setDirectUpCount(baseInfoBuilder.getKillDirectUpCount());
        resultBuilder.setNextSettleDanTime(timerCache.getInstance().getNextTriggerTime(TimerIdx.TI_SETTLE_ARENA_DAN));
        gsChn.send(MsgIdEnum.CS_GS_JoinArena_VALUE, resultBuilder);
    }
}
