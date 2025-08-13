package model.battle.pve;

import cfg.ArenaConfig;
import cfg.ArenaConfigObject;
import cfg.ArenaRobotConfig;
import cfg.ArenaRobotConfigObject;
import cfg.FightMake;
import cfg.FightMakeObject;
import cfg.GameConfig;
import cfg.PlayerLevelConfig;
import cfg.PlayerLevelConfigObject;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.entity.ScoreChange;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import model.arena.ArenaUtil;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.reward.RewardManager;
import model.team.dbCache.teamCache;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import protocol.Activity.EnumRankingType;
import protocol.Arena.ArenaBattlePlayBack;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.Arena.ArenaPlayerSimpleInfo;
import protocol.Arena.ArenaPlayerTeamInfo;
import protocol.Arena.ArenaRecord;
import protocol.Arena.ArenaRecord.Builder;
import protocol.Arena.SC_ArenaBattleResult;
import protocol.ArenaDB.DB_Arena;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PlayerBaseInfo;
import protocol.Battle.PlayerExtDataDict;
import protocol.Battle.PlayerExtDataEnum;
import protocol.Battle.SC_BattleResult;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_ArenaBattleResult;
import protocol.TargetSystem.TargetTypeEnum;
import server.event.Event;
import server.event.EventManager;
import util.EloScoreCompute;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author huhan
 * @date 2020/04/26
 */
@Getter
@Setter
public class ArenaPveBattleController extends AbstractPveBattleController {

    public static final String TARGET_IDX = "targetIdx";

    public static final String FROM_RECORD = "fromRecord";

    public static final String FROM_RECORD_STR = "1";

    /**
     * 当前的战斗序号, 0 -
     */
    private int curIndex = 0;

    private List<BattlePlayerInfo> playerBattleInfo = new ArrayList<>();
    /**
     * 对手的队伍信息,玩家的直接取当前阵容
     */
    private List<BattlePlayerInfo> targetBattleInfo = new ArrayList<>();

    /**
     * 进攻小队，数量 也是战斗局数
     */
    private List<Integer> attackTeam;

    private int winCount;
    private int failedCount;

