package server.handler.matchArena;

import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.nio.charset.StandardCharsets;
import model.matchArena.ArenaRankPlayer;
import model.matchArena.MatchArenaRankManager;
import model.warpServer.WarpServerConst;
import org.apache.commons.lang.StringUtils;
import protocol.MatchArenaDB;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer;
import protocol.ServerTransfer.GS_BS_MatchArenaStartRankMatch;
import static util.JedisUtil.jedis;
import util.LogUtil;

/**
 * @author huhan
 * @date 2021/05/25
 */
@MsgId(msgId = MsgIdEnum.GS_BS_MatchArenaStartRankMatch_VALUE)
public class MatchArenaStartRankMatchHandler extends AbstractHandler<GS_BS_MatchArenaStartRankMatch> {
    @Override
    protected GS_BS_MatchArenaStartRankMatch parse(byte[] bytes) throws Exception {
        return GS_BS_MatchArenaStartRankMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_MatchArenaStartRankMatch req, int i) {
        ServerTransfer.BS_GS_MatchArenaStartRankMatch.Builder resultBuilder = ServerTransfer.BS_GS_MatchArenaStartRankMatch.newBuilder();
        resultBuilder.setPlayerIdx(req.getPlayerIdx());

        if (MatchArenaRankManager.getInstance().playerIsInMatch(req.getPlayerIdx())) {
            resultBuilder.setRetCode(RetCodeEnum.RCE_MatchArena_RepeatedMatching);
            gsChn.send(MsgIdEnum.BS_GS_MatchArenaStartRankMatch_VALUE, resultBuilder);
            return;
        }
        ArenaRankPlayer player = loadFromRedis(req.getPlayerIdx());
        if (player == null) {
            resultBuilder.setRetCode(RetCodeEnum.RCE_MatchArena_UpdateDataFailed);
            gsChn.send(MsgIdEnum.BS_GS_MatchArenaStartRankMatch_VALUE, resultBuilder);
            return;
        }

        int serverIndex = StringHelper.stringToInt(gsChn.getPlayerId(), 0);
        player.setFromSvrIndex(serverIndex);

        if (MatchArenaRankManager.getInstance().addMatchPlayer(player)) {
            resultBuilder.setRetCode(RetCodeEnum.RCE_Success);
        } else {
            resultBuilder.setRetCode(RetCodeEnum.RCE_ErrorParam);
        }
        gsChn.send(MsgIdEnum.BS_GS_MatchArenaStartRankMatch_VALUE, resultBuilder);
    }

    private ArenaRankPlayer loadFromRedis(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        byte[] playerInfoBytes = jedis.hget(GameConst.RedisKey.MatchArenaPlayerInfo.getBytes(StandardCharsets.UTF_8),
                playerIdx.getBytes(StandardCharsets.UTF_8));
        if (playerInfoBytes == null) {
            return null;
        }
        MatchArenaDB.RedisMatchArenaPlayerInfo redisArenaRankPlayerInfo;
        try {
            redisArenaRankPlayerInfo = MatchArenaDB.RedisMatchArenaPlayerInfo.parseFrom(playerInfoBytes);
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error("MatchArenaStartMatchHandler.loadFromRedis, can not parse player info, playerIdx:" + playerIdx);
            return null;
        }
        Double score = jedis.zscore(GameConst.RedisKey.MatchArenaPlayerScore.getBytes(StandardCharsets.UTF_8), playerIdx.getBytes(StandardCharsets.UTF_8));
        if (score == null || score == 0D) {
            LogUtil.error("MatchArenaStartRankMatchHandler.loadFromRedis, can not get player score, playerIdx:" + playerIdx);
            return null;
        }

        ArenaRankPlayer player = new ArenaRankPlayer();
        player.setPlayerBaseInfo(redisArenaRankPlayerInfo.getPlayerBaseInfo());
        player.setTeamInfo(redisArenaRankPlayerInfo.getTeamInfo());
        player.setScore(score.intValue());
        player.setLosingStreak(redisArenaRankPlayerInfo.getLosingStreak());
        player.setDan(redisArenaRankPlayerInfo.getDan());
        return player;
    }

}
