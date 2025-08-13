package server.handler.matchArena;

import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import model.matchArena.MatchArenaNormalManager;
import model.matchArena.NormalMatchPlayer;
import model.warpServer.WarpServerConst;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import protocol.Battle;
import protocol.MatchArenaDB;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaStartNormalMatch;
import protocol.ServerTransfer.GS_BS_MatchArenaStartNormalMatch;
import static util.JedisUtil.jedis;
import util.LogUtil;

/**
 * @author huhan
 * @date 2021/05/25
 */
@MsgId(msgId = MsgIdEnum.GS_BS_MatchArenaStartNormalMatch_VALUE)
public class MatchArenaStartNormalMatchHandler extends AbstractHandler<GS_BS_MatchArenaStartNormalMatch> {
    @Override
    protected GS_BS_MatchArenaStartNormalMatch parse(byte[] bytes) throws Exception {
        return GS_BS_MatchArenaStartNormalMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_MatchArenaStartNormalMatch req, int i) {
        BS_GS_MatchArenaStartNormalMatch.Builder resultBuilder = BS_GS_MatchArenaStartNormalMatch.newBuilder();
        resultBuilder.setPlayerIdx(req.getPlayerId());

        if (MatchArenaNormalManager.getInstance().playerIsInMatch(req.getPlayerId())) {
            resultBuilder.setRetCode(RetCodeEnum.RCE_MatchArena_RepeatedMatching);
            gsChn.send(MsgIdEnum.BS_GS_MatchArenaStartNormalMatch_VALUE, resultBuilder);
            return;
        }

        NormalMatchPlayer player2 = loadFromRedis(req.getPlayerId());
        if (player2 == null) {
            resultBuilder.setRetCode(RetCodeEnum.RCE_MatchArena_UpdateDataFailed);
            gsChn.send(MsgIdEnum.BS_GS_MatchArenaStartNormalMatch_VALUE, resultBuilder);
            return;
        }

        int serverIndex = StringHelper.stringToInt(gsChn.getPlayerId(), 0);
        if (MatchArenaNormalManager.getInstance().addMatchPlayer(initNormalMatchPlayer(player2,req, serverIndex))) {
            resultBuilder.setRetCode(RetCodeEnum.RCE_Success);
        } else {
            resultBuilder.setRetCode(RetCodeEnum.RCE_ErrorParam);
        }
        gsChn.send(MsgIdEnum.BS_GS_MatchArenaStartNormalMatch_VALUE, resultBuilder);
    }

    private NormalMatchPlayer initNormalMatchPlayer(NormalMatchPlayer player, GS_BS_MatchArenaStartNormalMatch req, int serverIndex) {
        player.setFromSvrIndex(serverIndex);
        player.setPlayerIdx(req.getPlayerId());
        List<Integer> petCfgIds = player.getTeamInfo().getPetListList().stream()
                .map(Battle.BattlePetData::getPetCfgId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(petCfgIds)) {
            player.setPetCfgIds(petCfgIds);
        }
        return player;
    }

    private NormalMatchPlayer loadFromRedis(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        byte[] playerInfoBytes = jedis.hget(GameConst.RedisKey.MatchArenaPlayerInfo.getBytes(StandardCharsets.UTF_8),
                playerIdx.getBytes(StandardCharsets.UTF_8));
        if (playerInfoBytes == null) {
            return null;
        }
        MatchArenaDB.RedisMatchArenaPlayerInfo redisMatchArenaPlayerInfo;
        try {
            redisMatchArenaPlayerInfo = MatchArenaDB.RedisMatchArenaPlayerInfo.parseFrom(playerInfoBytes);
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error("MatchArenaStartMatchHandler.loadFromRedis, can not parse player info, playerIdx:" + playerIdx);
            return null;
        }

        NormalMatchPlayer player = new NormalMatchPlayer();
        player.setPlayerBaseInfo(redisMatchArenaPlayerInfo.getPlayerBaseInfo());
        player.setTeamInfo(redisMatchArenaPlayerInfo.getTeamInfo());
        return player;
    }

}
