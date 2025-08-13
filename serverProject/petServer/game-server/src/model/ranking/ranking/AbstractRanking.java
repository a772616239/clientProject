package model.ranking.ranking;

import cfg.RankConfig;
import cfg.RankConfigObject;
import com.alibaba.fastjson.JSONObject;
import common.HttpRequestUtil;
import common.SyncExecuteFunction;
import common.entity.*;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import helper.StringUtils;
import lombok.Getter;
import lombok.Setter;
import model.arena.ArenaManager;
import model.obj.BaseObj;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import model.rank.dbCache.rankCache;
import model.rank.entity.rankEntity;
import model.ranking.EnumRankingSenderType;
import model.ranking.sender.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Activity;
import protocol.Activity.EnumRankingType;
import server.handler.ranking.RankingEntranceDto;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static protocol.Activity.EnumRankingType.*;

/**
 * @author huhan
 * @date 2020/12/17
 */

public abstract class AbstractRanking extends BaseObj {

    /**
     * 清空平台排行榜最大重试次数
     */
    public static final int CLEAR_REMOTE_RANKING_RETRY_TIMES = 5;

    /**
     * 允许的最小更新间隔
     */
    public static final long RANKING_UPDATE_MIN_INTERVAL = TimeUtil.MS_IN_A_MIN * 2;

    /**
     * 允许的最小更新间隔
     */
    public static final long RANKING_DEFUlALT_INTERVAL = TimeUtil.MS_IN_A_MIN;

    /**
     * 排名Map保存的排行数量
     */
    public static final int RANKING_INFO_SIZE = 10;

    /**
     * 默认查询排行榜大小
     */
    public static final int RANKING_QUERY_DEFAULT_SIZE = 1000;

    /**
     * 新开发的排行榜需要处理老数据
     */
    public static final List<EnumRankingType> newDevelopRanking = Arrays.asList(
            ERT_Team1Ability, ERT_NaturePet, ERT_WildPet, ERT_AbyssPet, ERT_HellPet);

    /**
     * 排行榜类型
     */
    @Setter
    @Getter
    private EnumRankingType rankingType;

    /**
     * 排行榜名， 更新排行榜的主键,唯一主键
     */
    @Setter
    @Getter
    private String rankingName;

    /**
     * 是否时跨服排汗
     */
    @Getter
    @Setter
    private boolean crossRanking = false;


    /**
     * 更新排行榜单页大小
     */
    public static final int UPDATE_PLAYER_SCORE_PAGE_SIZE = 500;

    /**
     * 排行榜所有玩家的信息<playerIdx
     * 宠物排行榜保存的是<petIdx
     */
    protected final Map<String, RankingQuerySingleResult> totalRankingInfo = new ConcurrentHashMap<>();

    /**
     * 排行榜玩家信息，按照排名存储 ,默认保存前10
     */
    protected final Map<Integer, RankingQuerySingleResult> rankingInfo = new ConcurrentHashMap<>();

    private List<RankingQuerySingleResult> platformRankingResult;

    @Getter
    @Setter
    private int rankingInfoSize = RANKING_INFO_SIZE;


    /**
     * 停止更新玩家排行榜数据
     */
    @Getter
    private boolean stopUpdatePlayerScore = false;

    /**
     * 排行榜更新间隔 ms
     */
    private long rankingUpdateInterval = RANKING_DEFUlALT_INTERVAL;


    private final Map<String, AbstractRankingMsgSender<?>> senderMap = new ConcurrentHashMap<>();

    @Getter
    private long nextUpdateRankingTime;

    private final List<EnumRankingType> petRankingType = Arrays.asList(ERT_NaturePet, ERT_WildPet, ERT_AbyssPet, ERT_HellPet);


    @Getter
    private RankingEntranceDto clientRankEntranceInfo;

    public long getNextUpdateTime() {
        return nextUpdateTime;
    }

    /**
     * 查询玩家排行榜大小
     */
    @Getter
    private int updateSize = RANKING_QUERY_DEFAULT_SIZE;

    public void setUpdateSize(int newSize) {
        this.updateSize = Math.min(ServerConfig.getInstance().getMaxRankingSize(), newSize);
    }

    public void stopUpdatePlayerScore() {
        this.stopUpdatePlayerScore = true;
    }

