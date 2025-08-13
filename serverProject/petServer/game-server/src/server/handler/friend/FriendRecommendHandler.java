package server.handler.friend;

import cfg.GameConfig;
import cfg.GameConfigObject;
import cfg.PlayerLevelConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.FriendUtil;
import model.player.util.PlayerUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.Friend.CS_FriendRecommend;
import protocol.Friend.FriendBaseInfo;
import protocol.Friend.SC_FriendRecommend;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_OwnedFriendInfo;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.CS_FriendRecommend_VALUE)
public class FriendRecommendHandler extends AbstractBaseHandler<CS_FriendRecommend> {
    @Override
    protected CS_FriendRecommend parse(byte[] bytes) throws Exception {
        return CS_FriendRecommend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_FriendRecommend req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_FriendRecommend.Builder resultBuilder = SC_FriendRecommend.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("FriendRecommendHandler, playerIdx[" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_FriendRecommend_VALUE, resultBuilder);
            return;
        }

        //随机推荐的玩家Idx
        Collection<String> recommendPlayerIdx = randomRecommend(player);
        if (CollectionUtils.isEmpty(recommendPlayerIdx)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_FriendRecommend_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            Builder db_data = player.getDb_data();
            if (db_data == null) {
                LogUtil.error("FriendRecommendHandler, playerIdx[" + playerIdx + "] dbData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_FriendRecommend_VALUE, resultBuilder);
                return;
            }

            Map<String, DB_OwnedFriendInfo> ownedMap = db_data.getFriendInfo().getOwnedMap();
            for (String idx : recommendPlayerIdx) {
                if (ownedMap.containsKey(idx) || playerIdx.equals(idx)) {
                    continue;
                }

                FriendBaseInfo.Builder builder = FriendUtil.builderFriendBaseInfo(playerCache.getByIdx(idx), 0);
                if (builder != null) {
                    resultBuilder.addRecommand(builder);
                }
            }

            //设置上次推荐的id
            db_data.getFriendInfoBuilder().clearLastRecommendIdx();
            db_data.getFriendInfoBuilder().addAllLastRecommendIdx(recommendPlayerIdx);

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_FriendRecommend_VALUE, resultBuilder);
        });
    }

    /**
     * 随机好友推荐
     *
     * @param player entity
     * @return
     */
    private Collection<String> randomRecommend(playerEntity player) {
        if (player == null) {
            return null;
        }

        Set<String> alreadyFind = new HashSet<>();
        alreadyFind.add(player.getIdx());
        alreadyFind.addAll(player.getDb_data().getFriendInfo().getOwnedMap().keySet());
        alreadyFind.addAll(player.getDb_data().getFriendInfo().getLastRecommendIdxList());

        int playerLv = player.getLevel();
        int recommendCount = getRecommendCount();

        //先随机在线玩家
        Collection<String> onlineResult = randomOnlinePlayer(alreadyFind, recommendCount, playerLv);
        if (CollectionUtils.size(onlineResult) >= recommendCount) {
            return onlineResult;
        }
        List<String> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(onlineResult)) {
            result.addAll(onlineResult);

            alreadyFind.addAll(onlineResult);
        }

        Collection<String> offlineResult =
                randomOfflinePlayer(alreadyFind, recommendCount - result.size(), playerLv);
        if (CollectionUtils.isNotEmpty(offlineResult)) {
            result.addAll(offlineResult);
        }

        return result;
    }

    private Collection<String> randomOfflinePlayer(Set<String> alreadyFind, int needCount, int playerLv) {
        //筛选出离线玩家
        List<String> allPlayerIdx = new ArrayList<>(playerCache.getInstance().getAllPlayerIdx());
        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
        //移除所有在线玩家
        allPlayerIdx.removeAll(allOnlinePlayerIdx);


        //先随机离线三天内的玩家
    /*    List<String> logOutLessThanThreeDays = new ArrayList<>();
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        for (String idx : allPlayerIdx) {
            if (currentTime - PlayerUtil.queryPlayerLastLogOutTime(idx) <= TimeUtil.MS_IN_A_MIN * 3) {
                logOutLessThanThreeDays.add(idx);
            }
        }
        Collection<String> lessThanThreeDaysFindResult = random(logOutLessThanThreeDays, alreadyFind, needCount, playerLv);
        if (CollectionUtils.size(lessThanThreeDaysFindResult) >= needCount) {
            return lessThanThreeDaysFindResult;
        }

        List<String> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(lessThanThreeDaysFindResult)) {
            result.addAll(lessThanThreeDaysFindResult);
        }

        //在随机离线超过三天的玩家
        //删除上次离线在三天内的玩家
        allPlayerIdx.removeAll(logOutLessThanThreeDays);

        Collection<String> moreThanThreeDaysFindResult = random(allPlayerIdx, alreadyFind, needCount, playerLv);
        if (CollectionUtils.isNotEmpty(moreThanThreeDaysFindResult)) {
            result.addAll(moreThanThreeDaysFindResult);
        }
*/
        return getCanRecommendPlayer(allPlayerIdx);
    }

    private List<String> getCanRecommendPlayer(List<String> allPlayerIdx) {
        if (CollectionUtils.isEmpty(allPlayerIdx)){
            return Collections.emptyList();
        }

        List<String> recommends = new ArrayList<>();
        playerEntity player;
        for (String playerIdx : allPlayerIdx) {
            player = playerCache.getByIdx(playerIdx);
            if (player == null) {
                continue;
            }
            if (player.canRecommend()) {
                recommends.add(playerIdx);
            }
        }
        return recommends;
    }

    private Collection<String> randomOnlinePlayer(Set<String> alreadyFind, int needCount, int playerLv) {
        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
        return random(allOnlinePlayerIdx, alreadyFind, needCount, playerLv);
    }

    /**
     * 根据给定列表随机玩家
     *
     * @param targetList
     * @param alreadyFind
     * @param needCount
     * @return
     */
    private Collection<String> random(Collection<String> targetList, Set<String> alreadyFind, int needCount, int playerLv) {
        if (CollectionUtils.isEmpty(targetList) || needCount <= 0) {
            return null;
        }

        List<String> findList = new ArrayList<>(targetList);
        findList.removeAll(alreadyFind);

        Set<String> result = new HashSet<>();

        LevelScope scope = new LevelScope(playerLv);
        for (int i = 0; i < scope.getMaxCycleTimes(playerLv); i++) {
            for (String idx : findList) {
                if (scope.inScope(PlayerUtil.queryPlayerLv(idx))) {
                    result.add(idx);
                }
            }

            scope.generateNextScope();
            findList.removeAll(result);
        }

        return GameUtil.randomGet(result, needCount);
    }

    private int getRecommendCount() {
        GameConfigObject gameCfg = GameConfig.getById(GameConst.CONFIG_ID);
        if (gameCfg == null) {
            LogUtil.error("FriendRecommendHandler, gameCfg is null");
            return 0;
        }
        return gameCfg.getEachrecommandcount();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Friend;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_FriendRecommend_VALUE, SC_FriendRecommend.newBuilder().setRetCode(retCode));
    }
}

