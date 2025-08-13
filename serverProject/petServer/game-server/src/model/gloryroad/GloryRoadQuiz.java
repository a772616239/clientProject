package model.gloryroad;

import cfg.GloryRoadConfig;
import cfg.PlayerLevelConfig;
import cfg.PlayerLevelConfigObject;
import com.alibaba.fastjson.JSONObject;
import common.GameConst;
import common.GlobalData;
import common.tick.GlobalTick;
import common.tick.Tickable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.team.dbCache.teamCache;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Common.Consume;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.GameplayDB.DB_PlayerQuizInfo;
import protocol.GameplayDB.DB_Quiz;
import protocol.GameplayDB.DB_QuizConsumeInfo;
import protocol.GloryRoad.EnumGloryRoadSchedule;
import protocol.GloryRoad.GloryRoadQuizInfo;
import protocol.GloryRoad.GloryRoadQuizOdds;
import protocol.GloryRoad.GloryRoadQuizRecord;
import protocol.GloryRoad.SC_RefreshGloryRoadQuizInfo;
import protocol.GloryRoad.SC_RefreshGloryRoadQuizOdds;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huhan
 * @date 2021/3/16
 */
@Getter
@Setter
@ToString
public class GloryRoadQuiz implements Tickable {

    /**
     * 玩家竞猜信息
     */
    private GloryRoadQuizServerInfo player_1_quizInfo;
    private GloryRoadQuizServerInfo player_2_quizInfo;

    /**
     * 开始时间
     */
    private long startTime;
    /**
     * 竞猜结束时间
     */
    private long endTime;

    /**
     * 下次计算赔率时间
     */
    private long nextReCalcTime;

    /**
     * 竞猜阶段
     */
    private EnumGloryRoadSchedule schedule;

    /**
     * 评论
     */
    private final List<String> commentList = new ArrayList<>();

    private long quizOddsUpdateInterval;

    /**
     * 双方战斗父节点
     */
    private int parentIndex;

    private GloryRoadQuiz() {
        this.quizOddsUpdateInterval =
                GloryRoadConfig.getById(GameConst.CONFIG_ID).getQuizoddsupdateinterval() * TimeUtil.MS_IN_A_S;
    }

    public static GloryRoadQuiz createEntity(GloryRoadOpponent opponent, EnumGloryRoadSchedule schedule, long endTime) {
        if (opponent == null || opponent.isEmpty() || schedule == null || endTime <= 0) {
            LogUtil.error("GloryRoadQuiz.createEntity, error params, opponent:" + opponent
                    + ", schedule:" + schedule + ", endTime:" + endTime);
            return null;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime > endTime) {
            LogUtil.info("GloryRoadQuiz.createEntity, skip open quiz,currentTime:" + currentTime + ", is max than end time:" + endTime);
            return null;
        }

        GloryRoadQuiz quiz = new GloryRoadQuiz();
        if (opponent.getPlayerIdx1() != null) {
            quiz.setPlayer_1_quizInfo(new GloryRoadQuizServerInfo(opponent.getPlayerIdx1()));
        }

        if (opponent.getPlayerIdx2() != null) {
            quiz.setPlayer_2_quizInfo(new GloryRoadQuizServerInfo(opponent.getPlayerIdx2()));
        }
        quiz.setParentIndex(opponent.getParentIndex());
        quiz.setSchedule(schedule);
        quiz.setStartTime(GlobalTick.getInstance().getCurrentTime());
        quiz.setEndTime(endTime);
        quiz.sendQuizMsg();

        LogUtil.debug("GloryRoadQuiz.createEntity, detail:" + quiz);

        return quiz;
    }

    public static GloryRoadQuiz createEntity(DB_Quiz dbQuiz) {
        if (dbQuiz == null || dbQuiz.getPlayerQuizInfoCount() <= 0) {
            LogUtil.info("GloryRoadQuiz.createEntity, dbQuiz is null or player quiz info list is empty");
            return null;
        }

        GloryRoadQuiz quiz = new GloryRoadQuiz();
        quiz.setStartTime(dbQuiz.getStartTime());
        quiz.setEndTime(dbQuiz.getEndTime());
        quiz.setSchedule(dbQuiz.getSchedule());
        quiz.setParentIndex(dbQuiz.getParentIndex());

        for (DB_PlayerQuizInfo quizInfo : dbQuiz.getPlayerQuizInfoList()) {
            GloryRoadQuizServerInfo entity = GloryRoadQuizServerInfo.createEntity(quizInfo);
            if (entity != null) {
                quiz.addPlayerQuizInfo(entity);
            }
        }
        quiz.reCalcOdds();

        quiz.sendQuizMsg();

        LogUtil.debug("GloryRoadQuiz.createEntity, detail:" + quiz);
        return quiz;
    }

