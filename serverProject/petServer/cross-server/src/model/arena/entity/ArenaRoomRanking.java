package model.arena.entity;

import cfg.ArenaConfig;
import common.GameConst;
import common.GlobalData;
import common.GlobalTick;
import common.HttpRequestUtil;
import common.entity.HttpRankingResponse;
import common.entity.RankingQueryRequest;
import common.entity.RankingQueryResult;
import common.entity.RankingQuerySingleResult;
import common.entity.RankingScore;
import common.entity.RankingUpdateRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import model.arena.ArenaManager;
import model.arena.ArenaPlayerManager;
import model.arena.util.ArenaUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Arena.ArenaRankingPlayerInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.ArenaDB.DB_ArenaRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.ArenaRankingInfo;
import protocol.ServerTransfer.ArenaRankingInfo.Builder;
import protocol.ServerTransfer.CS_GS_ArenaRankingSettle;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2020/06/16
 */
@Getter
@Setter
public class ArenaRoomRanking {

    /**
     * 每次更新排行榜的最大条数
     */
    public static final int RANKING_UPDATE_MAX_SIZE = 100;

    private String roomId;

    private int dan;

    private final Map<String, RankingQuerySingleResult> playerRankingInfo = new ConcurrentHashMap<>();
    /**
     * 排行榜前数据
     */
    private final List<ArenaRankingPlayerInfo> rankingInfo = new ArrayList<>();

    private volatile long nextUpdateRankingTime;

    private ArenaRoomRanking() {
    }

    public int queryPlayerRanking(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return -1;
        }
        RankingQuerySingleResult rankingInfo = this.playerRankingInfo.get(playerIdx);
        if (rankingInfo == null) {
            return -1;
        }
        return rankingInfo.getRanking();
    }


    private List<RankingQuerySingleResult> queryRanking() {
        RankingQueryRequest query = new RankingQueryRequest();
        query.setRank(ArenaUtil.getRoomRankingName(getRoomId()));
        query.setServerIndex(GameConst.CROSS_RANKING_SERVER_INDEX);
        query.setPage(1);
        query.setSize(getRoomMaxSize());
        HttpRankingResponse result = HttpRequestUtil.queryRanking(query);
        if (result == null) {
            LogUtil.error("query arena ranking result is null");
            return null;
        }
        RankingQueryResult data = result.getData();
        if (data == null) {
            LogUtil.error("query arena ranking data is null");
            return null;
        }

        List<RankingQuerySingleResult> pageInfo = data.getPageInfo();
        if (pageInfo == null) {
            LogUtil.error("query arena ranking page data is null, curTime = " + GlobalTick.getInstance().getCurrentTime());
            return null;
        }
        return pageInfo;
    }

    public int getRoomMaxSize() {
        return ArenaUtil.getRoomMaxSize(getDan());
    }

    /**
     * 更新玩家排行榜数据
     */
    public void updatePlayerRanking(DB_ArenaPlayerInfo playerInfo) {
        updatePlayerRanking(Collections.singletonList(playerInfo));
    }

    public void updatePlayerRanking(Collection<DB_ArenaPlayerInfo> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        Collection<List<DB_ArenaPlayerInfo>> collections = GameUtil.splitCollection(entities, RANKING_UPDATE_MAX_SIZE);
        if (CollectionUtils.isEmpty(collections)) {
            return;
        }

        for (Collection<DB_ArenaPlayerInfo> collection : collections) {
            RankingUpdateRequest updateRequest = new RankingUpdateRequest(ArenaUtil.getRoomRankingName(getRoomId()), true);
            for (DB_ArenaPlayerInfo entity : collection) {
                updateRequest.addItems(new RankingScore(entity.getBaseInfo().getPlayerIdx(), entity.getScore()));
            }
//            EventUtil.updateRanking(updateRequest);
            HttpRequestUtil.updateRanking(updateRequest);
        }
    }

    /**
     * 更新房间排行榜
     */
    public synchronized void queryRoomRanking() {
        long startQueryTime = Instant.now().toEpochMilli();
        List<RankingQuerySingleResult> results = queryRanking();
        LogUtil.debug("model.arena.entity.ArenaRoomRanking.queryRoomRanking, roomId:" + getRoomId()
                + "query ranking use time:" + (Instant.now().toEpochMilli() - startQueryTime));

        long startBuildTime = Instant.now().toEpochMilli();
        if (results == null) {
//            LogUtil.error("ArenaRoom.updateRoomRanking, failed, result is empty");
            return;
        }

        playerRankingInfo.clear();
        rankingInfo.clear();

        List<RankingQuerySingleResult> needBuildPlayerInfo = new ArrayList<>();
        for (RankingQuerySingleResult result : results) {
            playerRankingInfo.put(result.getPrimaryKey(), result);

            if (result.getRanking() <= ArenaConfig.getById(GameConst.ConfigId).getRankingdisplaycount()) {
                needBuildPlayerInfo.add(result);
            }
        }

        if (CollectionUtils.isNotEmpty(needBuildPlayerInfo)) {
            List<ArenaRankingPlayerInfo> rankingPlayerInfoList
                    = ArenaUtil.buildArenaRankingPlayerInfoByList(needBuildPlayerInfo);
            if (CollectionUtils.isNotEmpty(needBuildPlayerInfo)) {
                rankingInfo.addAll(rankingPlayerInfoList);
            }
        }

        LogUtil.debug("model.arena.entity.ArenaRoomRanking.queryRoomRanking, build ranking info use time:"
                + ((Instant.now().toEpochMilli() - startBuildTime)));

        LogUtil.debug("update arenaRoomId:" + getRoomId() + " ranking finished, rankingSize:" + results.size());
    }

    public static ArenaRoomRanking create(String roomId, int dan) {
        if (StringUtils.isBlank(roomId)) {
            LogUtil.error("ArenaRoomRanking.create error params, roomId:" + roomId);
            return null;
        }

        ArenaRoomRanking ranking = new ArenaRoomRanking();
        ranking.setRoomId(roomId);
        ranking.setDan(dan);
        ranking.queryRoomRanking();
        return ranking;
    }

