package model.thewar;

import cfg.MailTemplateUsed;
import cfg.MailTemplateUsedObject;
import cfg.Mission;
import cfg.MissionObject;
import cfg.RankRewardTargetConfig;
import cfg.TheWarConstConfig;
import cfg.TheWarSeasonConfig;
import cfg.TheWarSeasonConfigObject;
import com.google.protobuf.InvalidProtocolBufferException;
import common.ExclusionType;
import common.FunctionExclusion;
import common.GameConst;
import common.GameConst.ActivityState;
import common.GameConst.EventType;
import common.GameConst.RedisKey;
import common.GlobalData;
import static common.JedisUtil.jedis;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.ranking.settle.MailRankingSettleHandler;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Activity.EnumRankingType;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.GameplayDB.DB_GamePlayTheWar;
import protocol.GameplayDB.GameplayTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_PlayerEnterTheWar;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TheWar.SC_UpdateTheWarState;
import protocol.TheWarDefine.TheWarRetCode;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

public class TheWarManager implements Tickable {
    private static TheWarManager instance;

    private String mapName;
    private int theWarSate;
    private long updateTime;
    private TheWarSeasonConfigObject warSeasonConfig;

    private Map<String, String> joinedWarPlayerMap = new ConcurrentHashMap<>();

    public static TheWarManager getInstance() {
        if (instance == null) {
            synchronized (TheWarManager.class) {
                if (instance == null) {
                    instance = new TheWarManager();
                }
            }
        }
        return instance;
    }

