package model.crossarena;

import common.JedisUtil;
import db.entity.BaseEntity;
import lombok.Getter;
import model.crazyDuel.CrazyDuelManager;
import model.crossarena.dbCache.playercrossarenaCache;
import model.crossarena.entity.playercrossarenaEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.ranking.ranking.AbstractRanking;
import model.ranking.ranking.crossarena.AbstractCrossArenaRanking;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import protocol.Activity;
import protocol.CrossArena;
import redis.clients.jedis.Tuple;
import util.LogUtil;
import util.TimeUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static common.GameConst.RedisKey.CrossArenaRankPrefix;
import static common.GameConst.RedisKey.CrossArenaRankSync;
import static protocol.Activity.EnumRankingType.ERT_Lt_Duel;
import static protocol.Activity.EnumRankingType.ERT_Lt_Score;
import static protocol.Activity.EnumRankingType.ERT_Lt_SerialWin;
import static protocol.CrossArena.CrossArenaDBKey.*;
import static protocol.CrossArena.CrossArenaRankExKey.*;
import static protocol.CrossArena.CrossArenaRankType.*;

public class CrossArenaRankManager  {

    private static final String CrossArenaDuelRankKey = CrossArenaRankPrefix + "crazyDuel";
    private static final String CrossArenaScoreRankKey = CrossArenaRankPrefix + "score";
    private static final String CrossArenaSerialWinRankKey = CrossArenaRankPrefix + "serialWin";
    private static final String CrossArenaShowData = CrossArenaRankPrefix + "showData";

    private static final int pageSize = 100;

    private static final long tickInterval = TimeUtil.MS_IN_A_S * 2;

    private static long nextTick;

    private static final Map<CrossArena.CrossArenaRankType, String> rankKeyMap = new HashMap<>();

    private static final Map<CrossArena.CrossArenaRankType, Activity.EnumRankingType> rankTypeMap = new HashMap<>();

    private static Map<CrossArena.CrossArenaRankType, List<CrossArena.CrossArenaRankItem>> rankDataMap = Collections.emptyMap();

    static {
        rankKeyMap.put(CART_Score, CrossArenaScoreRankKey);
        rankKeyMap.put(CART_SerialWin, CrossArenaSerialWinRankKey);
        rankKeyMap.put(CART_Duel, CrossArenaDuelRankKey);
        rankTypeMap.put(CART_Score, ERT_Lt_Score);
        rankTypeMap.put(CART_SerialWin, ERT_Lt_SerialWin);
        rankTypeMap.put(CART_Duel, ERT_Lt_Duel);
    }

    @Getter
    private static CrossArenaRankManager instance = new CrossArenaRankManager();

    public double getPlayerRankScore(String playerIdx, CrossArena.CrossArenaRankType rankType) {
        String rankKey = rankKeyMap.get(rankType);
        if (StringUtils.isEmpty(rankKey)) {
            return 0;
        }
        return JedisUtil.jedis.zscore(rankKey, playerIdx);
    }


    public void uploadRankShowData(String playerIdx) {
        playerEntity playerEntity = playerCache.getByIdx(playerIdx);
        if (playerEntity == null) {
            return;
        }
        CrossArena.CrossArenaRankItem.Builder item = CrossArena.CrossArenaRankItem.newBuilder();
        item.setPlayerIdx(playerIdx);
        item.setPlayerName(playerEntity.getName());
        item.setHeader(playerEntity.getAvatar());
        item.setScienceId(CrossArenaManager.getInstance().findPlayerMaxSceneId(playerIdx));
        item.setHonorLv(CrossArenaManager.getInstance().findPlayerGradeLv(playerIdx));
        item.addExData(buildExData(CARED_Duel_AttackWinRate, CrazyDuelManager.getInstance().findPlayerAttackWinRate(playerIdx)));
        item.addExData(buildExData(CARED_Duel_DefendWinRate, CrazyDuelManager.getInstance().findPlayerDefendWinRate(playerIdx)));
        item.addExData(buildExData(CARED_Score_WinRate, CrossArenaManager.getInstance().findWeeklyWinRate(playerIdx)));
        item.addExData(buildExData(CARED_Score_BattleCount, CrossArenaManager.getInstance().findPlayerDbsDataByKey(playerIdx, LT_BATTLENUM_WEEK)));
        JedisUtil.jedis.hset(CrossArenaShowData.getBytes(StandardCharsets.UTF_8), playerIdx.getBytes(StandardCharsets.UTF_8), item.build().toByteArray());
    }

    private CrossArena.CrossArenaRankExData.Builder buildExData(CrossArena.CrossArenaRankExKey key, int playerAttackWinRate) {
        CrossArena.CrossArenaRankExData.Builder exData = CrossArena.CrossArenaRankExData.newBuilder();
        exData.setKey(key);
        exData.setValue(playerAttackWinRate);
        return exData;
    }

/*    @Override
    public void onTick() {
        if (GlobalTick.getInstance().getCurrentTime() < nextTick) {
            return;
        }
        updateCacheRank();
        nextTick = GlobalTick.getInstance().getCurrentTime() + tickInterval;
    }*/