//    public void clearPlayerRanking(String playerIdx) {
//        if (StringUtils.isBlank(playerIdx)) {
//            return;
//        }
//        clearPlayerRankingByList(Collections.singletonList(playerIdx));
//    }
//
//    public void clearPlayerRankingByList(List<String> playerIdxList) {
//        if (CollectionUtils.isEmpty(playerIdxList)) {
//            return;
//        }
//        EventUtil.clearRanking(ArenaUtil.getRoomRankingName(getRoomId()), playerIdxList);
//    }

    /**
     * 最新的排行榜数据在开始结算时使用抛事件调用一次,再此处不再更新提高结算速度
     *
     * @return
     */
    public synchronized boolean settleRanking() {
        DB_ArenaRoom arenaRoom = ArenaManager.getInstance().getRoomByRoomId(getRoomId());
        if (arenaRoom == null) {
            LogUtil.error("ArenaRoomRanking.settleRanking, arena room is not exist, roomId:" + roomId);
            return false;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        int dayOfWeek = TimeUtil.getDayOfWeek(currentTime);

        CS_GS_ArenaRankingSettle.Builder builder = CS_GS_ArenaRankingSettle.newBuilder();
        //如果是周日结算周排行榜,否则结算日排行榜   1:每日， 2：每周,
        builder.setType(dayOfWeek == 7 ? 2 : 1);
        builder.setDan(getDan());

        //获取所有房间内玩家数据
        Map<String, DB_ArenaPlayerInfo> allRoomPlayerInfo = ArenaPlayerManager.getInstance().getPlayerInfoMap(new ArrayList<>(getPlayerRankingInfo().keySet()));

        if (MapUtils.isEmpty(allRoomPlayerInfo)) {
            LogUtil.error("model.arena.entity.ArenaRoomRanking.settleRanking, can not get room playerInfo, roomId:" + getRoomId());
            return false;
        }

        List<String> needRemoveFromRankingPlayer = new ArrayList<>();
        for (RankingQuerySingleResult value : getPlayerRankingInfo().values()) {
            //跳过机器人
            if (ArenaUtil.isRobot(allRoomPlayerInfo.get(value.getPrimaryKey()))) {
                continue;
            }

            //剔除不是本房间的玩家
            if (arenaRoom.getPlayerIdList().contains(value.getPrimaryKey())) {
                builder.putRankingInfo(value.getPrimaryKey(), value.getRanking());
            } else {
                needRemoveFromRankingPlayer.add(value.getPrimaryKey());
            }
        }

        //排行记录大于0结算
        if (builder.getRankingInfoCount() > 0) {
            GlobalData.getInstance().sendMsgToAllServer(MsgIdEnum.CS_GS_ArenaRankingSettle_VALUE, builder);
        }

        //清除不是本房间的玩家排行数据
        if (CollectionUtils.isNotEmpty(needRemoveFromRankingPlayer)) {
            EventUtil.clearRanking(ArenaUtil.getRoomRankingName(getRoomId()), needRemoveFromRankingPlayer);
            LogUtil.debug("model.arena.entity.ArenaRoomRanking.settleRanking, player is not exist in room, remove from room ranking ,id list :"
                    + GameUtil.collectionToString(needRemoveFromRankingPlayer));
        }

        return true;
    }

    /**
     * 查询玩家的个人信息
     *
     * @param playerIdx
     * @return
     */
    public RankingQuerySingleResult queryPlayerRankingResult(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }

        return playerRankingInfo.get(playerIdx);
    }

    /**
     * 查询房间排行数据
     *
     * @param limit
     * @return
     */
    public ArenaRankingInfo queryRanking(int limit) {
        if (limit == 0) {
            return null;
        }

        Builder result = ArenaRankingInfo.newBuilder();
        result.setRoomId(getRoomId());
        result.setDan(getDan());

        for (RankingQuerySingleResult info : getPlayerRankingInfo().values()) {
            if (limit == -1 || info.getRanking() < limit) {
                result.putArenaRankInfo(info.getPrimaryKey(), info.getRanking());
            }
        }
        return result.build();
    }
}