    public String getMapName() {
        return warSeasonConfig != null ? warSeasonConfig.getOpenmapname() : "";
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public void init() {
        if (!initTheWarTime()) {
            return;
        }
        GlobalTick.getInstance().addTick(this);
        fetchJoinedPlayers();
    }

    protected boolean initTheWarTime() {
        warSeasonConfig = TheWarSeasonConfig.getInstance().getWarOpenConfig();
        return true;
    }

    public TheWarRetCode enterWarRoom(playerEntity player, boolean isResume) {
        if (player == null) {
            return TheWarRetCode.TWRC_UnknownError; // 玩家信息错误
        }
        if (PlayerUtil.queryFunctionLock(player.getIdx(), EnumFunction.TheWar)) {
            return TheWarRetCode.TWRC_OpenLevelLimit; // 玩家等级不足
        }

        if (!TheWarManager.getInstance().open()) {
            return TheWarRetCode.TWRC_NotOpen; // 远征未开启
        }
        int sf = FunctionExclusion.getInstance().checkExclusionAll(player.getIdx());
        if (sf == ExclusionType.CROSSARENA_PVP) {
            return TheWarRetCode.TWRC_InQCFunction;
        }
        if (sf == ExclusionType.CROSSARENA_LEITAI_UP) {
            return TheWarRetCode.TWRC_InLtFunction;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        int svrIndex = CrossServerManager.getInstance().getSvrIndexByWarRoomIdx(roomIdx);
        GS_CS_PlayerEnterTheWar.Builder builder = GS_CS_PlayerEnterTheWar.newBuilder();
        if (svrIndex <= 0) {
            roomIdx = CrossServerManager.getInstance().getAvailableJoinRoomIdx();
            svrIndex = CrossServerManager.getInstance().getSvrIndexByWarRoomIdx(roomIdx);
        }
        builder.setIsResume(isResume);
        builder.setRoomIdx(roomIdx);
        builder.setFromSvrIndex(ServerConfig.getInstance().getServer());
        builder.setPlayerInfo(player.getBattleBaseData());
        builder.putAllPlayerBaseAdditions(player.getDb_data().getPetPropertyAdditionMap());
        boolean sendSuccess;
        if (svrIndex <= 0) {
            sendSuccess = CrossServerManager.getInstance().sendMsgToWarServer(MsgIdEnum.GS_CS_PlayerEnterTheWar_VALUE, builder);
            LogUtil.info("player first join the war,playerIdx=" + player.getIdx());
        } else {
            sendSuccess = CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_PlayerEnterTheWar_VALUE, builder);
        }
        return sendSuccess ? TheWarRetCode.TWRC_Success : TheWarRetCode.TWRC_ServerNotFound;
    }

    public void addJoinedWarPlayer(playerEntity player) {
        if (player == null || StringHelper.isNull(player.getDb_data().getTheWarRoomIdx())) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        joinedWarPlayerMap.put(player.getIdx(), roomIdx);
        jedis.hset(getJoinedPlayerRedisKey(), player.getIdx(), roomIdx);
    }

    public void settleTheWar() {
        settleSeasonRankingRewards();
        LogUtil.info("TheWarManger settleTheWar,joinedWarPlayer:{}", joinedWarPlayerMap.keySet());
        playerEntity player;
        for (String playerIdx : joinedWarPlayerMap.keySet()) {
            player = playerCache.getByIdx(playerIdx);
            if (player == null) {
                continue;
            }
            SyncExecuteFunction.executeConsumer(player, entity -> {
                entity.getDb_data().setTheWarRoomIdx("");
                entity.getDb_data().getTheWarDataBuilder().clearKillMonsterCount();
                entity.getDb_data().getTheWarDataBuilder().clearInWarPets();
                entity.getDb_data().getTheWarDataBuilder().clearOwedGridData();
                entity.getDb_data().getTheWarDataBuilder().clearLastSettleTime();
            });
            CrossServerManager.getInstance().removeTheWarPlayer(player);
        }
        joinedWarPlayerMap.clear();

        jedis.del(getJoinedPlayerRedisKey());

        //清空所有玩家的赛季任务进度
        EventUtil.unlockObjEvent(EventType.ET_ClearAllPlayerTheWarSeasonMissionPro, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
    }

    private void settleSeasonRankingRewards() {
        MailTemplateUsedObject mailTemplateUsedCfg = MailTemplateUsed.getById(GameConst.CONFIG_ID);
        MailRankingSettleHandler settleHandler = new MailRankingSettleHandler(EnumRankingType.ERT_TheWar_KillMonster,
                RankRewardTargetConfig.getInstance().getRangeRewardsByRankType(EnumRankingType.ERT_TheWar_KillMonster),
                mailTemplateUsedCfg.getThewarseason(), RewardSourceEnum.RSE_TheWar,
                this.joinedWarPlayerMap.keySet(), mailTemplateUsedCfg.getThewarseasonunranking());

        settleHandler.settleRanking();
        LogUtil.info("model.thewar.TheWarManager.settleSeasonRankingRewards, the war season ranking settle finished");
    }

    public void fetchJoinedPlayers() {
        Map<String, String> playerInfoMap = jedis.hgetAll(getJoinedPlayerRedisKey());
        joinedWarPlayerMap.clear();
        if (playerInfoMap != null) {
            joinedWarPlayerMap.putAll(playerInfoMap);
        }
    }

    protected String getJoinedPlayerRedisKey() {
        int curSeasonId = warSeasonConfig != null ? warSeasonConfig.getId() : 0;
        return RedisKey.TheWarJoinedPlayerInfo + curSeasonId + ":" + ServerConfig.getInstance().getServer();
    }

    public void onPlayerLogin(String playerIdx) {
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_UpdateTheWarState_VALUE, getNewWarState());
    }

    public SC_UpdateTheWarState.Builder getNewWarState() {
        SC_UpdateTheWarState.Builder builder = SC_UpdateTheWarState.newBuilder();
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (warSeasonConfig != null) {
            builder.setMapName(warSeasonConfig.getOpenmapname());
            if (curTime >= warSeasonConfig.getStartplaytime()) {
                if (curTime < warSeasonConfig.getEndplaytime()) {
                    builder.setOpen(true);
                    builder.setSeasonTimestamp(warSeasonConfig.getEndplaytime());
                }
            } else {
                builder.setSeasonTimestamp(warSeasonConfig.getStartplaytime());
            }
        }
        return builder;
    }

    public void broadcastWarStateChange() {
        GlobalData.getInstance().sendMsgToAllOnlinePlayer(MsgIdEnum.SC_UpdateTheWarState, getNewWarState());
    }

    @Override
    public void onTick() {
        if (warSeasonConfig == null) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (updateTime > curTime) {
            return;
        }
        updateTime = curTime + TimeUtil.MS_IN_A_S;
        switch (theWarSate) {
            case ActivityState.EndState: {
                if (curTime >= warSeasonConfig.getStartplaytime()) {
                    theWarSate = ActivityState.OpenState;
                    if (needClearRanking(warSeasonConfig.getId())) {
                        RankingManager.getInstance().clearRanking(EnumRankingType.ERT_TheWar_KillMonster);
//                        clearAllJoinPlayerKillMonsterCount();
                        saveClearedSeasonId(warSeasonConfig.getId());
                        LogUtil.info("model.thewar.TheWarManager.onTick, clear season id:" + warSeasonConfig.getId() + " ranking finished");
                    }

                    broadcastWarStateChange();
                    LogUtil.info("TheWarSeason open seasonId=" + warSeasonConfig.getId());
                }
                break;
            }
            case ActivityState.OpenState: {
                if (curTime > warSeasonConfig.getEndplaytime() - TheWarConstConfig.getById(GameConst.CONFIG_ID).getPreendtime() * TimeUtil.MS_IN_A_MIN) {
                    theWarSate = ActivityState.SettleState;
                }
                break;
            }
            case ActivityState.SettleState: {
                if (curTime > warSeasonConfig.getEndplaytime()) {
                    settleTheWar();
                    theWarSate = ActivityState.EndState;
                    warSeasonConfig = TheWarSeasonConfig.getInstance().getWarOpenConfig();
                    broadcastWarStateChange();
                }
                break;
            }
            default:
                break;
        }
    }

    public List<MissionObject> getSeasonMissionsByTargetType(TargetTypeEnum targetType) {
        if (targetType == null || this.warSeasonConfig == null) {
            return null;
        }
        List<MissionObject> result = new ArrayList<>();
        for (int missionId : this.warSeasonConfig.getMissions()) {
            MissionObject missionCfg = Mission.getById(missionId);
            if (missionCfg != null && missionCfg.getMissiontype() == targetType.getNumber()) {
                result.add(missionCfg);
            }
        }
        return result;
    }

    public boolean open() {
        return theWarSate == ActivityState.OpenState;
    }

    /**
     * @param curMissionId
     * @return -1未找到， = curIndex 没有更多任务
     */
    public int getSeasonMissionNextMissionId(int curMissionId) {
        if (this.warSeasonConfig == null) {
            return -1;
        }
        int[] missions = this.warSeasonConfig.getMissions();
        if (missions.length <= 0) {
            return -1;
        }
        if (curMissionId == 0) {
            return missions[0];
        }

        for (int i = 0; i < missions.length - 1; i++) {
            if (missions[i] == curMissionId) {
                return missions[i + 1];
            }
        }

        return curMissionId;
    }

    public boolean needClearRanking(int seasonId) {
        //线上兼容处理,防止当前赛季被清空
        int curOpenSessionId = 20;
        if (seasonId == curOpenSessionId) {
            saveClearedSeasonId(seasonId);
            LogUtil.info("model.thewar.TheWarManager.needClearRanking, session id:" + seasonId
                    + " is equals curOpenSessionId:" + curOpenSessionId + ", save and skip clear");
            return false;
        }

        gameplayEntity theWarEntity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_TheWar);
        if (theWarEntity.getGameplayinfo() == null) {
            return true;
        }
        try {
            return !DB_GamePlayTheWar.parseFrom(theWarEntity.getGameplayinfo()).getAlreadyClearSeasonRankingList().contains(seasonId);
        } catch (InvalidProtocolBufferException e) {
            LogUtil.printStackTrace(e);
            return true;
        }
    }

