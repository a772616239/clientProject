package model.battle.pve;

import cfg.CrazyDuelFloor;
import cfg.CrazyDuelFloorObject;
import cfg.MatchArenaConfig;
import common.GameConst;
import common.GlobalData;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import datatool.StringHelper;
import model.battle.AbstractPveBattleController;
import model.battlerecord.dbCache.battlerecordCache;
import model.battlerecord.entity.battlerecordEntity;
import model.crazyDuel.CrazyDuelCache;
import model.crazyDuel.CrazyDuelDataUpdateManager;
import model.crazyDuel.CrazyDuelManager;
import model.crazyDuel.CrazyDuelOpenManager;
import model.crazyDuel.CrazyDuelRobotManager;
import model.crazyDuel.dto.CrazyDuelPlayerPageDB;
import model.crossarena.CrossArenaManager;
import model.crossarena.CrossArenaUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import platform.logs.ReasonManager;
import protocol.Battle;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.CrayzeDuel;
import protocol.CrazyDuelDB;
import protocol.CrossArena;
import protocol.Forward;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.LogUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static protocol.Forward.ForwardMsgIdEnum.FM_SaveBattleRecord_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdatePlayerCrazyScore_VALUE;
import static protocol.TargetSystem.TargetTypeEnum.TTE_CrazyDuel_CompleteBattle;

public class CrazyDuelPveBattleController extends AbstractPveBattleController {

    private String battlePlayerIdx;

    private int battleFloor;

    private int attackScoreChange;

    private int defendScoreChange;

    private int attackPlayerScore;

