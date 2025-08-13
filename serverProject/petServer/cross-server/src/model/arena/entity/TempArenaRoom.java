package model.arena.entity;

import cfg.ArenaDan;
import cfg.ArenaDanObject;
import cfg.ArenaRobotConfigObject;
import common.entity.RankingQuerySingleResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import model.arena.ArenaManager;
import model.arena.ArenaPlayerManager;
import model.arena.ArenaRobotManager;
import model.arena.util.ArenaUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.ArenaDB.DB_ArenaRoom;
import protocol.ArenaDB.DB_ArenaRoom.Builder;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

/**
 * 临时房间,用于数据操作
 *
 * @author huhan
 * @date 2020/06/17
 */
@Getter
@Setter
public class TempArenaRoom {
    private String roomId;
    private int dan;
    /**
     * 机器人当前配置数量信息
     */
    private Map<Integer, Integer> robotCfgIdCountMap = new HashMap<>();
    /**
     * 玩家集合
     */
    private Set<String> playerIdxSet;

    private ArenaRoomRanking roomRanking;

    private DB_ArenaRoom beforeRoomInfo;

    private TempArenaRoom() {
    }

    public List<String> getPlayerIdsList() {
        if (CollectionUtils.isEmpty(playerIdxSet)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(playerIdxSet);
    }

    public static TempArenaRoom create(DB_ArenaRoom arenaRoom, ArenaRoomRanking roomRanking) {
        if (arenaRoom == null || roomRanking == null) {
            LogUtil.error("TempArenaRoom.create, params error, arena room:" + arenaRoom + ", room ranking:" + roomRanking);
            return null;
        }

        TempArenaRoom room = new TempArenaRoom();
        room.setRoomId(arenaRoom.getRoomId());
        room.setDan(arenaRoom.getDan());
        room.setPlayerIdxSet(new HashSet<>(arenaRoom.getPlayerIdList()));
        room.setRobotCfgIdCountMap(new HashMap<>(arenaRoom.getRobotCfgIdCountMapMap()));
        room.setRoomRanking(roomRanking);
        room.setBeforeRoomInfo(arenaRoom);
        return room;
    }

    /**
     * 为玩家刷新对手
     *
     * @param playerIdx
     * @param mustRobot
     */
    public synchronized List<ArenaOpponentTotalInfo> randomOpponent(List<OpponentRange> rangeList, String playerIdx, boolean mustRobot) {
        if (StringUtils.isEmpty(playerIdx) || rangeList.isEmpty()) {
            LogUtil.error("ArenaRoom.randomOpponent, arenaPlayer is empty, playerId:" + playerIdx
                    + ", or range is empty, rangeSize:" + CollectionUtils.size(rangeList) + ", room id=" + getRoomId());
            return null;
        }

        long start = Instant.now().toEpochMilli();
        List<ArenaOpponentTotalInfo> result = new ArrayList<>();
        Set<String> alreadyFind = new HashSet<>();
        alreadyFind.add(playerIdx);

        for (OpponentRange range : rangeList) {
            long startRangeTime = Instant.now().toEpochMilli();

            Set<String> findResult = null;
            if (range.getRangeType() == 1) {
                findResult = findOpponent(e -> GameUtil.inScope(range.getParamStart(), range.getParamEnd(), roomRanking.queryPlayerRanking(e)),
                        range.getNeedCount(), alreadyFind, mustRobot);

            } else if (range.getRangeType() == 2) {
                int playerScore = ArenaUtil.queryPlayerScore(playerIdx);
                int startScore = ((100 + range.getParamStart()) * playerScore) / 100;
                int endScore = ((100 + range.getParamEnd()) * playerScore) / 100;

                findResult = findOpponent(e -> GameUtil.inScope(startScore, endScore, queryPlayerRankingScore(e)),
                        range.getNeedCount(), alreadyFind, mustRobot);

                //未找到先看能否根据积分区间创建机器人
                if (GameUtil.collectionIsEmpty(findResult)) {
                    findResult = createNewRobotByScoreScope(playerIdx, range.getNeedCount(), startScore, endScore);
                }
            }

            if (GameUtil.collectionIsEmpty(findResult)) {
                findResult = randomGetPlayer(range.getNeedCount(), alreadyFind);
            }

            if (CollectionUtils.isEmpty(findResult)) {
                LogUtil.warn("ArenaRoom.randomOpponent, random failed, range:" + range.toString()
                        + ",roomSize=" + CollectionUtils.size(playerIdxSet) + ", add random player");
                continue;
            }

            alreadyFind.addAll(findResult);
            List<ArenaOpponentTotalInfo> opponents = buildOpponentTotalInfo(findResult, range.isDirectUp());
            if (!GameUtil.collectionIsEmpty(opponents)) {
                result.addAll(opponents);
            }

            LogUtil.debug("TempArenaRoom.randomOpponent, random once use time:" + (Instant.now().toEpochMilli() - startRangeTime));
        }
        LogUtil.debug("TempArenaRoom.randomOpponent, use time:" + (Instant.now().toEpochMilli() - start));

        //更新房间信息
        updateRoomInfoToRedis();
        return result;
    }

    /**
     * 更新房间数据到redis
     */
    public void updateRoomInfoToRedis() {
        DB_ArenaRoom beforeRoomInfo = getBeforeRoomInfo();
        if (beforeRoomInfo == null) {
            LogUtil.error("TempArenaRoom.updateRoomInfoToRedis, before room info is null, roomId:" + getRoomId());
            return;
        }

        Builder builder = beforeRoomInfo.toBuilder();
        builder.clearPlayerId();
        builder.addAllPlayerId(getPlayerIdxSet());

        builder.clearRobotCfgIdCountMap();
        builder.putAllRobotCfgIdCountMap(getRobotCfgIdCountMap());

        ArenaManager.getInstance().updateRoomInfo(builder.build());
    }

    private Set<String> createNewRobotByScoreScope(String playerIdx, int needCount, int scoreRange_1, int scoreRange_2) {
        if (StringUtils.isBlank(playerIdx) || needCount <= 0) {
            return null;
        }

        if (getRoomRobotCount() >= ArenaUtil.getDanMaxRobotSize(getDan())) {
            LogUtil.warn("robot size >= max size, can not create robot, dan:" + getDan() + ", robotCount:" + getRoomRobotCount());
            return null;
        }
        //当前机器人随机分数,用于查找机器人配置
        int robotScore = ArenaUtil.randomInScope(scoreRange_1, scoreRange_2);
        //将这个随机分数固定到当前段位机器人的分数区间
        robotScore = Math.min(Math.max(robotScore, ArenaUtil.getDanRobotMinScore(dan)), ArenaUtil.getDanRobotMaxScore(dan));

        ArenaRobotConfigObject robotCfg = ArenaUtil.getRobotCfgByDanAndScore(getDan(), robotScore);
        if (canNotCreateRobot(robotCfg, needCount)) {
            return null;
        }

        List<ArenaTotalInfo> robots = ArenaRobotManager.getInstance().createRobot(getRoomId(), robotCfg, needCount);
        if (CollectionUtils.isEmpty(robots)) {
            return null;
        }

        List<DB_ArenaPlayerInfo> successAdd = addRobot(robots);
        if (CollectionUtils.isEmpty(successAdd)) {
            return null;
        }
        return successAdd.stream().map(e -> e.getBaseInfo().getPlayerIdx()).collect(Collectors.toSet());
    }

    /**
     * 添加机器人， 返回添加成功的机器人数据
     *
     * @param robots
     * @return
     */
    private List<DB_ArenaPlayerInfo> addRobot(List<ArenaTotalInfo> robots) {
        if (CollectionUtils.isEmpty(robots)) {
            return null;
        }

        //先更新到redis数据,新机器人不需要加锁
        List<DB_ArenaPlayerInfo> successUpdate = ArenaPlayerManager.getInstance().updateAllPlayerToRedis(robots);
        if (CollectionUtils.isEmpty(successUpdate)) {
            return null;
        }
        for (DB_ArenaPlayerInfo baseInfo : successUpdate) {

            //更新机器人排行
            roomRanking.updatePlayerRanking(baseInfo);

            playerIdxSet.add(baseInfo.getBaseInfo().getPlayerIdx());

            //更新房间机器人数据
            int robotCfgId = baseInfo.getRobotCfgId();
            Integer oldNum = robotCfgIdCountMap.get(robotCfgId);
            robotCfgIdCountMap.put(robotCfgId, oldNum == null ? 1 : oldNum + 1);
        }
        return successUpdate;
    }

    private Set<String> findOpponent(Predicate<String> predicate, int needCount, Set<String> notRepeat, boolean mustRobot) {
        if (predicate == null || needCount <= 0 || this.playerIdxSet.isEmpty()) {
            LogUtil.error("ArenaRoom.findByRankingLimit, error params, room id:" + getRoomId()
                    + ", predicate is null:" + (predicate == null) + ", needCount=" + needCount + ", player Set Size:" + CollectionUtils.size(playerIdxSet));
            return null;
        }

        List<String> findList = this.playerIdxSet.stream()
                .filter(predicate)
                .filter(e -> notRepeat == null || !notRepeat.contains(e))
                .filter(e -> !mustRobot || ArenaPlayerManager.getInstance().isRobot(e))
                .collect(Collectors.toList());

        Set<String> result = new HashSet<>();
        if (findList.size() <= needCount) {
            result.addAll(findList);
        } else {
            Random random = new Random();
            for (int i = 0; i < needCount; i++) {
                String newFind = findList.get(random.nextInt(findList.size()));
                result.add(newFind);
                findList.remove(newFind);
            }
        }
        return result;
    }

    public int getRoomRobotCount() {
        int totalCount = 0;
        for (Integer value : robotCfgIdCountMap.values()) {
            totalCount += value;
        }
        return totalCount;
    }

    /**
     * 判断当前分段是否还可以添加新的机器人
     *
     * @param robotCfg
     * @param needCount
     * @return
     */
    private boolean canCreateRobot(ArenaRobotConfigObject robotCfg, int needCount) {
        if (robotCfg == null || needCount <= 0) {
            return false;
        }

        if (this.robotCfgIdCountMap.containsKey(robotCfg.getId())) {
            Integer curCount = this.robotCfgIdCountMap.get(robotCfg.getId());
            return (curCount + needCount) <= robotCfg.getNeedcount();
        }
        return true;
    }

    private boolean canNotCreateRobot(ArenaRobotConfigObject robotCfg, int needCount) {
        return !canCreateRobot(robotCfg, needCount);
    }

    /**
     * 随机获得指定数量玩家
     *
     * @param needCount
     * @param notRepeat
     * @return
     */
    private Set<String> randomGetPlayer(int needCount, Set<String> notRepeat) {
        ArrayList<String> list = new ArrayList<>(this.playerIdxSet);
        if (CollectionUtils.isNotEmpty(notRepeat)) {
            list.removeAll(notRepeat);
        }

        Set<String> result = new HashSet<>();
        if (CollectionUtils.isEmpty(list)) {
            LogUtil.info("room id:" + getRoomId() + " player remain size is null");
            return result;
        }

        Random random = new Random();
        for (int i = 0; i < needCount; i++) {
            String newIdx = list.get(random.nextInt(list.size()));
            result.add(newIdx);
            list.remove(newIdx);
        }
        return result;
    }

    public List<ArenaOpponentTotalInfo> buildOpponentTotalInfo(Collection<String> opponentIdxList, boolean direct) {
        if (CollectionUtils.isEmpty(opponentIdxList)) {
            LogUtil.error("ArenaRoom.buildOpponentTotalInfo, param is empty");
            return null;
        }
        List<ArenaOpponentTotalInfo> result = new ArrayList<>();

        for (String idx : opponentIdxList) {
            ArenaOpponentTotalInfo totalInfo
                    = ArenaPlayerManager.getInstance().buildArenaOpponentTotalInfo(idx, direct, ArenaUtil.getDanUseDefinedTeams(getDan()));
            if (totalInfo != null) {
                result.add(totalInfo);
            }
        }
        return result;
    }

    public boolean settleDan() {
        ArenaDanObject danCfg = ArenaDan.getById(getDan());
        if (danCfg == null || !danCfg.getNeedsettledan()) {
            LogUtil.error("ArenaRoom.setDan, dan cfg is null or not need settle, dan:" + getDan() + ",room id=" + getRoomId());
            return true;
        }

        //查询房间内所有玩家信息, 提高处理速度
        Map<String, DB_ArenaPlayerInfo> playerInfoMap = ArenaPlayerManager.getInstance().getPlayerInfoMap(beforeRoomInfo.getPlayerIdList());
        if (MapUtils.isEmpty(playerInfoMap)) {
            LogUtil.error("TempArenaRoom.settleDan, query room player failed, roomId:" + getRoomId());
            return false;
        }

        List<String> danUpPlayerIdx = new ArrayList<>();

        for (RankingQuerySingleResult result : roomRanking.getPlayerRankingInfo().values()) {
            //只结算玩家 不结算机器人
            if (ArenaUtil.isRobot(playerInfoMap.get(result.getPrimaryKey()))) {
                continue;
            }

            if (result.getRanking() <= danCfg.getUpgraderanking()
                    && result.getIntPrimaryScore() >= danCfg.getUpgradescore()) {
                danUpPlayerIdx.add(result.getPrimaryKey());
            }
        }

        //玩家段位晋升
        if (!danUpPlayerIdx.isEmpty()) {
            ArenaManager.getInstance().submitPlayerDanUp(danUpPlayerIdx, danCfg.getNextdan(), false);

            LogUtil.info("TempArenaRoom.settleDan, roomId:" + getRoomId() + "dan up, next dan:" + danCfg.getNextdan()
                    + " playerIdx:" + GameUtil.collectionToString(danUpPlayerIdx));

            //将玩家移除房间和移除排行榜
            removePlayer(danUpPlayerIdx);

            //更新信息到redis
            updateRoomInfoToRedis();
        }
        return true;
    }

    /**
     * 将玩家从当前房间移除且从排行榜移除该玩家
     *
     * @param playerIdxList
     */
    public void removePlayer(List<String> playerIdxList) {
        if (CollectionUtils.isEmpty(playerIdxList)) {
            return;
        }

        this.playerIdxSet.removeAll(playerIdxList);
        EventUtil.clearRanking(ArenaUtil.getRoomRankingName(getRoomId()), playerIdxList);
        LogUtil.debug("model.arena.entity.TempArenaRoom.removePlayer, remove player:"
                + GameUtil.collectionToString(playerIdxList) + " from room:" + getRoomId());
    }

    private int queryPlayerRankingScore(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return 0;
        }
        RankingQuerySingleResult singleResult = roomRanking.queryPlayerRankingResult(playerIdx);
        return singleResult == null ? 0 : singleResult.getIntPrimaryScore();
    }