    public void saveClearedSeasonId(int seasonId) {
        gameplayEntity theWarEntity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_TheWar);
        if (theWarEntity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(theWarEntity, entity -> {
            DB_GamePlayTheWar.Builder builder = null;
            if (theWarEntity.getGameplayinfo() != null) {
                try {
                    builder = DB_GamePlayTheWar.parseFrom(theWarEntity.getGameplayinfo()).toBuilder();
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            if (builder == null) {
                builder = DB_GamePlayTheWar.newBuilder();
            }

            if (builder.getAlreadyClearSeasonRankingList().contains(seasonId)) {
                return;
            }

            builder.addAlreadyClearSeasonRanking(seasonId);

            theWarEntity.setGameplayinfo(builder.build().toByteArray());
        });

        gameplayCache.put(theWarEntity);
    }

    private void clearAllJoinPlayerKillMonsterCount() {
        for (String playerIdx : joinedWarPlayerMap.keySet()) {
            playerEntity player = playerCache.getByIdx(playerIdx);
            if (player == null) {
                continue;
            }

            SyncExecuteFunction.executeConsumer(player, e -> {
                player.getDb_data().getTheWarDataBuilder().clearKillMonsterCount();
            });
        }

//        joinedWarPlayerMap.clear();
//
//        Jedis jedis = null;
//        try {
//            jedis = JedisUtil.getResource();
//            jedis.del(getJoinedPlayerRedisKey());
//        } finally {
//            if (jedis != null) {
//                JedisUtil.returnResource(jedis);
//            }
//        }

        LogUtil.info("TheWarManager.clearAllJoinPlayerKillMonsterCount, finished clear");
    }
}