class LevelScope {
    /**
     * 等级增长
     */
    public static final int LEVEL_GROWTH = 5;

    /**
     * |________|         |________|
     * min     minMin   minMax    maxMin   maxMax    max
     */
    private int maxMax;
    private int maxMin;
    private int minMax;
    private int minMin;

    private int getMinLv() {
        GameConfigObject gameCfg = GameConfig.getById(GameConst.CONFIG_ID);
        if (gameCfg == null) {
            LogUtil.error("FriendRecommendHandler, gameCfg is null");
            return 0;
        }
        return gameCfg.getDefaultlv();
    }

    private int getMaxLv() {
        return PlayerLevelConfig.maxLevel;
    }

    public LevelScope(int playerLv) {
        this.maxMax = Math.min(playerLv + LEVEL_GROWTH, getMaxLv());
        this.maxMin = playerLv;
        this.minMax = playerLv;
        this.minMin = Math.max(playerLv - LEVEL_GROWTH, getMinLv());
    }

    public boolean inScope(int lv) {
        return GameUtil.inScope(this.maxMax, this.maxMin, lv)
                || GameUtil.inScope(this.minMax, this.minMin, lv);
    }

    public void generateNextScope() {
        this.maxMin = this.maxMax;
        this.maxMax = Math.min(this.maxMax + LEVEL_GROWTH, getMaxLv());

        this.minMax = this.minMin;
        this.minMin = Math.max(this.minMin - LEVEL_GROWTH, getMinLv());
    }

    /**
     * 返回最大可以循环次数
     *
     * @return
     */
    public int getMaxCycleTimes(int playerLv) {
        return Math.max(getMaxLv() - playerLv, playerLv - getMinLv()) / LEVEL_GROWTH;
    }
}
