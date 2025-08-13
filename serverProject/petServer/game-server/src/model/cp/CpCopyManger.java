package model.cp;

import cfg.CpTeamCfg;
import cfg.CpTeamFloorCfg;
import cfg.CpTeamRewardCfg;
import cfg.CpTeamRewardCfgObject;
import cfg.CrossArenaLvCfg;
import cfg.CrossArenaLvCfgObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.GameConst;
import common.GlobalData;
import common.JedisUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.consume.ConsumeManager;
import model.cp.broadcast.CpCopyUpdate;
import model.cp.entity.*;
import model.crossarena.CrossArenaHonorManager;
import model.crossarena.CrossArenaManager;
import model.crossarena.CrossArenaUtil;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.team.dbCache.teamCache;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import protocol.*;
import protocol.Battle.BattlePetData;
import protocol.PetMessage.Pet;
import protocol.PrepareWar.TeamNumEnum;
import server.handler.cp.CpFunctionUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_UpCpCopyPlayTimes_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpCpCopyRevive_VALUE;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_CopyNotExists;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_NotMatchCondition;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_PointAlreadyExplore;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_PointCantReach;
import static protocol.RetCodeId.RetCodeEnum.RCE_CP_RepeatClaim;
import static protocol.RetCodeId.RetCodeEnum.RCE_ErrorParam;
import static protocol.RetCodeId.RetCodeEnum.RCE_Success;

@Slf4j
public class CpCopyManger {
    @Getter
    private static final CpCopyManger instance = new CpCopyManger();

    private final CpTeamCache cache = CpTeamCache.getInstance();


    public CpFunction.SC_TriggerCpEvent.Builder triggerEvent(String playerIdx, int pointId) {
        CpFunction.SC_TriggerCpEvent.Builder msg = CpFunction.SC_TriggerCpEvent.newBuilder();

        String mapId = CpTeamCache.getInstance().findPlayerCopyMapId(playerIdx);

        if (StringUtils.isEmpty(mapId)) {
            msg.setRetCode(GameUtil.buildRetCode(RCE_Success));
            return msg;
        }
        JedisUtil.syncExecBooleanSupplier(getCopyUpdateRedisKey(mapId), () -> {
            CpCopyMap mapData = findCopyMapData(mapId);
            if (mapData == null) {
                msg.setRetCode(GameUtil.buildRetCode(RCE_CP_CopyNotExists));
                return false;
            }
            CpTeamCopyPlayerProgress progress = mapData.getProgress(playerIdx);
            if (progress == null) {
                msg.setRetCode(GameUtil.buildRetCode(RCE_CP_CopyNotExists));
                return false;
            }
            if (progress.isFinish()) {
                msg.setRetCode(GameUtil.buildRetCode(RCE_CP_CopyNotExists));
                return false;
            }
            int floor = CpFunctionUtil.queryPointFloor(pointId);
            if (floor != progress.getFloor() + 1) {
                msg.setRetCode(GameUtil.buildRetCode(RCE_CP_PointCantReach));
                return false;
            }

            CpCopyMapFloor cpCopyMapFloor = mapData.getFloors().get(floor);

            CpCopyMapPoint point = cpCopyMapFloor.getPoints().get(pointId);
            if (point == null) {
                msg.setRetCode(GameUtil.buildRetCode(RCE_CP_PointCantReach));
                return false;
            }
            if (!StringUtils.isEmpty(point.getPlayerIdx())) {
                msg.setRetCode(GameUtil.buildRetCode(RCE_CP_PointAlreadyExplore));
                return false;
            }
            if (GameConst.CpCopyMapPointType.Random.getCode() != point.getPointType()) {
                msg.setRetCode(GameUtil.buildRetCode(RCE_CP_PointAlreadyExplore));
                return false;
            }

            handlerPointEvent(msg, playerIdx, mapData, progress, point);
            progress.setFloor(floor);
            if (playerFinishCopy(floor)) {
                progress.setSuccess(true);
                progress.setFinish(true);
            }
            CpBroadcastManager.getInstance().broadcastCopyUpdate(new CpCopyUpdate(point, progress, mapData.getMembers()));
            updateRobotPoint(mapData, point);

            CpTeamCache.getInstance().saveCopyMap(mapData);
           // trySettleCopy(mapData);
            msg.setRetCode(GameUtil.buildRetCode(RCE_Success));
            return true;
        });

        return msg;
    }