    public boolean addPlayerQuizInfo(GloryRoadQuizServerInfo info) {
        if (info == null) {
            return false;
        }

        if (this.player_1_quizInfo == null) {
            this.player_1_quizInfo = info;
            return true;
        }

        if (this.player_2_quizInfo == null) {
            this.player_2_quizInfo = info;
            return true;
        }

        LogUtil.error("GloryRoadQuiz.addPlayerQuizInfo, both player info is not empty");
        return false;
    }


    /**
     * 开启新竞猜时调用
     */
    public void sendQuizMsg() {
        GlobalData.getInstance().sendMsgToAllSatisfyOnlinePlayer(MsgIdEnum.SC_RefreshGloryRoadQuizInfo,
                SC_RefreshGloryRoadQuizInfo.newBuilder().setInfo(buildGloryRoadQuizPlayerInfo()),
                GloryRoadUtil.LV_CONDITION);
    }

    public GloryRoadQuizInfo.Builder buildGloryRoadQuizPlayerInfo(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        GloryRoadQuizInfo.Builder builder = buildGloryRoadQuizPlayerInfo();
        if (this.player_1_quizInfo != null && this.player_1_quizInfo.contains(playerIdx)) {
            builder.setPlayerSupport(this.player_1_quizInfo.getPlayerIdx());

        } else if (this.player_2_quizInfo != null && this.player_2_quizInfo.contains(playerIdx)) {
            builder.setPlayerSupport(this.player_2_quizInfo.getPlayerIdx());
        }
        return builder;
    }

    public GloryRoadQuizInfo.Builder buildGloryRoadQuizPlayerInfo() {
        GloryRoadQuizInfo.Builder builder = GloryRoadQuizInfo.newBuilder();
        builder.setBattleIndex(this.parentIndex);
        if (this.player_1_quizInfo != null) {
            builder.addOdds(this.player_1_quizInfo.buildOddsInfo());
        }

        if (this.player_2_quizInfo != null) {
            builder.addOdds(this.player_2_quizInfo.buildOddsInfo());
        }
        builder.setQuizEndTime(this.endTime);
        return builder;
    }


