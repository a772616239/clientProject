package model.battle;

import cfg.*;
import common.GameConst;
import common.GlobalData;
import common.tick.GlobalTick;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import model.battlerecord.dbCache.battlerecordCache;
import model.battlerecord.entity.battlerecordEntity;
import model.crossarena.CrossArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.team.dbCache.teamCache;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager;
import protocol.Battle;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleRemainPet;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.CS_EnterFight;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PlayerBaseInfo;
import protocol.Battle.SC_BattleResult;
import protocol.Battle.SC_BattleRevertData;
import protocol.Battle.SC_EnterFight;
import protocol.Battle.SC_EnterFight.Builder;
import protocol.Battle.SkillBattleDict;
import protocol.BattleMono.CS_FrameData;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.BattleMono.SC_FrameData;
import protocol.BattleRecordDB.DB_ServerBattleRecord;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_MonsterDifficultyInfo;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_BS_RevertBattle;
import protocol.ServerTransfer.PlayerOffline;
import protocol.TargetSystem;
import protocol.TargetSystem.TimeLimitGiftType;
import util.*;

/**
 * @author huhan
 * @date 2020/04/23
 */
@Getter
@Setter
public abstract class AbstractBattleController {
    private String playerIdx;
    private long battleId;
    private int camp;
    private int fightMakeId;
    private long enterBattleTime;
    private long battleTimeOut;
    private long randSeed;
    /**
     * 好友助战令
     */
    private long friendHelpCard;

    /**
     * 玩家数据
     */
    private List<BattlePlayerInfo> playerBattleData = new ArrayList<>();
    /**
     * 战斗参数
     */
    private List<FightParamTypeEnum> fightParams = new ArrayList<>();
    /**
     * 战斗中的怪物剩余血量 包括玩家和
     */
    private List<BattleRemainPet> remainMonsters = new ArrayList<>();
    /**
     * 战斗额外属性
     */
    private List<ExtendProperty> extendProp = new ArrayList<>();

    /**
     * 是否跳过战斗
     */
    private boolean skipBattle;

    /**
     * 自定义参数保存
     */
    private Map<String, String> enterParam = new HashMap<>();

    /**
     * pve字段
     */
    private int curPveFrameIndex;
    private Map<Integer, SC_FrameData.Builder> pveFrameMap = new HashMap<>();
    private SC_EnterFight.Builder pveEnterFightData;

    /**
     * 战斗是否挂起,挂起不tick逻辑, 多场战斗使用
     */
    private boolean hangOn;
    /**
     * 战斗挂起超时,
     */
    private long hangOnExpireTime;

    /**
     * d
     */
    public static final long AVERAGE_HANG_ON_WAIT_TIME = 2 * TimeUtil.MS_IN_A_MIN;

    public void setHangOn(boolean hangOn) {
        if (hangOn) {
            setHangOnExpireTime(GlobalTick.getInstance().getCurrentTime() + AVERAGE_HANG_ON_WAIT_TIME);
        }
        this.hangOn = hangOn;
    }

    /**
     * 初始化战斗相关信息
     *
     * @param enterFight 进入参数
     * @return
     */
    public abstract RetCodeEnum initBattle(CS_EnterFight enterFight);

    /**
     * 结算战斗
     *
     * @param battleId
     * @param winCamp
     */
    public void settleBattle(long battleId, int winCamp) {
        CS_BattleResult result = CS_BattleResult.newBuilder()
                .setBattleId(battleId)
                .setWinnerCamp(winCamp)
                .setPlaybackVersion(GameConfig.getById(GameConst.CONFIG_ID).getFightplaybackversion())
                .build();
        settleBattle(result);
    }

    public void settleBattle(int winCamp) {
        settleBattle(getBattleId(), winCamp);
    }

