package model.matchArena;

import cfg.MatchArenaConfig;
import cfg.MatchArenaDanConfig;
import cfg.MatchArenaDanConfigObject;
import common.GameConst;
import common.GlobalThread;
import common.GlobalTick;
import common.SyncExecuteFunction;
import common.TimeUtil;
import helper.StringUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import model.room.cache.RoomCache;
import model.warpServer.WarpServerManager;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Battle.BattleSubTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer;
import protocol.ServerTransfer.ApplyPvpBattleData;
import protocol.ServerTransfer.BS_GS_ReplyPvpBattle;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import protocol.ServerTransfer.ReplyPvpBattleData;
import protocol.ServerTransfer.ServerTypeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2021/05/24
 */
public class MatchArenaRankManager implements Runnable {
    private static MatchArenaRankManager instance;

    public static MatchArenaRankManager getInstance() {
        if (instance == null) {
            synchronized (MatchArenaRankManager.class) {
                if (instance == null) {
                    instance = new MatchArenaRankManager();
                }
            }
        }
        return instance;
    }

    private MatchArenaRankManager() {
    }

    /**
     * tick sleep time
     */
    public static final Long SLEEP_TIME = 500L;

    public static final String ROBOT_MARK = "robot";

    private final Map<String, ArenaRankPlayer> playerMap = new ConcurrentHashMap<>();

    /**
     * 根据玩家分值排序的list
     */
    private final List<String> matchList = new LinkedList<>();

    private final Comparator<String> comparator = Comparator.comparing(ele -> MatchArenaRankManager.getInstance().getPlayerScore(ele));

    private final AtomicBoolean run = new AtomicBoolean(true);

    public boolean init() {
        GlobalThread.getInstance().submit(this);
        return true;
    }

    public boolean addMatchPlayer(ArenaRankPlayer player) {
        if (player == null || player.getPlayerIdx() == null) {
            return false;
        }

        if (playerIsInMatch(player.getPlayerIdx())) {
            return true;
        }

        SyncExecuteFunction.executeConsumer(player, e -> player.resetMatchTime());

        this.playerMap.put(player.getPlayerIdx(), player);
        return addMatchPlayerIdx(player.getPlayerIdx());
    }