    public String getCopyUpdateRedisKey(String mapId) {
        return CpRedisKey.CpCopyUpdate + mapId;
    }

    public void playerLeaveCopy(String playerIdx, CpCopyMap mapData, boolean win) {
        playerLeaveCopy(playerIdx,mapData,win,0);
    }

    public void playerLeaveCopy(String playerIdx, CpCopyMap mapData, boolean win, int leaveType) {
        if (!CpFunctionUtil.isRobot(playerIdx)) {
            LogUtil.info("cp player leave copy,player:{},mapId:{},win:{},leaveType", playerIdx, mapData.getMapId(), win,leaveType);
        }
        mapData.getProgress(playerIdx).setLeave(true);
        trySettleCopy(mapData);
        CpBroadcastManager.getInstance().playerLeaveCopy(playerIdx, mapData.getMembers(), win,leaveType);
        mapData.removePlayingPlayerIdx(playerIdx);
        CpCopyManger.getInstance().removePlayerCopyMap(playerIdx);
        CpTeamCache.getInstance().removePlayerTeamMap(playerIdx);
        CpTeamCache.getInstance().removeCopyPlayerLeaveTime(playerIdx);
        resetPlayerUploadTeamStatus(playerIdx);
        if (CollectionUtils.isEmpty(mapData.getOnPlayPlayer())) {
            CpTeamManger.getInstance().removeTeamByAllPlayerLeave(mapData.getTeamId());
        }
    }

    private void resetPlayerUploadTeamStatus(String playerIdx) {
        if (CpFunctionUtil.isRobot(playerIdx)) {
            return;
        }
        CpTeamMember playerInfo = CpTeamManger.getInstance().findPlayerInfo(playerIdx);
        if (playerInfo == null) {
            return;
        }
        playerInfo.setUploadTeam(false);
        CpTeamCache.getInstance().savePlayerInfo(playerIdx, playerInfo);
    }

    private boolean playerFinishCopy(int floor) {
        return floor >= CpTeamFloorCfg.getInstance().getMaxFloor();
    }

    private void updateRobotPoint(CpCopyMap mapData, CpCopyMapPoint point) {
        for (String robotId : CpFunctionUtil.findRobotIds(mapData.getMembers())) {
            CpTeamCopyPlayerProgress robotProgress = mapData.getProgress(robotId);
            updateRobotPoint(mapData, point.getId(), robotProgress);
        }
    }


    public int queryCpCopyRemainTimes(String playerIdx) {
        return CpTeamCfg.getWeeklyFreePlayTimes() - cache.findPlayerCopyPlayTimes(playerIdx) + cache.queryPlayerBuyPlayerTimes(playerIdx);
    }

