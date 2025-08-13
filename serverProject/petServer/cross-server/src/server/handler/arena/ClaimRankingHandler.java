package server.handler.arena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.arena.*;
import model.arena.entity.ArenaRoomRanking;
import model.arena.util.ArenaUtil;
import model.warpServer.WarpServerConst;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Arena.SC_ClaimArenaRanking;
import protocol.Arena.SC_ClaimArenaRanking.Builder;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer;
import protocol.ServerTransfer.GS_CS_ClaimArenaRanking;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Set;


/**
 * @author huhan
 * @date 2020/05/13
 */
@MsgId(msgId = MsgIdEnum.GS_CS_ClaimArenaRanking_VALUE)
public class ClaimRankingHandler extends AbstractHandler<GS_CS_ClaimArenaRanking> {
    @Override
    protected GS_CS_ClaimArenaRanking parse(byte[] bytes) throws Exception {
        return GS_CS_ClaimArenaRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ClaimArenaRanking req, int i) {
        DB_ArenaPlayerInfo entity = ArenaPlayerManager.getInstance().getPlayerBaseInfo(req.getPlayerIdx());

        Builder resultBuilder = SC_ClaimArenaRanking.newBuilder();
        ArenaRoomRanking roomRanking = null;
        if (entity == null) {
            Set<String> totalRoomIdxSet = ArenaManager.getInstance().getTotalRoomIdxSet();
            if (CollectionUtils.isNotEmpty(totalRoomIdxSet)) {
                roomRanking = ArenaManager.getInstance().getRoomRanking(new ArrayList<>(totalRoomIdxSet).get(0));
            }
        } else {
            roomRanking = ArenaManager.getInstance().getRoomRanking(entity.getRoomId());
        }
        if (roomRanking == null) {
          //  LogUtil.error("ClaimRankingHandler, room ranking is not exist, roomId:" + entity.getRoomId());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.CS_GS_TransArenaInfo_VALUE,
                    ArenaUtil.buildCsGsTrans(req.getPlayerIdx(), MsgIdEnum.SC_ClaimRanking_VALUE, resultBuilder));
            return;
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.addAllRankingInfo(roomRanking.getRankingInfo());
        resultBuilder.setPlayerRanking(roomRanking.queryPlayerRanking(req.getPlayerIdx()));
        resultBuilder.setDan(roomRanking.getDan());
        if (entity!=null) {
            resultBuilder.setPlayerScore(entity.getScore());
        }
        gsChn.send(MsgIdEnum.CS_GS_TransArenaInfo_VALUE,
                ArenaUtil.buildCsGsTrans(req.getPlayerIdx(), MsgIdEnum.SC_ClaimRanking_VALUE, resultBuilder));
    }
}