    private List<DefaultKeyValue<String, Integer>> battleCondition = new ArrayList<>();

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (CollectionUtils.size(enterParams) < 2) {
            return false;
        }
        putEnterParam(TARGET_IDX, enterParams.get(0));
        putEnterParam(FROM_RECORD, enterParams.get(1));
        return true;
    }

    @Override
    public RetCodeEnum initPlayerBattleData() {
        //初始化战斗参数
        arenaEntity entity = arenaCache.getInstance().getEntity(getPlayerIdx());
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        if (!canChallenge(entity)) {
            return RetCodeEnum.RCE_Arena_OpponentCanNotBattle;
        }

        if (!consume(entity)) {
            return RetCodeEnum.RCE_MatieralNotEnough;
        }

        List<Integer> teamList = ArenaUtil.getAttackTeamNum(entity.getDbBuilder().getDan());
        if (CollectionUtils.isEmpty(teamList)) {
            LogUtil.error("arena dan have no attack team num, dan:" + entity.getDbBuilder().getDan());
            return RetCodeEnum.RCE_UnknownError;
        }

        this.attackTeam = teamList;

        int fightMakeId = getPlayerBattleFightMakeId(getPlayerIdx());
        if (fightMakeId == -1) {
            return RetCodeEnum.RCE_UnknownError;
        }
        setFightMakeId(fightMakeId);

        //检查玩家信息是否正确
        PlayerBaseInfo.Builder playerInfo = BattleUtil.buildPlayerBattleBaseInfo(getPlayerIdx());
        PlayerExtDataDict playerExtData = entity.buildArenaSpecialInfo();
        if (playerInfo == null || playerExtData == null) {
            LogUtil.error("build player info or arena special info failed, playerInfo:" + playerInfo + ", extData info:" + playerExtData);
            return RetCodeEnum.RCE_UnknownError;
        }
        for (Integer teamNum : teamList) {
            TeamNumEnum teamNumEnum = TeamNumEnum.forNumber(teamNum);
            List<BattlePetData> petDataList =
                    teamCache.getInstance().buildBattlePetData(getPlayerIdx(), teamNumEnum, getSubBattleType());
            if (GameUtil.collectionIsEmpty(petDataList)) {
                return RetCodeEnum.RCE_Battle_UsedTeamNotHavePet;
            }

            BattlePlayerInfo.Builder battlePlayerInfo = BattlePlayerInfo.newBuilder();
            battlePlayerInfo.setCamp(1);
            battlePlayerInfo.addAllPetList(petDataList);
            battlePlayerInfo.setPlayerInfo(playerInfo);
            battlePlayerInfo.setPlayerExtData(playerExtData);
            battlePlayerInfo.setIsAuto(true);
            buildSkillData(getPlayerIdx(), teamNumEnum, battlePlayerInfo);
            this.playerBattleInfo.add(battlePlayerInfo.build());
        }
        return RetCodeEnum.RCE_Success;
    }


    /**
     * @param playerIdx
     * @return
     */
    private int getPlayerBattleFightMakeId(String playerIdx) {
        int playerLv = PlayerUtil.queryPlayerLv(playerIdx);
        PlayerLevelConfigObject lvCfg = PlayerLevelConfig.getByLevel(playerLv);
        if (lvCfg == null) {
            LogUtil.error("ArenaPveBattleController.getPlayerBattleFightMakeId, lv cfg is not exist, playerLv:" + playerLv);
            return -1;
        }

        int fightMake = lvCfg.getArenafightmake();
        FightMakeObject fightMakeId = FightMake.getById(fightMake);
        if (fightMakeId == null) {
            LogUtil.error("ArenaPveBattleController.getPlayerBattleFightMakeId, fight make is not exist, fight make:"
                    + fightMake + ", playerLv:" + playerLv);
            return -1;
        }
        return fightMake;
    }


    @Override
    protected RetCodeEnum initFightInfo() {
        //初始化战斗参数
        arenaEntity entity = arenaCache.getInstance().getEntity(getPlayerIdx());
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        String targetIdx = getEnterParam(TARGET_IDX);
        if (targetIdx == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (CollectionUtils.isEmpty(attackTeam)) {
            LogUtil.error("attack team num is empty, playerIdx:" + getPlayerIdx() + ", battleId =" + getBattleId());
            return RetCodeEnum.RCE_UnknownError;
        }

        ArenaOpponentTotalInfo opponentInfo = entity.getOpponentInfo(getEnterParam(TARGET_IDX));
        if (opponentInfo == null) {
            LogUtil.error("opponent info is not exist, target idx:" + targetIdx + ",playerIdx:" + getPlayerIdx());
            return RetCodeEnum.RCE_ErrorParam;
        }

        PlayerBaseInfo.Builder targetInfo = PlayerBaseInfo.newBuilder();
        ArenaPlayerSimpleInfo simpleInfo = opponentInfo.getOpponnentInfo().getSimpleInfo();
        targetInfo.setPlayerId(simpleInfo.getPlayerIdx());
        targetInfo.setPlayerName(simpleInfo.getName());
        targetInfo.setLevel(simpleInfo.getLevel());
        targetInfo.setAvatar(simpleInfo.getAvatar());
        targetInfo.setVipLevel(simpleInfo.getVipLv());
        targetInfo.setAvatarBorder(simpleInfo.getAvatarBorder());
        targetInfo.setAvatarBorderRank(simpleInfo.getAvatarBorderRank());
        targetInfo.setTitleId(simpleInfo.getTitleId());

        PlayerExtDataDict.Builder playerExtData = PlayerExtDataDict.newBuilder();
        playerExtData.addKeys(PlayerExtDataEnum.PEDE_ServerIndex);
        playerExtData.addValues(ServerConfig.getInstance().getServer());
        playerExtData.addKeys(PlayerExtDataEnum.PEDE_Arena_Score);
        playerExtData.addValues(simpleInfo.getScore());
        playerExtData.addKeys(PlayerExtDataEnum.PEDE_Arena_Rank);
        playerExtData.addValues(opponentInfo.getOpponnentInfo().getRanking());

        for (Integer teamNum : attackTeam) {
            int definedTeamNum = ArenaUtil.getArenaAttackTeamNumLinkDefinedTeamNum(teamNum);
            ArenaPlayerTeamInfo teamInfo = null;
            List<ArenaPlayerTeamInfo> teamsList = opponentInfo.getTeamsInfoList();
            for (ArenaPlayerTeamInfo arenaPlayerTeamInfo : teamsList) {
                if (definedTeamNum == arenaPlayerTeamInfo.getTeanNumValue()) {
                    teamInfo = arenaPlayerTeamInfo;
                }
            }

            BattlePlayerInfo.Builder battlePlayerInfo = BattlePlayerInfo.newBuilder();
            battlePlayerInfo.setCamp(2);
            battlePlayerInfo.setPlayerInfo(targetInfo);
            if (teamInfo != null) {
                battlePlayerInfo.addAllPetList(teamInfo.getPetsList());
                buildSkillData(getPlayerIdx(), teamInfo.getTeanNum(), battlePlayerInfo);
            }
            battlePlayerInfo.setPlayerExtData(playerExtData);
            battlePlayerInfo.setIsAuto(true);

            this.targetBattleInfo.add(battlePlayerInfo.build());
        }


        //机器人额外属性加成
        ArenaRobotConfigObject robotCfg = ArenaRobotConfig.getById(simpleInfo.getRobotCfgId());
        if (robotCfg != null) {
            ExtendProperty.Builder property = BattleUtil.builderMonsterExtendProperty(2, robotCfg.getExproperty());
            if (property != null) {
                addExtendProp(property.build());
            }
        }

        //初始化机器人属性加成,当前屏蔽
//        if (simpleInfo.getIsRobot()) {
//            MonsterDifficultyObject diffCfg = MonsterDifficulty.getByPlayerIdx(getPlayerIdx());
//            if (diffCfg != null) {
//                FightMakeObject fightCfg = FightMake.getById(diffCfg.getArenarobotexpropertylinkfightmake());
//                if (fightCfg != null) {
//                    ExtendProperty.Builder property = BattleUtil.builderMonsterExtendProperty(2, fightCfg.getMonsterpropertyext());
//                    if (property != null) {
//                        addExtendProp(property.build());
//                    }
//                }
//            }
//        }

        return initFirstBattle();
    }

    public RetCodeEnum initFirstBattle() {
        if (CollectionUtils.isEmpty(playerBattleInfo) || CollectionUtils.isEmpty(targetBattleInfo)) {
            LogUtil.error("player battle info or target battle info is empty");
            return RetCodeEnum.RCE_ErrorParam;
        }

        addPlayerBattleData(playerBattleInfo.get(curIndex));
        addPlayerBattleData(targetBattleInfo.get(curIndex));
        return RetCodeEnum.RCE_Success;
    }


    @Override
    protected void initSuccess() {
        EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TEE_Arena_CumuBattle, 1, 0);
        LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), EnumFunction.Arena));
    }

    @Override
    public int getPointId() {
        return 0;
    }

    /**
     * 判断是否是竞技场最后一场战斗,战斗序号大于等于所有序号
     * 或者胜利场次或者结束场次大于总场次的一半
     *
     * @return
     */
    @Override
    protected boolean remainBattle() {
        if (directVictory()) {
            return false;
        }
        int settleTimes = (int) Math.ceil((getTotalBattleTimes() * 1.0) / 2);
        return curIndex < playerBattleInfo.size()
                && getWinCount() < settleTimes
                && getFailedCount() < settleTimes;
    }

    private int getTotalBattleTimes() {
        return playerBattleInfo == null ? 0 : playerBattleInfo.size();
    }

    /**
     * 返回胜利或者失败
     *
     * @return
     */
    private boolean isWin() {
        return getWinCount() > getFailedCount();
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        if (realResult == null) {
            LogUtil.error("tailSettle failed, playerIdx = " + getPlayerIdx() + ",battleType" + getBattleType()
                    + ", subType" + getSubBattleType() + ", battleId = " + getBattleId());
            return;
        }

        this.battleCondition.add(new DefaultKeyValue<>(String.valueOf(getBattleId()), realResult.getWinnerCamp()));
        //是否还剩余战斗
        resultBuilder.setRemainBattle(remainBattle());

        //如果不是最后一场战斗,挂起战斗,下次进入战斗需要请求进入, 先挂起战斗,提升当前序号
        if (resultBuilder.getRemainBattle()) {
            setHangOn(true);
            setCurIndex(getCurIndex() + 1);
            LogUtil.info("sub battle type:" + getSubBattleType() + ", is not the last battle,skip tail settle and hang On battle");
            return;
        }

        playerEntity player = playerCache.getByIdx(getPlayerIdx());
        arenaEntity entity = arenaCache.getInstance().getEntity(getPlayerIdx());
        if (entity == null || player == null) {
            LogUtil.error("arena entity is not found, playerIdx:" + getPlayerIdx());
            return;
        }

        String opponentIdx = getEnterParam(TARGET_IDX);
        ArenaOpponentTotalInfo opponentInfo = entity.getOpponentInfo(opponentIdx);
        if (opponentInfo == null || opponentInfo.getOpponnentInfo() == null
                || opponentInfo.getOpponnentInfo().getSimpleInfo() == null) {
            LogUtil.error("can not find opponent info, idx：" + opponentIdx + ", playerIdx:" + getPlayerIdx());
            return;
        }

        //计算积分变化
        ScoreChange scoreChange = EloScoreCompute.scoreChange(isWin(), entity.getDbBuilder().getScore(),
                opponentInfo.getOpponnentInfo().getSimpleInfo().getScore());
        LogUtil.info("ArenaPveBattleController.tailSettle,playerIdx:" + player.getIdx() + ",win:" + isWin()
                + ", score change:" + scoreChange.toString());

        //竞技场积分结算
        SC_ArenaBattleResult.Builder arenaScoreChange = SC_ArenaBattleResult.newBuilder();
        arenaScoreChange.setIsWin(isWin());
        arenaScoreChange.setOldScore(entity.getDbBuilder().getScore());
        arenaScoreChange.setNewScore(arenaScoreChange.getOldScore() + scoreChange.getPlayerScoreChange());
        GlobalData.getInstance().sendMsg(getPlayerIdx(), MsgIdEnum.SC_ArenaBattleResult_VALUE, arenaScoreChange);

        int winnerCamp = realResult.getWinnerCamp();
        long currentTime = GlobalTick.getInstance().getCurrentTime();

        Builder playerRecord = ArenaRecord.newBuilder();
        //投降处理成失败
        playerRecord.setBattleResult(winnerCamp == 3 ? 2 : winnerCamp);
        playerRecord.setBattleType(1);
        playerRecord.setBattleTime(currentTime);
        playerRecord.setScoreChange(scoreChange.getPlayerScoreChange());
        ArenaPlayerSimpleInfo opponentSimpleInfo = opponentInfo.getOpponnentInfo().getSimpleInfo();
        playerRecord.setOpponentAvatar(opponentSimpleInfo.getAvatar());
        playerRecord.setOpponentName(opponentSimpleInfo.getName());
        playerRecord.setOpponentLv(opponentSimpleInfo.getLevel());
        playerRecord.setOpponentIdx(opponentSimpleInfo.getPlayerIdx());
        playerRecord.setOpponentServer(opponentSimpleInfo.getServerIndex());
        playerRecord.setOpponentScore(opponentSimpleInfo.getScore());
        playerRecord.setOpponentAbility(opponentSimpleInfo.getFightAbility());
        playerRecord.setOpponentAvatarBorder(opponentSimpleInfo.getAvatarBorder());
        playerRecord.setOpponentTitleId(opponentSimpleInfo.getTitleId());
        playerRecord.addAllPlayBack(getCampPlayBack(1));

        //玩家记录
        Event event = Event.valueOf(EventType.ET_RECORD_ARENA_BATTLE, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(playerRecord.build());
        EventManager.getInstance().dispatchEvent(event);

        //更新对手积分变化
        Builder opponentRecord = ArenaRecord.newBuilder();
        opponentRecord.setBattleResult(getOpponentBattleResult(winnerCamp));
        opponentRecord.setOpponentAvatar(player.getAvatar());
        opponentRecord.setOpponentName(player.getName());
        opponentRecord.setOpponentLv(player.getLevel());
        opponentRecord.setBattleType(2);
        opponentRecord.setBattleTime(currentTime);
        opponentRecord.setScoreChange(scoreChange.getOpponentScoreChange());
        opponentRecord.setOpponentServer(ServerConfig.getInstance().getServer());
        opponentRecord.setOpponentScore(entity.getDbBuilder().getScore());
        opponentRecord.setOpponentIdx(player.getIdx());
        opponentRecord.setOpponentAbility(ArenaUtil.getArenaDefinesBattleTotalAbility(player.getIdx(), entity.getDbBuilder().getDan()));
        opponentRecord.setOpponentAvatarBorder(player.getDb_data().getCurAvatarBorder());
        opponentRecord.setOpponentTitleId(PlayerUtil.queryPlayerTitleId(getPlayerIdx()));
        opponentRecord.addAllPlayBack(getCampPlayBack(2));


        //跨境服务器通知
        GS_CS_ArenaBattleResult.Builder builder = GS_CS_ArenaBattleResult.newBuilder();
        builder.setPlayerIdx(getPlayerIdx());
        builder.setIsDirectUp(opponentInfo.getOpponnentInfo().getDerectUp());
        builder.setOpponentIdx(opponentIdx);
        builder.setOpponentRecord(opponentRecord);
        builder.setScoreChange(scoreChange.getPlayerScoreChange());
        builder.setPlayerWin(isWin());
        if (!CrossServerManager.getInstance().sendMsgToArena(getPlayerIdx(), MsgIdEnum.GS_CS_ArenaBattleResult_VALUE, builder, false)) {
            LogUtil.error("ArenaPveBattleController.tailSettle, can not find available arena server");
        }

        if (scoreChange.getPlayerScoreChange() > 0) {
            int playerChange = scoreChange.getPlayerScoreChange();
            EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TEE_Arena_CumuGainScore
                    , playerChange, 0);

            //更新活动数据
            RankingManager.getInstance().addActivityRankingScore(getPlayerIdx(), EnumRankingType.ERT_ArenaGainScore, playerChange);
        }
    }



    @Override
    public void beforeSettle(CS_BattleResult resultData) {
        if (resultData == null) {
            return;
        }
        addBattleCondition(resultData.getWinnerCamp());
    }

    /**
     * 添加战斗结果
     *
     * @param winnerCamp
     */
    private void addBattleCondition(int winnerCamp) {
        if (winnerCamp == getCamp()) {
            setWinCount(getWinCount() + 1);
        } else {
            setFailedCount(getFailedCount() + 1);
        }
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        if (remainBattle()) {
            return null;
        }

        int rewardId;
        if (isWin()) {
            rewardId = ArenaConfig.getById(GameConst.CONFIG_ID).getVictoryreward();
        } else {
            rewardId = ArenaConfig.getById(GameConst.CONFIG_ID).getFailedreward();
        }

        return RewardManager.getInstance().doRewardByRewardId(getPlayerIdx(), rewardId,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Arena), false);
    }

    @Override
    public RetCodeEnum enterNextBattle() {
        if (!remainBattle()) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        initCommon();
        if (getCurIndex() >= playerBattleInfo.size() || getCurIndex() >= targetBattleInfo.size()) {
            LogUtil.error("this battle have not remain battle, subType:" + getSubBattleType());
            return RetCodeEnum.RCE_ErrorParam;
        }
        addPlayerBattleData(playerBattleInfo.get(getCurIndex()));
        addPlayerBattleData(targetBattleInfo.get(getCurIndex()));
        initTime();

        //取消挂起
        setHangOn(false);
        return RetCodeEnum.RCE_Success;
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_Arena;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_Arena;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Arena;
    }

    public void clearSub() {
        curIndex = 0;
        playerBattleInfo.clear();
        targetBattleInfo.clear();
        attackTeam.clear();
        winCount = 0;
        failedCount = 0;
        battleCondition.clear();
    }

    @Override
    public void clear() {
        super.clear();
        clearSub();
    }

    @Override
    public boolean checkSpeedUp(CS_BattleResult clientResult, long realBattleTime) {
        playerEntity owner = playerCache.getByIdx(getPlayerIdx());
        if (owner == null) {
            return false;
        }
        if (owner.getVip() >= GameConfig.getById(GameConst.CONFIG_ID).getArenaskipviplv()) {
            return true;
        }
        return super.checkSpeedUp(clientResult, realBattleTime);
    }

    @Override
    public boolean checkRealResultSpeedUp(CS_BattleResult realResult, long realBattleTime) {
        playerEntity owner = playerCache.getByIdx(getPlayerIdx());
        if (owner == null) {
            return false;
        }
        if (owner.getVip() >= GameConfig.getById(GameConst.CONFIG_ID).getArenaskipviplv()) {
            return true;
        }
        return super.checkRealResultSpeedUp(realResult, realBattleTime);
    }

    @Override
    public boolean checkFightPower() {
        long ownerPower = getFightPower(getCamp());
        long needFightPower = getFightPower(2); // 镜像战斗力
        return needFightPower > 0 && ownerPower > needFightPower + needFightPower * 200 / 1000;
    }

    @Override
    protected boolean directVictory() {
        boolean totalEmpty = true;
        for (BattlePlayerInfo playerInfo : targetBattleInfo) {
            if (playerInfo.getPetListCount() > 0) {
                totalEmpty = false;
                break;
            }
        }
        return totalEmpty;
    }

    private boolean canChallenge(arenaEntity entity) {
        if (entity == null) {
            return false;
        }

        String targetIdx = getEnterParam(TARGET_IDX);
        String fromRecord = getEnterParam(FROM_RECORD);
        if (Objects.equals(FROM_RECORD_STR, fromRecord)) {
            return entity.existDefendAndFailRecord(targetIdx);
        } else {
            return entity.existInChallengeListAndUnBeat(targetIdx);
        }
    }

    /**
     * 消耗挑战或者免费道具
     *
     * @return
     */
    public boolean consume(arenaEntity entity) {
        if (entity == null) {
            return false;
        }

        boolean free = SyncExecuteFunction.executeFunction(entity, e -> {
            DB_Arena.Builder builder = entity.getDbBuilder();
            if (builder == null) {
                return false;
            }
            if (builder.getTodayFreeChallengeTimes() >= ArenaConfig.getById(GameConst.CONFIG_ID).getDailyfreetimes()) {
                return false;
            }

            builder.setTodayFreeChallengeTimes(builder.getTodayFreeChallengeTimes() + 1);
            return true;
        });

        if (free) {
            return true;
        }

        Consume consume = ConsumeUtil.parseConsume(ArenaConfig.getById(GameConst.CONFIG_ID).getChallengeconsume());
        if (consume == null) {
            return false;
        }

        return ConsumeManager.getInstance().consumeMaterial(entity.getPlayeridx(), consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Arena));
    }

    public List<ArenaBattlePlayBack> getCampPlayBack(int camp) {
        if (directVictory()) {
            LogUtil.info("model.battle.pve.ArenaPveBattleController.getCampPlayBack,playerIdx:" + getPlayerIdx()
                    + "battleId:" + getBattleId() + ", direct victory, need not save playback");
            return Collections.emptyList();
        }
        List<ArenaBattlePlayBack> result = new ArrayList<>();
        for (DefaultKeyValue<String, Integer> condition : battleCondition) {
            ArenaBattlePlayBack.Builder playBackBuilder = ArenaBattlePlayBack.newBuilder()
                    .setLinkBattleId(condition.getKey())
                    .setBattleResult(condition.getValue());

            if (camp == 2) {
                playBackBuilder.setBattleResult(getOpponentBattleResult(condition.getValue()));
            }

            result.add(playBackBuilder.build());
        }

        return result;
    }

    public int getOpponentBattleResult(int playerBattleResult) {
        return playerBattleResult == 1 ? 2 : playerBattleResult == 2 ? 1 : playerBattleResult == 3 ? 1 : playerBattleResult;
    }
}