    public List<CrossArena.CrossArenaRankItem> findRank(CrossArena.CrossArenaRankType type, int areaId) {
        Activity.EnumRankingType rankingType = rankTypeMap.get(type);
        if (rankingType == null) {
            return null;
        }
        String rankName = getRankName(rankingType, areaId);
        if (StringUtils.isEmpty(rankName)) {
            return Collections.emptyList();
        }
        AbstractRanking ranking = RankingManager.getInstance().getRanking(rankingType, rankName);
        if (!(ranking instanceof AbstractCrossArenaRanking)) {
            return Collections.emptyList();
        }
        return ((AbstractCrossArenaRanking) ranking).getShowRankData();
    }

    private String getRankName(Activity.EnumRankingType type, int sceneId) {

        if (type== ERT_Lt_SerialWin){
            return RankingUtils.getLtSerialWinRankName(sceneId);
        }
        return RankingUtils.getRankingTypeDefaultName(type);
    }

    private void updateCacheRank() {

        Map<CrossArena.CrossArenaRankType,
                List<CrossArena.CrossArenaRankItem>> allRankData = new HashMap<>();

        for (Map.Entry<CrossArena.CrossArenaRankType, String> entry : rankKeyMap.entrySet()) {

            Set<Tuple> rankInfos = findRankData(entry.getValue(), 0, pageSize - 1);
            if (CollectionUtils.isEmpty(rankInfos)) {
                allRankData.put(entry.getKey(), Collections.emptyList());
                continue;
            }

            List<CrossArena.CrossArenaRankItem> rankData = new ArrayList<>(pageSize);
            List<String> playerIds = rankInfos.stream().map(Tuple::getElement).collect(Collectors.toList());
            Map<String, CrossArena.CrossArenaRankItem> showMap = findPlayerShowData(playerIds);

            CrossArena.CrossArenaRankItem rankItem;
            int rank = 0;
            for (Tuple rankInfo : rankInfos) {
                rank++;
                rankItem = showMap.get(rankInfo.getElement());
                if (rankItem != null) {
                    rankItem = rankItem.toBuilder().setRank(rank).setRankScore((long) rankInfo.getScore()).build();
                    rankData.add(rankItem);
                } else {
                    LogUtil.error("cross arena rank:{} player:{} data is miss", entry.getKey(), rankInfo.getElement());
                }
            }
            allRankData.put(entry.getKey(), rankData);
        }
        rankDataMap = allRankData;
    }

    private Set<Tuple> findRankData(String key, int start, int end) {
        return JedisUtil.jedis.zrevrangeWithScores(key, start, end);
    }

    public Map<String, CrossArena.CrossArenaRankItem> findPlayerShowData(List<String> playerIds) {
        List<byte[]> source = new ArrayList<>();
        for (String playerId : playerIds) {
            source.add(playerId.getBytes(StandardCharsets.UTF_8));
        }

        List<byte[]> data = JedisUtil.jedis.hmget(CrossArenaShowData.getBytes(StandardCharsets.UTF_8), source.toArray(new byte[][]{}));
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyMap();
        }

        Map<String, CrossArena.CrossArenaRankItem> result = new HashMap<>();

        for (byte[] bytes : data) {
            try {
                if (bytes != null) {
                    CrossArena.CrossArenaRankItem rankItem = CrossArena.CrossArenaRankItem.parseFrom(bytes);
                    result.put(rankItem.getPlayerIdx(), rankItem);
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return result;

    }

    public void init() {
       // GlobalTick.getInstance().addTick(this);
        moveRedisDataToPlatform();
    }

    /**
     * 把redis的排行榜移动到平台排行榜
     */
    public void moveRedisDataToPlatform() {
        Set<byte[]> hkeys = JedisUtil.jedis.hkeys(CrossArenaShowData.getBytes(StandardCharsets.UTF_8));
        if (CollectionUtils.isEmpty(hkeys)) {
            return;
        }
        String playerIdx;
        for (BaseEntity value : playercrossarenaCache.getInstance()._ix_id.values()) {
            playercrossarenaEntity entity = (playercrossarenaEntity) value;
            playerIdx = entity.getIdx();
            int maxSceneId = CrossArenaManager.getInstance().findPlayerMaxSceneId(playerIdx);
            for (int sceneId = 1; sceneId <= maxSceneId; sceneId++) {
                int serialWinNumHis = Math.min(10, entity.getDataMsg().getDbsOrDefault(LT_WINCOTHIS_VALUE, 0));
                int winNumByScienceId = CrossArenaManager.getInstance().getWinNumByScienceId(entity, sceneId, LT_WINCOTHISNum);
                if (serialWinNumHis > 0 && winNumByScienceId > 0) {
                    RankingManager.getInstance().updatePlayerRankingScore(playerIdx, Activity.EnumRankingType.ERT_Lt_SerialWin,
                            RankingUtils.getLtSerialWinRankName(sceneId),
                            serialWinNumHis
                            , winNumByScienceId);
                }
            }
        }



        for (byte[] bytes : hkeys) {
            try {
                uploadRankShowData(new String(bytes));
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
    }
}