    private int defendPlayerScore;

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TTE_CrazyDuel_CompleteBattle, 1, 0);
        boolean win = realResult.getWinnerCamp() == 1;

        boolean defeat = win && CrazyDuelManager.getInstance().finishBattle(battleFloor);

        int scoreAddition = CrazyDuelManager.getInstance().queryBattleScoreAddition(getPlayerIdx(),battlePlayerIdx);

        attackPlayerScore = CrazyDuelManager.getInstance().findPlayerScore(getPlayerIdx());

        defendPlayerScore = CrazyDuelManager.getInstance().findPlayerScore(battlePlayerIdx);

        attackScoreChange = (int) (computeScoreChange(true, win, defeat) * (1 + scoreAddition / 1000.0));

        defendScoreChange = computeScoreChange(false, win, defeat);


        CrazyDuelManager.getInstance().updatePlayerBattleFloor(win, getPlayerIdx(), battlePlayerIdx, battleFloor, defeat);

        CrazyDuelDataUpdateManager.getInstance().addUpdateRecord(battlePlayerIdx, getPlayerIdx());

        settlePlayerRankScore(getPlayerIdx(), attackScoreChange);

        settlePlayerRankScore(battlePlayerIdx, defendScoreChange);

        LogUtil.info("crazy duel pve battle settle , win camp:{} ,player:{} before score:{} ,score change:{},battle player:{} before score:{} ,score change:{}",
                realResult.getWinnerCamp(), getPlayerIdx(), attackPlayerScore, attackScoreChange, battlePlayerIdx, defendPlayerScore, defendScoreChange);

    }

    /**
     * 计算积分变化
     *
     * @param attack true:进攻方 false:防守方
     */
    public int computeScoreChange(boolean attack, boolean attackWin, boolean defeat) {

        int temp = (int) Math.round((Math.abs(attackPlayerScore - defendPlayerScore)) * 0.1);

        if (attack) {
            return computeAttackScore(attackWin, defeat, temp);
        }
        return computeDefendScore(attackWin, defeat, temp);
    }

    private int computeDefendScore(boolean attackWin, boolean defeat, int temp) {
        if (!attackWin) {//防守成功不加分,失败扣分
            return 0;
        }
        int score;
        if (defendPlayerScore >= attackPlayerScore) {
            score = Math.max(battleFloor * 5 + temp, 5);
            if (defeat) {
                score = score + 10 * battleFloor + temp;
            }
        } else {
            score = Math.max(battleFloor * 5 - temp, 5);
            if (defeat) {
                score = score + 10 * battleFloor - temp;
            }
        }
        return -score;

    }

    private int computeAttackScore(boolean attackWin, boolean defeat, int temp) {
        if (!attackWin) {
            return 0;
        }
        int score;
        if (attackPlayerScore >= defendPlayerScore) {
            score = Math.max(battleFloor * 10 - temp, 10);
            if (defeat) {
                score = score + 10 * battleFloor - temp;
            }
        } else {
            score = Math.max(battleFloor * 10 + temp, 10);
            if (defeat) {
                score = score + 10 * battleFloor + temp;
            }
        }
        return score;
    }


    private void settlePlayerRankScore(String playerIdx, int scoreChange) {
        int score = CrazyDuelCache.getInstance().incrPlayerScore(playerIdx, scoreChange);

        if (CrazyDuelRobotManager.getInstance().isRobot(playerIdx)) {
            return;
        }
        sendUpdateScore(playerIdx, score);

        //擂台赛当前最高分
        CrossArenaManager.getInstance().savePlayerDBInfo(playerIdx, CrossArena.CrossArenaDBKey.FKDJ_MaxScore, score, CrossArenaUtil.DbChangeRepMax);

        CrazyDuelManager.getInstance().updateRankScore(playerIdx, score);

    }


    private void sendUpdateScore(String playerIdx, int score) {
        String fromSvrIndex = CrazyDuelCache.getInstance().findPlayerFromSvrIndex(playerIdx);
        CrayzeDuel.SC_UpdatePlayerCrazyScore.Builder msg = CrayzeDuel.SC_UpdatePlayerCrazyScore.newBuilder().setScore(score);
        GlobalData.getInstance().forwardMsg(Collections.singletonMap(playerIdx, fromSvrIndex), SC_UpdatePlayerCrazyScore_VALUE, msg.build());
    }


    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_CrazyDuel;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_MatchArena;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_CrazyDuel;
    }

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (enterParams.size() != 2) {
            return false;
        }
        this.battlePlayerIdx = enterParams.get(0);
        this.battleFloor = Integer.parseInt(enterParams.get(1));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        if (!CrazyDuelOpenManager.getInstance().isOpen()) {
            return RetCodeEnum.RCE_Activity_NotOpen;
        }
        RetCodeEnum retCodeEnum;

        if ((retCodeEnum = CrazyDuelManager.getInstance().checkCanBattle(getPlayerIdx(), battlePlayerIdx, battleFloor)) != RetCodeEnum.RCE_Success) {
            return retCodeEnum;
        }

        CrazyDuelDB.CrazyDuelSettingDB playerSetting = CrazyDuelManager.getInstance().findPlayerSetting(battlePlayerIdx);
        if (playerSetting == null) {
            return RetCodeEnum.RSE_ConfigNotExist;
        }

        List<Battle.BattlePetData> battleData = playerSetting.getBattleDataList();

        CrazyDuelPlayerPageDB playerVo = CrazyDuelManager.getInstance().findPagePlayerById(battlePlayerIdx);
        Battle.BattlePlayerInfo.Builder opponentInfo = Battle.BattlePlayerInfo.newBuilder();
        Battle.PlayerBaseInfo.Builder basePlayerInfo = Battle.PlayerBaseInfo.newBuilder();
        basePlayerInfo.setPlayerName(playerVo.getName());
        basePlayerInfo.setAvatar(playerVo.getHeadId());
        basePlayerInfo.setLevel(playerVo.getPlayerLevel());
        opponentInfo.setPlayerInfo(basePlayerInfo);
        opponentInfo.setCamp(2);
        opponentInfo.addAllPetList(battleData);
        opponentInfo.setIsAuto(true);

        addPlayerBattleData(opponentInfo.build());

        setTeamBuff(playerSetting, battleFloor);

        setFightMakeId(MatchArenaConfig.getById(GameConst.CONFIG_ID).getPvefightmakeid());

        return RetCodeEnum.RCE_Success;
    }

    private void setTeamBuff(CrazyDuelDB.CrazyDuelSettingDB playerSetting, int battleFloor) {
        CrayzeDuel.CrazyDuelBuffSetting buffSetting = playerSetting.getBuffSettingMap().get(battleFloor);
        if (buffSetting == null) {
            return;
        }
        if (CollectionUtils.isEmpty(buffSetting.getBuffList())) {
            return;
        }

        Map<Integer, Long> buffCountMap = buffSetting.getBuffList().stream().filter(e -> e > 0).collect(Collectors.groupingBy(a -> a, Collectors.counting()));
        if (MapUtils.isEmpty(buffCountMap)) {
            return;
        }

        Battle.ExtendProperty.Builder extendProperty = Battle.ExtendProperty.newBuilder();
        // debuff或者怪物增强阵营为2
        extendProperty.setCamp(2);
        // 获取buffId，默认设置buff层数为1
        for (Map.Entry<Integer, Long> entry : buffCountMap.entrySet()) {
            extendProperty.addBuffData(Battle.PetBuffData.newBuilder().setBuffCfgId(entry.getKey()).setBuffCount(Math.toIntExact(entry.getValue())));
        }
        addExtendProp(extendProperty.build());
    }


    @Override
    protected void initSuccess() {
    }

    @Override
    public int getPointId() {
        return 0;
    }


    @Override
    public void saveBattlePlayBack(CS_BattleResult battleResult, SC_BattleResult.Builder toClientResult) {
        super.saveBattlePlayBack(battleResult, toClientResult);

        CrazyDuelPlayerPageDB attackPlayerInfo = CrazyDuelCache.getInstance().findPagePlayer(getPlayerIdx());
        CrazyDuelPlayerPageDB battlePlayerInfo = CrazyDuelCache.getInstance().findPagePlayer(battlePlayerIdx);

        CrayzeDuel.CrazyBattleRecord playerRecord = buildRecord(battleResult, attackPlayerInfo, battlePlayerInfo, true);

        CrazyDuelManager.getInstance().saveBattleRecord(getPlayerIdx(), playerRecord);

        CrayzeDuel.CrazyBattleRecord opponentRecord = buildRecord(battleResult, attackPlayerInfo, battlePlayerInfo, false);

        CrazyDuelManager.getInstance().saveBattleRecord(battlePlayerIdx, opponentRecord);

        saveBattleRecordEntityForAnther();
    }

    private void saveBattleRecordEntityForAnther() {
        String fromSvrData = CrazyDuelCache.getInstance().findPlayerFromSvrIndex(battlePlayerIdx);
        int batPlyFromSvrIndex = StringHelper.stringToInt(fromSvrData, 0);
        if (batPlyFromSvrIndex > 0) {
            if (ServerConfig.getInstance().getServer() == batPlyFromSvrIndex) {
                return;
            }
        } else {
            if (ServerConfig.getInstance().getIp().equals(fromSvrData)) {
                return;
            }
        }
        if (batPlyFromSvrIndex <= 0 || ServerConfig.getInstance().getServer() == batPlyFromSvrIndex) {
            return;
        }
        battlerecordEntity recordEntity = battlerecordCache.getByIdx(String.valueOf(getBattleId()));
        if (recordEntity == null) {
            LogUtil.warn("CrazyDuelPveBattleController saveBattleRecordEntityForAnther not find record by battleId:{}", getBattleId());
            return;
        }
        Forward.SaveBattleRecord.Builder msg = Forward.SaveBattleRecord.newBuilder();
        msg.setBattleId(String.valueOf(getBattleId()));
        msg.setVersion(recordEntity.getVersion());
        msg.setData(recordEntity.getServerBattleRecordBuilder().build().toByteString());
        BattleServerManager.getInstance().transferMsgGSToGS(FM_SaveBattleRecord_VALUE, msg.build().toByteString(), ServerConfig.getInstance().getServer());
    }

    private CrayzeDuel.CrazyBattleRecord buildRecord(CS_BattleResult battleResult, CrazyDuelPlayerPageDB attackPlayerInfo, CrazyDuelPlayerPageDB defendPlayerInfo, boolean attackPlayer) {
        int scoreChange = attackPlayer ? attackScoreChange : defendScoreChange;
        int result = attackPlayer ? battleResult.getWinnerCamp() : getOpponentBattleResult(battleResult.getWinnerCamp());
        CrazyDuelPlayerPageDB opponentInfo = attackPlayer ? defendPlayerInfo : attackPlayerInfo;
        long opponentAbility = attackPlayer ? defendPlayerInfo.getAbility() : getAttackPlayerAbility();
        int opponentScore = attackPlayer ? defendPlayerScore : attackPlayerScore;

        CrayzeDuel.CrazyBattleRecord.Builder record = CrayzeDuel.CrazyBattleRecord.newBuilder();
        record.setBattleResult(result);
        record.setOpponentAvatar(opponentInfo.getHeadId());
        record.setOpponentAvatarBorder(opponentInfo.getHeadBorderId());
        record.setOpponentName(opponentInfo.getName());
        record.setOpponentIdx(opponentInfo.getPlayerId());
        record.setBattleRecordId(getBattleId());
        record.setFloor(battleFloor);
        record.setScoreChange(scoreChange);
        record.setOpponentAbility(opponentInfo.getAbility());
        record.setOpponentScore(opponentScore);
        record.setAttack(attackPlayer);
        record.setPlayerAbility(opponentAbility);
        record.setBattleTime(GlobalTick.getInstance().getCurrentTime());
        record.setScore(opponentScore);
        return record.build();
    }

    private long getAttackPlayerAbility() {
        Battle.BattlePlayerInfo battlePlayerInfo = getPlayerBattleData().get(0);
        return battlePlayerInfo.getPetListList().stream().mapToLong(Battle.BattlePetData::getAbility).sum();
    }

    private int getOpponentBattleResult(int winnerCamp) {
        if (winnerCamp == 1) {
            return 2;
        }
        if (winnerCamp == 2) {
            return 1;
        }
        return winnerCamp;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
            return Collections.emptyList();
        }

        CrazyDuelFloorObject cfg = CrazyDuelFloor.getByFloor(battleFloor);
        if (cfg == null) {
            LogUtil.error("playerIdx:{},doBattleRewards CrazyDuelFloor is null by floor:{}", getPlayerIdx(), battleFloor);
            return Collections.emptyList();
        }

        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(cfg.getFightreward());

        RewardManager.getInstance().doRewardByList(getPlayerIdx(), rewards,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrazyDuel, "疯狂对决战斗胜利"), false);

        return rewards;
    }


}