    /**
     * 检查房间信息是否引用了信息不匹配的玩家id，例如玩家当前玩家引用的房间号与房间引用不匹配,或者房间段位不匹配
     *
     * @return 已经从房间移除的玩家Id
     */
    public List<String> checkRoomInfo() {
        Map<String, DB_ArenaPlayerInfo> totalPlayerInfo = ArenaPlayerManager.getInstance().getPlayerInfoMap(new ArrayList<>(this.playerIdxSet));
        if (MapUtils.isEmpty(totalPlayerInfo)) {
            LogUtil.error("model.arena.entity.TempArenaRoom.checkRoomInfo, can not get room playerInfo, roomId:" + getRoomId());
            return null;
        }

        List<String> needRemoveFromRoom = new ArrayList<>();

        for (String idx : this.playerIdxSet) {
            DB_ArenaPlayerInfo playerInfo = totalPlayerInfo.get(idx);
            if (ArenaUtil.isRobot(totalPlayerInfo.get(idx))) {
                continue;
            }

            //房间Id不一致,或者房间段位不匹配需要移除
            if (!Objects.equals(getRoomId(), playerInfo.getRoomId()) || getDan() != playerInfo.getDan()) {
                LogUtil.info("model.arena.entity.TempArenaRoom.checkRoomInfo, player " + idx
                        + ",roomId:" + playerInfo.getRoomId() + ", is not match roomId:" + getRoomId()
                        + ",or player dan:" + playerInfo.getDan() + ", is not match room dan:" + getDan()
                        + ", need remove from cur room");
                needRemoveFromRoom.add(idx);
            }
        }

        if (CollectionUtils.isNotEmpty(needRemoveFromRoom)) {
            LogUtil.info("model.arena.entity.TempArenaRoom.checkRoomInfo, remove playerIdx:"
                    + GameUtil.collectionToString(needRemoveFromRoom) + " from room:" + getRoomId());
            removePlayer(needRemoveFromRoom);
        }

        return needRemoveFromRoom;
    }
}
