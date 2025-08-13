package model.gloryroad;

import cfg.FightMake;
import cfg.GloryRoadConfig;
import common.GameConst;
import common.GlobalData;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import model.battle.BattleManager;
import model.battle.BattleUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.team.dbCache.teamCache;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_EnterFight;
import protocol.Battle.SkillBattleDict;
import protocol.GloryRoad.SC_GloryRoadManualOperateBattleEnsure;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_BS_ApplyPvpBattle;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import util.EventUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2021/3/18
 */
@ToString
public class GloryRoadBattle implements Tickable {
    /**
     * 总对手个数
     */
    public static final int OPPONENT_COUNT = 2;

    /**
     * 进入战斗重试次数
     */
    public static final int ENTER_FIGHT_RETRY_MAX_TIMES = 5;
    /**
     * 对手状态保存
     */
    private DefaultKeyValue<String, Boolean> player_1_ManualStatus;
    private DefaultKeyValue<String, Boolean> player_2_ManualStatus;

    /**
     * 需要等待后再进入战斗
     */
    @Setter
    @Getter
    private long waitEndTime;

    /**
     * 是否需要发送战斗申请
     */
    @Getter
    @Setter
    private boolean needSendApply;

    /**
     * 是否已经进入 战斗
     * 进入战斗后不需要tick
     */
    @Getter
    @Setter
    private boolean alreadyEnterBattle;

    /**
     * 战斗父节点, 进入战斗参数, 会随着战斗结算返回
     */
    @Getter
    @Setter
    private int parentIndex;

    /**
     * 手动战斗是已经确认得数量
     */
    private final Set<String> ensureSet = new HashSet<>();

    private int enterFightTryTimes;

    /**
     * 玩家在线状态
     */
    private final Map<String, Boolean> playerOnlineStatus = new HashMap<>();

    private GloryRoadBattle() {
    }

    public synchronized boolean addOpponent(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            LogUtil.error("GloryRoadBattle.addOpponent, param is null");
            return false;
        }