    public boolean playerIsInMatch(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return false;
        }
        return this.matchList.contains(playerIdx);
    }

    public synchronized boolean addMatchPlayerIdx(String playerIdx) {
        if (playerIdx == null) {
            return false;
        }

        if (this.matchList.isEmpty()) {
            this.matchList.add(playerIdx);
            return true;
        }

        if (this.comparator.compare(playerIdx, this.matchList.get(0)) <= 0) {
            this.matchList.add(0, playerIdx);
            return true;
        }

        if (this.comparator.compare(playerIdx, this.matchList.get(this.matchList.size() - 1)) >= 0) {
            this.matchList.add(playerIdx);
            return true;
        }

        int insertIndex = binarySearchFindInsertIndex(playerIdx);
        if (insertIndex != -1) {
            this.matchList.add(insertIndex, playerIdx);
        } else {
            //未找到插入时的保底处理
            this.matchList.add(playerIdx);
            this.matchList.sort(this.comparator);
            LogUtil.warn("MatchArenaRankManager.addMatchPlayerIdx, find insert index failed,");
        }
        return true;
    }

    private int binarySearchFindInsertIndex(String playerIdx) {
        int startIndex = 0;
        int endIndex = this.matchList.size();
        for (int i = 0; i < this.matchList.size(); i++) {
            int middleIndex = (startIndex + endIndex) / 2;
            int compareResult = this.comparator.compare(playerIdx, this.matchList.get(middleIndex));
            if (compareResult == 0) {
                return middleIndex;
            } else if (compareResult < 0) {
                endIndex = middleIndex;
            } else {
                startIndex = middleIndex;
            }

            if (startIndex - endIndex >= -1) {
                return endIndex;
            }
        }
        LogUtil.error("model.matchArena.SortedLinkedList.binarySearchFindInsertIndex, can not find insert index, ele:" + playerIdx);
        return -1;
    }

    @Override
    public void run() {
        LogUtil.info("model.matchArena.MatchArenaRankManager.run, curTime = " + GlobalTick.getInstance().getCurrentTime());
        Thread.currentThread().setName("model.matchArena.MatchArenaRankManager.run");
        while (run.get()) {
            try {
                onTick();
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            } finally {
                GameUtil.sleep(SLEEP_TIME);
            }
        }
        LogUtil.warn("model.matchArena.MatchArenaRankManager.run is over");
    }

    public void close() {
        this.run.set(false);
    }

    private void onTick() {
        List<DefaultKeyValue<String, String>> matchResult = matchPlayer();
        if (matchResult.isEmpty()) {
            return;
        }

        for (DefaultKeyValue<String, String> keyValue : matchResult) {
            if (Objects.equals(keyValue.getValue(), ROBOT_MARK)) {
                if (enterPve(keyValue.getKey())) {
                    matchRobotSuccess(keyValue.getKey());
                }

            } else {
                if (enterPvpBattle(keyValue.getKey(), keyValue.getValue())) {
                    matchPlayerSuccess(keyValue.getKey(), keyValue.getValue());
                    matchPlayerSuccess(keyValue.getValue(), keyValue.getKey());
                }
            }
        }
    }

    private void matchRobotSuccess(String playerIdx) {
        removeMatchPlayer(playerIdx);

        ArenaRankPlayer player = getPlayer(playerIdx);
        if (player != null) {
            SyncExecuteFunction.executeConsumer(player, e -> {
                player.successMatchRobot();
            });
            player.updateMatchInfo();
        }
        LogUtil.info("model.matchArena.MatchArenaRankManager.matchRobotSuccess, playerIdx:" + playerIdx);
    }

    private void matchPlayerSuccess(String playerIdx, String opponentIdx) {
        ArenaRankPlayer player = getPlayer(playerIdx);
        if (player != null) {
            SyncExecuteFunction.executeConsumer(player, e -> {
                player.successMatchPlayer(opponentIdx);
            });
            player.updateMatchInfo();
        }

        removeMatchPlayer(playerIdx);
        LogUtil.info("model.matchArena.MatchArenaRankManager.matchRobotSuccess, playerIdx:" + playerIdx
                + ", opponent idx:" + opponentIdx);
    }

    private synchronized void removeMatchPlayer(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        this.matchList.remove(playerIdx);
        this.playerMap.remove(playerIdx);
    }

    private synchronized List<DefaultKeyValue<String, String>> matchPlayer() {
        List<DefaultKeyValue<String, String>> result = new ArrayList<>();

        //用于标识已经匹配成功的下标
        Set<Integer> matchedSuccess = new HashSet<>();
        for (int i = 0; i < this.matchList.size(); i++) {
            if (matchedSuccess.contains(i)) {
                continue;
            }

            ArenaRankPlayer matchPlayer = getPlayer(this.matchList.get(i));
            if (matchPlayer == null) {
                continue;
            }

            //是否触发连败保护 给玩家匹配机器人
            if (triggerLosingStreakProtect(matchPlayer)) {
                matchRobot(result, matchedSuccess, i, matchPlayer);
                continue;
            }

            boolean matchPlayerSuccess = false;
            for (int j = i + 1; j < this.matchList.size(); j++) {
                if (matchedSuccess.contains(j)) {
                    continue;
                }

                ArenaRankPlayer targetPlayer = getPlayer(this.matchList.get(i + 1));
                if (targetPlayer == null) {
                    continue;
                }

                //已经大于最大的可匹配分数
                int maxScoreDiff = MatchArenaUtil.getMaxPlayerScoreDiff(matchPlayer);
                if (targetPlayer.getScore() > matchPlayer.getScore() + maxScoreDiff) {
//                    LogUtil.debug("MatchArenaRankManager.matchPlayer, match score:" + matchPlayer.getScore()
//                            + ", target score:" + targetPlayer.getScore() + ", score diff :" + maxScoreDiff + ", break");
                    break;
                }

                if (!matchPlayer.matchPlayer(targetPlayer) || !targetPlayer.matchPlayer(matchPlayer)) {
                    continue;
                }

                if (matchPlayer.matchScore(targetPlayer) || targetPlayer.matchScore(matchPlayer)) {
                    result.add(new DefaultKeyValue<>(matchPlayer.getPlayerIdx(), targetPlayer.getPlayerIdx()));

                    matchedSuccess.add(i);
                    matchedSuccess.add(j);

                    matchPlayerSuccess = true;
                    LogUtil.info("model.matchArena.MatchArenaRankManager.matchPlayer, match success, playerIdx:"
                            + matchPlayer.getPlayerIdx() + ", opponentIdx:" + targetPlayer.getPlayerIdx());
                    break;
                }
            }

            if (!matchPlayerSuccess && matchPlayer.canMatchRobot()) {
                matchRobot(result, matchedSuccess, i, matchPlayer);
            }
        }
        return result;
    }

    private long randomCanEnterPveTime() {
        return GlobalTick.getInstance().getCurrentTime() + (long) (5 + RandomUtils.nextInt(10)) * TimeUtil.MS_IN_A_MIN;
    }

    /**
     * 是否触发连败保护
     *
     * @param matchPlayer
     * @return
     */
    private boolean triggerLosingStreakProtect(ArenaRankPlayer matchPlayer) {
        int dan = matchPlayer.getDan();
        MatchArenaDanConfigObject cfg = MatchArenaDanConfig.getById(dan);
        if (cfg == null) {
            return false;
        }
        return matchPlayer.getLosingStreak() >= cfg.getMatchaifailtimes();
    }

    private void matchRobot(List<DefaultKeyValue<String, String>> result, Set<Integer> matchedSuccess,
                            int i, ArenaRankPlayer matchPlayer) {
        result.add(new DefaultKeyValue<>(matchPlayer.getPlayerIdx(), ROBOT_MARK));

        matchedSuccess.add(i);

        LogUtil.info("model.matchArena.MatchArenaRankManager.matchPlayer, match robot success, playerIdx:"
                + matchPlayer.getPlayerIdx());
    }

    public int getPlayerScore(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return 0;
        }
        ArenaRankPlayer player = this.playerMap.get(playerIdx);
        return player == null ? 0 : player.getScore();
    }


    private boolean enterPve(String playerIdx) {
        ArenaRankPlayer player = this.playerMap.get(playerIdx);
        if (player == null) {
            return false;
        }

        ServerTransfer.BS_GS_MatchArenaEnterRankPveBattle.Builder pveEnterBuilder = ServerTransfer.BS_GS_MatchArenaEnterRankPveBattle.newBuilder();
        pveEnterBuilder.setPlayerIdx(playerIdx);

        return WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, player.getFromSvrIndex(),
                MsgIdEnum.BS_GS_MatchArenaEnterRankPveBattle_VALUE, pveEnterBuilder);
    }

    private boolean enterPvpBattle(String firstPlayerIdx, String secondPlayerIdx) {
        if (StringUtils.isEmpty(firstPlayerIdx) || StringUtils.isEmpty(secondPlayerIdx)) {
            return false;
        }
        ArenaRankPlayer firstPlayer = this.playerMap.get(firstPlayerIdx);
        ArenaRankPlayer secondPlayer = this.playerMap.get(secondPlayerIdx);
        if (firstPlayer == null || secondPlayer == null) {
            LogUtil.error("model.matchArena.MatchArenaRankManager.enterPvpBattle, firstPlayer:"
                    + firstPlayerIdx + "or , secondPlayer:" + secondPlayerIdx + ", is null");
            return false;
        }

        int battlePetLimit = MatchArenaUtil.getBattlePetLimit(firstPlayer.getScore(), secondPlayer.getScore());
        PvpBattlePlayerInfo firstPvpInfo = firstPlayer.buildPvpPlayerInfo(1, battlePetLimit);
        PvpBattlePlayerInfo secondPvpInfo = secondPlayer.buildPvpPlayerInfo(2, battlePetLimit);
        if (firstPvpInfo == null || secondPvpInfo == null) {
            LogUtil.error("model.matchArena.MatchArenaRankManager.enterPvpBattle, firstPlayer:"
                    + firstPlayerIdx + "or , secondPlayer:" + secondPlayerIdx + ", build pvp info failed");
            return false;
        }

        ApplyPvpBattleData.Builder applyPvpBuilder = ApplyPvpBattleData.newBuilder();
        applyPvpBuilder.setFightMakeId(MatchArenaConfig.getById(GameConst.ConfgId).getPvpfightmakeid());
        applyPvpBuilder.setSubBattleType(BattleSubTypeEnum.BSTE_MatchArena);
        applyPvpBuilder.addPlayerInfo(firstPvpInfo);
        applyPvpBuilder.addPlayerInfo(secondPvpInfo);

        ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(applyPvpBuilder.build(),
                ServerTypeEnum.STE_GameServer, firstPlayer.getFromSvrIndex());

        BS_GS_ReplyPvpBattle.Builder builder = BS_GS_ReplyPvpBattle.newBuilder();
        builder.setReplyPvpBattleData(replyBuilder);

        WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, firstPlayer.getFromSvrIndex(),
                MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);

        if (firstPlayer.getFromSvrIndex() != secondPlayer.getFromSvrIndex()) {
            WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, secondPlayer.getFromSvrIndex(),
                    MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
        }
        return true;
    }

    public ArenaRankPlayer getPlayer(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        return this.playerMap.get(playerIdx);
    }

    public synchronized RetCodeEnum cancelMatch(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (!this.matchList.contains(playerIdx)) {
            return RetCodeEnum.RCE_MatchArena_NotInMatchStatus;
        }

        return this.matchList.remove(playerIdx) ? RetCodeEnum.RCE_Success : RetCodeEnum.RCE_UnknownError;
    }
}