    @Override
    public synchronized void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime > this.endTime) {
            return;
        }
        if (currentTime > this.nextReCalcTime) {
            reCalcOdds();
            refreshOddsMsg();
            this.nextReCalcTime = currentTime + quizOddsUpdateInterval;
        }
    }

    /**
     * 赔率计算方式
     * 1.取双方队伍战力值
     * 战力大的一方战力记为：powerA
     * 战力小的一方战力记为：powerB
     * 若双方战力一致，则认为第一个队伍为战力大的一方
     * <p>
     * 2.取双方魔灵战力方差
     * 战力大一方的方差记为：stdevA
     * 战力小一方的方差记为：stdevB
     * <p>
     * 3.计算战力大的一方胜率
     * 对战力大一方的胜利进行预测计算：
     * 将胜率概率记为：winrateA
     * winrateA=powerA/powerB*0.5+(stdevA-stdevB)/stdevA
     * 同时需满足0.9>=winrateA>=0.1
     * <p>
     * 4.计算赔率
     * 战力大一方赔率=(1/((1+winrateA)*（1+winrateA)))*4
     * 战力小一方赔率=(1/((1+winrateB)*（1+winrateB)))*4
     */
    private void reCalcOdds() {
        List<BattlePetData> player_1_battlePetData = getPetBattleData(this.player_1_quizInfo);
        List<BattlePetData> player_2_battlePetData = getPetBattleData(this.player_2_quizInfo);

        long player_1_power = player_1_battlePetData.stream().map(BattlePetData::getAbility).reduce(Long::sum).orElse(0L);
        long player_2_power = player_2_battlePetData.stream().map(BattlePetData::getAbility).reduce(Long::sum).orElse(0L);

        if (player_1_power >= player_2_power) {
            calcOdds(this.player_1_quizInfo, player_1_battlePetData, player_1_power,
                    this.player_2_quizInfo, player_2_battlePetData, player_2_power);
        } else {
            calcOdds(this.player_2_quizInfo, player_2_battlePetData, player_2_power,
                    this.player_1_quizInfo, player_1_battlePetData, player_1_power);
        }
        LogUtil.info("GloryRoadQuiz.reCalcOdds, update finished");
    }

    public void calcOdds(GloryRoadQuizServerInfo largePowerInfo, List<BattlePetData> largePetBattle, long largePower,
                         GloryRoadQuizServerInfo smallPowerInfo, List<BattlePetData> smallPetBattle, long smallPower) {
//        printQuizPlayerTeamPetInfo(largePowerInfo.getPlayerIdx(), largePetBattle, largePower);
//        printQuizPlayerTeamPetInfo(smallPowerInfo.getPlayerIdx(), smallPetBattle, smallPower);

        double largeWinRate = GloryRoadUtil.calcLargePowerWinRateWithFix(largePetBattle, largePower, smallPetBattle, smallPower);
        double smallWinRate = 1 - largeWinRate;
        LogUtil.info("GloryRoadQuiz.calcOdds, largeWinRate:" + largeWinRate);


        double largeOdds = (1 / Math.pow(1 + largeWinRate, 2)) * 4;
        double smallOdds = (1 / Math.pow(1 + smallWinRate, 2)) * 4;

        LogUtil.info("GloryRoadQuiz.calcOdds, largeOdds:" + largeOdds + ", smallOdds:" + smallOdds);

        if (largePowerInfo != null) {
            largePowerInfo.setOdds(largeOdds);
        }

        if (smallPowerInfo != null) {
            smallPowerInfo.setOdds(smallOdds);
        }
    }

    private void printQuizPlayerTeamPetInfo(String playerIdx, List<BattlePetData> petBattle, long power) {
        JSONObject object = new JSONObject();
        if (CollectionUtils.isNotEmpty(petBattle)) {
            for (BattlePetData petData : petBattle) {
                if (petData == null) {
                    continue;
                }
                //petIdx:power
                object.put(petData.getPetId(), petData.getAbility());
            }
        }

        LogUtil.debug("model.gloryroad.GloryRoadQuiz.printQuizPlayerTeamPetInfo, playerIdx:" + playerIdx +
                ", totalPower:" + power + ", detail:" + object.toJSONString());

    }

    private List<BattlePetData> getPetBattleData(GloryRoadQuizServerInfo info) {
        if (info == null) {
            return Collections.emptyList();
        }
        List<BattlePetData> battlePetData = teamCache.getInstance().buildBattlePetData(info.getPlayerIdx(),
                TeamTypeEnum.TTE_GloryRoad, BattleSubTypeEnum.BSTE_GloryRoad);
        return battlePetData == null ? Collections.emptyList() : battlePetData;
    }

    private void refreshOddsMsg() {
        GlobalData.getInstance().sendMsgToAllSatisfyOnlinePlayer(MsgIdEnum.SC_RefreshGloryRoadQuizOdds,
                buildRefreshOddsMsg(),
                GloryRoadUtil.LV_CONDITION);
    }

    private SC_RefreshGloryRoadQuizOdds.Builder buildRefreshOddsMsg() {
        SC_RefreshGloryRoadQuizOdds.Builder builder = SC_RefreshGloryRoadQuizOdds.newBuilder();
        if (this.player_1_quizInfo != null) {
            builder.addNewOdds(this.player_1_quizInfo.buildOddsInfo());
        }

        if (this.player_2_quizInfo != null) {
            builder.addNewOdds(this.player_2_quizInfo.buildOddsInfo());
        }
        return builder;
    }

    public RetCodeEnum supportPlayer(String playerIdx, String supportIdx) {
        GloryRoadQuizServerInfo quizInfo = getQuizInfo(supportIdx);
        if (StringUtils.isEmpty(playerIdx) || quizInfo == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (GameUtil.outOfScope(this.startTime, this.endTime, GlobalTick.getInstance().getCurrentTime())) {
            return RetCodeEnum.RCE_GloryRoad_Quiz_OutOfTime;
        }

        if (!canQuiz(playerIdx)) {
            return RetCodeEnum.RCE_GloryRoad_Quiz_AlreadySupported;
        }

        int playerLv = PlayerUtil.queryPlayerLv(playerIdx);
        PlayerLevelConfigObject levelConfig = PlayerLevelConfig.getByLevel(playerLv);
        if (levelConfig == null) {
            LogUtil.error("GloryRoadQuiz.supportPlayer, player level config is null, playerLv:" + playerLv);
            return RetCodeEnum.RCE_UnknownError;
        }

        Consume consume = ConsumeUtil.parseConsume(levelConfig.getGloryroadquizconsume());
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GloryRoadQuiz);
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            return RetCodeEnum.RCE_MatieralNotEnough;
        }

        quizInfo.putPlayerQuizConsume(playerIdx, consume);

        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_GloryRoad_CumuQuiz, 1, 0);

        LogUtil.info("GloryRoadQuiz.supportPlayer, playerIdx:" + playerIdx
                + ", support idx:" + supportIdx + ", consume:" + consume);
        return RetCodeEnum.RCE_Success;
    }

    public boolean canQuiz(String playerIdx) {
        boolean result = true;
        if (this.player_1_quizInfo != null && this.player_1_quizInfo.contains(playerIdx)) {
            result = false;
        }

        if (this.player_2_quizInfo != null && this.player_2_quizInfo.contains(playerIdx)) {
            result = false;
        }

        return result;
    }

    public GloryRoadQuizServerInfo getQuizInfo(String supportIdx) {
        if (this.player_1_quizInfo != null && Objects.equals(supportIdx, this.player_1_quizInfo.getPlayerIdx())) {
            return this.player_1_quizInfo;
        }

        if (this.player_2_quizInfo != null && Objects.equals(supportIdx, this.player_2_quizInfo.getPlayerIdx())) {
            return this.player_2_quizInfo;
        }
        return null;
    }

    public boolean settleQuiz(String winPlayerIdx, String failedPlayerIdx, String linkBattleRecord) {
        GloryRoadQuizServerInfo win = getQuizInfo(winPlayerIdx);
        GloryRoadQuizServerInfo failed = getQuizInfo(failedPlayerIdx);
        if (win == null && failed == null) {
            LogUtil.info("GloryRoadQuiz.settleQuiz, quiz is not belong to player1:" + winPlayerIdx + ", player2:" + failedPlayerIdx);
            return false;
        }

        GloryRoadQuizRecord.Builder recordBuilder = GloryRoadQuizRecord.newBuilder();
        recordBuilder.setBattleIndex(this.parentIndex);
        recordBuilder.setWinPlayerIdx(winPlayerIdx);
        if (StringUtils.isNotEmpty(linkBattleRecord)) {
            recordBuilder.setLinkBattleRecordId(linkBattleRecord);
        }

        //处理竞猜胜利玩家
        if (win != null) {
            LogUtil.info("GloryRoadQuiz.settleQuiz, win playerIdx:" + winPlayerIdx + ", cur odds:" + win.getOdds());
            for (Entry<String, Consume> entry : win.getPlayerQuizConsumeMap().entrySet()) {
                Consume consume = entry.getValue();
                if (consume == null) {
                    LogUtil.error("GloryRoadQuiz.settleQuiz, playerIdx:" + entry.getKey() + ", consume is null");
                    continue;
                }

                recordBuilder.clearReward();

                int newCount = (int) (consume.getCount() * win.getOdds());
                Reward.Builder rewardBuilder = Reward.newBuilder()
                        .setRewardType(consume.getRewardType())
                        .setId(consume.getId())
                        .setCount(newCount);

                Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GloryRoadQuiz);
                if (GlobalData.getInstance().checkPlayerOnline(entry.getKey())) {
                    RewardManager.getInstance().doReward(entry.getKey(), rewardBuilder.build(), reason, false);
                } else {
                    EventUtil.triggerAddMailEvent(entry.getKey(), GloryRoadConfig.getById(GameConst.CONFIG_ID).getQuizsuccessmail(),
                            Collections.singletonList(rewardBuilder.build()), reason);
                }

                //记录里面需要去掉本金
                recordBuilder.setReward(rewardBuilder.setCount(newCount - consume.getCount()).build());
                recordBuilder.setQuizCostCount(consume.getCount());
                EventUtil.addGloryRoadQuizRecord(entry.getKey(), recordBuilder.build());
            }
        }

        //处理竞猜失败玩家
        if (failed != null) {
            for (Entry<String, Consume> entry : failed.getPlayerQuizConsumeMap().entrySet()) {
                Consume consume = entry.getValue();
                if (consume == null) {
                    LogUtil.error("GloryRoadQuiz.settleQuiz, playerIdx:" + entry.getKey() + ", consume is null");
                    continue;
                }

                recordBuilder.clearReward();

                Reward reward = Reward.newBuilder()
                        .setRewardType(consume.getRewardType())
                        .setId(consume.getId())
                        .setCount(-consume.getCount())
                        .build();
                recordBuilder.setReward(reward);
                recordBuilder.setQuizCostCount(consume.getCount());
                EventUtil.addGloryRoadQuizRecord(entry.getKey(), recordBuilder.build());
            }
        }
        return true;
    }

    public DB_Quiz buildQuizDbData() {
        DB_Quiz.Builder resultBuilder = DB_Quiz.newBuilder();
        resultBuilder.setStartTime(getStartTime());
        resultBuilder.setEndTime(getEndTime());
        resultBuilder.setParentIndex(getParentIndex());
        resultBuilder.setSchedule(getSchedule());

        if (this.player_1_quizInfo != null) {
            resultBuilder.addPlayerQuizInfo(this.player_1_quizInfo.buildDbData());
        }

        if (this.player_2_quizInfo != null) {
            resultBuilder.addPlayerQuizInfo(this.player_2_quizInfo.buildDbData());
        }
        return resultBuilder.build();
    }
}