    /**
     * 战斗结算
     *
     * @param clientResultData 战斗结果
     */
    public void settleBattle(CS_BattleResult clientResultData) {
        SC_BattleResult.Builder builder = SC_BattleResult.newBuilder();
        builder.setFightMakeId(getFightMakeId());
        builder.setBattleSubType(getSubBattleType());
        if (clientResultData == null || clientResultData.getBattleId() == 0) {
            LogUtil.error("settle battle ClientResultData is null,playerId={},battleId={},subtype={}",getPlayerIdx(),getBattleId(),getSubBattleType());
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Battle_NotInBattle));
            GlobalData.getInstance().sendMsg(getPlayerIdx(), MsgIdEnum.SC_BattleResult_VALUE, builder);
            return;
        }
        if (getBattleId() == 0) {
            LogUtil.error("settle battle  battleId is 0,playerId={},subtype={}",getPlayerIdx(),getSubBattleType());
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Battle_DataError));
            GlobalData.getInstance().sendMsg(getPlayerIdx(), MsgIdEnum.SC_BattleResult_VALUE, builder);
            return;
        }
        if (clientResultData.getBattleId() != getBattleId()) {
            LogUtil.error("settle battle battleId diff playerId={},clientSettleId={},battleId={},subtype={}", getPlayerIdx(), clientResultData.getBattleId(), getBattleId(), getSubBattleType());
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Battle_NotInBattle));
            GlobalData.getInstance().sendMsg(getPlayerIdx(), MsgIdEnum.SC_BattleResult_VALUE, builder);
            return;
        }

        //战斗校验
        LogUtil.info("check battle start,playerIdx=" + getPlayerIdx() + ", battleId = " + getBattleId()
                + " battleType =" + getBattleType() + "subType=" + getSubBattleType());
        CS_BattleResult realResult = checkBattle(clientResultData);
        LogUtil.debug("check battle finished");

        beforeSettle(realResult);
        //战斗奖励，
        List<Reward> rewardList = doBattleRewards(realResult);
        if (rewardList != null) {
            builder.addAllRewardList(rewardList);
        }

        builder.setBattleId(realResult.getBattleId());
        builder.setWinnerCamp(realResult.getWinnerCamp());
        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        if (getSubBattleType() == BattleSubTypeEnum.BSTE_MistForest &&
                CrossServerManager.getInstance().getMistForestPlayerServerIndex(getPlayerIdx()) > 0) {
            builder.setIsInMistForest(true);
        }
        builder.setRemainBattle(remainBattle());

        //先tailSettle 然后在发送消息
        tailSettle(realResult, builder.getRewardListList(), builder);

        if (clientResultData.getExtCount() > 0) {
            builder.addAllExt(clientResultData.getExtList());
        }

        GlobalData.getInstance().sendMsg(getPlayerIdx(), MsgIdEnum.SC_BattleResult_VALUE, builder);

        battleLog(realResult.getWinnerCamp(), builder.getRewardListList());

        triggerGift(realResult);

        saveBattlePlayBack(realResult, builder);

        if (remainBattle()) {
            unfinishedClear();
        } else {
            clear();
        }
    }

    private void triggerGift(CS_BattleResult realResult) {
        if (realResult.getWinnerCamp() == 2) {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_BattleFail, 1, 1);
            TimeLimitGiftType giftType = getTimeLimitGiftTypeByBattleType(getSubBattleType());
            if (giftType != null) {
                EventUtil.triggerTimeLimitGift(getPlayerIdx(), giftType, GameConst.TimeLimitGiftDefaultTarget);
            }
        }
    }

    TimeLimitGiftType getTimeLimitGiftTypeByBattleType(BattleSubTypeEnum subBattleType) {
        if (BattleSubTypeEnum.BSTE_Patrol == subBattleType) {
            return TimeLimitGiftType.TLG_Patrol;
        }
        if (BattleSubTypeEnum.BSTE_BreaveChallenge == subBattleType) {
            return TimeLimitGiftType.TLG_BraveChallenge;
        }
        if (BattleSubTypeEnum.BSTE_MainLineCheckPoint == subBattleType ||
                BattleSubTypeEnum.BSTE_EndlessSpire == subBattleType ||
                BattleSubTypeEnum.BSTE_PointCopy == subBattleType ||
                BattleSubTypeEnum.BSTE_Arena == subBattleType ||
                BattleSubTypeEnum.BSTE_BossTower == subBattleType ||
                BattleSubTypeEnum.BSTE_MineFight == subBattleType
        ) {
            return TimeLimitGiftType.TLG_LosingStreak;
        }
        return null;
    }

    /**
     * 是否还剩余战斗
     *
     * @return
     */
    protected boolean remainBattle() {
        return false;
    }

    public void beforeSettle(CS_BattleResult resultData) {
    }

    /**
     * 战斗胜利奖励：胜负是否发放请在方法内处理, 奖励是否发放请在方法内处理
     *
     * @return 返回战斗结束后需要展示给玩家的奖励列表
     */
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
            return null;
        }

        FightMakeObject fightMakeCfg = FightMake.getById(getFightMakeId());
        if (fightMakeCfg != null) {
            return RewardManager.getInstance().doRewardByRewardId(getPlayerIdx(), fightMakeCfg.getRewardid(),
                    ReasonManager.getInstance().borrowReason(getRewardSourceType(), getLogExInfo()), false);
        }
        return null;
    }

    /**
     * 日志额外信息
     * @return
     */
    /**
     * 返回日志额外信息
     *
     * @return
     */
    public abstract String getLogExInfo();


    /**
     * 战斗日志
     *
     * @param winnerCamp
     * @param rewardListList
     */
    protected abstract void battleLog(int winnerCamp, List<Reward> rewardListList);

    /**
     * 战斗结算后的处理，尽量使用抛事件的处理方式
     *
     * @param realResult     结算结果
     * @param rewardListList 战斗奖励
     * @param resultBuilder  此处用于设置需要返回的数据
     */
    protected abstract void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder);

    /**
     * 战斗校验
     *
     * @param resultData
     * @return
     */
    abstract public CS_BattleResult checkBattle(CS_BattleResult resultData);

    /**
     * 初始化战斗时间相关信息
     */
    public void initTime() {
        setEnterBattleTime(GlobalTick.getInstance().getCurrentTime());
        setBattleTimeOut(getTimeOutTime());
    }

    /**
     * 获得超时时间
     *
     * @return
     */
    private long getTimeOutTime() {
        if (GameUtil.isNewbieMap(getFightMakeId())) {
            return GameConst.NewBeeBattleTimeout;
        }
        return GameConst.BattleTimeout;
    }

    /**
     * 超时结算检查，是否已经结束
     *
     * @return true 超时结算, false 未结算
     */
    public boolean timeOutTick() {
        //暂时挂起
        if (isHangOn()) {
            //超过挂起等待时间,直接进入下一场战斗
            if (GlobalTick.getInstance().getCurrentTime() > getHangOnExpireTime()) {
                enterNextBattle();
                Builder enterBuilder = buildEnterBattleBuilder();
                setPveEnterFightData(enterBuilder);

                if (GlobalData.getInstance().checkPlayerOnline(getPlayerIdx())) {
                    GlobalData.getInstance().sendMsg(getPlayerIdx(), MsgIdEnum.SC_EnterFight_VALUE, enterBuilder);
                }
            }
            return false;
        }

        //参数错误结算,回收
        if (battleId == 0 || enterBattleTime <= 0 || battleTimeOut <= 0) {
            LogUtil.info("battle params is null, recycle this controller now, enterTime:" +
                    this.enterBattleTime + ", timeout = " + this.battleTimeOut
                    + ", battleId=" + this.battleId + ",playerIdx = " + this.playerIdx + " battle sub type:" + getSubBattleType());
            return true;
        }

        //敌方阵容和血量为空直接胜利
        if (directVictory()) {
            try {
                settleBattle(getCamp());
            } catch (Exception ex) {
                LogUtil.error("player:{} settle battle error ,battleSubType", getPlayerIdx(), getSubBattleType());
                LogUtil.printStackTrace(ex);
            }
            return true;
        }

        //超时结算
        if (GlobalTick.getInstance().getCurrentTime() - enterBattleTime > battleTimeOut) {
            try {
                timeoutSettle();
            }catch (Exception ex){
                LogUtil.error("player:{} settle battle error ,battleSubType",getPlayerIdx(),getSubBattleType());
                LogUtil.printStackTrace(ex);
            }
            return true;
        }

        return false;
    }

    /**
     * 超时结算
     */
    abstract public void timeoutSettle();

    /**
     * 获取战斗类型
     *
     * @return
     */
    public abstract BattleTypeEnum getBattleType();

    /**
     * 获取战斗子类型
     *
     * @return
     */
    public abstract BattleSubTypeEnum getSubBattleType();

    /**
     * 玩家登陆
     *
     * @param isResume
     */
    public void onPlayerLogin(boolean isResume) {
        boolean updateBattlingState = true;
        if (getBattleId() > 0) {
            if (getBattleType() == BattleTypeEnum.BTE_PVP) {
                BaseNettyClient nettyClient = BattleServerManager.getInstance().getBsClientByPlayerIdx(getPlayerIdx(), true);
                if (nettyClient != null && nettyClient.getState() == 2) {
                    GS_BS_RevertBattle.Builder builder = GS_BS_RevertBattle.newBuilder().setPlayerIdx(getPlayerIdx());
                    nettyClient.send(MsgIdEnum.GS_BS_RevertBattle_VALUE, builder);
                    updateBattlingState = false;
                } else {
                    clear();
                }
            } else if (getBattleType() == BattleTypeEnum.BTE_PVE) {
                if (GameUtil.isNewbieMap(getFightMakeId()) && !isResume) {
                    //原自动结算, 直接超时结算
                    timeoutSettle();
                }
            }
        }
        if (updateBattlingState) {
            sendPveRevertData();
        }
    }

    public void sendPveRevertData() {
        boolean isBattling = getBattleId() > 0;
        SC_BattleRevertData.Builder builder = SC_BattleRevertData.newBuilder();
        builder.setIsBattling(isBattling);
        if (isBattling) {
            if (pveEnterFightData != null) {
                builder.setEnterFightData(pveEnterFightData);
            }
            builder.setFrameIndex(curPveFrameIndex);
            if (pveFrameMap != null) {
                for (SC_FrameData.Builder frameData : pveFrameMap.values()) {
                    builder.addFrameData(frameData);
                }
            }
        }
//        } else if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(getPlayerIdx()) != null) {
//            builder.setIsMistBattling(true);
//        }
        GlobalData.getInstance().sendMsg(getPlayerIdx(), MsgIdEnum.SC_BattleRevertData_VALUE, builder);
    }

    /**
     * 返回奖励来源枚举
     *
     * @return
     */
    public abstract RewardSourceEnum getRewardSourceType();

    /**
     * 添加玩家找到信息
     */
    public RetCodeEnum addPlayerBattleData(String playerIdx, TeamTypeEnum teamType, int camp) {
        return addPlayerBattleData(playerIdx, teamType, camp, false);
    }

    public RetCodeEnum addPlayerBattleData(String playerIdx, TeamTypeEnum teamType, int camp, boolean isAuto) {
        return addPlayerBattleData(playerIdx, teamCache.getInstance().getCurUsedTeamNum(getPlayerIdx(), teamType), camp, isAuto);
    }

    /**
     * 添加玩家找到信息
     */
    public RetCodeEnum addPlayerBattleData(String playerIdx, TeamNumEnum teamNum, int camp) {
        return addPlayerBattleData(playerIdx, teamNum, camp, false);
    }

    /**
     * 添加玩家找到信息
     */
    public RetCodeEnum addPlayerBattleData(String playerIdx, TeamNumEnum teamNum, int camp, boolean isAuto) {
        List<BattlePetData> petDataList = buildBuildBattlePetData(playerIdx, teamNum);
        if (GameUtil.collectionIsEmpty(petDataList)) {
            return RetCodeEnum.RCE_Battle_UsedTeamNotHavePet;
        }

        //检查玩家信息是否正确
        PlayerBaseInfo.Builder playerInfo = BattleUtil.buildPlayerBattleBaseInfo(playerIdx);
        if (playerInfo == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        BattlePlayerInfo.Builder battlePlayerInfo = BattlePlayerInfo.newBuilder();
        battlePlayerInfo.setCamp(camp);
        battlePlayerInfo.addAllPetList(petDataList);
        battlePlayerInfo.setPlayerInfo(playerInfo);
        battlePlayerInfo.setIsAuto(isAuto);
        buildSkillData(playerIdx, teamNum, battlePlayerInfo);
        addPlayerBattleData(battlePlayerInfo.build());

        addExtendGradeBuffProp(playerIdx);


        return RetCodeEnum.RCE_Success;
    }

    private void addExtendGradeBuffProp(String playerIdx) {
        List<Integer> buffs = findPlayerGradeBuff(playerIdx);
        if (CollectionUtils.isEmpty(buffs)) {
            return;
        }

        ExtendProperty.Builder extendProperty = ExtendProperty.newBuilder();
        for (Integer buff : buffs) {
            extendProperty.addBuffData(Battle.PetBuffData.newBuilder().setBuffCfgId(buff).setBuffCount(1));
        }
        addExtendProp(extendProperty.setCamp(1).build());
    }

    private List<Integer> findPlayerGradeBuff(String playerIdx) {
        int gradeLv = CrossArenaManager.getInstance().findPlayerGradeLv(playerIdx);
        CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(gradeLv);
        if (cfg == null) {
            return Collections.emptyList();
        }
        int[] fightbuff = cfg.getFightbuff();
        if (fightbuff.length <= 0) {
            return Collections.emptyList();
        }
        return ArrayUtil.intArrayToList(fightbuff);
    }

    public List<BattlePetData> buildBuildBattlePetData(String playerIdx, TeamNumEnum teamNum) {
        return teamCache.getInstance().buildBattlePetData(playerIdx, teamNum, getSubBattleType());
    }

    protected void buildSkillData(String playerIdx, TeamNumEnum teamNum, BattlePlayerInfo.Builder battlePlayerInfo) {
        List<Integer> skillList = teamCache.getInstance().getPlayerTeamSkillList(playerIdx, teamNum);
        if (CollectionUtils.isEmpty(skillList)) {
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        for (Integer skillId : skillList) {
            battlePlayerInfo.addPlayerSkillIdList(SkillBattleDict.newBuilder()
                    .setSkillId(skillId).setSkillLv(player.getSkillLv(skillId)).build());
        }
    }

    /**
     * 返回该战斗类型使用的小队类型
     *
     * @return
     */
    public abstract TeamTypeEnum getUseTeamType();

    /**
     * 战斗帧处理
     *
     * @param req 帧请求
     */
    public abstract void handleBattleFrameData(CS_FrameData req);

    /**
     * PVP
     * 玩家离线战斗服托管
     * 其他服务器异常，强制结算
     *
     * @param settlePvpBattle
     */
    public void onOwnerLeave(boolean settlePvpBattle) {
        if (getBattleId() > 0) {
            if (getBattleType() == BattleTypeEnum.BTE_PVP) {
                if (settlePvpBattle) {
                    settleBattle(getBattleId(), -1);
                } else {
                    PlayerOffline.Builder builder = PlayerOffline.newBuilder();
                    builder.setPlayerIdx(getPlayerIdx());
                    BattleServerManager.getInstance().sendMsgToBattleServer(getPlayerIdx(), MsgIdEnum.PlayerOffline_VALUE, builder, false);
                }
            }
        }
    }

    /**
     * 是否在战斗中,或者战斗挂起
     */
    public boolean isInBattle() {
        return getBattleId() > 0 || isHangOn();
    }

    /**
     * 进入下一场战斗
     */
    public RetCodeEnum enterNextBattle() {
        return RetCodeEnum.RCE_ErrorParam;
    }

    /**
     * 构建进入战斗消息
     *
     * @return
     */
    public SC_EnterFight.Builder buildEnterBattleBuilder() {
        SC_EnterFight.Builder result = SC_EnterFight.newBuilder();

        if (directVictory()) {
            //敌方没有阵容时,自动结算RetCode返回
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Battle_EnemyPetIsEmpty));
        } else {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        }

        result.setBattleId(getBattleId());
        result.setCamp(getCamp());
        result.setBattleType(getBattleType());
        result.setFightMakeId(getFightMakeId());
        result.setFriendHelpCard(getFriendHelpCard());
        result.setSubType(getSubBattleType());
        result.setRandSeed(getRandSeed());

        if (!GameUtil.collectionIsEmpty(getExtendProp())) {
            result.addAllExtendProp(getExtendProp());
        }
        if (!GameUtil.collectionIsEmpty(getFightParams())) {
            result.addAllFightParams(getFightParams());
        }

        if (!GameUtil.collectionIsEmpty(getRemainMonsters())) {
            result.addAllRemainMonsters(getRemainMonsters());
        }

        if (!GameUtil.collectionIsEmpty(getPlayerBattleData())) {
            result.addAllPlayerInfo(getPlayerBattleData());
        }

        //build怪物难度
        DB_MonsterDifficultyInfo.Builder diff = getMonsterDiff();
        if (diff != null) {
            result.setMonsterDiffLevel(diff.getLevel());
        }

        result.setSkipBattle(this.skipBattle);

        return result;
    }

    private DB_MonsterDifficultyInfo.Builder getMonsterDiff() {
        playerEntity entity = playerCache.getByIdx(getPlayerIdx());
        if (entity == null) {
            return null;
        }
        return entity.getMonsterDiffByFunction(getFunctionEnum());
    }

    public EnumFunction getFunctionEnum() {
        return EnumFunction.NullFuntion;
    }

    public void addPlayerBattleData(BattlePlayerInfo battlePlayerInfo) {
        addAllPlayerBattleData(Collections.singletonList(battlePlayerInfo));
    }

    public void addAllPlayerBattleData(List<BattlePlayerInfo> battlePlayerInfos) {
        if (GameUtil.collectionIsEmpty(battlePlayerInfos)) {
            return;
        }
        if (this.playerBattleData == null) {
            this.playerBattleData = new ArrayList<>();
        }
        this.playerBattleData.addAll(battlePlayerInfos);
    }

    public void putEnterParam(String key, String value) {
        if (StringHelper.isNull(key) || StringHelper.isNull(value)) {
            return;
        }
        if (this.enterParam == null) {
            this.enterParam = new HashMap<>();
        }
        this.enterParam.put(key, value);
    }

    public void addFightParams(FightParamTypeEnum fightParam) {
        if (fightParam == null || fightParam == FightParamTypeEnum.FPTE_Null) {
            return;
        }
        addAllFightParams(Collections.singletonList(fightParam));
    }

    public void addAllFightParams(List<FightParamTypeEnum> fightParamList) {
        if (GameUtil.collectionIsEmpty(fightParamList)) {
            return;
        }

        if (this.fightParams == null) {
            this.fightParams = new ArrayList<>();
        }
        this.fightParams.addAll(fightParamList);
    }


    public void addRemainMonsters(BattleRemainPet remainPet) {
        if (remainPet == null) {
            return;
        }
        addAllRemainMonsters(Collections.singletonList(remainPet));
    }

    public void addAllRemainMonsters(Collection<BattleRemainPet> remainPetList) {
        if (GameUtil.collectionIsEmpty(remainPetList)) {
            return;
        }
        if (this.remainMonsters == null) {
            this.remainMonsters = new ArrayList<>();
        }
        this.remainMonsters.addAll(remainPetList);
    }


    public void addExtendProp(ExtendProperty extendPro) {
        if (extendPro == null) {
            return;
        }
        addAllExtendProp(Collections.singletonList(extendPro));
    }

    public void addAllExtendProp(List<ExtendProperty> extendPropList) {
        if (GameUtil.collectionIsEmpty(extendPropList)) {
            return;
        }
        if (this.extendProp == null) {
            this.extendProp = new ArrayList<>();
        }
        this.extendProp.addAll(extendPropList);
    }

    public void putEnterParam(String key, long value) {
        this.putEnterParam(key, String.valueOf(value));
    }

    public String getEnterParam(String key) {
        if (this.enterParam == null) {
            return "";
        }
        return enterParam.get(key);
    }

    public int getIntEnterParam(String key) {
        return StringHelper.stringToInt(getEnterParam(key), 0);
    }

    public long getLongEnterParam(String key) {
        return GameUtil.stringToLong(getEnterParam(key), 0L);
    }

    public boolean frameDataContain(int frameIndex) {
        if (this.pveFrameMap == null) {
            return false;
        }
        return this.pveFrameMap.containsKey(frameIndex);
    }

    public void putPveFrameMap(SC_FrameData.Builder frameData) {
        if (frameData == null) {
            return;
        }
        if (this.pveFrameMap == null) {
            this.pveFrameMap = new HashMap<>();
        }

        if (this.pveFrameMap.containsKey(frameData.getFrameIndex())) {
            return;
        }

        this.pveFrameMap.put(frameData.getFrameIndex(), frameData);
    }

    public String getCampPlayerIdx(int camp) {
        for (BattlePlayerInfo battleData : playerBattleData) {
            if (battleData.getCamp() == camp) {
                return battleData.getPlayerInfo().getPlayerId();
            }
        }
        return null;
    }

    /**
     * 是否可以直接胜利
     *
     * @return
     */
    abstract protected boolean directVictory();

    public void clear() {
//        playerIdx = null;
        unfinishedClear();
        fightMakeId = 0;
        enterParam.clear();
        extendProp.clear();
    }

    /**
     * 战斗未结束清空，只清空部分数据
     */
    public void unfinishedClear() {
        battleId = 0;
        camp = 0;
        enterBattleTime = 0L;
        battleTimeOut = 0L;
        randSeed = 0L;
        friendHelpCard = 0L;
        playerBattleData.clear();
        fightParams.clear();
        remainMonsters.clear();
//        extendProp.clear();
        curPveFrameIndex = 0;
        pveFrameMap.clear();
        pveEnterFightData = null;
    }

    /**
     * 战斗记录保存
     * 因为frame的来源不同  pvp来源是战斗服务器,pvp时从战斗服务器传下来
     */
    protected abstract void saveBattlePlayBack(CS_BattleResult battleResult, SC_BattleResult.Builder toClientResult);

    public void internalSaveBattleRecord(CS_BattleResult battleResult,
                                         SC_BattleResult.Builder toClientResult,
                                         List<SC_FrameData> frameColl) {
    	if (battleResult == null || StringUtils.isEmpty(battleResult.getPlaybackVersion()) || toClientResult == null) {
            LogUtil.error("AbstractBattleController.saveBattlePlayBack, error param, battleId:" + getBattleId()
                    + "battle result:" + battleResult + ", toClientResult:" + toClientResult);
            return;
        }

        if (directVictory()) {
            LogUtil.info("AbstractBattleController.internalSaveBattleRecord, playerIdx:" + getPlayerIdx()
                    + ", battle type:" + getSubBattleType() + "battleId:" + getBattleId()
                    + ", direct victory, need not save playback");
            return;
        }

        if (!BattleSubTypeConfig.needRecord(getSubBattleType())) {
            return;
        }

        if (this.battleId == 0) {
            LogUtil.error("AbstractBattleController.internalSaveBattleRecord, battle id is zero. battle sub type:" + getSubBattleType());
            return;
        }

        String battleId = String.valueOf(getBattleId());
        if (!battlerecordCache.getInstance().needRecord(battleId)) {
            LogUtil.info("AbstractBattleController.saveBattlePlayBack, battle id :" + battleId + ", is already exist in db");
            return;
        }

        battlerecordEntity newEntity = new battlerecordEntity();
        newEntity.setBattleid(battleId);
        newEntity.setVersion(battleResult.getPlaybackVersion());

        DB_ServerBattleRecord.Builder battleRecordBuilder = DB_ServerBattleRecord.newBuilder();
        SC_EnterFight.Builder enterFightData = getPveEnterFightData();
        if (enterFightData == null) {
            enterFightData = buildEnterBattleBuilder();
        }
        battleRecordBuilder.setEnterFight(enterFightData);

        if (CollectionUtils.isNotEmpty(frameColl)) {
            battleRecordBuilder.addAllFrameData(frameColl);
        }

        battleRecordBuilder.setStatisticData(battleResult.getStatisticData());
        battleRecordBuilder.getStatisticDataBuilder().setBattleId(getBattleId());
        battleRecordBuilder.setBattleResult(toClientResult);
        newEntity.setServerBattleRecordBuilder(battleRecordBuilder);

        newEntity.transformDBData();
        newEntity.putToCache();

        LogUtil.debug("AbstractPveBattleController.saveBattlePlayBack, save battle success, battle Id:" + getBattleId());
    }
}