        if (player_1_ManualStatus == null) {
            this.player_1_ManualStatus = new DefaultKeyValue<>(playerIdx, false);
        } else if (player_2_ManualStatus == null) {
            this.player_2_ManualStatus = new DefaultKeyValue<>(playerIdx, false);
        } else {
            LogUtil.error("GloryRoadBattle.addOpponent, opponent is full, playerIdx1:"
                    + this.player_1_ManualStatus.getKey() + ", playerIdx2:" + this.player_2_ManualStatus.getKey()
                    + ", parentIndex:" + this.parentIndex);
        }
        return true;
    }

    @Override
    public synchronized void onTick() {
        if (this.alreadyEnterBattle) {
            return;
        }

        sendManualApply();

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime > this.waitEndTime || ensureSet.size() >= OPPONENT_COUNT) {
            if (enterBattle()) {
                this.alreadyEnterBattle = true;
                LogUtil.info("GloryRoadBattle.onTick, player id list:" + this.player_1_ManualStatus
                        + "," + this.player_2_ManualStatus + ", enter battle success");
            } else {
                LogUtil.info("GloryRoadBattle.onTick, player id list:" + this.player_1_ManualStatus
                        + "," + this.player_2_ManualStatus + ", enter battle failed");
            }
        }
    }

    /**
     * 只有一方有玩家时,直接玩家胜利
     *
     * @return
     */
    private synchronized boolean enterBattle() {
        LogUtil.info("GloryRoadBattle.enterBattle, player1Idx:" + getPlayer_1_Idx() + ", player2Idx:" + getPlayer_2_Idx());
        if (StringUtils.isEmpty(getPlayer_1_Idx()) && StringUtils.isEmpty(getPlayer_2_Idx())) {
            LogUtil.warn("GloryRoadBattle.enterBattle, both of player is null, return");
            return true;
        }

        if (StringUtils.isEmpty(getPlayer_1_Idx())) {
            EventUtil.gloryRoadBattleResult(getPlayer_1_Idx(), getPlayer_2_Idx(), 2, "");
            return true;
        }

        if (StringUtils.isEmpty(getPlayer_2_Idx())) {
            EventUtil.gloryRoadBattleResult(getPlayer_1_Idx(), getPlayer_2_Idx(), 1, "");
            return true;
        }

        //双方都有玩家
        List<BattlePetData> player_1_battlePets = getPetBattleData(getPlayer_1_Idx());
        List<BattlePetData> player_2_battlePets = getPetBattleData(getPlayer_2_Idx());

        if (CollectionUtils.isEmpty(player_1_battlePets)) {
            EventUtil.gloryRoadBattleResult(getPlayer_1_Idx(), getPlayer_2_Idx(), 2, "");
            LogUtil.info("GloryRoadBattle.enterBattle, player1Idx:" + getPlayer_1_Idx() + " gloryRoad team is empty");
            return true;
        }

        if (CollectionUtils.isEmpty(player_2_battlePets)) {
            EventUtil.gloryRoadBattleResult(getPlayer_1_Idx(), getPlayer_2_Idx(), 1, "");
            LogUtil.info("GloryRoadBattle.enterBattle, player2Idx:" + getPlayer_2_Idx() + " gloryRoad team is empty");
            return true;
        }

        ensurePlayerBattleStatus();
        int ensureCount = getEnsureCount();
        boolean enterResult;
        if (ensureCount == 0 || this.enterFightTryTimes >= ENTER_FIGHT_RETRY_MAX_TIMES) {
            enterResult = directCheck();
        } else if (ensureCount == 2) {
            enterResult = enterPvP();
        } else {
            enterResult = enterPve();
        }
        this.enterFightTryTimes++;
        LogUtil.info("GloryRoadBattle.enterBattle, player1:" + getPlayer_1_Idx() + ", player2:" + getPlayer_2_Idx()
                + ", enter try times:" + this.enterFightTryTimes + ", enterResult:" + enterResult);
        return enterResult;
    }

    private int getOpponentCount() {
        int result = 0;
        if (this.player_1_ManualStatus != null) {
            result++;
        }
        if (this.player_2_ManualStatus != null) {
            result++;
        }
        return result;
    }

    private boolean enterPve() {
        CS_EnterFight.Builder enterBuilder = CS_EnterFight.newBuilder();
        enterBuilder.setType(BattleSubTypeEnum.BSTE_GloryRoad);
        enterBuilder.addParamList(getUnEnsurePlayerIdx());

        RetCodeEnum retCode = BattleManager.getInstance().enterPveBattle(getEnsurePlayerIdx(), enterBuilder.build());
        return retCode == RetCodeEnum.RCE_Success;
    }

    private boolean enterPvP() {
        int fightMakeId = GloryRoadConfig.getById(GameConst.CONFIG_ID).getPvpfightmakeid();
        if (FightMake.getById(fightMakeId) == null) {
            LogUtil.error("GloryRoadBattle.enterPvP, fight make id is not exist: " + fightMakeId);
            return false;
        }

        GS_BS_ApplyPvpBattle.Builder battleBuilder = GS_BS_ApplyPvpBattle.newBuilder();
        battleBuilder.getApplyPvpBattleDataBuilder().setFightMakeId(fightMakeId);
        battleBuilder.getApplyPvpBattleDataBuilder().setSubBattleType(BattleSubTypeEnum.BSTE_GloryRoad);
        battleBuilder.getApplyPvpBattleDataBuilder().addParam(String.valueOf(getParentIndex()));

        PvpBattlePlayerInfo.Builder player1Builder = buildPlayerBattleData(getPlayer_1_Idx(), 1);
        if (player1Builder == null) {
            return false;
        }
        battleBuilder.getApplyPvpBattleDataBuilder().addPlayerInfo(player1Builder);

        PvpBattlePlayerInfo.Builder player2Builder = buildPlayerBattleData(getPlayer_2_Idx(), 2);
        if (player2Builder == null) {
            return false;
        }
        battleBuilder.getApplyPvpBattleDataBuilder().addPlayerInfo(player2Builder);


        BaseNettyClient nettyClient = BattleServerManager.getInstance().getAvailableBattleServer();
        if (nettyClient == null) {
            LogUtil.error("model.gloryroad.GloryRoadBattle.enterPvP, not found battle server client");
            return false;
        }
        nettyClient.send(MsgIdEnum.GS_BS_ApplyPvpBattle_VALUE, battleBuilder);
        return true;
    }

    public PvpBattlePlayerInfo.Builder buildPlayerBattleData(String playerIdx, int camp) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        PvpBattlePlayerInfo.Builder pvpPlayerInfo = PvpBattlePlayerInfo.newBuilder();
        pvpPlayerInfo.setPlayerInfo(BattleUtil.buildPlayerBattleBaseInfo(playerIdx));
        pvpPlayerInfo.setCamp(camp);
        pvpPlayerInfo.setFromSvrIndex(ServerConfig.getInstance().getServer());

        TeamNumEnum nowUsedTeamNum = teamCache.getInstance().getNowUsedTeamNum(playerIdx, TeamTypeEnum.TTE_GloryRoad);

        List<BattlePetData> petBattleDataList = teamCache.getInstance().buildBattlePetData(playerIdx,
                nowUsedTeamNum, BattleSubTypeEnum.BSTE_GloryRoad);
        if (CollectionUtils.isNotEmpty(petBattleDataList)) {
            pvpPlayerInfo.addAllPetList(petBattleDataList);
        }

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            LogUtil.error("GloryRoadBattle.buildPlayerBattleData, can not get player:" + playerIdx + " entity");
            return null;
        }

        List<Integer> skillList = teamCache.getInstance().getPlayerTeamSkillList(playerIdx, nowUsedTeamNum);
        List<SkillBattleDict> skillDictList = entity.getSkillBattleDict(skillList);
        if (CollectionUtils.isNotEmpty(skillDictList)) {
            pvpPlayerInfo.addAllPlayerSkillIdList(skillDictList);
        }
        return pvpPlayerInfo;
    }

    /**
     * 直接走校验逻辑,战力相等时直接五五开判断胜负
     */
    public boolean directCheck() {
        List<BattlePetData> player_1_pets = getPetBattleData(getPlayer_1_Idx());
        List<BattlePetData> player_2_pets = getPetBattleData(getPlayer_2_Idx());

        long player_1_power = player_1_pets.stream().map(BattlePetData::getAbility).reduce(Long::sum).orElse(0L);
        long player_2_power = player_2_pets.stream().map(BattlePetData::getAbility).reduce(Long::sum).orElse(0L);

        boolean checkResult;
        if (player_1_power >= player_2_power) {
            checkResult = directCheck(getPlayer_1_Idx(), player_1_pets, player_1_power,
                    getPlayer_2_Idx(), player_2_pets, player_2_power);
        } else {
            checkResult = directCheck(getPlayer_2_Idx(), player_2_pets, player_2_power,
                    getPlayer_1_Idx(), player_1_pets, player_1_power);
        }
        return checkResult;
    }

    private boolean directCheck(String largePlayerIdx, List<BattlePetData> largePets, long largePower,
                                String smallPlayerIdx, List<BattlePetData> smallPets, long smallPower) {
        LogUtil.info("GloryRoadBattle.directCheck, large idx:" + largePlayerIdx + ", large power:" + largePower
                + ", small idx:" + smallPlayerIdx + ", small power:" + smallPower);
        double largePowerDirectWinRate = ((largePower * 1.0) / smallPower) * 100;
        if (largePowerDirectWinRate >= GloryRoadConfig.getById(GameConst.CONFIG_ID).getBattledirectwinpowerrate()) {
            EventUtil.gloryRoadBattleResult(largePlayerIdx, smallPlayerIdx, 1, "");
            LogUtil.info("GloryRoadBattle.directCheck, large player win rate:" + largePowerDirectWinRate + " max than direct win rate");
            return true;
        }

        double largeWinRate = GloryRoadUtil.calcLargePowerWinRateWithFix(largePets, largePower, smallPets, smallPower);
        LogUtil.info("GloryRoadBattle.directCheck, largeWinRate:" + largeWinRate);

        int winRate = (int) (largeWinRate * 100);
        int randomNum = RandomUtils.nextInt(100);
        int largeResult = winRate > randomNum ? 1 : 2;
        EventUtil.gloryRoadBattleResult(largePlayerIdx, smallPlayerIdx, largeResult, "");
        LogUtil.info("GloryRoadBattle.directCheck, large idx:" + largePlayerIdx + ", winRate:" + winRate
                + ", random num:" + randomNum + ", opponent:" + smallPlayerIdx);
        return true;
    }

    private List<BattlePetData> getPetBattleData(String playerIdx) {
        List<BattlePetData> battlePetData = teamCache.getInstance().buildBattlePetData(playerIdx,
                TeamTypeEnum.TTE_GloryRoad, BattleSubTypeEnum.BSTE_GloryRoad);
        return battlePetData == null ? Collections.emptyList() : battlePetData;
    }

    private int getEnsureCount() {
        int result = 0;
        if (this.player_1_ManualStatus != null && this.player_1_ManualStatus.getValue()) {
            result++;
        }
        if (this.player_2_ManualStatus != null && this.player_2_ManualStatus.getValue()) {
            result++;
        }
        return result;
    }

    public synchronized RetCodeEnum manualOperate(String playerIdx, boolean manual) {
        if (GlobalTick.getInstance().getCurrentTime() > getWaitEndTime()) {
            return RetCodeEnum.RCE_GloryRoad_EnsureOutOfTime;
        }

        DefaultKeyValue<String, Boolean> playerStatus = getPlayerStatus(playerIdx);
        if (playerStatus == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        playerStatus.setValue(manual);
        this.ensureSet.add(playerIdx);

        LogUtil.info("GloryRoadBattle.manualOperate,change status, playerIdx:" + playerIdx + ", status:" + manual);
        return RetCodeEnum.RCE_Success;
    }

    public String getPlayer_1_Idx() {
        return this.player_1_ManualStatus == null ? null : this.player_1_ManualStatus.getKey();
    }

    public String getPlayer_2_Idx() {
        return this.player_2_ManualStatus == null ? null : this.player_2_ManualStatus.getKey();
    }

    public static GloryRoadBattle createEntity(GloryRoadOpponent opponent, long waitTime) {
        if (opponent == null || opponent.isEmpty()) {
            LogUtil.error("GloryRoadBattle.createEntity failed, params, opponent is empty,waitTime:" + waitTime);
            return null;
        }

        GloryRoadBattle battle = new GloryRoadBattle();
        battle.addOpponent(opponent.getPlayerIdx1());
        battle.addOpponent(opponent.getPlayerIdx2());
        battle.setParentIndex(opponent.getParentIndex());
        battle.setNeedSendApply(waitTime > 0);
        battle.setWaitEndTime(GlobalTick.getInstance().getCurrentTime() + waitTime);

        return battle;
    }

    public DefaultKeyValue<String, Boolean> getPlayerStatus(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        if (this.player_1_ManualStatus != null && Objects.equals(this.player_1_ManualStatus.getKey(), playerIdx)) {
            return this.player_1_ManualStatus;
        }

        if (this.player_2_ManualStatus != null && Objects.equals(this.player_2_ManualStatus.getKey(), playerIdx)) {
            return this.player_2_ManualStatus;
        }
        LogUtil.error("GloryRoadBattle.getPlayerStatus, player:" + playerIdx + " idx is not in cur battle, player1:"
                + this.player_1_ManualStatus + ", player2:" + player_2_ManualStatus);
        return null;
    }

    public boolean containsPlayer(String playerIdx) {
        return StringUtils.isEmpty(playerIdx) || getPlayerStatus(playerIdx) != null;
    }

    public String getEnsurePlayerIdx() {
        if (this.player_1_ManualStatus.getValue()) {
            return this.player_1_ManualStatus.getKey();
        } else {
            return this.player_2_ManualStatus.getKey();
        }
    }

    public String getUnEnsurePlayerIdx() {
        if (this.player_1_ManualStatus.getValue()) {
            return this.player_2_ManualStatus.getKey();
        } else {
            return this.player_1_ManualStatus.getKey();
        }
    }

    public void sendManualApply() {
        if (!this.needSendApply || GlobalTick.getInstance().getCurrentTime() >= this.waitEndTime) {
            return;
        }
        sendPlayerManualApply(getPlayer_1_Idx());
        sendPlayerManualApply(getPlayer_2_Idx());
    }

    public void sendPlayerManualApply(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx) || this.ensureSet.contains(playerIdx)) {
            return;
        }
        Boolean oldStatus = this.playerOnlineStatus.get(playerIdx);
        boolean newStatus = GlobalData.getInstance().checkPlayerOnline(playerIdx);

        //当前在线
        boolean needSend = oldStatus == null && newStatus;
        if (oldStatus != null && oldStatus != newStatus && newStatus) {
            needSend = true;
        }

        if (needSend) {
            sendBattleManualApplyMsg(playerIdx);
        }

        if (oldStatus == null || oldStatus != newStatus) {
            this.playerOnlineStatus.put(playerIdx, newStatus);
        }
    }

    public void onPlayerLogin(String playerIdx) {
//        long currentTime = GlobalTick.getInstance().getCurrentTime();
//        if (currentTime > getWaitEndTime()
//                || this.alreadyEnterBattle
//                || this.ensureSet.contains(playerIdx)
//                || this.needSendApply
//        ) {
//            return;
//        }
//        if (Objects.equals(playerIdx, getPlayer_1_Idx())
//                || Objects.equals(playerIdx, getPlayer_2_Idx())) {
//            sendBattleManualApplyMsg(playerIdx);
//        }
    }

    public void sendBattleManualApplyMsg(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)
                || !GlobalData.getInstance().checkPlayerOnline(playerIdx)) {
            return;
        }
        SC_GloryRoadManualOperateBattleEnsure.Builder builder
                = SC_GloryRoadManualOperateBattleEnsure.newBuilder().setWaitEndTime(this.waitEndTime);
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_GloryRoadManualOperateBattleEnsure_VALUE, builder);
    }

    /**
     * 确认玩家战斗状态,如果在战斗中,修改战斗确认未非确认
     */
    private void ensurePlayerBattleStatus() {
        if (this.player_1_ManualStatus != null) {
            if (BattleManager.getInstance().isInBattle(player_1_ManualStatus.getKey())) {
                LogUtil.info("GloryRoadBattle.ensurePlayerBattleStatus, player:" + getPlayer_1_Idx()
                        + " is in battle, change ensure status false");
                this.player_1_ManualStatus.setValue(false);
            }
        }

        if (this.player_2_ManualStatus != null) {
            if (BattleManager.getInstance().isInBattle(player_2_ManualStatus.getKey())) {
                LogUtil.info("GloryRoadBattle.ensurePlayerBattleStatus, player:" + getPlayer_2_Idx()
                        + " is in battle, change ensure status false");
                this.player_2_ManualStatus.setValue(false);
            }
        }
    }

    /**
     * 当玩家超时结算时会调用此方法重置状态使进入自动结算
     */
    public synchronized void prepareToDirectCheck() {
        this.alreadyEnterBattle = false;
        this.needSendApply = false;
        this.waitEndTime = 0;
        this.ensureSet.clear();

        if (this.player_1_ManualStatus != null) {
            this.player_1_ManualStatus.setValue(false);
        }

        if (this.player_2_ManualStatus != null) {
            this.player_2_ManualStatus.setValue(false);
        }
    }
}