@ToString
class GloryRoadQuizServerInfo {
    @Getter
    private final String playerIdx;

    /**
     * 玩家竞猜信息
     */
    @Getter
    private final Map<String, Consume> playerQuizConsumeMap = new ConcurrentHashMap<>();

    /**
     * 竞猜赔率
     */
    @Getter
    @Setter
    private volatile double odds;

    public GloryRoadQuizServerInfo(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public void putPlayerQuizConsume(String playerIdx, Consume consume) {
        if (StringUtils.isEmpty(playerIdx) || consume == null) {
            LogUtil.error("GloryRoadQuizPlayerInfo.addQuiz, error params, playerIdx:" + playerIdx + ", consume:" + consume);
            return;
        }

        this.playerQuizConsumeMap.put(playerIdx, consume);
    }

    public boolean canQuiz(String playerIdx) {
        return !this.contains(playerIdx);
    }

    public boolean contains(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return true;
        }
        return this.playerQuizConsumeMap.containsKey(playerIdx);
    }

    public GloryRoadQuizOdds buildOddsInfo() {
        GloryRoadQuizOdds.Builder resultBuilder = GloryRoadQuizOdds.newBuilder();
        resultBuilder.setPlayerIdx(this.playerIdx);
        resultBuilder.setNewOdds(this.odds);
        return resultBuilder.build();
    }

    public DB_PlayerQuizInfo buildDbData() {
        DB_PlayerQuizInfo.Builder resultBuilder = DB_PlayerQuizInfo.newBuilder();
        resultBuilder.setPlayerIdx(getPlayerIdx());
        for (Entry<String, Consume> entry : playerQuizConsumeMap.entrySet()) {
            DB_QuizConsumeInfo.Builder consumeBuilder = DB_QuizConsumeInfo.newBuilder()
                    .setPlayerIdx(entry.getKey())
                    .setConsume(entry.getValue());

            resultBuilder.addQuizConsume(consumeBuilder);
        }

        return resultBuilder.build();
    }

    public static GloryRoadQuizServerInfo createEntity(DB_PlayerQuizInfo playerQuizInfo) {
        if (playerQuizInfo == null) {
            return null;
        }

        GloryRoadQuizServerInfo playerInfo = new GloryRoadQuizServerInfo(playerQuizInfo.getPlayerIdx());
        for (DB_QuizConsumeInfo consumeInfo : playerQuizInfo.getQuizConsumeList()) {
            playerInfo.putPlayerQuizConsume(consumeInfo.getPlayerIdx(), consumeInfo.getConsume());
        }
        return playerInfo;
    }
}