    public void useCopyPlayTimes(String playerIdx) {
        if (CpFunctionUtil.isRobot(playerIdx)) {
            return;
        }
        int times = CpTeamCache.getInstance().incrPlayerCopyPlayTimes(playerIdx);

        CpFunction.SC_UpCpCopyPlayTimes.Builder msg = CpFunction.SC_UpCpCopyPlayTimes.newBuilder();
        msg.setTimes(times);
        GlobalData.getInstance().sendMsg(playerIdx, SC_UpCpCopyPlayTimes_VALUE, msg);

        EventUtil.triggerUpdateCrossArenaWeeklyTask(playerIdx, CrossArena.CrossArenaGradeType.GRADE_CP_Join,1);

        CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerIdx, CrossArenaUtil.HR_TEAM_JION, 1);
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_CrossArena_TEAM, 1, 0);
        LogService.getInstance().submit(new GamePlayLog(playerIdx, Common.EnumFunction.LtCp));
    }

    private boolean canSettleCopy(CpCopyMap mapData) {
        if (mapData.isApplySettle()){
            return false;
        }
        for (String member : mapData.getMembers()) {
            if (CpFunctionUtil.isRobot(member)) {
                continue;
            }
            if (!mapData.getProgress(member).isLeave()) {
                return false;
            }
        }
        return true;
    }

    public void trySettleCopy(CpCopyMap mapData) {
        if (!canSettleCopy(mapData)) {
            return;
        }
        mapData.setApplySettle(true);
        //所有玩家都通关
        if (allPass(mapData)) {
            for (String playerId : mapData.getInitRealPlayerIds()) {
                CrossArenaManager.getInstance().savePlayerDBInfo(playerId, CrossArena.CrossArenaDBKey.ZD_PASSNUM, 1, CrossArenaUtil.DbChangeAdd);
                CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_TEAM_PASS, 1);
            }
        }
        CpBroadcastManager.getInstance().broadcastCopySettle(mapData);
    }

    private boolean allPass(CpCopyMap mapData) {
        if (mapData == null) {
            return false;
        }
        return mapData.getProgress().values().stream().allMatch(e -> CpFunctionUtil.isRobot(e.getPlayerIdx()) || e.isSuccess());
    }


    private void handlerPointEvent(CpFunction.SC_TriggerCpEvent.Builder msg, String playerIdx, CpCopyMap
            mapData, CpTeamCopyPlayerProgress progress, CpCopyMapPoint point) {
        int eventType = CpTeamCfg.randomEvent(point.getDifficulty(),CpFunctionUtil.queryPointFloor(point.getId()));
        CpFunction.CpFunctionEvent event = CpFunction.CpFunctionEvent.forNumber(eventType);
        if (event == null) {
            LogUtil.error("CpCopyManager handlerPointEvent error CpFunctionEvent parse is null by event:{}", eventType);
            return;
        }
        LogUtil.info("player:{} player cp team function,pass point:{} trigger event:{}", playerIdx, point.getId(), event);
        progress.addPassPoint(point.getId());
        progress.setCurPoint(point.getId());
        switch (event) {
            case CFE_Buff:
                int buffId = handlerBuffEvent(mapData.getProgress(playerIdx));
                msg.setBuffId(buffId);
                break;
            case CFE_Treasure:
                handlerTreasureEvent(playerIdx);
                break;
            case CFE_LuckyStar:
                handlerLuckyStarEvent(playerIdx, progress);
                break;
            case CFE_DoubleReward:
                handlerDoubleRewardEvent(progress);
                break;
            default:
                LogUtil.error("CpTeamCfg randomEvent error,eventType:{}", eventType);

        }
        msg.setEvent(event);
        point.setPointType(eventType);
    }

    private void handlerDoubleRewardEvent(CpTeamCopyPlayerProgress progress) {
        progress.setDoubleStarReward(true);
    }

    private void handlerLuckyStarEvent(String playerIdx, CpTeamCopyPlayerProgress progress) {
        Common.Reward reward = CpTeamCfg.getLuckyStarReward();
        if (progress.isDoubleStarReward()) {
            reward = RewardUtil.multiReward(reward, 2);
            progress.setDoubleStarReward(false);
        }

        RewardManager.getInstance().doReward(playerIdx, reward, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_LT_CP), true);

    }

    private void handlerTreasureEvent(String playerIdx) {
        Common.Reward reward = CpTeamCfg.randomTresure();
        RewardManager.getInstance().doReward(playerIdx, reward, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_LT_CP), true);
    }

    private int handlerBuffEvent(CpTeamCopyPlayerProgress progress) {
        int buffId = CpTeamCfg.randomBuff();
        progress.addBuffId(buffId);
        return buffId;
    }

    public CpCopyMap findCopyMapData(String mapId) {
        return CpTeamCache.getInstance().loadCopyMapInfo(mapId);
    }

    public void sendCopyInit(String playerIdx) {
        CpFunction.SC_CpTeamFunctionInit.Builder msg = CpFunction.SC_CpTeamFunctionInit.newBuilder();
        CpTeamMember playerInfo = CpTeamManger.getInstance().findPlayerInfo(playerIdx);
        msg.setNeedClientUpdateTeam(isNeedClientUpdateTeam(playerIdx, playerInfo));
        msg.setInCpTeamCopy(CpTeamManger.getInstance().playerInCpCopy(playerIdx));
        msg.setBuyGamePlayTimes(cache.queryPlayerBuyPlayerTimes(playerIdx));
        msg.setPlayTimes(cache.findPlayerCopyPlayTimes(playerIdx));
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CpTeamFunctionInit_VALUE, msg);


    }

    private boolean isNeedClientUpdateTeam(String playerIdx, CpTeamMember playerInfo) {
        if (playerInfo == null) {
            return true;
        }
        List<String> teamPetIdxList = teamCache.getInstance().getTeamPetIdxList(playerIdx, TeamNumEnum.TNE_LtCP_1);

        petEntity entity = petCache.getInstance().getEntityByPlayer(playerIdx);

        if (entity == null) {
            return false;
        }

        if (CollectionUtils.isEmpty(teamPetIdxList)) {
            return true;
        }
        for (BattlePetData petData : playerInfo.getPetData()) {
            Pet pet = entity.getPetById(petData.getPetId());
            if (pet == null) {
                return true;
            }
            if (pet.getPetBookId() != petData.getPetCfgId()) {
                return true;
            }
            if (!teamPetIdxList.contains(petData.getPetId())) {
                return true;
            }
        }
        return false;
    }

    public CpCopyMap findMapDataByPlayerId(String playerIdx) {

        String mapId = CpTeamCache.getInstance().findPlayerCopyMapId(playerIdx);
        CpCopyMap copyMapData = null;
        if (!StringUtils.isEmpty(mapId)) {
            copyMapData = findCopyMapData(mapId);
        }
        if (copyMapData == null) {
            return CpTeamManger.getInstance().checkAndCreateMap(playerIdx);
        }
        return copyMapData;
    }

    public void removePlayerCopyMap(String playerIdx) {
        CpTeamCache.removeCopyMap(playerIdx);
    }

    public Map<Integer,Integer> queryPlayerBattleBuff(String playerIdx) {
        CpCopyMap mapData = CpTeamCache.getInstance().findPlayerCopyMapInfo(playerIdx);
        if (mapData == null) {
            return Collections.emptyMap();
        }
        CpTeamCopyPlayerProgress progress = mapData.getProgress(playerIdx);
        if (progress == null) {
            return Collections.emptyMap();
        }
        return progress.getBattleBuff();
    }


    public void settleRobot(CpCopyMap mapData, int playerPointId, boolean playerWin) {
        List<String> robotIds = CpFunctionUtil.findRobotIds(mapData.getMembers());
        if (CollectionUtils.isEmpty(robotIds)) {
            return;
        }
        for (String robotId : robotIds) {
            CpTeamCopyPlayerProgress robotProgress = mapData.getProgress(robotId);
            if (playerWin) {
                updateRobotPoint(mapData, playerPointId, robotProgress);
            }
        }

    }

    private boolean updateRobotPoint(CpCopyMap mapData, int playerPointId, CpTeamCopyPlayerProgress robotProgress) {
        if (CpFunctionUtil.queryPointFloor(robotProgress.getCurPoint()) >= CpFunctionUtil.queryPointFloor(playerPointId)) {
            return true;
        }

        int robotNextPoint = randomRobotPoint(playerPointId, robotProgress, mapData);
        if (robotNextPoint == -1) {
            return true;
        }
        robotProgress.addPassPoint(robotNextPoint);
        robotProgress.setCurPoint(robotNextPoint);
        CpCopyMapPoint point = mapData.queryPointById(robotNextPoint);
        if (point == null) {
            return true;
        }
        if (point.getPointType() == GameConst.CpCopyMapPointType.Random.getCode()) {
            int eventType = CpTeamCfg.randomEvent(point.getDifficulty(),CpFunctionUtil.queryPointFloor(point.getId()));
            point.setPointType(eventType);
        } else {
            robotProgress.addStarScore(CpTeamCfg.queryDiffScore(point.getDifficulty()));
        }
        CpBroadcastManager.getInstance().broadcastCopyUpdate(new CpCopyUpdate(point, robotProgress, mapData.getMembers()));
        return false;
    }

    private int randomRobotPoint(int playerPointId, CpTeamCopyPlayerProgress progress, CpCopyMap mapData) {
        int robotFloor = CpFunctionUtil.queryPointFloor(progress.getCurPoint());
        int playerFloor = CpFunctionUtil.queryPointFloor(playerPointId);
        if (robotFloor >= playerFloor) {
            return -1;
        }

        if (CpFunctionUtil.isBattleFloor(robotFloor)) {
            int curPoint = progress.getCurPoint();
            if (curPoint <= 0) {
                return -1;
            }
            return CpFunctionUtil.getNextFloorPoint(curPoint);
        }


        return randomRobotBattlePoint(mapData, playerFloor);
    }

    private int randomRobotBattlePoint(CpCopyMap mapData, int playerFloor) {
        List<Integer> points = new ArrayList<>();

        for (CpTeamCopyPlayerProgress progress : mapData.getProgress().values()) {
            points.addAll(progress.getPassPointIds());
        }
        int result;
        for (int i = 0; i < 5; i++) {
            result = CpFunctionUtil.generatePointId(playerFloor, i);
            if (!points.contains(result)) {
                return result;
            }
        }
        return -1;
    }

    public RetCodeId.RetCodeEnum playerRevive(String playerIdx, boolean pay) {

        CpCopyMap mapData = findMapDataByPlayerId(playerIdx);
        if (mapData == null) {
            return RCE_CP_CopyNotExists;
        }
        CpTeamCopyPlayerProgress progress = mapData.getProgress(playerIdx);
        if (progress == null) {
            return RCE_CP_CopyNotExists;
        }
        //免费复活
        CpDailyData playerDailyData = findPlayerDailyData(playerIdx);
        RetCodeId.RetCodeEnum check = reviveCheckAndConsume(playerIdx, pay, playerDailyData);

        if (check != RCE_Success) {
            return check;
        }
        progress.setFinish(false);

        updateAndSavePlayerDailyData(playerIdx, pay, playerDailyData);

        mapData.updatePlayerState(playerIdx, CpFunction.CpCopyPlayerState.CCPS_Survive);

        cache.saveCopyMap(mapData);

        CpBroadcastManager.getInstance().broadcastPlayerState(playerIdx,
                mapData.getOnPlayPlayer(), CpFunction.CpCopyPlayerState.CCPS_Survive);

        CpBroadcastManager.getInstance().broadcastPlayerReviveTimes(playerIdx,
                mapData.getOnPlayPlayer());


        return RCE_Success;

    }

    private void updateAndSavePlayerDailyData(String playerIdx, boolean pay, CpDailyData playerDailyData) {
        if (pay) {
            playerDailyData.setBuyReviveNum(playerDailyData.getBuyReviveNum() + 1);
        } else {
            playerDailyData.setFreeReviveNum(playerDailyData.getFreeReviveNum() + 1);
        }
        cache.savePlayerDailyData(playerIdx, playerDailyData);

        sendUpdatePlayerCopyReviveTime(playerIdx, playerDailyData);
    }

    private void sendUpdatePlayerCopyReviveTime(String playerIdx, CpDailyData playerDailyData) {
        CpFunction.SC_UpCpCopyRevive.Builder msg = CpFunction.SC_UpCpCopyRevive.newBuilder();

        msg.setFreeReviveTimes(playerDailyData.getFreeReviveNum());

        msg.setBuyReviveTimes(playerDailyData.getBuyReviveNum());

        GlobalData.getInstance().sendMsg(playerIdx, SC_UpCpCopyRevive_VALUE, msg);

    }

    public CpDailyData findPlayerDailyData(String playerIdx) {
        return cache.findPlayerDailyData(playerIdx);
    }


    private RetCodeId.RetCodeEnum reviveCheckAndConsume(String playerIdx, boolean pay, CpDailyData playerDailyData) {
        if (pay) {
            if (!canBuyRevive(playerIdx, playerDailyData)) {
                return RetCodeId.RetCodeEnum.RCE_CP_ReviveTimeUseOut;
            }
            if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, CpTeamCfg.getBuyReviveConsume()
                    , ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_LT_CP))) {
                return RetCodeId.RetCodeEnum.RCE_Player_CurrencysNotEnought;
            }
            return RCE_Success;

        }
        if (!canFreeRevive(playerIdx, playerDailyData)) {
            return RetCodeId.RetCodeEnum.RCE_CP_ReviveTimeUseOut;
        }

        return RCE_Success;
    }

    /**
     * 玩家可否复活
     *
     * @param playerIdx
     * @return
     */
    public boolean canRevive(String playerIdx) {
        CpDailyData data = cache.findPlayerDailyData(playerIdx);
        return canFreeRevive(playerIdx, data) || canBuyRevive(playerIdx, data);
    }

    private boolean canBuyRevive(String playerIdx, CpDailyData data) {
        int playerGradeLv = CrossArenaManager.getInstance().findPlayerGradeLv(playerIdx);
        CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(playerGradeLv);
        if (cfg == null) {
            return false;
        }
        return queryBuyReviveTimes(playerIdx) - data.getBuyReviveNum() > 0;
    }

    /**
     * 查询玩家复活次数
     *
     * @param playerIdx
     * @return
     */
    public int queryPlayerReviveTime(String playerIdx) {
        CpDailyData playerDailyData = cache.findPlayerDailyData(playerIdx);
        if (playerDailyData == null) {
            return 0;
        }
        return playerDailyData.getBuyReviveNum() + playerDailyData.getFreeReviveNum();
    }

    /**
     * 能否免费复活
     *
     * @param playerIdx
     * @param data
     * @return
     */
    private boolean canFreeRevive(String playerIdx, CpDailyData data) {
        return queryFreeReviveLimit(playerIdx) - data.getFreeReviveNum() > 0;
    }

    /**
     * 查询购买次数复活上限
     * @param playerIdx
     * @return
     */
    private int queryBuyReviveTimes(String playerIdx) {
        int vipLv = PlayerUtil.queryPlayerVipLv(playerIdx);
        VIPConfigObject cfg = VIPConfig.getById(vipLv);
        if (cfg == null) {
            return 0;
        }
        return cfg.getCpdailyrevive();
    }


    /**
     * 查询免费复活上限
     *
     * @param playerIdx
     * @return
     */
    private int queryFreeReviveLimit(String playerIdx) {
        int playerGradeLv = CrossArenaManager.getInstance().findPlayerGradeLv(playerIdx);
        CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(playerGradeLv);
        if (cfg == null) {
            return 0;
        }
        return cfg.getCprevivetimes();
    }

    public RetCodeId.RetCodeEnum claimCpCopyReward(String playerIdx, int rewardId) {
        LogUtil.info("player claim Cp CopyReward,playerId:{},rewardId:{}",playerIdx,rewardId);

        String mapId = CpTeamCache.getInstance().findPlayerCopyMapId(playerIdx);

        if (StringUtils.isEmpty(mapId)) {
            return RCE_CP_CopyNotExists;
        }
        return JedisUtil.syncExecSupplier(getCopyUpdateRedisKey(mapId), () -> {
            CpTeamRewardCfgObject cfg = CpTeamRewardCfg.getById(rewardId);
            CpCopyMap copyMapData = findCopyMapData(mapId);
            if (StringUtils.isEmpty(mapId)) {
                return RCE_CP_CopyNotExists;
            }
            int teamScore = copyMapData.getTeamScore();
            if (cfg == null) {
                return RCE_ErrorParam;
            }
            int playerMaxSceneId = CrossArenaManager.getInstance().findPlayerMaxSceneId(playerIdx);
            if (teamScore < cfg.getNeedscore()
                    || playerMaxSceneId != cfg.getScenceid()) {
                LogUtil.info("player :{} claim cp copy reward,fail,mapId:{},teamScore:{},playerScenceId:{}" +
                        ",needScore:{},need scenceId:{}", playerIdx, mapId, teamScore, playerMaxSceneId, cfg.getNeedscore(), cfg.getScenceid());
                //不满足条件
                return RCE_CP_NotMatchCondition;
            }
            if (copyMapData.claimedReward(playerIdx, rewardId)) {
                return RCE_CP_RepeatClaim;
            }
            copyMapData.addClaimReward(playerIdx, rewardId);
            cache.saveCopyMap(copyMapData);

            RewardManager.getInstance().doRewardByList(playerIdx, RewardUtil.parseRewardIntArrayToRewardList(cfg.getRewards()),
                    ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_LT_CP), true);
            LogUtil.info("player claim Cp CopyReward,playerId:{},rewardId:{}",playerIdx,rewardId);
            return RCE_Success;
        });
    }

    public void logoutCpTeamCopy(String playerIdx) {
        String mapId = CpTeamCache.getInstance().findPlayerCopyMapId(playerIdx);
        if (mapId == null) {
            return;
        }
        JedisUtil.syncExecBooleanSupplier(getCopyUpdateRedisKey(mapId), () -> {

            CpCopyMap mapData = findCopyMapData(mapId);
            if (mapData == null) {
                return true;
            }
            mapData.playerOffline(playerIdx);
            cache.saveCopyMap(mapData);
            CpBroadcastManager.getInstance().broadcastPlayerOnlineState(playerIdx, mapData.getRealPlayers(), false);
            return true;
        });
    }

    public void loginCpTeamCopy(String playerIdx) {
        String mapId = CpTeamCache.getInstance().findPlayerCopyMapId(playerIdx);
        if (mapId == null) {
            return;
        }
        JedisUtil.syncExecBooleanSupplier(getCopyUpdateRedisKey(mapId), () -> {
            CpCopyMap mapData = findCopyMapData(mapId);
            if (mapData == null) {
                return true;
            }
            mapData.playerOnline(playerIdx);
            cache.saveCopyMap(mapData);
            CpBroadcastManager.getInstance().broadcastPlayerOnlineState(playerIdx, mapData.getRealPlayers(), true);
            return true;
        });
        CpTeamCache.getInstance().removeCopyPlayerLeaveTime(playerIdx);
    }

    public RetCodeId.RetCodeEnum playerLeaveOutCopy(String playerIdx) {
        CpCopyMap mapData = findMapDataByPlayerId(playerIdx);
        if (mapData == null) {
            return RCE_CP_CopyNotExists;
        }
        JedisUtil.syncExecBooleanSupplier(getCopyUpdateRedisKey(mapData.getMapId()), () -> {
            playerLeaveCopy(playerIdx, mapData, playerWin(mapData, playerIdx));
            mapData.updatePlayerState(playerIdx, CpFunction.CpCopyPlayerState.CCPS_Out);
            CpBroadcastManager.getInstance().broadcastPlayerState(playerIdx, mapData.getOnPlayPlayer(), CpFunction.CpCopyPlayerState.CCPS_Out);
            cache.saveCopyMap(mapData);
            return true;
        });
        return RCE_Success;
    }

    private boolean playerWin(CpCopyMap mapData, String playerIdx) {
        if (mapData==null){
            return false;
        }
        CpTeamCopyPlayerProgress progress = mapData.getProgress(playerIdx);
        if (progress==null){
            return false;
        }
        return progress.isSuccess();
    }

    public int queryLimitReviveTimes(String playerIdx) {
        return queryBuyReviveTimes(playerIdx) + queryFreeReviveLimit(playerIdx);
    }
}