    public void startUpdatePlayerScore() {
        this.stopUpdatePlayerScore = false;
    }

    /**
     * 该方法返回的排名是无序的
     *
     * @return
     */
    public List<RankingQuerySingleResult> getRankingTotalInfoList() {
        return new ArrayList<>(this.totalRankingInfo.values());
    }

    public Collection<RankingQuerySingleResult> get1To10RankingInfo(){
       return this.rankingInfo.values();
    }


    public void updateClientRankEntranceInfo() {
        if (rankingInfo.size() <= 0) {
            return;
        }
        Activity.RankingEntrance.Builder result = Activity.RankingEntrance.newBuilder();
        result.setRankType(this.rankingType);
        RankingQuerySingleResult playerRank = rankingInfo.get(1);
        if (playerRank == null) {
            return;
        }

        if (petRankingType(this.rankingType)) {
            updatePetRankingEntrance(playerRank);
        } else {
            updatePlayerRankingEntrance(playerRank);
        }
    }

    private void updatePlayerRankingEntrance(RankingQuerySingleResult playerRank) {
        long primaryScore = getPrimaryScore(playerRank);

        List<Integer> canClaimTargetIds = RankingTargetManager.getInstance().getUnLockTargetIds(rankingType);
        clientRankEntranceInfo = new RankingEntranceDto();
        clientRankEntranceInfo.setCanClaimRankTargetIds(canClaimTargetIds);
        String playerIdx = playerRank.getPrimaryKey();
        playerEntity player = playerCache.getByIdx(playerIdx);
        clientRankEntranceInfo.setRankType(this.getRankingType());
        clientRankEntranceInfo.setRankingScore(getPrimaryScore(primaryScore));
        if (player == null) {
            return;
        }
        setClientRankPlayerInfo(playerIdx, player);


    }

    private long getPrimaryScore(RankingQuerySingleResult playerRank) {
        if (EnumRankingType.ERT_ArenaScoreLocal == this.rankingType) {
            return playerRank.getSubScore();
        }
        return playerRank.getPrimaryScore();
    }

    private long getPrimaryScore(long primaryScore) {
        return primaryScore;
    }

    private void setClientRankPlayerInfo(String playerIdx, playerEntity player) {
        clientRankEntranceInfo.setPlayerName(player.getName());
        clientRankEntranceInfo.setPlayerAvatar(player.getAvatar());
        clientRankEntranceInfo.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
        if (player.getDb_data().getCurAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            clientRankEntranceInfo.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(playerIdx));
        }
        clientRankEntranceInfo.setTitleId(player.getTitleId());
    }

    public JSONObject parseJsonObject(String str) {
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(str);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
        return jsonObject;
    }

    public String parsePlayerIdxFromRankingResult(RankingQuerySingleResult playerRank) {
        if (playerRank == null) {
            return null;
        }
        JSONObject jsonObject = parseJsonObject(playerRank.getExtInfo());
        if (jsonObject == null || !jsonObject.containsKey("playerId")) {
            return null;
        }
        return jsonObject.getString("playerId");
    }

    private synchronized void updatePetRankingEntrance(RankingQuerySingleResult playerRank) {
        long primaryScore = getPrimaryScore(playerRank);
        clientRankEntranceInfo = new RankingEntranceDto();
        clientRankEntranceInfo.setCanClaimRankTargetIds(RankingTargetManager.getInstance().getUnLockTargetIds(rankingType));
        clientRankEntranceInfo.setRankingScore(primaryScore);
        clientRankEntranceInfo.setRankType(this.getRankingType());

        JSONObject jsonObject = parseJsonObject(playerRank.getExtInfo());
        if (jsonObject == null) {
            return;
        }
        if (!jsonObject.containsKey("playerId")) {
            return;
        }
        String playerIdx = jsonObject.getString("playerId");
        if (!jsonObject.containsKey("petBookId")) {
            return;
        }
        int petBookId = jsonObject.getIntValue("petBookId");
        clientRankEntranceInfo.setPetBookId(petBookId);

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        setClientRankPlayerInfo(playerIdx, player);

    }

    private boolean petRankingType(EnumRankingType rankingType) {
        return petRankingType.contains(rankingType);
    }

    public void setRankingUpdateInterval(long newInterval) {
        this.rankingUpdateInterval = Math.max(RANKING_UPDATE_MIN_INTERVAL, newInterval);
    }

    protected RankingQuerySingleResult queryPlayerRankingData(String playerIdx) {
        return this.totalRankingInfo.get(playerIdx);
    }

    public RankingQuerySingleResult queryPlayerRankingResult(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        RankingQuerySingleResult result = queryPlayerRankingData(playerIdx);
        if (result == null) {
            result = new RankingQuerySingleResult();
            result.setPrimaryKey(playerIdx);
            result.setRanking(-1);
            result.addScore(getLocalScore(playerIdx));
        }
        return result;
    }

    public int queryPlayerRanking(String playerIdx) {
        RankingQuerySingleResult result = queryPlayerRankingResult(playerIdx);
        return result == null ? -1 : result.getRanking();
    }

    public int queryPlayerIntScore(String playerIdx) {
        RankingQuerySingleResult result = queryPlayerRankingResult(playerIdx);
        return result == null ? Math.max((int) getLocalScore(playerIdx), 0) : result.getIntPrimaryScore();
    }

    public long queryPlayerScore(String playerIdx) {
        RankingQuerySingleResult result = queryPlayerRankingResult(playerIdx);
        return result == null ? getLocalScore(playerIdx) : getPrimaryScore(result);
    }

    private RankingUpdateRequest rankRequest = createRankRequest();

    private RankingUpdateRequest createRankRequest() {
        return new RankingUpdateRequest(this.rankingName, getRankingServerIndex(), getSortRules());
    }


    public void updatePlayerRankingScore(String playerIdx, long primaryScore, long subScore) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
     //  this.rankRequest.addScore(playerIdx, primaryScore, subScore);
        RankingUpdateRequest request = new RankingUpdateRequest(this.rankingName, getRankingServerIndex(), getSortRules());
        request.addScore(playerIdx, primaryScore, subScore);
       /* if (!HttpRequestUtil.updateRanking(request)) {
            LogUtil.error("AbstractRanking.updatePlayerRankingScore, update ranking failed, rankingName："
                    + rankingName + ", playerIdx:" + playerIdx);
        }*/
        HttpRequestUtil.asyncUpdateRanking(request);
    }

    private  AtomicBoolean fixData = new AtomicBoolean(false);

    private List<RankingQuerySingleResult> sortTotalRanking = Collections.emptyList();

    public List<RankingQuerySingleResult> getSortTotalRanking() {
    /*    if (isNeedSort()) {
            synchronized (this){
                if (isNeedSort()){*/
        List<RankingQuerySingleResult> ranking = new ArrayList<>(queryPlatformRanking());
        ranking.sort(Comparator.comparingInt(RankingQuerySingleResult::getRanking));
/*                }
                alreadySort.set(true);
            }
        }*/
        return ranking;
    }

    private AtomicBoolean alreadySort = new AtomicBoolean(false);

    private boolean isNeedSort() {
        return !CollectionUtils.isEmpty(sortTotalRanking) && !CollectionUtils.isEmpty(platformRankingResult)&&!alreadySort.get();
    }

    public void updateRanking() {
        platformRankingResult = queryPlatformRanking();
        alreadySort.set(false);
        if (platformRankingResult == null) {
            LogUtil.warn("DefaultRankingImpl.updateRanking, update ranking failed, ranking name:" + this.rankingName);
            return;
        }
/*        if (rankingType== ERT_PlayerLevel&&!fixData.get()){
            //todo 等级排行榜修复补丁（下个版本删除）
            fixPlayerLevelRanking();
        }*/

        clearLocalRankingWithOutPlatform();

        for (RankingQuerySingleResult result : platformRankingResult) {
            this.totalRankingInfo.put(result.getPrimaryKey(), result);

            if (result.getRanking() <= RANKING_INFO_SIZE) {
                this.rankingInfo.put(result.getRanking(), result);
            }
        }

        invokeUpdateSenderInfo(platformRankingResult);

        updateClientRankEntranceInfo();

        updateRankingTargetRewardInfo();
    }

    private synchronized void fixPlayerLevelRanking() {
        if (fixData.get()) {
            return;
        }
        platformRankingResult.sort(Comparator.comparingInt(RankingQuerySingleResult::getRanking));

        RankingUpdateRequest request = new RankingUpdateRequest(getRankingName(), getRankingServerIndex(), getSortRules());
        for (RankingQuerySingleResult item : platformRankingResult) {
            request.addScore(item.getPrimaryKey(), item.getPrimaryScore());
        }
        LogUtil.info("level rank fix data:{}",request);
        if (!HttpRequestUtil.updateRanking(request)) {
            LogUtil.error("fixPlayerLevelRanking updatePlayerRankingScore, update ranking failed, rankingName：" + getRankingName());
        }
        fixData.set(true);
    }

    private void updateRankingTargetRewardInfo() {
        RankingTargetManager.getInstance().updateRankingTargetRewardInfo(this.rankingType);

    }

    private void invokeUpdateSenderInfo(List<RankingQuerySingleResult> results) {
        if (this.senderMap == null || CollectionUtils.isEmpty(results)) {
            return;
        }
        for (AbstractRankingMsgSender<?> value : senderMap.values()) {
            value.updateRanking(results);
        }
    }

    protected int getRankingServerIndex() {
        return this.crossRanking ? -1 : ServerConfig.getInstance().getServer();
    }

    protected List<RankingQuerySingleResult> queryPlatformRanking() {
        RankingQueryRequest query = new RankingQueryRequest();
        query.setSortRules(getSortRules());
        query.setRank(this.rankingName);
        query.setServerIndex(getRankingServerIndex());
        query.setPage(1);
        query.setSize(getUpdateSize());
        HttpRankingResponse result = HttpRequestUtil.queryRanking(query);
        if (result == null) {
            LogUtil.error("query forInv ranking result is null");
            return null;
        }
        RankingQueryResult data = result.getData();
        if (data == null) {
            LogUtil.error("query forInv ranking data is null");
            return null;
        }

        List<RankingQuerySingleResult> pageInfo = data.getPageInfo();
        if (CollectionUtils.isEmpty(pageInfo)) {
       /*     LogUtil.error("query forInv ranking page data is null, curTime = "
                    + GlobalTick.getInstance().getCurrentTime() + ",query result:" + result);*/
            return null;
        }
        return pageInfo;
    }


    /**
     * 是否需要更新
     *
     * @return
     */
    public boolean needUpdate() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime < this.nextUpdateRankingTime) {
            return false;
        }
        setNextUpdateTime();
        return true;
    }

    /**
     * 加入随机延迟
     */
    private synchronized void setNextUpdateTime() {
        long randomDelayTime = RandomUtils.nextInt(10) * TimeUtil.MS_IN_A_S;
        this.nextUpdateRankingTime = GlobalTick.getInstance().getCurrentTime() + this.rankingUpdateInterval + randomDelayTime;
    }

    public synchronized boolean clearRanking() {
        clearLocalRanking();
        clearSenderInfo();
        clearRankingEntranceInfo();
        return clearRemoteRanking();
    }

    private void clearRankingEntranceInfo() {
        clientRankEntranceInfo = new RankingEntranceDto();
    }

    public void clearSenderInfo() {
        if (MapUtils.isNotEmpty(this.senderMap)) {
            for (AbstractRankingMsgSender<?> value : senderMap.values()) {
                value.clear();
            }
        }
    }

    protected void clearLocalRanking() {
        clearLocalRankingWithOutPlatform();
        this.platformRankingResult = null;
    }

    protected void clearLocalRankingWithOutPlatform() {
        this.totalRankingInfo.clear();
        this.rankingInfo.clear();
    }

    /**
     * 清空平台排行榜
     */
    private boolean clearRemoteRanking() {
        boolean clearSuccess = false;
        for (int i = 0; i < CLEAR_REMOTE_RANKING_RETRY_TIMES; i++) {
            if (HttpRequestUtil.clearRanking(getRankingName(), getRankingServerIndex(), getSortRules())) {
                clearSuccess = true;
                break;
            }
        }

        if (!clearSuccess) {
            LogUtil.error("model.foreigninvasion.NewForeignInvasionManager.clearRemoteRanking, " +
                    "clear remote ranking failed, retry times:" + CLEAR_REMOTE_RANKING_RETRY_TIMES);
        }
        return clearSuccess;
    }

    /**
     * 清除排行榜上为key的数据
     * @param key
     */
    public void clearRankingMember(String key) {
        if (!HttpRequestUtil.clearRanking(rankingName, getRankingServerIndex(), getSortRules(), Collections.singletonList(key))) {
            LogUtil.error("model.ranking.ranking.AbstractRanking.clearRankingMember err. rankingName:{}, key:{}", rankingName, key);
        }
    }

    /**
     * 获取指定排名的玩家数据
     *
     * @param rankingNum
     * @return
     */
    public RankingQuerySingleResult getPlayerInfoByRanking(int rankingNum) {
        if (rankingNum > RANKING_INFO_SIZE) {
            LogUtil.warn("AbstractRanking.getPlayerInfoByRanking, ranking num is max than cache size:" + getRankingInfoSize());
        }
        return rankingInfo.get(rankingNum);
    }

    public void sendRankingMsgToPlayer(EnumRankingSenderType senderType, String playerIdx) {
        if (senderType == null || StringUtils.isEmpty(playerIdx)) {
            return;
        }

        String sendKey = senderType.toString();
        AbstractRankingMsgSender<?> msgSender = this.senderMap.get(sendKey);
        if (msgSender == null) {
            msgSender = createRankingMsgSender(senderType);
            if (msgSender != null) {
                initSender(msgSender);
                this.senderMap.put(sendKey, msgSender);
                invokeUpdateSenderInfo(platformRankingResult);
            }
        }

        if (msgSender != null) {
            msgSender.sendRankingInfo(playerIdx, queryPlayerRankingResult(playerIdx), getRankingType());
        }
    }

    private static final Map<EnumRankingSenderType, Supplier<? extends AbstractRankingMsgSender<?>>> RANKING_SENDER_SUPPLIER;

    static {
        Map<EnumRankingSenderType, Supplier<? extends AbstractRankingMsgSender<?>>> tempMap = new HashMap<>();

        tempMap.put(EnumRankingSenderType.ERST_Common, CommonRankingMsgSender::new);
        tempMap.put(EnumRankingSenderType.ERST_MainLine, MainLineRankingMsgSender::new);
        tempMap.put(EnumRankingSenderType.ERST_EndlessSpire, EndlessSpireRankingMsgSender::new);
        tempMap.put(EnumRankingSenderType.ERST_NewForeignInvasion, NewForeignInvasionMsgSender::new);
        tempMap.put(EnumRankingSenderType.ERST_Activity, ActivityRankingMsgSender::new);
        tempMap.put(EnumRankingSenderType.ERST_PetAbilityRanking, PetAbilityRankingMsgSender::new);
        tempMap.put(EnumRankingSenderType.ERST_ArenaDanRanking, ArenaDanMsgSender::new);
        tempMap.put(EnumRankingSenderType.ERST_MatchArenaRanking, MatchArenaRankingSender::new);
        RANKING_SENDER_SUPPLIER = Collections.unmodifiableMap(tempMap);
    }

    private AbstractRankingMsgSender<?> createRankingMsgSender(EnumRankingSenderType senderType) {
        if (senderType == null) {
            return null;
        }

        Supplier<? extends AbstractRankingMsgSender<?>> supplier = RANKING_SENDER_SUPPLIER.get(senderType);
        if (supplier == null) {
            LogUtil.error("RankingManager.getRankingMsgSender, senderType have not supplier:" + senderType);
            return null;
        }
        return supplier.get();
    }

    /**
     * 若排行榜需要调用此方法,注意有副分数的排行榜,需要重写getLocalSubScore(java.lang.String)方法
     */
    public void updateTotalPlayerScore() {
        List<String> allPlayerIdx = playerCache.getInstance().getAllPlayerIdx();
        List<List<String>> lists = GameUtil.splitList(allPlayerIdx, UPDATE_PLAYER_SCORE_PAGE_SIZE);
        if (CollectionUtils.isEmpty(lists)) {
            return;
        }

        for (List<String> list : lists) {
            RankingUpdateRequest request = new RankingUpdateRequest(getRankingName(), getRankingServerIndex(), getSortRules());
            for (String playerIdx : list) {
                long localScore = getLocalScore(playerIdx);
                if (localScore > 0) {
                    request.addScore(playerIdx, getLocalScore(playerIdx), getLocalSubScore(playerIdx));
                }
            }
            if (!HttpRequestUtil.updateRanking(request)) {
                LogUtil.error("AbstractRanking.updatePlayerRankingScore, update ranking failed, rankingName：" + getRankingName());
            }
        }
    }

    /**
     * ==================================================================
     */
    @Override
    public String getBaseIdx() {
        return "";
    }

    @Override
    public String getClassType() {
        return "Ranking";
    }

    @Override
    public void putToCache() {
    }

    @Override
    public void transformDBData() {
    }
    /**
     * ===========================需要子类继承的方法=================
     */

    /**
     * 排行榜初始化时调用
     */
    public void init() {
        setRankingUpdateInterval(getConfigRefreshInterVal());
    }

    public boolean needSettleOldData(AbstractRanking ranking) {
        if (ranking == null) {
            return false;
        }
        if (!(ranking instanceof TargetRewardRanking)) {
            return false;
        }

        rankEntity rankEntity = rankCache.getInstance().getByRankTypeNumber(this.rankingType.getNumber());
        if (rankEntity == null) {
            return false;
        }
        return SyncExecuteFunction.executeFunction(rankEntity, r -> !rankEntity.getDb_data().isSettleOldData());

    }

    private long getConfigRefreshInterVal() {
        if (rankingType == null) {
            LogUtil.error("this ranking:{} rankType is null", this.getClass());
            return 0L;
        }
        RankConfigObject rankCfg = RankConfig.getByRankid(this.rankingType.getNumber());
        if (rankCfg == null) {
            LogUtil.warn("this ranking:{} rankCfg is null by rankingType:{}", this.getClass(), this.rankingType);
            return 0L;
        }
        return rankCfg.getRankrefreshtime() * TimeUtil.MS_IN_A_S;
    }

    /**
     * 查询玩家本地分数
     *
     * @param playerIdx
     * @return
     */
    public abstract long getLocalScore(String playerIdx);

    public List<Integer> getSortRules() {
        return RankingUpdateRequest.DEFAULT_SORT_RULES;
    }

    /**
     * 平台会处理排序分数相同 排名先来后到
     * @param playerIdx
     * @return
     */
    public long getLocalSubScore(String playerIdx) {
        return 0;
    }

    public void doSettleOldData() {
        rankEntity rankEntity = rankCache.getInstance().getByRankTypeNumber(this.rankingType.getNumber());
        if (rankEntity == null) {
            throw new RuntimeException("settleOldData rankEntity is null by RankType " + getRankingType());
        }
        if (newDevelopRanking.contains(this.rankingType)) {
            updateTotalPlayerScore();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            LogUtil.printStackTrace(e);
        }
        updateRanking();

        no1DataTriggerRankingTargetReward();

        SyncExecuteFunction.executeConsumer(rankEntity, r -> rankEntity.getDb_data().setSettleOldData(true));
    }

    protected void no1DataTriggerRankingTargetReward() {
        if (MapUtils.isEmpty(rankingInfo)) {
            return;
        }

        RankingQuerySingleResult no1Player = this.rankingInfo.get(1);
        if (no1Player == null) {
            LogUtil.error("rankType:{} no1DataTriggerRankingTargetReward no1Data not exists:{}", rankingType);
            return;
        }
        RankingTargetManager.getInstance().updateRankTarget(this.rankingType.getNumber(), no1Player.getPrimaryKey(), getPrimaryScore(no1Player));
        RankingTargetManager.getInstance().updateRankingTargetRewardInfo(rankingType);
    }


    public void sendNewRankData() {
        nextUpdateTime =GlobalTick.getInstance().getCurrentTime()+TimeUtil.MS_IN_A_S;
        RankingUpdateRequest requestData =this.rankRequest;
        this.rankRequest = createRankRequest();
        

        HttpRequestUtil.updateRanking(requestData);
    }
    private long nextUpdateTime;

    public boolean needSendNewRankData() {
        if (this.rankRequest.getItems().size()>0&&nextUpdateTime< GlobalTick.getInstance().getCurrentTime()){
            return true;
        }
        return false;
    }

    protected void initSender(AbstractRankingMsgSender<?> msgSender) {

    }
}
