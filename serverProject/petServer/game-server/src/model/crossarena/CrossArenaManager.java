package model.crossarena;

import cfg.CrossArenaCfg;
import cfg.CrossArenaCfgObject;
import cfg.CrossArenaEvent;
import cfg.CrossArenaEventObject;
import cfg.CrossArenaExpCfg;
import cfg.CrossArenaLvCfg;
import cfg.CrossArenaLvCfgObject;
import cfg.CrossArenaLvTask;
import cfg.CrossArenaLvTaskObject;
import cfg.CrossArenaRobot;
import cfg.CrossArenaRobotObject;
import cfg.CrossArenaScene;
import cfg.CrossArenaSceneObject;
import cfg.CrossArenaTaskAward;
import cfg.CrossArenaTaskAwardObject;
import cfg.CrossArenaWinTask;
import cfg.CrossArenaWinTaskObject;
import cfg.GameConfig;
import cfg.Head;
import cfg.Mission;
import cfg.MissionObject;
import cfg.PetBaseProperties;
import com.google.protobuf.InvalidProtocolBufferException;
import common.FunctionExclusion;
import common.GameConst;
import static common.GameConst.CrossArenaScoreItemId;
import common.GameConst.RedisKey;
import static common.GameConst.RedisKey.CrossArenaBSSid;
import static common.GameConst.RedisKey.CrossArenaProtectCard;
import static common.GameConst.RedisKey.CrossArenaTableState;
import static common.GameConst.RedisKey.CrossArenaTableStateEndTime;
import common.GlobalData;
import common.IdGenerator;
import helper.StringUtils;
import model.activity.ActivityData;
import model.activity.TimeRuleManager;
import model.crossarena.bean.CrossArenaTablesPage;
import model.crossarena.bean.DailyWinTaskReward;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import common.FunctionExclusion;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalData;
import common.JedisUtil;
import static common.JedisUtil.jedis;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import datatool.StringHelper;
import db.entity.BaseEntity;
import helper.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.activity.ActivityData;
import model.activity.TimeRuleManager;
import model.battle.BattleManager;
import model.battle.BattleUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import static model.crossarena.CrossArenaUtil.SYNCACHE;
import model.crossarena.bean.CrossArenaTablesPage;
import model.crossarena.bean.DailyWinTaskReward;
import model.crossarena.dbCache.playercrossarenaCache;
import model.crossarena.entity.playercrossarenaEntity;
import model.crossarenapvp.CrossArenaPvpManager;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.matcharena.MatchArenaManager;
import model.patrol.entity.PatrolBattleResult;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.RankingManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.team.dbCache.teamCache;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.PlayerTargetLog;
import protocol.Activity;
import protocol.Battle;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PetBuffData;
import protocol.Common;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.CrossArena;
import protocol.CrossArena.CrossArenaDBKey;
import static protocol.CrossArena.CrossArenaDBKey.LT_10SerialWinWeek;
import static protocol.CrossArena.CrossArenaDBKey.LT_10SerialWinWeek_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_BATTLENUM;
import static protocol.CrossArena.CrossArenaDBKey.LT_BATTLENUM_DAY_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_BATTLENUM_WEEK;
import static protocol.CrossArena.CrossArenaDBKey.LT_BATTLENUM_WEEK_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_BATTLEWinNUM_WEEK;
import static protocol.CrossArena.CrossArenaDBKey.LT_BATTLEWinNUM_WEEK_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_CurSerialFailNum;
import static protocol.CrossArena.CrossArenaDBKey.LT_GRADECUR_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_GRADELV;
import static protocol.CrossArena.CrossArenaDBKey.LT_GRADELV_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_LastSerialWinNum;
import static protocol.CrossArena.CrossArenaDBKey.LT_LastSerialWinNum_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_MAXSCENEID;
import static protocol.CrossArena.CrossArenaDBKey.LT_WEEKSCORE_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINCOTCUR;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINCOTCUR_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINCOTDAY;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINCOTDAY_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINCOTHIS;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINCOTHISNum;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINCOTHIS_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINCOTWeekly;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINCOTWeekly_VALUE;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINNUM;
import static protocol.CrossArena.CrossArenaDBKey.LT_WINNUM_DAY_VALUE;
import protocol.CrossArena.CrossArenaGradeType;
import protocol.CrossArena.CrossArenaPlyCacheRAM;
import protocol.CrossArena.CrossArenaQueuePet;
import protocol.CrossArena.CrossArenaQueueTable;
import protocol.CrossArena.CrossArenaTaskCommon;
import protocol.CrossArena.CrossArenaTaskWeekBox;
import protocol.CrossArena.SC_CrossArenaGradePanel;
import protocol.CrossArena.SC_CrossArenaPanel;
import protocol.CrossArena.SC_CrossArenaTableReadyFight;
import protocol.CrossArena.SC_CrossArenaWeekBoxPanel;
import protocol.CrossArena.SC_CrossArenaWeekBoxReward;
import protocol.CrossArena.SC_CrossArenaWinPanel;
import protocol.CrossArena.SC_CrossArenaWinTask;
import protocol.CrossArenaDB;
import protocol.CrossArenaDB.CrossArenaPlayerDB;
import protocol.CrossArenaDB.RedisCrossArenaPlayer;
import protocol.CrossArenaDB.RedisCrossArenaTableDB;
import protocol.CrossArenaDB.RedisCrossArenaTableDB.Builder;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.SC_Claim10WinActivityTime_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_CrossArenaLtDel_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_CrossArenaLvUpdate_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_CrossArenaQueuePanel_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_CrossArenaTablePage_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PlayerLeaveQueuePanel_VALUE;
import protocol.PetMessage;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer;
import protocol.ServerTransfer.CrossArenaCacheSyn;
import protocol.ServerTransfer.GS_BS_CrossArenaReadyFight;
import protocol.TargetSystem;
import static protocol.TargetSystem.TargetTypeEnum.TTE_CrossArenaHonor1003;
import static protocol.TargetSystem.TargetTypeEnum.TTE_CrossArena_GRADELvReach;
import static protocol.TargetSystem.TargetTypeEnum.TTE_CrossArena_SCENEIDReach;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.ObjUtil;
import util.RandomUtil;
import util.TimeUtil;
import util.timerule.DateUtil;

/**
 * 跨服擂台赛
 */
public class CrossArenaManager implements Tickable {

    private static CrossArenaManager instance;

    public static CrossArenaManager getInstance() {
        if (instance == null) {
            synchronized (CrossArenaManager.class) {
                if (instance == null) {
                    instance = new CrossArenaManager();
                }
            }
        }
        return instance;
    }

    private CrossArenaManager() {
    }

    private static final long delayCloseTime = TimeUtil.MS_IN_A_S * 60;

    private static final  Map<String,Long> delayClosePlayerTimeMap = new ConcurrentHashMap<>();

    public static final int EventRewardDefault = 1;
    private static final int EventReward2 = 2;

    /**
     * 机器人ID
     */
    private String AINAME = "rb";

    /**
     * 活动是否开启
     */
    private boolean isOpen = true;
    private boolean isOpenDub = false;
    private boolean isGM = false;

    /**
     * 缓存本服玩家在擂台得数据
     */
    private Map<String, CrossArenaPlyCacheRAM> playerPosMap = new ConcurrentHashMap<String, CrossArenaPlyCacheRAM>();
    /**
     * 缓存玩家进入擂台得时间，建议只用来做时间累计 或判断是否在线
     */
    private Map<String, Long> playerJionTime = new ConcurrentHashMap<>();
    /**
     * 缓存离线玩家数据
     */
    private long machineTime = 0;
    private Map<String, CrossArenaPlyCacheRAM> cacheInfoMachine = new ConcurrentHashMap<>();
    /**
     * 缓存玩家位置信息
     */
    private long cacheInfoSynTime = 0;
    /**
     * 同步数据缓存
     */
    private Map<Integer, Map<String, CrossArenaPlyCacheRAM>> cacheInfoSyn = new ConcurrentHashMap<>();

    private Random random = new Random();

    private long tenOpenTime = 0;
    private long tenCloseTime = 0;

    private int findTableWorkServer(int tableId) {
        String svrIndexStr = jedis.hget(getTableServerKey(tableId), tableId + "");
        return StringHelper.stringToInt(svrIndexStr, 0);
    }

    private void saveTableWorkServer(int tableId, int serverIndex) {
        jedis.hset(getTableServerKey(tableId), tableId + "", StringHelper.IntTostring(serverIndex, "0"));
    }

    public boolean init() {
        openInitData(false);
        CrossArenaScene._ix_id.keySet().forEach(sceneId -> queuePanelPlayerMap.put(sceneId, new ConcurrentHashMap<>()));
        CrossArenaScene._ix_id.keySet().forEach(sceneId -> screenPagePlayers.put(sceneId, new ConcurrentHashMap<>()));
        // 启动服务器做数据检查
        if (JedisUtil.lockRedisKey(GameConst.RedisKey.CrossArenaTimeLock, 5000l)) {
            String timeStr = jedis.get(GameConst.RedisKey.CrossArenaTen);
            if (!StringHelper.isNull(timeStr)) {
                long time = NumberUtils.toLong(timeStr);
                if (System.currentTimeMillis() > time) {
                    clearTenWinPlayers();
                }
            }
            JedisUtil.unlockRedisKey(GameConst.RedisKey.CrossArenaTimeLock);
        }
        CrossArenaRankManager.getInstance().init();
        return GlobalTick.getInstance().addTick(this);
    }
    private Map<Integer, String> tableServerKeyMap  = new ConcurrentHashMap();

    public String getTableServerKey(int tableId) {
        int sceneId = getSceneIdByTableId(tableId);
        return tableServerKeyMap.computeIfAbsent(tableId, a -> CrossArenaBSSid + sceneId);
    }

    private void clearTenWinPlayers() {
        for (Integer scienceId : CrossArenaScene._ix_id.keySet()) {
            if (jedis.scard(getCrossArenaTenPlayerKey(scienceId)) > 0) {
                jedis.del(getCrossArenaTenPlayerKey(scienceId));
            }
        }
    }

    private String getCrossArenaTenPlayerKey(int scienceId) {
        return RedisKey.CrossArenaTenPlayer + scienceId;
    }

    /**
     * 开启擂台活动
     */
    public void openLT(long openTime) {
        isOpen = true;
        openInitData(false);
    }

    public void closeLT() {
        if (isGM) {
            return;
        }
        isOpen = false;
        try {
            String openTime = jedis.get(GameConst.RedisKey.CrossArenaTime);
            if (!StringHelper.isNull(openTime)) {
                if (Long.valueOf(openTime) <= 0) {
                    return;
                }
            }
            // 数据加锁失败
            if (!JedisUtil.lockRedisKey(GameConst.RedisKey.CrossArenaTimeLock, 5000l)) {
                return;
            }
            resetLTData();
            jedis.set(GameConst.RedisKey.CrossArenaTime, String.valueOf(0));
            // 数据解锁
            JedisUtil.unlockRedisKey(GameConst.RedisKey.CrossArenaTimeLock);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void openLT10(long endTime, boolean isGM) {
        isOpenDub = true;
        if (!JedisUtil.lockRedisKey(GameConst.RedisKey.CrossArenaTimeLock, 5000l)) {
            return;
        }
        String timeStr = jedis.get(GameConst.RedisKey.CrossArenaTen);
        if (!StringHelper.isNull(timeStr)) {
            long time = NumberUtils.toLong(timeStr);
            if (!isGM && DateUtil.isToday(time)) {
                return;
            }
        }
        jedis.set(GameConst.RedisKey.CrossArenaTen, "" + endTime);
        this.tenCloseTime = endTime;
        JedisUtil.unlockRedisKey(GameConst.RedisKey.CrossArenaTimeLock);
    }

    public void closeLT10() {
        isOpenDub = false;
        if (!JedisUtil.lockRedisKey(GameConst.RedisKey.CrossArenaTimeLock, 5000l)) {
            return;
        }
        jedis.set(GameConst.RedisKey.CrossArenaTen, "" + 0);
        JedisUtil.unlockRedisKey(GameConst.RedisKey.CrossArenaTimeLock);
        // 提示10连任务结束
        for (String pid : playerJionTime.keySet()) {
            sendPlayerEvents(pid);
        }
        this.tenOpenTime = 0;
        this.tenCloseTime = 0;
    }

    private static final long checkOfflineInterval = TimeUtil.MS_IN_A_S;
    private static  long nextOfflineCheck ;

    @Override
    public void onTick() {
        if (GlobalTick.getInstance().getCurrentTime() > machineTime) {
            machineTime = System.currentTimeMillis() + 600000L;
            synOfflineCache();
        }

        delayClosePlayerMapCheck();
    }

    private void delayClosePlayerMapCheck() {
        if (GlobalTick.getInstance().getCurrentTime() < nextOfflineCheck) {
            return;
        }
        for (Entry<String, Long> entry : delayClosePlayerTimeMap.entrySet()) {
            if (entry.getValue() < GlobalTick.getInstance().getCurrentTime()) {
                delayClosePlayerTimeMap.remove(entry.getKey());
                closeTable(entry.getKey());
            }
        }
        nextOfflineCheck = GlobalTick.getInstance().getCurrentTime() + checkOfflineInterval;
    }

    /**
     * 同步离线玩家
     */
    public void synOfflineCache() {
        Map<Integer, CrossArenaPlyCacheRAM> tempmap = new TreeMap<>();
        ServerTransfer.GS_BS_CrossArenaPlyoffline.Builder msg = ServerTransfer.GS_BS_CrossArenaPlyoffline.newBuilder();
        for (BaseEntity ent : playercrossarenaCache.getInstance().getAll().values()) {
            playercrossarenaEntity pv = (playercrossarenaEntity) ent;
            playerEntity pe = playerCache.getByIdx(pv.getIdx());
            if (null == pe) {
                continue;
            }
            if (GlobalData.getInstance().checkPlayerOnline(pv.getIdx())) {
                continue;
            }
            CrossArenaPlyCacheRAM capr = createPlyCacheRAM(pv, 0, 1, 1);
            tempmap.put(1000000 - pe.getLevel(), capr);
            msg.addPartInfo(capr);
        }
        int i = 0;
        for (CrossArenaPlyCacheRAM cccc : tempmap.values()) {
            msg.addPartInfo(cccc);
            i++;
            if (i > 30) {
                break;
            }
        }
        try {
            int svrIndex = findSynCacheServerIndex();
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(svrIndex);
            if (null == bnc) {
                return;
            }
            bnc.send(MessageId.MsgIdEnum.GS_BS_CrossArenaPlyoffline_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void resetLTData() {
        try {
            jedis.del(GameConst.RedisKey.CrossArenaNoteCotMap);
            jedis.del(GameConst.RedisKey.CrossArenaPlayerTable);
            jedis.del(GameConst.RedisKey.CrossArenaPlayerInfo.getBytes());
            jedis.del(GameConst.RedisKey.CrossArenaPlOnline);
            jedis.del(GameConst.RedisKey.CrossArenaRBPlayerInfo);
            for (Integer scienceId : CrossArenaScene._ix_id.keySet()) {
                String tempksy = GameConst.RedisKey.CrossArenaNoteInsMap + scienceId;
                jedis.del(tempksy);
                List<Integer> allTableIds = findAllTableByScene(scienceId);
                for (Integer tableId : allTableIds) {
                    // 获取擂台数据
                    // 桌子枷锁
                    String tableLockKey = RedisKey.CrossArenaData + "" + tableId;
                    jedis.del(tableLockKey.getBytes());
                    jedis.hdel(GameConst.RedisKey.CrossArenaBSSid + scienceId, tableId + "");
                }
                String keyQue = GameConst.RedisKey.CrossArenaQue + "" + scienceId;
                jedis.del(keyQue);
            }
            jedis.del(GameConst.RedisKey.CrossArenaTableNum);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * 开启擂台赛，初始化擂台数据
     */
    public void openInitData(boolean isGM) {
        Map<Integer, CrossArenaSceneObject> cfgData = CrossArenaScene._ix_id;
        if (null == cfgData) {
            LogUtil.error("擂台赛数据初始化时，没有获取到擂台配置数据");
            return;
        }
        try {
            String openTime = jedis.get(GameConst.RedisKey.CrossArenaTime);
            if (!isGM && !StringHelper.isNull(openTime)) {
                // 判断存储数据是否是同一条数据
                if (Long.parseLong(openTime) > 0) {
                    // 数据是同一天则表示活动已经开启
                    return;
                }
            }
            // 数据加锁失败
            if (!JedisUtil.lockRedisKey(GameConst.RedisKey.CrossArenaTimeLock, 5000l)) {
                return;
            }
            // 设置活动时间
            long startTime = GlobalTick.getInstance().getCurrentTime();
            jedis.set(GameConst.RedisKey.CrossArenaYear, String.valueOf(startTime));
            resetLTData();
            checkCreateProtectRobot();
            jedis.set(GameConst.RedisKey.CrossArenaTime, String.valueOf(startTime));
            // 获取全部战斗服务器，动态分配到每个服务器均分跨服擂台赛

            List<Integer> allBS = new ArrayList<>(BattleServerManager.getInstance().getBatServerIndexAddrMap().keySet());
            if (allBS.isEmpty()) {
                LogUtil.error("擂台赛数据初始化时，没有获取到可执行逻辑的跨服服务器!");
                // 没有战斗服务器，则默认给予一个初始值，方便初始化数据
                allBS.add(0);
            }
            // 所有玩家信息缓存在一个服务器
            saveSynCacheServerIndex(allBS.get(0));
            int size = allBS.size();
            int i = 0, j = 0;
            for (CrossArenaSceneObject caso : cfgData.values()) {
                if (caso.getId() <= 0) {
                    continue;
                }
                // 首先存储道场数据redis
                // 获取一个跨服服务器作为该道场的中心服
                if (i >= size) {
                    i = 0;
                }
                int bsSid = allBS.get(i);
                jedis.hset(GameConst.RedisKey.CrossArenaBSSid, "" + caso.getId(), StringHelper.IntTostring(bsSid, "0"));
                jedis.hset(GameConst.RedisKey.CrossArenaTableNum, "" + caso.getId(), "" + caso.getTablenum());
                // 获取到了一个战斗服，作为擂台道场数据中心服
                // 初始化该道场的擂台数据
                i += 1;
                for (int num = 1; num <= caso.getTablenum(); num++) {
                    if (j >= size) {
                        j = 0;
                    }
                    int leitaiId = cretaeTableIdId(caso.getId(), num);

                    // 初始化擂台数据
                    RedisCrossArenaTableDB.Builder leitaimsg = RedisCrossArenaTableDB.newBuilder();
                    leitaimsg.setLeitaiId(leitaiId);
                    leitaimsg.setState(CrossArena.CrossArenaState.IDLE_VALUE);
                    leitaimsg.setDefTime(startTime);
                    leitaimsg.setDefWinNum(0);
                    leitaimsg.setBattleId(0);
                    // 存储单个擂台数据至redis
                    saveLtDataToCache(leitaimsg);
                    // 给擂台分配逻辑服
                    saveTableWorkServer(leitaiId,allBS.get(j));
                    j++;
                }
            }
            // 数据解锁
            JedisUtil.unlockRedisKey(GameConst.RedisKey.CrossArenaTimeLock);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }


    private void saveSynCacheServerIndex(int svrIndex) {
        jedis.hset(GameConst.RedisKey.CrossArenaBSSid, SYNCACHE, StringHelper.IntTostring(svrIndex, "0"));
    }

    private int findSynCacheServerIndex() {
        String svrIndexStr = jedis.hget(GameConst.RedisKey.CrossArenaBSSid, SYNCACHE);
        return StringHelper.stringToInt(svrIndexStr, 0);
    }
    private static final int protectRobot = 1;
    private static final int normalMatchRobot = 0;

    private void checkCreateProtectRobot() {
        for (Integer sceneId : CrossArenaScene._ix_id.keySet()) {
            CrossArenaRobot._ix_id.values().stream().filter(e -> e.getRank() == sceneId && e.getUsetype() ==protectRobot ).map(CrossArenaRobotObject::getDifficult).forEach(difficult -> {
                String queKey = getProtectRobotQueKey(sceneId, difficult);
                // 队列非空
                if (jedis.llen(queKey) < 1) {
                    for (int i = 0; i < 10; i++) {
                        String robotIdx = AINAME + "-" + protectRobot * 100 + difficult + "-" + IdGenerator.getInstance().generateId();
                        needAI(sceneId, robotIdx, 0, protectRobot, difficult);
                    }
                }
            });
        }
    }

    private String getProtectRobotQueKey(int sceneId, int robotDifficult) {
        return RedisKey.CrossArenaProtectRobotQue + sceneId + "-" + robotDifficult;
    }

    public void cacheSynOff(List<CrossArenaCacheSyn> infos) {
        Map<String, CrossArenaPlyCacheRAM> cacheInfoMachinet = new ConcurrentHashMap<>();
        for (CrossArenaCacheSyn cacs : infos) {
            cacheInfoMachinet.putAll(cacs.getAllInfoMap());
        }
        this.cacheInfoMachine = cacheInfoMachinet;
    }

    /**
     * @param time
     * @param infos 数据同步
     */
    public void cacheSyn(long time, List<CrossArenaCacheSyn> infos) {
        if (cacheInfoSynTime != time) {
            Map<Integer, Map<String, CrossArenaPlyCacheRAM>> cacheInfoSynT = new ConcurrentHashMap<>();
            for (CrossArenaCacheSyn cacs : infos) {
                cacheInfoSynT.put(cacs.getSceneId(), cacs.getAllInfoMap());
            }
            this.cacheInfoSyn = cacheInfoSynT;
        } else {
            for (CrossArenaCacheSyn cacs : infos) {
                cacheInfoSyn.computeIfAbsent(cacs.getSceneId(), k -> new ConcurrentHashMap<>()).putAll(cacs.getAllInfoMap());
            }
        }
    }

    private int getRobotShowPetId(Battle.BattlePlayerInfo attinfo) {
        List<Integer> te = new ArrayList<>();
        for (Battle.BattlePetData ent : attinfo.getPetListList()) {
            te.add(ent.getPetCfgId());
        }
        Collections.shuffle(te);
        if (te.size() > 0) {
            return te.get(0);
        } else {
            return 1001;
        }
    }

    /**
     * @param playerIdx
     * @param pidTar    查看玩家信息
     */
    public void seePlayerInfo(String playerIdx, String pidTar) {
        CrossArena.SC_CrossArenaPlayerById.Builder msg = CrossArena.SC_CrossArenaPlayerById.newBuilder();
        msg.setRetCode((GameUtil.buildRetCode(RetCodeEnum.RCE_Success)));
        // 查看的是否是自己
        if (Objects.equals(playerIdx, pidTar)) {
            playercrossarenaEntity pe = getPlayerEntity(playerIdx);
            // 获取当前场景ID，第一次进入默认进入当前ID
            CrossArenaPlyCacheRAM ram = createPlyCacheRAM(pe, 0, 1, 1);
            msg.setInfo(ram.getAllInfo());
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPlayerById_VALUE, msg);
            return;
        }
        // 查看的是否是本服玩家
        if (null != playerCache.getByIdx(pidTar)) {
            playercrossarenaEntity pe = getPlayerEntity(pidTar);
            // 获取当前场景ID，第一次进入默认进入当前ID
            CrossArenaPlyCacheRAM ram = createPlyCacheRAM(pe, 0, 1, 1);
            msg.setInfo(ram.getAllInfo());
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPlayerById_VALUE, msg);
            return;
        }
        if (playerPosMap.containsKey(pidTar)) {
            msg.setInfo(playerPosMap.get(pidTar).getAllInfo());
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPlayerById_VALUE, msg);
        } else {
            CrossArenaPlyCacheRAM ram = playerPosMap.get(playerIdx);
            if (null != playerCache.getByIdx(pidTar)) {
                playercrossarenaEntity pv = playercrossarenaCache.getByIdx(pidTar);
                ram = createPlyCacheRAM(pv, 0, 1, 1);
            }
            if (null != ram) {
                CrossArenaPlyCacheRAM temp = cacheInfoSyn.getOrDefault(ram.getSceneId(), new ConcurrentHashMap<>()).get(pidTar);
                if (null != temp) {
                    msg.setInfo(temp.getAllInfo());
                    GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPlayerById_VALUE, msg);
                }
            }
            msg.setRetCode((GameUtil.buildRetCode(RetCodeEnum.RCE_CrossArena_OFFLINE)));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPlayerById_VALUE, msg);
        }
    }

    public void readyFight(String playerIdx, int state) {

        CrossArena.SC_CrossArenaReadyFight.Builder msg = CrossArena.SC_CrossArenaReadyFight.newBuilder();
        msg.setRetCode((GameUtil.buildRetCode(RetCodeEnum.RCE_Success)));
        int hasAtTable = hasAtTable(playerIdx);
        if (hasAtTable <= 0) {
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaReadyFight_VALUE, msg);
            return;
        }
        String tableLockKey = GameConst.RedisKey.CrossArenaTableLock + "" + hasAtTable;
        if (!JedisUtil.lockRedisKey(tableLockKey, 3000l)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Failure));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaReadyFight_VALUE, msg);
            return;
        }
        String createRedisKeyLT = createRedisKeyLT(hasAtTable);
        byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT.getBytes());
        if (null == oneLeiTaiDB) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_CrossArena_TableStop));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaReadyFight_VALUE, msg);
            return;
        }
        try {
            RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
            List<Integer> readyStateList = tableDB.getReadyStateList();
            List<Integer> newReadyStateList = new ArrayList<>();
            boolean sameServer = false;
            boolean att = false;
            if (tableDB.getAttPlayer().getSvrIndex() > 0 && tableDB.getAttPlayer().getSvrIndex() == tableDB.getDefPlayer().getSvrIndex()) {
                sameServer = true;
            }
            if (readyStateList.size() == 0) {
                readyStateList = new ArrayList<Integer>();
                readyStateList.add(0);
                readyStateList.add(0);
            }
            if (Objects.equals(tableDB.getAttPlayer().getPlayerId(), playerIdx)) {
                newReadyStateList.add(readyStateList.get(0));
                newReadyStateList.add(state);
                att = true;
            } else {
                newReadyStateList.add(state);
                newReadyStateList.add(readyStateList.get(1));
            }
            Builder builder = tableDB.toBuilder();

            builder.clearReadyState();
            builder.addAllReadyState(newReadyStateList);
            saveLtDataToCache(builder);
            putTableNextCanTickTime(tableDB.getLeitaiId(),GlobalTick.getInstance().getCurrentTime());
            JedisUtil.unlockRedisKey(tableLockKey);

            String noticePlayer = "";
            int noticeSvrIndex = 0;
            String noticeSvrIp = ""; // 兼容代码
            if (att) {
                noticePlayer = tableDB.getDefPlayer().getPlayerId();
                noticeSvrIndex = tableDB.getDefPlayer().getSvrIndex();
                noticeSvrIp = tableDB.getDefPlayer().getFormIpPort();
            } else {
                noticePlayer = tableDB.getAttPlayer().getPlayerId();
                noticeSvrIndex = tableDB.getAttPlayer().getSvrIndex();
                noticeSvrIp = tableDB.getAttPlayer().getFormIpPort();
            }
            if (sameServer) {
                noticeReadyFight(playerIdx, tableDB.getStateEndTime(), tableDB.getReadyStateList(), att ? 1 : 0);
                noticeReadyFight(noticePlayer, tableDB.getStateEndTime(), tableDB.getReadyStateList(), att ? 0 : 1);
            } else {
                // 获取本擂台缓存数据中心服连接
                int svrIndex = findSynCacheServerIndex();
                BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(svrIndex);
                if (null == bnc) {
                    LogUtil.error("该服务器没有查询到战场服地址=" + svrIndex);
                    return;
                }
                GS_BS_CrossArenaReadyFight.Builder noticeBuilder = GS_BS_CrossArenaReadyFight.newBuilder();
                noticeBuilder.setPlayerId(noticePlayer);
                noticeBuilder.setFromIp(noticeSvrIp);
                noticeBuilder.setSvrIndex(noticeSvrIndex);
                noticeBuilder.addAllState(tableDB.getReadyStateList());
                noticeBuilder.setAtt(att ? 1 : 0);
                bnc.send(MsgIdEnum.SC_CrossArenaReadyFight_VALUE, noticeBuilder);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaReadyFight_VALUE, msg);
    }

    private void putTableNextCanTickTime(int tableId, long stateEndTime) {
        jedis.hset(CrossArenaTableStateEndTime, tableId+"", stateEndTime+"");
    }

    public void noticeReadyFight(String playerId, long endTime, List<Integer> stateList, int att) {
        SC_CrossArenaTableReadyFight.Builder builder = SC_CrossArenaTableReadyFight.newBuilder();
        builder.setEndtime(endTime);
        builder.addAllState(stateList);
        builder.setAtt(att);
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaTableReadyFight_VALUE, builder);
    }

    /**
     * 保存玩家数据到redis缓存(暂时不改为redis方式，可以改该方式)
     *
     * @param cache
     */
    public void savePlayerCacheToRedis(CrossArenaPlyCacheRAM cache) {
        jedis.hset(GameConst.RedisKey.CrossArenaPlCache.getBytes(), cache.getPid().getBytes(), cache.toByteArray());
    }

    /**
     * @param pe
     * @param senceId
     * @param viewPos
     * @param curPos  创建玩家缓存信息至跨服
     * @return
     */
    private CrossArenaPlyCacheRAM createPlyCacheRAM(playercrossarenaEntity pe, int senceId, int viewPos, int curPos) {
        CrossArenaPlyCacheRAM.Builder msg = CrossArenaPlyCacheRAM.newBuilder();
        msg.setPid(pe.getIdx());
        msg.setSceneId(senceId);
        msg.setViewPos(viewPos);
        msg.setCurrPos(curPos);
        msg.setFormIpPort(ServerConfig.getInstance().getIp());
        msg.setRefTime(System.currentTimeMillis() + 300000L);
        CrossArena.CrossArenaPlyInfoBase.Builder msgb = CrossArena.CrossArenaPlyInfoBase.newBuilder();
        Battle.PlayerBaseInfo.Builder playerInfo = BattleUtil.buildPlayerBattleBaseInfo(pe.getIdx());
        if (playerInfo != null) {
            msgb.setPlayerIdx(playerInfo.getPlayerId());
            msgb.setName(playerInfo.getPlayerName());
            msgb.setHead(playerInfo.getAvatar());
            msgb.setTitleId(playerInfo.getNewTitleId());
        }
        msgb.setCurrScreenIdId(viewPos);
        CrossArena.CrossArenaPlyViewAll.Builder msgall = CrossArena.CrossArenaPlyViewAll.newBuilder();
        msgall.setBase(msgb);
        for (Map.Entry<Integer, Integer> ent : pe.getDataMsg().getDbsMap().entrySet()) {
            msgall.addDbKey(CrossArenaDBKey.forNumber(ent.getKey()));
            msgall.addDbKeyVue(ent.getValue());
        }
        Map<Integer, Integer> winTemp = pe.getDataMsg().getLtPetWinNumMap();
        for (Map.Entry<Integer, Integer> ent : pe.getDataMsg().getLtPetNumMap().entrySet()) {
            float rate = winTemp.getOrDefault(ent.getKey(), 0) * 1F / ent.getValue() * 10000F;
            msgall.addDbPetId(ent.getKey());
            msgall.addDbPetRate(Math.round(rate));
        }
        msg.setAllInfo(msgall);
        return msg.build();
    }

    /**
     * @param playerIdx 玩家进入擂台玩法大场景
     */
    public void jionScene(String playerIdx) {
        SC_CrossArenaPanel.Builder msg = SC_CrossArenaPanel.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPanel_VALUE, msg);
            return;
        }
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        // 获取当前场景ID，第一次进入默认进入当前ID
        int scid = getPlayerDBInfo(pe, CrossArenaDBKey.LT_SCENEID);
        scid = scid < 1 ? 1 : scid;
        // 需要判断是否是新加入
        CrossArenaPlyCacheRAM cache = playerPosMap.get(playerIdx);
        if (null == cache) {
            cache = createPlyCacheRAM(pe, scid, 1, 1);
            RetCodeEnum rce = updatePlayerPos(playerIdx, cache);
            if (rce != RetCodeEnum.RCE_Success) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPanel_VALUE, msg);
                return;
            }
            String time = GlobalTick.getInstance().getCurrentTime() + "";
            // 保存玩家数据
            jedis.hset(GameConst.RedisKey.CrossArenaPlOnline, playerIdx, time);
            playerPosMap.put(playerIdx, cache);
            playerJionTime.put(playerIdx, GlobalTick.getInstance().getCurrentTime());
        }
        // 打开主面板信息(表示玩家进入了该玩法)
        sendMainPanelInfo(playerIdx);
        // 进入擂台
        checkHuoYueDay(pe);
        //refPlayerView(playerIdx);
        //refPlayerInfoViewAll(scid);
    }

    /**
     * @param playerIdx
     * @param senceId   玩家选择进入擂台场景
     */
    public void changeScene(String playerIdx, int senceId) {

        CrossArena.SC_CrossArenaChoose.Builder msg = CrossArena.SC_CrossArenaChoose.newBuilder();
        int maxStageId = getMaxStageId(playerIdx);
        if (senceId > maxStageId) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Failure));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaChoose_VALUE, msg);
            return;
        }
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        if (!CrossArenaScene._ix_id.containsKey(senceId)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaChoose_VALUE, msg);
            return;
        }
        updatePlayerNowScienceId(playerIdx, senceId, pe);
        CrossArenaPlyCacheRAM cacheOld = playerPosMap.get(playerIdx);
        if (null == cacheOld) {
            jionScene(playerIdx);
            return;
        } else {
            if (cacheOld.getSceneId() == senceId) {
                msg.setSceneId(senceId);
                msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaChoose_VALUE, msg);
                return;
            }
        }
        quitAll(playerIdx, true);
        CrossArenaPlyCacheRAM cache = createPlyCacheRAM(pe, senceId, 1, 1);
        RetCodeEnum rce = updatePlayerPos(playerIdx, cache);
        if (rce == RetCodeEnum.RCE_Success) {
            msg.setSceneId(senceId);
            msg.setRetCode(GameUtil.buildRetCode(rce));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaChoose_VALUE, msg);

            playerPosMap.put(playerIdx, cache);
            refPlayerView(playerIdx);
            //refPlayerInfoViewAll(senceId);
            sendMainPanelInfo(playerIdx);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaChoose_VALUE, msg);
    }

    private void updatePlayerNowScienceId(String playerIdx, int senceId, playercrossarenaEntity pe) {
        if (findPlayerNowInSceneId(playerIdx) == senceId) {
            return;
        }
        LogUtil.info("lt player:{} updatePlayerNowScienceId:{}", playerIdx, senceId);
        SyncExecuteFunction.executeConsumer(pe, t -> {
            CrossArenaDB.DB_LTSerialWin winData = pe.getDataMsg().getSerialWinDataMap().getOrDefault(senceId, CrossArenaDB.DB_LTSerialWin.getDefaultInstance());
            pe.getDataMsg().putDbs(LT_WINCOTDAY_VALUE, winData.getWinDataOrDefault(LT_WINCOTDAY_VALUE, 0));
            pe.getDataMsg().putDbs(LT_WINCOTWeekly_VALUE, winData.getWinDataOrDefault(LT_WINCOTWeekly_VALUE, 0));
            pe.getDataMsg().putDbs(LT_WINCOTCUR_VALUE,0);
            pe.getDataMsg().putDbs(LT_10SerialWinWeek_VALUE,winData.getWinDataOrDefault(LT_10SerialWinWeek_VALUE, 0));
            pe.getDataMsg().putDbs(CrossArenaDBKey.LT_SCENEID_VALUE, senceId);
        });
    }

    public int getBossFightMakeId(String playerId) {
        int maxStageId = getMaxStageId(playerId);
        CrossArenaSceneObject config = CrossArenaScene.getById(maxStageId + 1);
        if (config == null) {
            return 0;
        }
        return config.getBoss();
    }

    public void changeStageId(String playerId) {
        playercrossarenaEntity pe = getPlayerEntity(playerId);

        int maxStageId = getMaxStageId(playerId);
        SyncExecuteFunction.executeConsumer(pe, p -> {
            p.getDataMsg().putDbs(CrossArenaDBKey.LT_MAXSCENEID_VALUE, maxStageId + 1);
        });
        LogUtil.info("lt change player:{} stageId,now maxStageId:{}", playerId, maxStageId + 1);
        sendMainPanelInfo(playerId);
        LogService.getInstance().submit(new PlayerTargetLog(playerId, TTE_CrossArena_SCENEIDReach, maxStageId + 1));
    }

    private int getMaxStageId(String playerIdx) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        int maxStageId = 1;

        if (pe.getDataMsg().getDbsMap().containsKey(CrossArenaDBKey.LT_MAXSCENEID_VALUE)) {
            maxStageId = pe.getDataMsg().getDbsMap().get(CrossArenaDBKey.LT_MAXSCENEID_VALUE);
        }
        return maxStageId;
    }

    public int findPlayerDbsDataByKey(String playerIdx,CrossArenaDBKey key){
        if (key==null){
            return 0;
        }
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        return pe.getDataMsg().getDbsMap().getOrDefault(key.getNumber(),0);
    }

    /**
     * @param playerIdx
     * @param viewPos
     * @param curPos
     * @param isPlay    移动视野
     */
    public void movePos(String playerIdx, int viewPos, int curPos, int isPlay) {
        CrossArena.SC_CrossArenaMove.Builder msg = CrossArena.SC_CrossArenaMove.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaMove_VALUE, msg);
            return;
        }
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        CrossArenaPlyCacheRAM capp = playerPosMap.getOrDefault(playerIdx, null);
        if (null == capp) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_CrossArena_CHOOSE));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaMove_VALUE, msg);
            return;
        } else {
            if (capp.getViewPos() != viewPos) {
                if (System.currentTimeMillis() > capp.getRefTime()) {
                    capp = createPlyCacheRAM(pe, capp.getSceneId(), viewPos, curPos);
                }
                CrossArenaPlyCacheRAM.Builder cappNew = capp.toBuilder();
                cappNew.setViewPos(viewPos);
                cappNew.setCurrPos(curPos);
                RetCodeEnum rce = updatePlayerPos(playerIdx, capp);
                if (rce == RetCodeEnum.RCE_Success) {
                    playerPosMap.put(playerIdx, cappNew.build());
                    refPlayerView(playerIdx);
                }
                msg.setRetCode(GameUtil.buildRetCode(rce));
            }
        }
   /*     if (isPlay > 0) {
            // 下发人员信息
           // refPlayerInfoView(playerIdx, capp.getSceneId());
        }*/
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        msg.setCurrScreenIdId(viewPos);
        sendMainPanelInfo(playerIdx);
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaMove_VALUE, msg);
    }

    /**
     * 更新玩家位置信息
     */
    public RetCodeEnum updatePlayerPos(String playerIdx, CrossArenaPlyCacheRAM cacheRam) {
        try {
            // 判断该擂台是否工作中
            if (!hasTable()) {
                return RetCodeEnum.RCE_CrossArena_TableStop;
            }
            // 获取本擂台缓存数据中心服连接
            int svrIndex = findSynCacheServerIndex();
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(svrIndex);
            if (null == bnc) {
                LogUtil.error("该服务器没有查询到战场服地址=" + svrIndex);
                return RetCodeEnum.RCE_CrossArena_TableStop;
            }
            // 刷新玩家视野信息
            ServerTransfer.GS_BS_CrossArenaPos.Builder msgatt = ServerTransfer.GS_BS_CrossArenaPos.newBuilder();
            msgatt.setOper(0);
            msgatt.setAllInfo(cacheRam);
            bnc.send(MessageId.MsgIdEnum.GS_BS_CrossArenaPos_VALUE, msgatt);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return RetCodeEnum.RCE_CrossArena_TableStop;
        }
        return RetCodeEnum.RCE_Success;
    }

    /**
     * 检查活跃天数
     */
    public void checkHuoYueDay(playercrossarenaEntity pe) {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (DateUtil.isToday(pe.getDataMsg().getJionTime(), curTime)) {
            return;
        }
        SyncExecuteFunction.executeConsumer(pe, p -> {
            p.getDataMsg().setJionTime(curTime);
            p.getDataMsg().setHyDay(p.getDataMsg().getHyDay() + 1);
            p.getDataMsg().setIsAward(0);
        });
        CrossArenaHonorManager.getInstance().honorVueByKeyAdd(pe.getIdx(), CrossArenaUtil.HR_DAY, 1);
        if (pe.getDataMsg().getHyDay() >= 90) {
            CrossArenaHonorManager.getInstance().honorVueFirst(pe.getIdx(), CrossArenaUtil.HR_LT90DAY);
        }
    }

    /**
     * @param playerIdx 下发主面板显示信息
     */
    public void sendMainPanelInfo(String playerIdx) {
        SC_CrossArenaPanel.Builder msg = SC_CrossArenaPanel.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPanel_VALUE, msg);
            return;
        }
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        int currLv = getPlayerDBInfo(pe, CrossArenaDBKey.LT_GRADELV);
        CrossArenaLvCfgObject lvCfg = CrossArenaLvCfg.getByLv(currLv);
        if (null != lvCfg) {
            msg.setMaxGrade(lvCfg.getNextlvlexp());
        }
        int sceneId = getPlayerDBInfo(pe, CrossArenaDBKey.LT_SCENEID);
        msg.setCurrWinNum(getPlayerDBInfo(pe, CrossArenaDBKey.LT_WINCOTCUR));
        msg.setDayWinNum(getPlayerDBInfo(pe, CrossArenaDBKey.LT_WINCOTDAY));
        msg.setCurrGrade(getPlayerDBInfo(pe, CrossArenaDBKey.LT_GRADECUR));
        msg.setLvGrade(getPlayerDBInfo(pe, CrossArenaDBKey.LT_GRADELV));
        if (isOpenDub) {
            msg.setLastSerialWin(getPlayerDBInfo(pe, LT_LastSerialWinNum));
        }
        msg.setSceneId(sceneId);
        int maxSceneId = getPlayerDBInfo(pe, LT_MAXSCENEID);
        msg.setMaxSceneId(maxSceneId);
        msg.setRefreshTime(TimeUtil.getNextWeekStamp(GlobalTick.getInstance().getCurrentTime()));
        msg.setTodayUseWinProtect(pe.getDataMsg().getTodayUseWinProtect());
        msg.setCurFreeAttTime(pe.getDataMsg().getCurFreeLtAttTimes());
        msg.setWinTaskRewardRate(findWinTaskRewardReduceRate(playerIdx, sceneId, maxSceneId));
        msg.setCurWeekSerialWin(getPlayerDBInfo(pe, LT_WINCOTWeekly));
        CrossArenaPlyCacheRAM myarm = playerPosMap.get(playerIdx);
        if (null != myarm) {
            msg.setSceneId(myarm.getSceneId());
//			msg.setCurrSceneId(myarm.getSceneId());
            msg.setCurrScreenId(myarm.getCurrPos());
            if (sceneId > 0 && sceneId < 6) {
                msg.setQueId(claWaitTime(playerIdx, sceneId));
            }
            int tid = hasAtTable(playerIdx);
            if (tid > 0) {
                msg.setTableId(tid);
            }
        }
        setTenActivityTime(msg);
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPanel_VALUE, msg);
        sendPlayerEvents(playerIdx);
    }

    private static final int onePersonWaitTime = 90;

    private int claWaitTime(String playerIdx, int sceneId) {
        String keyQue = GameConst.RedisKey.CrossArenaQue + "" + sceneId;
        List<String> list = jedis.lrange(keyQue, 0, -1);
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        int waitPerson = list.indexOf(playerIdx);
        int tableNum = findTableNum(sceneId);
        if (waitPerson <= 0 || tableNum <= 0) {
            return 0;
        }
        CrossArenaSceneObject cfg = CrossArenaScene.getById(sceneId);
        int waitUnit = cfg == null ? onePersonWaitTime : cfg.getVstime();
        return Math.max(waitUnit, waitPerson / tableNum * waitUnit);
    }

    private void setTenActivityTime(SC_CrossArenaPanel.Builder msg) {
        ActivityData activityData = TimeRuleManager.getInstance().getActivityData(2);
        if (activityData == null) {
            return;
        }
        if (isOpenDub) {
            msg.setTenCloseTime(activityData.getCloseTime());
        } else {
            msg.setTenOpenTime(activityData.getNextOpenTime());
        }
    }

    /**
     * @param playerIdx
     * @return 创建玩家数据
     */
    public playercrossarenaEntity getPlayerEntity(String playerIdx) {
        playercrossarenaEntity pe = playercrossarenaCache.getByIdx(playerIdx);
        if (null == pe) {
            pe = new playercrossarenaEntity();
            pe.setIdx(playerIdx);
            try {
                pe.toBuilder();
                pe.getDataMsg().putDbs(CrossArenaDBKey.LT_GRADELV_VALUE, 1);
                pe.getDataMsg().putDbs(CrossArenaDBKey.LT_SCENEID_VALUE, 1);
                pe.getDataMsg().putDbs(CrossArenaDBKey.LT_MAXSCENEID_VALUE, 1);
                pe.getDataMsg().setFirstJoinTime(GlobalTick.getInstance().getCurrentTime());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            SyncExecuteFunction.executeConsumer(pe, cacheTemp -> {
                // 保存数据
            });
        }
        return pe;
    }

    public CrossArena.CrossArenaOneInfo findTableOneInfo(String playerIdx, int tableId) {
        byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
        try {
            if (null != oneLeiTaiDB) {
                RedisCrossArenaTableDB tableInfo = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
                return tableInfoDBToMsgOne(tableInfo, playerIdx);
            }
        } catch (Exception ex) {
            LogUtil.printStackTrace(ex);
            return null;
        }
        return null;
    }

    public void sendTableOneInfo(String playerIdx, int tableId) {
        CrossArena.SC_CrossArenaTableOneInfo.Builder msg = CrossArena.SC_CrossArenaTableOneInfo.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        try {
            if (!hasTable()) {
                return;
            }
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
            if (null != oneLeiTaiDB) {
                RedisCrossArenaTableDB tableInfo = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
                msg.setTable(tableInfoDBToMsgOne(tableInfo, playerIdx));
                if (null != tableInfo.getDefPlayer()) {
                    msg.setDefBattleInfo(tableInfo.getDefPlayer().getTeamInfo());
                }
            }
            // 数据转换为可操作数据
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTableOneInfo_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void refPlayerInfoViewAll(int scendId) {
        Map<String, CrossArenaPlyCacheRAM> sceneMap = cacheInfoSyn.get(scendId);
        Map<String, CrossArenaPlyCacheRAM> res = new ConcurrentHashMap<>();
        if (null != sceneMap) {
            res.putAll(sceneMap);
        }
        if (null == sceneMap) {
            return;
        }
        List<CrossArenaPlyCacheRAM> temp = new ArrayList<>();
        for (CrossArenaPlyCacheRAM ent : sceneMap.values()) {
            if (res.size() < 100) {
                // 人数不够需要补充机器人
                res.putAll(cacheInfoMachine);
            }
            temp.addAll(res.values());
            Collections.shuffle(temp);
            if (temp.size() > 50) {
                temp = temp.subList(0, 50);
            }
            if (!playerPosMap.containsKey(ent.getPid())) {
                continue;
            }
            CrossArena.SC_CrossArenaPlayers.Builder msg2 = CrossArena.SC_CrossArenaPlayers.newBuilder();
            for (CrossArenaPlyCacheRAM capr : temp) {
                msg2.addInfos(capr.getAllInfo().getBase());
            }
            if (playerPosMap.containsKey(ent.getPid())) {
                msg2.addInfos(playerPosMap.get(ent.getPid()).getAllInfo().getBase());
            }
            GlobalData.getInstance().sendMsg(ent.getPid(), MessageId.MsgIdEnum.SC_CrossArenaPlayers_VALUE, msg2);
        }
    }

    public void refPlayerInfoView(String playerIdx, int scendId) {
        // 下发人员信息
        Map<String, CrossArenaPlyCacheRAM> sceneMap = cacheInfoSyn.get(scendId);
        Map<String, CrossArenaPlyCacheRAM> res = new ConcurrentHashMap<>();
        if (null != sceneMap) {
            res.putAll(sceneMap);
        }
        List<CrossArenaPlyCacheRAM> temp = new ArrayList<>();
        if (res.size() < 100) {
            // 人数不够需要补充机器人
            res.putAll(cacheInfoMachine);
        }
        temp.addAll(res.values());
        Collections.shuffle(temp);
        if (temp.size() > 50) {
            temp = temp.subList(0, 50);
        }
        CrossArena.SC_CrossArenaPlayers.Builder msg2 = CrossArena.SC_CrossArenaPlayers.newBuilder();
        for (CrossArenaPlyCacheRAM capr : temp) {
            msg2.addInfos(capr.getAllInfo().getBase());
        }
        if (playerPosMap.containsKey(playerIdx)) {
            msg2.addInfos(playerPosMap.get(playerIdx).getAllInfo().getBase());
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaPlayers_VALUE, msg2);
    }

    /**
     * @param playerIdx 刷新玩家机器人视野信息
     */
    public void refPlayerView(String playerIdx) {
        CrossArenaPlyCacheRAM ram = playerPosMap.get(playerIdx);
        if (null == ram) {
            return;
        }
        try {
            if (!hasTable()) {
                return;
            }
            int pos = ram.getViewPos();
            // 根据位置计算出全部相关桌子
            int posStart = (pos - 1) * CrossArenaUtil.SCREEN_TABLENUM;
            int posEnd = (pos + 1) * CrossArenaUtil.SCREEN_TABLENUM;
            List<RedisCrossArenaTableDB> temp = new ArrayList<>();
            CrossArena.SC_CrossArenaTableInfo.Builder msg = CrossArena.SC_CrossArenaTableInfo.newBuilder();
            List<Integer> allTableByScene = findAllTableByScene(ram.getSceneId());
            for (int i = posStart; i < posEnd; i++) {
                if (i < 0 || i >= allTableByScene.size()) {
                    continue;
                }
                int tableId = allTableByScene.get(i);
                byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
                if (null == oneLeiTaiDB) {
                    continue;
                }
                RedisCrossArenaTableDB tableInfo = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
                temp.add(tableInfo);
            }
            // 数据转换为可操作数据
            msg.addAllTables(tableInfoDBToMsg(temp, playerIdx));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTableInfo_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    private int findTableNum(int sceneId) {
        String tableNumStr = jedis.hget(RedisKey.CrossArenaTableNum, sceneId + "");
        return NumberUtils.toInt(tableNumStr, 4);
    }

    public void refPlayerView(String playerIdx, int tableId) {
        RedisCrossArenaTableDB tableInfo = getTableDBInfo(tableId);
        if (null != tableInfo) {
            CrossArena.SC_CrossArenaTableInfo.Builder msg = CrossArena.SC_CrossArenaTableInfo.newBuilder();
            msg.addAllTables(tableInfoDBToMsg(tableInfo, playerIdx));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTableInfo_VALUE, msg);
        }
    }

    public RedisCrossArenaTableDB getTableDBInfo(int tableId) {
        RedisCrossArenaTableDB tableInfo = null;
        try {
            if (!hasTable()) {
                return null;
            }
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
            if (null == oneLeiTaiDB) {
                return null;
            }
            // 数据转换为可操作数据
            tableInfo = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return tableInfo;
    }

    /**
     * @param tableId 擂台数据变动后需要主动刷新数据给视野玩家
     */
    public void tableChangeAfter(int tableId) {
        RedisCrossArenaTableDB tableInfo = getTableDBInfo(tableId);
        if (null == tableInfo) {
            return;
        }
        tableChangeAfter(tableInfo);
    }

    /**
     * @param tableInfo 擂台数据变动后需要主动刷新数据给视野玩家
     */
    public void tableChangeAfter(RedisCrossArenaTableDB tableInfo) {
        List<RedisCrossArenaTableDB> temp = new ArrayList<>();
        temp.add(tableInfo);

        Set<String> players = findTableChangeNeedPushPlayerIds(tableInfo.getLeitaiId());
        if (CollectionUtils.isEmpty(players)) {
            return;
        }

        for (String player : players) {
            if (!playerPosMap.containsKey(player)||BattleManager.getInstance().isInBattle(player)) {
                continue;
            }
            CrossArena.SC_CrossArenaTableInfo.Builder msg = CrossArena.SC_CrossArenaTableInfo.newBuilder();
            msg.addAllTables(tableInfoDBToMsg(temp, player));
            GlobalData.getInstance().sendMsg(player, MessageId.MsgIdEnum.SC_CrossArenaTableInfo_VALUE, msg);
        }
    }



    public List<CrossArena.CrossArenaOneInfo> tableInfoDBToMsg(RedisCrossArenaTableDB dbData, String playerIdx) {
        List<RedisCrossArenaTableDB> temp = new ArrayList<>();
        temp.add(dbData);
        return tableInfoDBToMsg(temp, playerIdx);
    }

    /**
     * @param dbData
     * @param playerIdx 擂台DB数据转换为消息数据
     * @return
     */
    public List<CrossArena.CrossArenaOneInfo> tableInfoDBToMsg(List<RedisCrossArenaTableDB> dbData, String playerIdx) {
        List<CrossArena.CrossArenaOneInfo> result = new ArrayList<CrossArena.CrossArenaOneInfo>();
        for (RedisCrossArenaTableDB oneDB : dbData) {
            result.add(tableInfoDBToMsgOne(oneDB, playerIdx));
        }
        return result;
    }

    /**
     * @param oneDB
     * @param playerIdx 擂台DB数据转换为消息数据
     * @return
     */
    public CrossArena.CrossArenaOneInfo tableInfoDBToMsgOne(RedisCrossArenaTableDB oneDB, String playerIdx) {
        CrossArena.CrossArenaOneInfo.Builder msgOne = CrossArena.CrossArenaOneInfo.newBuilder();
        msgOne.setTableId(oneDB.getLeitaiId());
        msgOne.setTableSort(getTableNumByTableId(oneDB.getLeitaiId()));
        msgOne.setState(CrossArena.CrossArenaState.forNumber(oneDB.getState()));
        if (null != oneDB.getDefPlayer()) {
            msgOne.setPlayerDef(oneDB.getDefPlayer().getTeamInfo().getPlayerInfo());
        }
        if (null != oneDB.getAttPlayer()) {
            msgOne.setPlayerAtt(oneDB.getAttPlayer().getTeamInfo().getPlayerInfo());
        }
        msgOne.setZanDef(oneDB.getGuessDefSvrDataCount());
        msgOne.setZanAtt(oneDB.getGuessAttSvrDataCount());
        msgOne.setDefTime(oneDB.getDefTime());
        msgOne.setDefWinNum(oneDB.getDefWinNum());
        int isGuess = 0;
        if (oneDB.containsGuessDefSvrData(playerIdx)) {
            isGuess = 1;
        } else if (oneDB.containsGuessAttSvrData(playerIdx)) {
            isGuess = 2;
        }
        for (RedisCrossArenaPlayer ent : oneDB.getDuiList()) {
            CrossArena.CrossArenaQueue.Builder msg = CrossArena.CrossArenaQueue.newBuilder();
            msg.setPlayerName(ent.getName());
            msg.setPower(ent.getPower());
            msg.setTableId(0);
            msg.setWinNum(ent.getDefNum());
            msg.setPlayerId(ent.getPlayerId());
            msgOne.addQueue(msg);
        }
        msgOne.setShowPetIdDef(oneDB.getDefPlayer().getShowPetId());
        msgOne.setShowPetIdAtt(oneDB.getAttPlayer().getShowPetId());
        msgOne.setIsGuess(isGuess);
        msgOne.setStateEndTime(oneDB.getStateEndTime());
        return msgOne.build();
    }

    /**
     * @param playerIdx
     * @param tableId   请求观战
     */
    public void reqViewFight(String playerIdx, int tableId) {
        CrossArena.SC_CrossArenaViewFight.Builder msg = CrossArena.SC_CrossArenaViewFight.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaViewFight_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        try {
            if (!hasTable()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaViewFight_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            // 判断该擂台是否工作中
            int svrIndex = findTableWorkServer(tableId);
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(svrIndex);
            if (null == bnc) {
                LogUtil.error("该服务器没有查询到战场服地址=" + svrIndex);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaViewFight_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
            if (null == oneLeiTaiDB) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaViewFight_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            // 数据转换为可操作数据
            RedisCrossArenaTableDB oneLeiTaiProtoDB = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != CrossArena.CrossArenaState.FIGHT_VALUE) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_BattleEnd);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaViewFight_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            if (oneLeiTaiProtoDB.getIsAIBattle() > 0) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_AIBATTLE);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaViewFight_VALUE, msg);
                return;
            }
            // 执行观战逻辑，根据战斗ID处理
            BattleManager.getInstance().sendBattleServerBattleWatch(bnc, String.valueOf(oneLeiTaiProtoDB.getBattleId()), playerIdx);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaViewFight_VALUE, msg);
    }

    /**
     * @param playerIdx 玩家退出全部擂台(队列中和擂台上)
     * @param clearLastSerialWin
     */
    public void quitAll(String playerIdx, boolean clearLastSerialWin) {
        CrossArena.SC_CrossArenaAtt.Builder msg = CrossArena.SC_CrossArenaAtt.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        playercrossarenaEntity pce = getPlayerEntity(playerIdx);
        int sceneId = getPlayerDBInfo(pce, CrossArenaDBKey.LT_SCENEID);
        clearPlayerDbDataAfterQuitAll(pce,clearLastSerialWin);
        try {
            String queLockKey = GameConst.RedisKey.CrossArenaQueLock + "" + sceneId;
            // 优先退出队列
            // 多客户端操作同队列，需要给队列上锁
            if (JedisUtil.lockRedisKey(queLockKey, 3000l)) {
                String keyQue = GameConst.RedisKey.CrossArenaQue + "" + sceneId;
                List<String> list = jedis.lrange(keyQue, 0, -1);
                if (null != list && list.contains(playerIdx)) {
                    jedis.lrem(keyQue, 0, playerIdx);
                }
                JedisUtil.unlockRedisKey(queLockKey);
            }
            int tableId = hasAtTable(playerIdx);
            if (tableId <= 0) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
            String tableLockKey = GameConst.RedisKey.CrossArenaTableLock + "" + tableId;
            if (!JedisUtil.lockRedisKey(tableLockKey, 3000l)) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
            if (null == oneLeiTaiDB) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_CrossArena_TableStop));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
            RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
            RedisCrossArenaTableDB.Builder newTableDB = null;
            LogUtil.info("lt player:{} quitAll ,tableDB:{}", playerIdx, tableDB);
            if (Objects.equals(tableDB.getDefPlayer().getPlayerId(), playerIdx)) {
                // 擂主下台
                if (tableDB.getState() == CrossArena.CrossArenaState.FIGHT_VALUE) {
                    JedisUtil.unlockRedisKey(tableLockKey);
                    msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Battle_Batting));
                    GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                    LogUtil.info("lt player:{} is fighting,can`t quit", playerIdx);
                    return;
                }
                newTableDB = tbRedisQuitDef(tableDB);
            } else if (Objects.equals(tableDB.getAttPlayer().getPlayerId(), playerIdx)) {
                if (tableDB.getState() == CrossArena.CrossArenaState.FIGHT_VALUE) {
                    JedisUtil.unlockRedisKey(tableLockKey);
                    msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Battle_Batting));
                    GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                    LogUtil.info("lt player:{} is fighting,can`t quit", playerIdx);
                    return;
                }
                // 玩家是擂主,只有等待状态可以退出擂主
                newTableDB = tbRedisQuitAtt(tableDB);
            } else {
                newTableDB = tbRedisQuitQue(tableDB, playerIdx);
            }
            if (null != newTableDB) {
                saveLtDataToCache(newTableDB);
                // 通知全服刷新该擂台数据
                refTableInfoToAllServer(newTableDB.build());
            }
            JedisUtil.unlockRedisKey(tableLockKey);
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    private void clearPlayerDbDataAfterQuitAll(playercrossarenaEntity pce, boolean clearLastSerialWin) {
        if (pce == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(pce, p -> {
            p.getDataMsg().putDbs(LT_WINCOTCUR_VALUE, 0);
            if (clearLastSerialWin) {
                p.getDataMsg().putDbs(LT_LastSerialWinNum_VALUE, 0);
            }
        });
    }

    /**
     * @param playerIdx 推出匹配队列
     */
    public void quitQue(String playerIdx) {
        CrossArena.SC_CrossArenaAtt.Builder msg = CrossArena.SC_CrossArenaAtt.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        playercrossarenaEntity pce = getPlayerEntity(playerIdx);
        try {
            int sceneId = getPlayerDBInfo(pce, CrossArenaDBKey.LT_SCENEID);
            if (!hasTable()) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_CrossArena_TableStop));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
            String tableLockKey = GameConst.RedisKey.CrossArenaQueLock + "" + sceneId;
            // 多客户端操作同队列，需要给队列上锁
            if (JedisUtil.lockRedisKey(tableLockKey, 3000l)) {
                String keyQue = GameConst.RedisKey.CrossArenaQue + "" + sceneId;
                List<String> list = jedis.lrange(keyQue, 0, -1);
                if (null != list && list.contains(playerIdx)) {
                    jedis.lrem(keyQue, 0, playerIdx);
                }
                JedisUtil.unlockRedisKey(tableLockKey);
            }
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx 请求队列列表
     */
    public void getQue(String playerIdx, int sceneId) {
        CrossArena.SC_CrossArenaQueAll.Builder msg = CrossArena.SC_CrossArenaQueAll.newBuilder();
        try {
            String keyQue = GameConst.RedisKey.CrossArenaQue + "" + sceneId;
            List<String> list = jedis.lrange(keyQue, 0, -1);
            if (null == list) {
                list = new ArrayList<>();
            }
            int jj = 0;
            CrossArenaSceneObject caso = CrossArenaScene.getById(sceneId);
            if (null == caso) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaQueAll_VALUE, msg);
                return;
            }
            List<CrossArena.CrossArenaQueue> tempTables1 = new ArrayList<>();
            List<CrossArenaQueueTable> queueTables = new ArrayList<>();
            for (String pid : list) {
                if (jj > 50) {
                    break;
                }
                CrossArena.CrossArenaQueue.Builder msggg = CrossArena.CrossArenaQueue.newBuilder();

                byte[] plInfo = null;

                if (pid.contains(AINAME)) {
                    plInfo = jedis.hget(GameConst.RedisKey.CrossArenaRBPlayerInfo.getBytes(), pid.getBytes());
                } else {
                    plInfo = jedis.hget(GameConst.RedisKey.CrossArenaPlayerInfo.getBytes(), pid.getBytes());
                }
                if (null == plInfo) {
                    continue;
                }
                RedisCrossArenaPlayer plInfoDB = RedisCrossArenaPlayer.parseFrom(plInfo);
                if (null == plInfoDB) {
                    continue;
                }
                msggg.setPlayerName(plInfoDB.getName());
                msggg.setPower(plInfoDB.getTeamInfo().getPlayerInfo().getPower());
                msggg.addAllPet(createPetShowData(plInfoDB.getTeamInfo()));
                msggg.setPlayerId(plInfoDB.getPlayerId());
                msggg.setBase(plInfoDB.getTeamInfo().getPlayerInfo());
                jj++;
                tempTables1.add(msggg.build());
            }
            msg.setSceneId(sceneId);
            for (Integer tableId : findAllTableByScene(sceneId)) {
            // 遍历所有擂台，分配队列中玩家战斗
                // 获取擂台数据
                byte[] tableDBByte = jedis.get(createRedisKeyLT(tableId).getBytes());
                if (null == tableDBByte) {
                    continue;
                }
                RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(tableDBByte);
                queueTables.add(createQueueTable(tableDB, tableId));
            }
            msg.addAllQueue(tempTables1);
            msg.addAllQueueTable(queueTables);
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaQueAll_VALUE, msg);
            sendMainPanelInfo(playerIdx);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    private int findNowTableNum(int scienceId) {
        return findAllTableByScene(scienceId).size();
    }

    private List<Integer> findAllTableByScene(int sceneId) {
        Set<String> allTables = jedis.hkeys(CrossArenaBSSid + sceneId);
        List<Integer> tables = new ArrayList<>();
        int tableId;
        for (String table : allTables) {
            tableId = Integer.parseInt(table);
            tables.add(tableId);
        }
        tables.sort(Integer::compareTo);
        return tables;
    }

    private List<CrossArenaQueuePet> createPetShowData(BattlePlayerInfo battleInfo) {
        List<CrossArenaQueuePet> list = new ArrayList<>();

        for (BattlePetData pet : battleInfo.getPetListList()) {
            CrossArenaQueuePet.Builder builder = CrossArenaQueuePet.newBuilder();
            builder.setPetCfgId(pet.getPetCfgId());
            builder.setGrade(pet.getPetRarity());
            builder.setLv(pet.getPetLevel());
            list.add(builder.build());
        }
        return list;
    }

    public CrossArenaQueueTable createQueueTable(RedisCrossArenaTableDB tableDB, int tableId) {
        CrossArenaQueueTable.Builder builder = CrossArenaQueueTable.newBuilder();
        int tureTableId = tableId;
        if (tableDB.getDefPlayer().getJionTime() > 0) {
            CrossArena.CrossArenaQueue.Builder msggg = CrossArena.CrossArenaQueue.newBuilder();
            msggg.setPlayerName(tableDB.getDefPlayer().getName());
            msggg.setPower(tableDB.getDefPlayer().getPower());
            msggg.setTableId(tureTableId);
            msggg.setWinNum(tableDB.getDefPlayer().getDefNum());
            msggg.setPlayerId(tableDB.getDefPlayer().getPlayerId());
            builder.setDef(msggg);
        }
        if (tableDB.getAttPlayer().getJionTime() > 0) {
            CrossArena.CrossArenaQueue.Builder msggg = CrossArena.CrossArenaQueue.newBuilder();
            msggg.setPlayerName(tableDB.getAttPlayer().getName());
            msggg.setPower(tableDB.getAttPlayer().getPower());
            msggg.setTableId(tureTableId);
            msggg.setWinNum(tableDB.getAttPlayer().getDefNum());
            msggg.setPlayerId(tableDB.getAttPlayer().getPlayerId());
            builder.setAtt(msggg);
        }
        for (RedisCrossArenaPlayer ent : tableDB.getDuiList()) {
            CrossArena.CrossArenaQueue.Builder msggg = CrossArena.CrossArenaQueue.newBuilder();
            msggg.setPlayerName(ent.getName());
            msggg.setPower(ent.getPower());
            msggg.setTableId(tureTableId);
            msggg.setWinNum(0);
            msggg.setPlayerId(ent.getPlayerId());
            builder.addDui(msggg);
        }
        return builder.build();
    }

    /**
     * @param playerIdx 加入匹配队列(目前处理逻辑，直接本服修改redis数据。后期如果量太大失败频率太高则可以改为中心服调度)
     */
    public void jionQue(String playerIdx) {
        CrossArena.SC_CrossArenaAtt.Builder msg = CrossArena.SC_CrossArenaAtt.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        if (MatchArenaManager.getInstance().isMatching(playerIdx)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatchArena_PlayerIsMatching));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        playercrossarenaEntity pce = getPlayerEntity(playerIdx);
        CrossArenaPlyCacheRAM ram = playerPosMap.get(playerIdx);
        if (null == ram) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        int sf = FunctionExclusion.getInstance().checkExclusionAll(playerIdx);
        if (sf > 0) {
            msg.setRetCode(GameUtil.buildRetCode(FunctionExclusion.getInstance().getRetCodeByType(sf)));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        // 加入队列
        try {
            if (!hasTable()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableStop);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
            int sceneId = getPlayerDBInfo(pce, CrossArenaDBKey.LT_SCENEID);
            if (sceneId <= 0 || !CrossArenaScene._ix_id.containsKey(sceneId)) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_SCENENEP));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
            int tableId = hasAtTable(playerIdx);
            if (tableId > 0) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_ATTABLE);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
            RedisCrossArenaPlayer attPlayerInfo = getPlayerCrossArenaInfo(playerIdx);
            if (null == attPlayerInfo) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CP_TeamNotExists);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
//			if (jedis.sismember(RedisKey.CrossArenaTenPlayer, playerIdx)) {
//				retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CP_TeamNotExists);
//				msg.setRetCode(retCode);
//				GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
//				return;
//			}
            // 刷新一次玩家擂台赛队伍数据
            jedis.hset(GameConst.RedisKey.CrossArenaPlayerInfo.getBytes(), playerIdx.getBytes(), attPlayerInfo.toByteArray());
            // 开始操作队列，给队列上锁
            String QueLockKey = GameConst.RedisKey.CrossArenaQueLock + "" + sceneId;
            String keyQue = GameConst.RedisKey.CrossArenaQue + "" + sceneId;
            if (JedisUtil.lockRedisKey(QueLockKey, 3000l)) {
                List<String> list = jedis.lrange(keyQue, 0, -1);
                if (null != list && list.contains(playerIdx)) {
                    retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_ATQUE);
                    msg.setRetCode(retCode);
                    GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                    JedisUtil.unlockRedisKey(QueLockKey);
                    return;
                }
                // 成功加入该队列
                jedis.rpush(keyQue, playerIdx);
                JedisUtil.unlockRedisKey(QueLockKey);
                msg.setRetCode(retCode);
            } else {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_ATTABLE);
                msg.setRetCode(retCode);
            }
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            sendMainPanelInfo(playerIdx);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx 玩家保存雷塔赛队伍
     */
    public void saveCrossArenaTeam(String playerIdx) {
        RedisCrossArenaPlayer attPlayerInfo = getPlayerCrossArenaInfo(playerIdx);
        if (null == attPlayerInfo) {
            return;
        }
        jedis.hset(GameConst.RedisKey.CrossArenaPlayerInfo.getBytes(), playerIdx.getBytes(), attPlayerInfo.toByteArray());
    }

    /**
     * @param playerIdx
     * @param tableId   挑战擂台(逻辑服收到客户端消息处理逻辑，上行至中心服，等待中心服返回结果)
     */
    public void attTable(String playerIdx, int tableId) {
        CrossArena.SC_CrossArenaAtt.Builder msg = CrossArena.SC_CrossArenaAtt.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        int sf = FunctionExclusion.getInstance().checkExclusionAll(playerIdx);
        if (sf > 0) {
            msg.setRetCode(GameUtil.buildRetCode(FunctionExclusion.getInstance().getRetCodeByType(sf)));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        if (MatchArenaManager.getInstance().isMatching(playerIdx)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatchArena_PlayerIsMatching));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        playercrossarenaEntity pce = getPlayerEntity(playerIdx);
        CrossArenaPlyCacheRAM ram = playerPosMap.get(playerIdx);
        if (null == ram) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        CrossArenaSceneObject caso = CrossArenaScene.getById(getSceneIdByTableId(tableId));
        if (null == caso) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        int sceneId = getSceneIdByTableId(tableId);
        if (getPlayerDBInfo(pce, CrossArenaDBKey.LT_SCENEID) != sceneId) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_SCENENEP));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return;
        }
        try {
            if (!hasTable()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
            if (null == oneLeiTaiDB) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableStop);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            // 数据转换为可操作数据
            RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
            if (null == tableDB || null == tableDB.getDefPlayer()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableStop);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            if (null != tableDB.getDefPlayer() && null != tableDB.getAttPlayer() && tableDB.getDuiCount() >= caso.getQueuenum()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableQueueMax);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            String keyQue = GameConst.RedisKey.CrossArenaQue + "" + sceneId;
            List<String> list = jedis.lrange(keyQue, 0, -1);
            if (null != list && list.contains(playerIdx)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_ATQUE);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
            // 获取连接
            // 判断该擂台是否工作中
            int svrIndex = findTableWorkServer(tableId);
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(svrIndex);
            if (null == bnc) {
                LogUtil.error("该服务器没有查询到战场服地址=" + svrIndex);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableStop);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }

            ServerTransfer.GS_BS_CrossArenaAtt.Builder msgatt = ServerTransfer.GS_BS_CrossArenaAtt.newBuilder();
            msgatt.setLeitaiId(tableId);
            Battle.BattlePlayerInfo attinfo = createAttData(playerIdx, TeamNumEnum.TNE_MatchArenaLeiTai_1, Battle.BattleSubTypeEnum.BSTE_MatchArenaLeitai);
            if (null == attinfo) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_TheWar_EmptyPetTeam);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }

            RedisCrossArenaPlayer attPlayerInfo = getPlayerCrossArenaInfo(playerIdx);
            msgatt.setAttInfo(attPlayerInfo);
            if (null == attPlayerInfo) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CP_TeamNotExists);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                return;
            }
            jedis.hset(GameConst.RedisKey.CrossArenaPlayerInfo.getBytes(), playerIdx.getBytes(), attPlayerInfo.toByteArray());

            // 增加判断只能在一个队列中
            int tableIdOld = hasAtTable(playerIdx);
            if (tableIdOld > 0) {
                // 数据检查(确认是否在擂台)
                RedisCrossArenaTableDB pTableDB = tableDB;
                if (tableId != tableIdOld) {
                    byte[] oneTableDB = jedis.get(createRedisKeyLT(tableIdOld).getBytes());
                    if (null == oneTableDB) {
                        pTableDB = null;
                    } else {
                        pTableDB = RedisCrossArenaTableDB.parseFrom(oneTableDB);
                    }
                }
                if (checkPlayerAtTable(pTableDB, playerIdx) == CrossArenaUtil.AT_NOT) {
                    // 清除玩家位置信息
                    jedis.hset(GameConst.RedisKey.CrossArenaPlayerTable, playerIdx, "");
                } else {
                    retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_RPT);
                    msg.setRetCode(retCode);
                    GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
                    return;
                }
            }
            if (attLtConsume(playerIdx, msg, retCode, caso)) {
                bnc.send(MessageId.MsgIdEnum.GS_BS_CrossArenaAtt_VALUE, msgatt);
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            LogUtil.error("上擂异常!!!");
        }
    }

    private boolean attLtConsume(String playerIdx, CrossArena.SC_CrossArenaAtt.Builder msg, RetCode.Builder retCode, CrossArenaSceneObject caso) {
        CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(findPlayerGradeLv(playerIdx));
        playercrossarenaEntity playerEntity = getPlayerEntity(playerIdx);
        if (cfg!=null&& playerEntity.getDataMsg().getCurFreeLtAttTimes()<cfg.getFreeltatt()){
            SyncExecuteFunction.executeConsumer(playerEntity,entity->{
                entity.getDataMsg().setCurFreeLtAttTimes(entity.getDataMsg().getCurFreeLtAttTimes()+1);
            });
            return true;
        }

        // 判断消耗
        Common.Consume consume = ConsumeUtil.parseConsume(caso.getUptablexh());
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossAreanUP);
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            retCode.setRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought);
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
            return false;
        }
        return true;
    }

    /**
     * @param playerIdx 获取玩家擂台赛战斗数据
     * @return
     */
    private RedisCrossArenaPlayer getPlayerCrossArenaInfo(String playerIdx) {
        Battle.BattlePlayerInfo attinfo = createAttData(playerIdx, TeamNumEnum.TNE_MatchArenaLeiTai_1, Battle.BattleSubTypeEnum.BSTE_MatchArenaLeitai);
        if (null == attinfo) {
            return null;
        }
        RedisCrossArenaPlayer.Builder attPlayerInfo = RedisCrossArenaPlayer.newBuilder();
        attPlayerInfo.setName(attinfo.getPlayerInfo().getPlayerName());
        attPlayerInfo.setPlayerId(playerIdx);
        attPlayerInfo.setSvrIndex(ServerConfig.getInstance().getServer());
        attPlayerInfo.setTeamInfo(attinfo);
        attPlayerInfo.setShowPetId(getDisPetId(playerIdx));
        attPlayerInfo.setJionTime(System.currentTimeMillis());
        attPlayerInfo.setPower(attinfo.getPlayerInfo().getPower());
        int lv = getPlayerDBInfo(playerIdx, CrossArenaDBKey.LT_GRADELV);
        CrossArenaLvCfgObject lvCfg = CrossArenaLvCfg.getByLv(lv);
        if (null != lvCfg) {
            ExtendProperty.Builder msg = ExtendProperty.newBuilder();
            for (int bid : lvCfg.getAddbuff()) {
                PetBuffData.Builder dataBuilder = PetBuffData.newBuilder();
                dataBuilder.setBuffCount(bid);
                dataBuilder.setBuffCfgId(1);
                msg.addBuffData(dataBuilder.build());
            }
            attPlayerInfo.addExtendProp(msg);
        }
        return attPlayerInfo.build();
    }

    /**
     * @param tableDB
     * @param playerIdx 检查玩家是否在该卓
     * @return
     */
    private int checkPlayerAtTable(RedisCrossArenaTableDB tableDB, String playerIdx) {
        if (null == tableDB) {
            return CrossArenaUtil.AT_NOT;
        }
        if (null != tableDB.getDefPlayer() && Objects.equals(tableDB.getDefPlayer().getPlayerId(), playerIdx)) {
            return CrossArenaUtil.AT_DEF;
        } else if (null != tableDB.getAttPlayer() && Objects.equals(tableDB.getAttPlayer().getPlayerId(), playerIdx)) {
            // 玩家是攻擂者，且已经开始战斗
            return CrossArenaUtil.AT_ATT;
        } else {
            List<RedisCrossArenaPlayer> tempDB = new ArrayList<RedisCrossArenaPlayer>();
            tempDB.addAll(tableDB.getDuiList());
            for (RedisCrossArenaPlayer rcap : tempDB) {
                if (Objects.equals(rcap.getPlayerId(), playerIdx)) {
                    return CrossArenaUtil.AT_QUE;
                }
            }
            return CrossArenaUtil.AT_NOT;
        }
    }

    /**
     * @param playerIdx
     * @param code      攻打擂台中心服返回数据
     */
    public void attTableBSBack(String playerIdx, RetCodeId.RetCodeEnum code, int tableId) {
        CrossArena.SC_CrossArenaAtt.Builder msg = CrossArena.SC_CrossArenaAtt.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        if (code == RetCodeId.RetCodeEnum.RCE_Success) {
            msg.setRetCode(retCode);
        } else {
            retCode.setRetCode(code);
            msg.setRetCode(retCode);
            // 刷新一次擂台数据
            refPlayerView(playerIdx, tableId);
            // 返还道具
            CrossArenaSceneObject caso = CrossArenaScene.getById(getSceneIdByTableId(tableId));
            if (null != caso) {
                Common.Reward.Builder rewardBuilder = RewardUtil.parseRewardBuilder(caso.getUptablexh());
                ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_CrossAreanUP);
                RewardManager.getInstance().doReward(playerIdx, rewardBuilder.build(), reason, false);
            }
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaAtt_VALUE, msg);
        sendMainPanelInfo(playerIdx);
    }

    /**
     * @param playerIdx 获取玩家展示模型ID
     * @return
     */
    public int getDisPetId(String playerIdx) {
        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return 1001;
        }

        int petId = PetBaseProperties.getPetIdByUnlockHeadId(entity.getAvatar());
        if (petId == -1) {
            return 1001;
        }
        return petId;
    }

    public void battleWin(ServerTransfer.BS_GS_CrossArenaWinResult msg) {
        if (!isOpen) {
            return;
        }
        String winPlayer = "";
        String failPlayer = "";
        int couWinnumT = Math.min(10, msg.getWinNumDef());
        int winOther = msg.getWinNumOther();
        ;
        if (msg.getIsWin() == 2) {
            // 防守胜利
            winPlayer = msg.getPlayerIdDef();
            failPlayer = msg.getPlayerIdAtt();
        } else {
            // 攻击胜利
            winPlayer = msg.getPlayerIdAtt();
            failPlayer = msg.getPlayerIdDef();
        }
        add10WinNote(winPlayer, couWinnumT);
        playerCrossArenaWin(winPlayer, couWinnumT, winOther);
        playerCrossArenaFail(failPlayer);

        CrossArenaHonorManager.getInstance().honorVueByKeyAdd(msg.getPlayerIdAtt(), CrossArenaUtil.HR_LT_ATT, 1);
        CrossArenaHonorManager.getInstance().honorVueByKeyAdd(msg.getPlayerIdDef(), CrossArenaUtil.HR_LT_DEF, 1);
    }

    private void playerCrossArenaWin(String playerIdx, int couWinnumT, int winOtherCou) {
        playerEntity peWin = playerCache.getByIdx(playerIdx);
        if (null == peWin) {
            return;
        }
        if (couWinnumT >= 10) {
            quitAll(playerIdx,true);
        }
        CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerIdx, CrossArenaUtil.HR_LT_JION, 1);
        CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerIdx, CrossArenaUtil.HR_LT_WIN, 1);
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_CrossArena_Jion, 1, 0);
        if (isOpenDub) {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_CrossArena_COTWin, couWinnumT, 0);
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_CrossArena_YSerialWinXTime, 1, couWinnumT);
        }
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        int nowInSceneId = findPlayerNowInSceneId(playerIdx);
        // 竞猜成功次数累加
        int couWinnum = couWinnumT;
        SyncExecuteFunction.executeConsumer(pe, p -> {
            savePlayerDBInfoNotLock(pe, LT_BATTLEWinNUM_WEEK, 1, CrossArenaUtil.DbChangeAdd);
            if (isOpenDub) {
                int beforeWinCountHis = pe.getDataMsg().getDbsOrDefault(LT_WINCOTHIS_VALUE,0);
                savePlayerDBInfoNotLock(pe, CrossArenaDBKey.LT_WINCOTCUR, couWinnum, CrossArenaUtil.DbChangeRep);
                savePlayerDBInfoNotLock(pe, CrossArenaDBKey.LT_WINCOTDAY, couWinnum, CrossArenaUtil.DbChangeRepMax);
                savePlayerDBInfoNotLock(pe, LT_WINCOTHIS, couWinnum, CrossArenaUtil.DbChangeRepMax);
                savePlayerDBInfoNotLock(pe, LT_WINCOTWeekly, couWinnum, CrossArenaUtil.DbChangeRepMax);
                savePlayerDBInfoNotLock(pe, LT_LastSerialWinNum, couWinnum, CrossArenaUtil.DbChangeRep);
                savePlayerDBInfoNotLock(pe,CrossArenaDBKey.LT_CurSerialFailNum, 0, CrossArenaUtil.DbChangeRep);
                saveWinNumByScienceId(pe, nowInSceneId, CrossArenaDBKey.LT_WINCOTDAY, couWinnum, CrossArenaUtil.DbChangeRepMax);
                saveWinNumByScienceId(pe, nowInSceneId, LT_WINCOTWeekly, couWinnum, CrossArenaUtil.DbChangeRepMax);
                if (couWinnum == beforeWinCountHis) {
                    saveWinNumByScienceId(pe, nowInSceneId, LT_WINCOTHISNum, 1, CrossArenaUtil.DbChangeAdd);
                } else if (couWinnum > beforeWinCountHis) {
                    saveWinNumByScienceId(pe, nowInSceneId, LT_WINCOTHISNum, 1, CrossArenaUtil.DbChangeRep);
                }
                if (couWinnum == 10) {
                    savePlayerDBInfoNotLock(pe, LT_10SerialWinWeek, 1, CrossArenaUtil.DbChangeAdd);
                    saveWinNumByScienceId(pe, nowInSceneId, LT_10SerialWinWeek, 1, CrossArenaUtil.DbChangeAdd);
                    LogService.getInstance().submit(new PlayerTargetLog(playerIdx, TTE_CrossArenaHonor1003, 1));
                }
                if (couWinnum >= beforeWinCountHis) {
                    //更新擂台赛排行榜
                   updatePlayerSerialWinRankScore(playerIdx);
                }

            }
            updateWeekTask(pe, CrossArenaGradeType.GRADE_JOIN_VALUE, CrossArenaUtil.DbChangeAdd, 1);
            updateWeekTask(pe, CrossArenaGradeType.GRADE_WIN_VALUE, CrossArenaUtil.DbChangeAdd, 1);
            updateWeekTask(pe, CrossArenaGradeType.GRADE_MAXWIN_VALUE, CrossArenaUtil.DbChangeRepMax, couWinnum);
            savePlayerDBInfoNotLock(pe , LT_WINNUM, 1, CrossArenaUtil.DbChangeAdd);
            savePlayerDBInfoNotLock(pe, CrossArenaDBKey.LT_WINNUM_DAY, 1, CrossArenaUtil.DbChangeAdd);
            savePlayerDBInfoNotLock(pe, LT_BATTLENUM, 1, CrossArenaUtil.DbChangeAdd);
            savePlayerDBInfoNotLock(pe, CrossArenaDBKey.LT_BATTLENUM_DAY, 1, CrossArenaUtil.DbChangeAdd);
            savePlayerDBInfoNotLock(pe, CrossArenaDBKey.LT_BATTLENUM_WEEK, 1, CrossArenaUtil.DbChangeAdd);
            int js = pe.getDataMsg().getWeekBattleResCount() + 1;
            pe.getDataMsg().putWeekBattleRes(js, 1);
            if (couWinnum <= 10) {
                int total = pe.getDataMsg().getLtCotCootOrDefault(couWinnum, 0);
                pe.getDataMsg().putLtCotCoot(couWinnum, total);
            }
            Map<Integer, Integer> needEVent = new HashMap<>();
            needEVent.put(CrossArenaUtil.TRIGGER_DAY, getPlayerDBInfo(pe, CrossArenaDBKey.LT_BATTLENUM_DAY));
            needEVent.put(CrossArenaUtil.TRIGGER_WEEK, getPlayerDBInfo(pe, CrossArenaDBKey.LT_BATTLENUM_WEEK));
            if (isOpenDub) {
                needEVent.put(CrossArenaUtil.TRIGGER_COU, getPlayerDBInfo(pe, CrossArenaDBKey.LT_WINCOTCUR));
            }
            needEVent.put(CrossArenaUtil.TRIGGER_WEEKRATE, couWinnumT);
            needEVent.put(CrossArenaUtil.TRIGGER_COUINS, winOtherCou);
            eventTaggiers(playerIdx, needEVent, false);
            add100WinRate(p, 1);
        });
        CrossArenaCfgObject caco = CrossArenaCfg.getById(GameConst.CONFIG_ID);
        int lsgb = caco.getLsgb();
        if (lsgb > 0 && couWinnumT >= caco.getMarqueeminwin()) {
            GlobalData.getInstance().sendMarqueeToAllOnlinePlayer(lsgb, peWin.getName(),couWinnumT);
        }
        sendMainPanelInfo(playerIdx);
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_CrossArenaHonor1002, 1, 0);
    }

    private void updatePlayerSerialWinRankScore(String playerIdx) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        if (pe == null){
            return;
        }
        int playerNowInSceneId = findPlayerNowInSceneId(playerIdx);
        int winNumByScienceId = getWinNumByScienceId(pe, playerNowInSceneId, LT_WINCOTHISNum);
        if (winNumByScienceId <= 0) {
            return;
        }
        RankingManager.getInstance().updatePlayerRankingScore(playerIdx, Activity.EnumRankingType.ERT_Lt_SerialWin,
                                                 RankingUtils.getLtSerialWinRankName(playerNowInSceneId),
                                                 pe.getDataMsg().getDbsOrDefault(LT_WINCOTHIS_VALUE, 0)
                                                , winNumByScienceId);
    }

    private void saveWinNumByScienceId(playercrossarenaEntity pe, int nowInSceneId, CrossArenaDBKey dbKey, int vue, int addOrRep) {
        CrossArenaDB.DB_LTSerialWin dbWinData = pe.getDataMsg().getSerialWinDataOrDefault(nowInSceneId, CrossArenaDB.DB_LTSerialWin.getDefaultInstance());
        CrossArenaDB.DB_LTSerialWin.Builder builder = dbWinData.toBuilder();
        if (addOrRep == CrossArenaUtil.DbChangeRep) {
            builder.putWinData(dbKey.getNumber(), vue);
        } else if (addOrRep == CrossArenaUtil.DbChangeRepMax) {
            if (builder.getWinDataOrDefault(dbKey.getNumber(), 0) < vue) {
                builder.putWinData(dbKey.getNumber(), vue);
            }
        } else {
            int newVal = builder.getWinDataOrDefault(dbKey.getNumber(), 0) + vue;
            builder.putWinData(dbKey.getNumber(), newVal);
        }
        pe.getDataMsg().putSerialWinData(nowInSceneId, builder.build());
    }

    public int getWinNumByScienceId(playercrossarenaEntity pe, int nowInSceneId, CrossArenaDBKey dbKey) {
        return pe.getWinNumByScienceId(nowInSceneId, dbKey);
    }

    private static final Map<CrossArenaGradeType,Integer> crossArenaGradeTypeMap = new HashMap<>();


    static {
        crossArenaGradeTypeMap.put(CrossArenaGradeType.GRADE_QC_Join, CrossArenaUtil.DbChangeAdd);
        crossArenaGradeTypeMap.put(CrossArenaGradeType.GRADE_XS_Join, CrossArenaUtil.DbChangeAdd);
        crossArenaGradeTypeMap.put(CrossArenaGradeType.GRADE_CP_Join, CrossArenaUtil.DbChangeAdd);
        crossArenaGradeTypeMap.put(CrossArenaGradeType.GRADE_CrazyDuel_Join, CrossArenaUtil.DbChangeAdd);
    }


    public void updateWeeklyTaskWithLock(playercrossarenaEntity pe, CrossArenaGradeType type, int value) {
        Integer changeType = crossArenaGradeTypeMap.get(type);
        if (changeType == null) {
            LogUtil.error("updateWeeklyTaskWithLock error,crossArenaGradeTypeMap not contains :{}", type);
            return;
        }

        updateWeekTask(pe, type.getNumber(), changeType, value);
    }

    private void playerCrossArenaFail(String playerIdx) {
        playerEntity peFail = playerCache.getByIdx(playerIdx);
        if (null == peFail) {
            return;
        }
        quitAll(playerIdx, false);
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        // 竞猜成功次数累加
        if (null != pe) {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_CrossArena_Jion, 1, 0);
            CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerIdx, CrossArenaUtil.HR_LT_JION, 1);
            SyncExecuteFunction.executeConsumer(pe, p -> {
                savePlayerDBInfoNotLock(pe, CrossArenaDBKey.LT_WINCOTCUR, 0, CrossArenaUtil.DbChangeRep);
                savePlayerDBInfoNotLock(pe, LT_BATTLENUM, 1, CrossArenaUtil.DbChangeAdd);
                savePlayerDBInfoNotLock(pe, CrossArenaDBKey.LT_BATTLENUM_DAY, 1, CrossArenaUtil.DbChangeAdd);
                savePlayerDBInfoNotLock(pe, CrossArenaDBKey.LT_BATTLENUM_WEEK, 1, CrossArenaUtil.DbChangeAdd);
                int curSerialFail = getPlayerDBInfo(pe, LT_CurSerialFailNum);
                savePlayerDBInfoNotLock(pe,CrossArenaDBKey.LT_CurSerialFailNum, 1, CrossArenaUtil.DbChangeAdd);
                if (curSerialFail>1) {
                    savePlayerDBInfoNotLock(pe, LT_LastSerialWinNum, 0, CrossArenaUtil.DbChangeRep);
                }
                updateWeekTask(pe, CrossArenaGradeType.GRADE_JOIN_VALUE, CrossArenaUtil.DbChangeAdd, 1);
                int js = pe.getDataMsg().getWeekBattleResCount() + 1;
                pe.getDataMsg().putWeekBattleRes(js, 0);

                Map<Integer, Integer> needEVent = new HashMap<>();
                needEVent.put(CrossArenaUtil.TRIGGER_DAY, getPlayerDBInfo(pe, CrossArenaDBKey.LT_BATTLENUM_DAY));
                needEVent.put(CrossArenaUtil.TRIGGER_WEEK, getPlayerDBInfo(pe, CrossArenaDBKey.LT_BATTLENUM_WEEK));
                eventTaggiers(playerIdx, needEVent, false);

                add100WinRate(p, 0);
            });
        }
        sendMainPanelInfo(playerIdx);
    }

    private void add100WinRate(playercrossarenaEntity pe, int isWin) {
        pe.getDataMsg().addAllBatWin(isWin);
        List<Integer> temp = new LinkedList<>();
        temp.addAll(pe.getDataMsg().getAllBatWinList());
        if (temp.size() > 100) {
            temp.remove(0);
            pe.getDataMsg().clearAllBatWin();
            pe.getDataMsg().addAllAllBatWin(temp);

            // 计算胜率
            int i = 0;
            for (int r : temp) {
                if (r > 1) {
                    i++;
                }
            }
            int rate = Math.round(i * 1F / temp.size() * 10000);
            if (rate >= 8000) {
                CrossArenaHonorManager.getInstance().honorVueFirst(pe.getIdx(), CrossArenaUtil.HR_FIRST_100RATE);
            }
        }
    }

    public void add10WinNote(String playerId, int num) {
        if (!isOpenDub) {
            return;
        }
        // 闯关事件
        playerEntity pen = playerCache.getByIdx(playerId);
        if (num < 10 || null == pen) {
            return;
        }
        String rkey = GameConst.RedisKey.CrossArenaNoteCotMap;
        long len = jedis.hlen(rkey);
        boolean hasName = jedis.hexists(rkey, pen.getName());
        String yr = jedis.get(GameConst.RedisKey.CrossArenaYear);
        int yaer = 1;
        if (!StringHelper.isNull(yr)) {
            yaer = DateUtil.differDays(NumberUtils.toLong(yr)) + 1;
        }
        long yaerKey = yaer * 1000L + len;
        String yaerStr = "" + yaerKey;
        if (len < 50 && !hasName) {
            jedis.hset(rkey, yaerStr, pen.getName());
        }
        CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_LT_10WIN, 1);
        CrossArenaHonorManager.getInstance().honorVueFirst(playerId, CrossArenaUtil.HR_FIRST_10WIN);
    }

    /**
     * @param playerIdx 玩家退出擂台
     */
    public void closeTable(String playerIdx) {
        try {
            if (!hasTable()) {
                return;
            }
            CrossArenaPvpManager.getInstance().isHaveCrossArenaPvpRoomWithMsg(playerIdx);
            // 判断该擂台是否工作中
            // 获取连接
            int svrIndex = findSynCacheServerIndex();
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(svrIndex);
            if (null != bnc) {
                ServerTransfer.GS_BS_CrossArenaPos.Builder msgatt = ServerTransfer.GS_BS_CrossArenaPos.newBuilder();
                msgatt.setOper(1);
                CrossArenaPlyCacheRAM.Builder msg = CrossArenaPlyCacheRAM.newBuilder();
                msg.setPid(playerIdx);
                msgatt.setAllInfo(msg);
                bnc.send(MessageId.MsgIdEnum.GS_BS_CrossArenaPos_VALUE, msgatt);
            }
            // 刷新玩家视野信息
            playerPosMap.remove(playerIdx);

            removeScreenPagePlayerMap(playerIdx);
            removeQueuePanelPlayerMap(playerIdx);
            // 退出擂台
//            String ppos = jedis.hget(GameConst.RedisKey.CrossArenaPlayerTable, playerIdx);
//            if (null != ppos && !"".equals(ppos)) {
//                quitTable(playerIdx, Integer.valueOf(ppos));
//            }
            // 退出匹配擂台
            quitAll(playerIdx, true);
            // 计算时间
            quitCrossArenaLJTime(playerIdx, true);
            jedis.hdel(GameConst.RedisKey.CrossArenaPlOnline, playerIdx);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return;
        }
        return;
    }

    /**
     * @param playerIdx 退出擂台累计在线时间
     */
    public void quitCrossArenaLJTime(String playerIdx, boolean isQuit) {
        // 计算时间
        long atTime = playerJionTime.getOrDefault(playerIdx, 0L);
        if (atTime <= 0L) {
            return;
        }
        long ljTime = GlobalTick.getInstance().getCurrentTime() - atTime;
        if (ljTime > 86400000L) {
            ljTime = 86400000L;
        }
        playercrossarenaEntity pe = playercrossarenaCache.getByIdx(playerIdx);
        if (ljTime > 0) {
            long finalLjTime = ljTime;
            SyncExecuteFunction.executeConsumer(pe, p -> {
                p.getDataMsg().setLeijiTime(p.getDataMsg().getLeijiTime() + finalLjTime);
            });
        }
        if (isQuit) {
            playerJionTime.remove(playerIdx);
        } else {
            playerJionTime.put(playerIdx, GlobalTick.getInstance().getCurrentTime());
        }
    }

    /**
     * @param playerIdx
     * @param tableId   主动退出XX擂台(下阵和对出队列都都用该方法)
     */
    public void quitTable(String playerIdx, int tableId) {
        CrossArena.SC_CrossArenaQuit.Builder msg = CrossArena.SC_CrossArenaQuit.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaQuit_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        // 原服先判断下逻辑
        try {
            if (!hasTable()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaQuit_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            // 获取擂台数据
            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
            if (null == oneLeiTaiDB) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableStop);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaQuit_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            // 数据转换为可操作数据
            RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
            int atTable = checkPlayerAtTable(tableDB, playerIdx);
            if (atTable == CrossArenaUtil.AT_NOT || atTable == CrossArenaUtil.AT_ATT) {
                // 玩家是攻擂者,或者不在该擂台，且已经开始战斗
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaQuit_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            } else if (atTable == CrossArenaUtil.AT_DEF) {
                // 玩家是擂主,只有等待状态可以退出擂主
                if (tableDB.getState() != CrossArena.CrossArenaState.WAIT_VALUE) {
                    retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
                    msg.setRetCode(retCode);
                    GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaQuit_VALUE, msg);
                    refPlayerView(playerIdx, tableId);
                    return;
                }
            } else {
                // 在队列可以退出
            }
            // 判断该擂台是否工作中
            // 获取连接
            int svrIndex = findTableWorkServer(tableId);
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(svrIndex);
            if (null == bnc) {
                LogUtil.error("该服务器没有查询到战场服地址=" + svrIndex);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableStop);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaQuit_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            // 发送消息至擂台逻辑管理服务器
            ServerTransfer.GS_BS_CrossArenaQuit.Builder msgquit = ServerTransfer.GS_BS_CrossArenaQuit.newBuilder();
            msgquit.setLeitaiId(tableId);
            msgquit.setPlayerId(playerIdx);
            msgquit.setSvrIndex(ServerConfig.getInstance().getServer());
            bnc.send(MessageId.MsgIdEnum.GS_BS_CrossArenaQuit_VALUE, msgquit);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx
     * @param code      攻打擂台中心服返回数据
     */
    public void quitTableBSBack(String playerIdx, RetCodeId.RetCodeEnum code) {
        CrossArena.SC_CrossArenaQuit.Builder msg = CrossArena.SC_CrossArenaQuit.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        if (code == RetCodeId.RetCodeEnum.RCE_Success) {
            msg.setRetCode(retCode);
        } else {
            retCode.setRetCode(code);
            msg.setRetCode(retCode);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaQuit_VALUE, msg);
    }

    /**
     * @param playerIdx
     * @param tableId   竞猜信息查看
     */
    public void guessInfoView(String playerIdx, int tableId) {
        CrossArena.SC_CrossArenaGuessInfo.Builder msg = CrossArena.SC_CrossArenaGuessInfo.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuessInfo_VALUE, msg);
            return;
        }
        CrossArenaSceneObject caso = CrossArenaScene.getById(getSceneIdByTableId(tableId));
        if (null == caso) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuessInfo_VALUE, msg);
            return;
        }
        try {
            if (!hasTable()) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuessInfo_VALUE, msg);
                return;
            }
            // 获取擂台数据
            byte[] tableDBByte = jedis.get(createRedisKeyLT(tableId).getBytes());
            if (null == tableDBByte) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableStop));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuessInfo_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            // 数据转换为可操作数据
            RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(tableDBByte);
            if (null == tableDB || (tableDB.getState() != CrossArena.CrossArenaState.FIGHT_VALUE && tableDB.getState() != CrossArena.CrossArenaState.READY_VALUE)) {
                // 刷新一次擂台数据
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuessInfo_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            if (null == tableDB.getDefPlayer() || null == tableDB.getAttPlayer()) {
                // 刷新一次擂台数据
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuessInfo_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            Battle.BattlePlayerInfo.Builder defInfo = tableDB.getDefPlayer().getTeamInfo().toBuilder();
            defInfo.setCamp(2);
            msg.addPlayerInfo(defInfo);
            Battle.BattlePlayerInfo.Builder attInfo = tableDB.getAttPlayer().getTeamInfo().toBuilder();
            attInfo.setCamp(1);
            msg.addPlayerInfo(attInfo);
            playercrossarenaEntity pe = getPlayerEntity(playerIdx);
            if (null != pe) {
                int sy = caso.getGuessnum() - getPlayerDBInfo(pe, CrossArenaDBKey.LT_ADMIRECUR);
                msg.setSynum(Math.max(sy, 0));
            }
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuessInfo_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx
     * @param tableId   竞猜
     */
    public void guess(String playerIdx, int tableId, int isWin) {
        CrossArena.SC_CrossArenaGuess.Builder msg = CrossArena.SC_CrossArenaGuess.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuess_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        // 原服先判断下逻辑
        try {
            if (!hasTable()) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuess_VALUE, msg);
                return;
            }
            // 获取擂台数据
            byte[] tableDBByte = jedis.get(createRedisKeyLT(tableId).getBytes());
            if (null == tableDBByte) {
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableStop));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuess_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            // 数据转换为可操作数据
            RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(tableDBByte);
            if (null == tableDB || (tableDB.getState() != CrossArena.CrossArenaState.FIGHT_VALUE && tableDB.getState() != CrossArena.CrossArenaState.READY_VALUE)) {
                // 刷新一次擂台数据
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuess_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            if (null == tableDB.getDefPlayer() || null == tableDB.getAttPlayer()) {
                // 刷新一次擂台数据
                msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT));
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuess_VALUE, msg);
                refPlayerView(playerIdx, tableId);
                return;
            }
            // 判断该擂台是否工作中
            // 获取连接
            int svrIndex = findTableWorkServer(tableId);
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(svrIndex);
            if (null == bnc) {
                LogUtil.error("该服务器没有查询到战场服地址=" + svrIndex);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableStop);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuess_VALUE, msg);
                return;
            }
            ServerTransfer.GS_BS_CrossArenaGuess.Builder msgquit = ServerTransfer.GS_BS_CrossArenaGuess.newBuilder();
            msgquit.setLeitaiId(tableId);
            msgquit.setPlayerId(playerIdx);
            msgquit.setSvrIndex(ServerConfig.getInstance().getServer());
            msgquit.setIsWin(isWin);
            bnc.send(MessageId.MsgIdEnum.GS_BS_CrossArenaGuess_VALUE, msgquit);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx
     * @param code      竞猜返回
     */
    public void guessBSBack(String playerIdx, RetCodeId.RetCodeEnum code) {
        CrossArena.SC_CrossArenaGuess.Builder msg = CrossArena.SC_CrossArenaGuess.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        if (code == RetCodeId.RetCodeEnum.RCE_Success) {
            msg.setRetCode(retCode);
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_CrossArena_Guess, 1, 0);
            savePlayerDBInfoAdd(playerIdx, CrossArenaDBKey.LT_ADMIRECUR, 1);
            savePlayerDBInfoAdd(playerIdx, CrossArenaDBKey.LT_ADMIRE, 1);
            CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerIdx, CrossArenaUtil.HR_LT_ZAN, 1);
        } else {
            retCode.setRetCode(code);
            msg.setRetCode(retCode);
            refPlayerView(playerIdx);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaGuess_VALUE, msg);
    }

    private boolean hasTable() {
        return jedis.hlen(RedisKey.CrossArenaBSSid) > 0;
    }

    /**
     * @param playerIdx 被竞猜返回
     */
    public void guessBSBe(String playerIdx) {
        savePlayerDBInfoAdd(playerIdx, CrossArenaDBKey.LT_ADMIRE_BE,1);
        CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerIdx, CrossArenaUtil.HR_LT_HOT, 1);

    }

    /**
     * @param wins 竞猜成功
     */
    public void guessResult(List<String> wins) {
        for (String idx : wins) {
            playerEntity player = playerCache.getByIdx(idx);
            if (null == player) {
                continue;
            }
            EventUtil.triggerUpdateTargetProgress(idx, TargetSystem.TargetTypeEnum.TTE_MatchArenaLT_GuessWin, 1, 0);
        }
    }

    /**
     * @param playerIdx
     * @param teamNum
     * @param subTypeEnum
     * @return 创建参加擂台的玩家战斗数据
     */
    public Battle.BattlePlayerInfo createAttData(String playerIdx, TeamNumEnum teamNum, Battle.BattleSubTypeEnum subTypeEnum) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (null == player) {
            return null;
        }
        List<Battle.BattlePetData> petDataList = teamCache.getInstance().buildBattlePetData(playerIdx, teamNum, subTypeEnum);
        if (GameUtil.collectionIsEmpty(petDataList)) {
            return null;
        }
        // 检查玩家信息是否正确
        Battle.PlayerBaseInfo.Builder playerInfo = BattleUtil.buildPlayerBattleBaseInfo(playerIdx);
        if (playerInfo == null) {
            return null;
        }
        playerInfo.setPower(teamCache.getInstance().getTeamFightAbility(playerIdx, teamNum));
        Battle.BattlePlayerInfo.Builder battlePlayerInfo = Battle.BattlePlayerInfo.newBuilder();
        battlePlayerInfo.setCamp(0);
        battlePlayerInfo.addAllPetList(petDataList);
        battlePlayerInfo.setPlayerInfo(playerInfo);
        battlePlayerInfo.setIsAuto(false);
        List<Integer> skillList = teamCache.getInstance().getPlayerTeamSkillList(playerIdx, teamNum);
        if (!CollectionUtils.isEmpty(skillList)) {
            for (Integer skillId : skillList) {
                battlePlayerInfo.addPlayerSkillIdList(Battle.SkillBattleDict.newBuilder().setSkillId(skillId).setSkillLv(player.getSkillLv(skillId)).build());
            }
        }
        return battlePlayerInfo.build();
    }

    public void needAI(int sceneId, String playerId, int winNum, int useType, int difficult) {
        CrossArenaSceneObject caso = CrossArenaScene.getById(sceneId);
        if (null == caso) {
            return;
        }
//		System.out.println(">>>>>>>>>>ADD>>>>>>>>>>>>" + playerId);
        RedisCrossArenaPlayer.Builder createAttDataRobot = createAttDataRobot(playerId, caso, winNum, useType,difficult);
        jedis.hset(GameConst.RedisKey.CrossArenaRBPlayerInfo.getBytes(), playerId.getBytes(), createAttDataRobot.build().toByteArray());
        if (useType==protectRobot){
            String queKey = getProtectRobotQueKey(sceneId, difficult);
            jedis.lpush(queKey, playerId);
        }
    }

/*    public void needAIAddTable(int tableId) {
        try {
            String tableKeyLock = GameConst.RedisKey.CrossArenaTableLock + tableId;
            String tableKey = createRedisKeyLT(tableId);
            byte[] oneLeiTaiDB = jedis.get(tableKey.getBytes());
            if (null == oneLeiTaiDB) {
                return;
            }
            CrossArenaSceneObject caso = CrossArenaScene.getById(getSceneIdByTableId(tableId));
            if (null == caso) {
                return;
            }
            // 数据转换为可操作数据
            RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
            if (null == tableDB.getDefPlayer() || null == tableDB.getAttPlayer()) {
                return;
            }
            if (!JedisUtil.lockRedisKey(tableKeyLock, 3000l)) {
                return;
            }
            if (tableDB.getState() != CrossArena.CrossArenaState.READY_VALUE) {
                JedisUtil.unlockRedisKey(tableKeyLock);
                return;
            }
            RedisCrossArenaTableDB.Builder newDB = tableDB.toBuilder();
            if (tableDB.getDefPlayer().getIsAI() > 0 && tableDB.getDefPlayer().getJionTime() <= 0) {
                RedisCrossArenaPlayer.Builder def = createAttDataRobot(tableDB.getDefPlayer().getPlayerId(), caso, tableDB.getDefPlayer().getDefNum());
                newDB.setDefPlayer(def);
            }
            if (tableDB.getAttPlayer().getIsAI() > 0 && tableDB.getAttPlayer().getJionTime() <= 0) {
                RedisCrossArenaPlayer.Builder att = createAttDataRobot(tableDB.getAttPlayer().getPlayerId(), caso, tableDB.getAttPlayer().getDefNum());
                newDB.setAttPlayer(att);
            }
            jedis.set(tableKey.getBytes(), newDB.build().toByteArray());
            JedisUtil.unlockRedisKey(tableKeyLock);
            refTableInfoToAllServer(newDB.build());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }*/

    /**
     * @return 创建参加擂台的玩家战斗数据
     */
    public RedisCrossArenaPlayer.Builder createAttDataRobot(String idx, CrossArenaSceneObject caso, int winNum,int useType,int difficult) {
        // 构建玩家基础信息
        Battle.PlayerBaseInfo.Builder playerInfo = Battle.PlayerBaseInfo.newBuilder();
        Common.LanguageEnum lag = Common.LanguageEnum.forNumber(ServerConfig.getInstance().getLanguage());
        if (null == lag) {
            lag = Common.LanguageEnum.LE_SimpleChinese;
        }
        playerInfo.setPlayerName(ObjUtil.createRandomName(lag));
        playerInfo.setLevel(50);
        playerInfo.setPlayerId(idx);
        playerInfo.setAvatar(Head.randomGetAvatar());
        playerInfo.setVipLevel(1);
        playerInfo.setAvatarBorder(0);
        playerInfo.setAvatarBorderRank(0);
        playerInfo.setTitleId(0);
        playerInfo.setNewTitleId(0);
//        playerInfo.setPower();
        CrossArenaRobotObject robotCfg = getRobotCfg(caso.getId(), winNum, difficult, useType);
        if (robotCfg == null) {
            LogUtil.error("cross arena getRobotCfg is null by scienceId:{},winNum:{},difficult:{},useType:{}"
                    , caso.getId(), winNum, difficult, useType);

            return null;
        }
        playerInfo.setHonorLv(robotCfg.getHonrlv());

        List<PetMessage.Pet> petList = new ArrayList<>();
        for (int petBookId : robotCfg.getTeam()) {
            PetMessage.Pet.Builder petBuilder = petCache.getInstance().getPetBuilder(petBookId, 0);
            if (petBuilder == null) {
                continue;
            }
            int randomRarity = RandomUtil.getRandomValue(0, robotCfg.getRarity().length);
            petBuilder.setPetRarity(robotCfg.getRarity()[randomRarity]);
            petBuilder.setPetLvl(RandomUtil.getRandomValue(robotCfg.getLevel()[0], robotCfg.getLevel()[1]));
            petCache.getInstance().refreshPetData(petBuilder, null);
            petList.add(petBuilder.build());
        }
        List<Battle.BattlePetData> petDataList = petCache.getInstance().buildPetBattleData(null, petList, Battle.BattleSubTypeEnum.BSTE_MatchArenaLeitai, true);
        if (GameUtil.collectionIsEmpty(petDataList)) {
            return null;
        }
        long power = 0;
        for (Battle.BattlePetData petData : petDataList) {
            power += petData.getAbility();
        }
        playerInfo.setPower(power);
        Battle.BattlePlayerInfo.Builder battlePlayerInfo = Battle.BattlePlayerInfo.newBuilder();
        battlePlayerInfo.setCamp(0);
        battlePlayerInfo.addAllPetList(petDataList);
        battlePlayerInfo.setPlayerInfo(playerInfo);
        battlePlayerInfo.setIsAuto(true);

        RedisCrossArenaPlayer.Builder attPlayerInfo = RedisCrossArenaPlayer.newBuilder();
        attPlayerInfo.setName(playerInfo.getPlayerName());
        attPlayerInfo.setPlayerId(idx);
        attPlayerInfo.setSvrIndex(ServerConfig.getInstance().getServer());
        attPlayerInfo.setTeamInfo(battlePlayerInfo);
        attPlayerInfo.setShowPetId(getRobotShowPetId(battlePlayerInfo.build()));
        attPlayerInfo.setJionTime(System.currentTimeMillis());
        attPlayerInfo.setIsAI(1);
        attPlayerInfo.setPower(power);
        return attPlayerInfo;
    }

    private CrossArenaRobotObject getRobotCfg(int scienceId, int winNum, int difficult, int useType) {
        if (useType == normalMatchRobot) {
            return getMatchRobotInitCfg(scienceId, winNum);
        }
        return getProtectRobotInitCfg(scienceId, difficult);
    }

    private CrossArenaRobotObject getProtectRobotInitCfg(int scienceId, int robotDifficult) {
        List<CrossArenaRobotObject> cfgs = CrossArenaRobot._ix_id.values().stream().filter(cfg -> cfg.getRank() == scienceId && cfg.getDifficult() == robotDifficult && cfg.getUsetype() == 1).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(cfgs)) {
            return null;
        }
        return cfgs.get(RandomUtils.nextInt(cfgs.size()));
    }

    /**
     * @param stageId
     * @param idx
     * @return 生成擂台ID
     */
    public int cretaeTableIdId(int stageId, int idx) {
        return stageId * 10000 + idx;
    }

    /**
     * @param tableId
     * @return 根据擂台ID获取段位ID
     */
    public int getSceneIdByTableId(int tableId) {
        return tableId / 10000;
    }

    /**
     * @param tableId
     * @return 根据擂台ID，获取擂台实际编号
     */
    public int getTableNumByTableId(int tableId) {
        return tableId % 10000;
    }

    /**
     * @param tableId 根据擂台ID，计算所处屏幕位置
     * @return
     */
    public int computeScreenIdByTableId(int tableId) {
        int tableNum = tableId % 10000;
        return tableNum / CrossArenaUtil.SCREEN_TABLENUM + (tableNum % CrossArenaUtil.SCREEN_TABLENUM > 0 ? 1 : 0);
    }

    /**
     * @param leitaiId
     * @return 创建单个擂台数据key
     */
    public String createRedisKeyLT(int leitaiId) {
        return GameConst.RedisKey.CrossArenaData + leitaiId;
    }

    public CrossArenaRobotObject getMatchRobotInitCfg(int stageId, int winNum) {
        List<CrossArenaRobotObject> temp = new ArrayList<>();
        for (CrossArenaRobotObject ent : CrossArenaRobot._ix_id.values()) {
            if (ent.getRank() == stageId&&ent.getUsetype()==0) {
                temp.add(ent);
            }
        }
        if (temp.isEmpty()) {
            return null;
        }
        if (winNum > 0) {
            List<CrossArenaRobotObject> temp2 = new ArrayList<>();
            int x = 0;
            for (CrossArenaRobotObject ent2 : temp) {
                if (ent2.getWinnum() > winNum) {
                    continue;
                }
                if (ent2.getWinnum() > x) {
                    x = ent2.getWinnum();
                    temp2.clear();
                    temp2.add(ent2);
                } else if (ent2.getWinnum() == x) {
                    temp2.add(ent2);
                }
                if (ent2.getWinnum() <= winNum) {
                    temp2.add(ent2);
                }
            }
            if (temp2.isEmpty()) {
                return null;
            }
            Collections.shuffle(temp2);
            return temp2.get(0);
        } else {
            Collections.shuffle(temp);
            return temp.get(0);
        }
    }

    /**
     * @param playerIdx
     * @param dbKey
     * @return 获取玩家统计数据
     */
    public int getPlayerDBInfo(String playerIdx, CrossArenaDBKey dbKey) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        // 竞猜成功次数累加
        if (null != pe) {
            return pe.getDataMsg().getDbsOrDefault(dbKey.getNumber(), 0);
        }
        return 0;
    }

    /**
     * @param pe
     * @param dbKey 获取玩家统计记录数据
     * @return
     */
    public int getPlayerDBInfo(playercrossarenaEntity pe, CrossArenaDBKey dbKey) {
        return pe.getDataMsg().getDbsOrDefault(dbKey.getNumber(), 0);
    }

    /**
     * @param playerIdx
     * @param dbKey
     * @param vue       替换玩家统计数据
     */
    public void savePlayerDBInfoRep(String playerIdx, CrossArenaDBKey dbKey, int vue) {
        savePlayerDBInfo(playerIdx, dbKey, vue, CrossArenaUtil.DbChangeRep);
    }

    /**
     * @param playerIdx
     * @param dbKey
     * @param vue       增加玩家数据
     */
    public void savePlayerDBInfoAdd(String playerIdx, CrossArenaDBKey dbKey, int vue) {
        savePlayerDBInfo(playerIdx, dbKey, vue, CrossArenaUtil.DbChangeAdd);
    }

    /**
     * @param pe
     * @param dbKey
     * @param vue
     * @param addOrRep 0替换1增加 改变玩家统计数据
     */
    public void savePlayerDBInfo(playercrossarenaEntity pe, CrossArenaDBKey dbKey, int vue, int addOrRep) {
        if (null != pe) {
            SyncExecuteFunction.executeConsumer(pe, p -> {
                if (addOrRep == CrossArenaUtil.DbChangeRep) {
                    pe.getDataMsg().putDbs(dbKey.getNumber(), vue);
                } else if (addOrRep == CrossArenaUtil.DbChangeRepMax) {
                    if (pe.getDataMsg().getDbsOrDefault(dbKey.getNumber(), 0) < vue) {
                        pe.getDataMsg().putDbs(dbKey.getNumber(), vue);
                    }
                } else {
                    int guessNum = pe.getDataMsg().getDbsOrDefault(dbKey.getNumber(), 0) + vue;
                    pe.getDataMsg().putDbs(dbKey.getNumber(), guessNum);
                }
            });
        }
    }

    public void savePlayerDBInfoNotLock(playercrossarenaEntity pe, CrossArenaDBKey dbKey, int vue, int addOrRep) {
        if (addOrRep == CrossArenaUtil.DbChangeRep) {
            pe.getDataMsg().putDbs(dbKey.getNumber(), vue);
        } else if (addOrRep == CrossArenaUtil.DbChangeRepMax) {
            if (pe.getDataMsg().getDbsOrDefault(dbKey.getNumber(), 0) < vue) {
                pe.getDataMsg().putDbs(dbKey.getNumber(), vue);
            }
        } else {
            int guessNum = pe.getDataMsg().getDbsOrDefault(dbKey.getNumber(), 0) + vue;
            pe.getDataMsg().putDbs(dbKey.getNumber(), guessNum);
        }
    }

    /**
     * @param playerIdx
     * @param dbKey
     * @param vue
     * @param addOrRep  0替换1增加 改变玩家统计数据
     */
    public void savePlayerDBInfo(String playerIdx, CrossArenaDBKey dbKey, int vue, int addOrRep) {
        if (null == playerCache.getByIdx(playerIdx)) {
            return;
        }
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        savePlayerDBInfo(pe, dbKey, vue, addOrRep);
    }


    public RetCodeEnum eventFinish(String playerIdx, int eventId, String parm) {
        CrossArenaEventObject cfg = CrossArenaEvent.getById(eventId);
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        if (null == cfg || null == pe || cfg.getType() != 1
                || (cfg.getExetype() != 2 && cfg.getExetype() != 3)) {
            LogUtil.warn("playerIdx:{} finish crossArena event:{} warning,this event can`t finish by this method", playerIdx, eventId);
            return RetCodeEnum.RCE_ErrorParam;
        }
        Long limitTime = pe.getDataMsg().getEventCurMap().get(eventId);
        if (limitTime == null || limitTime < GlobalTick.getInstance().getCurrentTime()) {
            //不存在或者过期
            return RetCodeEnum.RCE_Activity_MissionIsExpire;
        }
        SyncExecuteFunction.executeConsumer(pe, p -> {
            p.getDataMsg().removeEventCur(eventId);
            int x = p.getDataMsg().getEventFlishNumOrDefault(eventId, 0) + 1;
            p.getDataMsg().putEventFlishNum(eventId, x);
        });
        sendPlayerEvents(playerIdx);
        List<Common.Reward> award = getEventReward(eventId, getEventRewardIndex(parm));
        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossAreanEVENT);
        RewardManager.getInstance().doRewardByList(playerIdx, award, reason, true);
        return RetCodeEnum.RCE_Success;
    }

    private int getEventRewardIndex(String parm) {
        if (StringUtils.isEmpty(parm)) {
            return EventRewardDefault;
        }
        try {
            return Integer.parseInt(parm);
        } catch (Exception ex) {
            throw new RuntimeException("getEventRewardIndex client params is error");
        }

    }

    /**
     * @param playerIdx 玩家升级，触发事件
     */
    public void playerUPLv(String playerIdx) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        playerEntity ple = playerCache.getByIdx(playerIdx);
        // 竞猜成功次数累加
        if (null == pe || null == ple) {
            return;
        }
        eventTaggier(playerIdx, CrossArenaUtil.TRIGGER_LV, ple.getLevel());
    }

    public Map<Integer, Long> eventTaggierList(String playerIdx, Map<Integer, Integer> alls) {
        Map<Integer, Long> temp = new HashMap<>();
        for (Map.Entry<Integer, Integer> ent : alls.entrySet()) {
            Map<Integer, Long> temp2 = eventTaggierList(playerIdx, ent.getKey(), ent.getValue());
            temp.putAll(temp2);
        }
        return temp;
    }

    public Map<Integer, Long> eventTaggierList(String playerIdx, int type, int parm) {
        Map<Integer, Long> rs = new HashMap<>();
        playerEntity ple = playerCache.getByIdx(playerIdx);
        // 竞猜成功次数累加
        if (null == ple) {
            return rs;
        }
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        int sceneId = getPlayerDBInfo(pe, CrossArenaDBKey.LT_SCENEID);
        int gradeLv = getPlayerDBInfo(pe, LT_GRADELV);
        Map<Integer, CrossArenaEventObject> eventCfg = CrossArenaEvent._ix_id;
        rs.putAll(triggerBossEvent(pe, sceneId, gradeLv, type, parm));
        for (CrossArenaEventObject eventc : eventCfg.values()) {
            if (eventc.getType() == 3 || !canTriggerEvent(type, parm, pe, sceneId, gradeLv, eventc)) {
                continue;
            }
            addEventByCfg(rs, eventc);
        }
        return rs;
    }

    private void addEventByCfg(Map<Integer, Long> rs, CrossArenaEventObject eventc) {
        if (eventc.getCoutime() > 0) {
            rs.put(eventc.getId(), System.currentTimeMillis() + eventc.getCoutime() * 60000L);
        } else {
            rs.put(eventc.getId(), 0L);
        }
    }

    private boolean canTriggerEvent(int type, int parm, playercrossarenaEntity pe, int sceneId, int gradeLv, CrossArenaEventObject eventc) {
        //boss挑战类的不在这个地方判断,逻辑不一样
        if (eventc.getTriggertype() != type) {
            return false;
        }
        if (gradeLv > 0 && gradeLv != eventc.getHonrlv()) {
            return false;
        }
        if (eventc.getType() != 2 && eventc.getSceneid() != sceneId) {
            return false;
        }
        if (type == CrossArenaUtil.TRIGGER_COUINS) {
            if (eventc.getTriggerparm1() < parm || eventc.getTriggerparm2() > parm) {
                return false;
            }
        } else if (type == CrossArenaUtil.TRIGGER_WEEKRATE) {
            int all = pe.getDataMsg().getWeekBattleResCount();
            if (all < eventc.getTriggerparm1() || eventc.getTriggerparm1() <= 0) {
                // 小于挑战次数则不计算
                return false;
            }
            // 计算触发概率
            int start = all - eventc.getTriggerparm1() + 1;
            int win = 0;
            for (int i = start; i < all; i++) {
                if (pe.getDataMsg().getWeekBattleResOrDefault(i, 0) > 0) {
                    win++;
                }
            }
            int sl = (int) (win * 1F / eventc.getTriggerparm1() * 10000);
            if (sl < eventc.getTriggerparm2()) {
                return false;
            }
        } else {
            if (eventc.getIseq() == 0) {
                if (parm < eventc.getTriggerparm1()) {
                    return false;
                }
            } else {
                if (eventc.getTriggerparm1() != parm) {
                    return false;
                }
            }
        }
        int flishNum = pe.getDataMsg().getEventFlishNumOrDefault(eventc.getId(), 0);
        if (flishNum >= eventc.getFlishnum()) {
            return false;
        }
        long lastTime = pe.getDataMsg().getEventCurOrDefault(eventc.getId(), 1L);
        if (lastTime > 0 && lastTime < System.currentTimeMillis()) {
            // 计算触发概率
            if (RandomUtil.getRandomValue(0, 10000) < eventc.getRate()) {
                return true;
            }
        }
        return false;
    }

    private Map<Integer, Long> triggerBossEvent(playercrossarenaEntity pe, int sceneId, int gradeLv, int type, int parm) {
        List<CrossArenaEventObject> bossEventCfg = CrossArenaEvent.getInstance().getBossEventCfg(sceneId, gradeLv);
        if (CollectionUtils.isEmpty(bossEventCfg)) {
            return Collections.emptyMap();
        }
        CrossArenaEventObject cfg = bossEventCfg.get(0);
        if (!canTriggerEvent(type, parm, pe, sceneId, gradeLv, cfg)) {
            return Collections.emptyMap();
        }
        //如果有boss奇遇也不触发了
        if (hasBossEvent(pe.getDataMsg().getEventCurMap().keySet())) {
            return Collections.emptyMap();
        }
        Map<Integer, Long> rs = new HashMap<>();
        //boss 类触发一个即触发所有
        for (CrossArenaEventObject eventc : bossEventCfg) {
            addEventByCfg(rs, eventc);
        }
        return rs;
    }

    private boolean hasBossEvent(Set<Integer> eventIds) {
        if (CollectionUtils.isEmpty(eventIds)) {
            return true;
        }
        CrossArenaEventObject cfg;
        for (Integer eventId : eventIds) {
            cfg = CrossArenaEvent.getById(eventId);
            if (cfg.getType() == 3) {
                return true;
            }
        }
        return false;
    }

    public void eventTaggier(String playerIdx, int type, int parm) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        // 竞猜成功次数累加
        if (null == pe) {
            return;
        }
        Map<Integer, Long> rs = eventTaggierList(playerIdx, type, parm);
        if (rs.isEmpty()) {
            return;
        }
        SyncExecuteFunction.executeConsumer(pe, p -> {
            p.getDataMsg().putAllEventCur(rs);
        });
        if (playerJionTime.containsKey(playerIdx)) {
            sendPlayerEvents(playerIdx);
        }
    }

    public void eventTaggiers(String playerIdx, Map<Integer, Integer> alls, boolean isLock) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        // 竞猜成功次数累加
        if (null == pe) {
            return;
        }
        Map<Integer, Long> rs = eventTaggierList(playerIdx, alls);
        if (rs.isEmpty()) {
            return;
        }
        if (isLock) {
            SyncExecuteFunction.executeConsumer(pe, p -> {
                p.getDataMsg().putAllEventCur(rs);
            });
        } else {
            pe.getDataMsg().putAllEventCur(rs);
        }
        sendPlayerEvents(playerIdx);
    }

    /**
     * 创建战斗数据
     *
     * @param playerIdx
     * @param eventid
     * @return
     */
    public PatrolBattleResult getFightMakeId(String playerIdx, int eventid) {
        PatrolBattleResult result = new PatrolBattleResult();
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        // 竞猜成功次数累加
        CrossArenaEventObject ecfg = CrossArenaEvent.getById(eventid);
        if (null == pe || null == ecfg) {
            result.setCode(RetCodeEnum.RCE_ErrorParam);
            return result;
        }
        if (!pe.getDataMsg().containsEventCur(eventid)) {
            result.setCode(RetCodeEnum.RCE_CrossArena_EVENT_END);
            return result;
        }
        result.setMakeId(ecfg.getFightmakeid());
        result.setSuccess(true);
        return result;
    }


    public List<Common.Reward> getEventReward(int eventId, int eventIndex) {
        CrossArenaEventObject ecfg = CrossArenaEvent.getById(eventId);
        if (null == ecfg) {
            return Collections.emptyList();
        }
        int[][] awardCfg = eventIndex == EventReward2 ? ecfg.getAward2() : ecfg.getAward();
        List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(awardCfg);
        if (null == rewards) {
            return Collections.emptyList();
        }
        return rewards;
    }

    /**
     * @param playerIdx
     * @return 玩家是否参与了擂台玩法
     */
    public int hasJionArena(String playerIdx) {
        // 增加判断只能在一个队列中
        if (hasAtTable(playerIdx) > 0) {
            return 1;
        }
        int sceneId = getPlayerDBInfo(playerIdx, CrossArenaDBKey.LT_SCENEID);
        if (hasAtQue(playerIdx, sceneId)) {
            return 2;
        }
        return 0;
    }

    /**
     * @param playerIdx
     * @return 是否在擂台上
     */
    private int hasAtTable(String playerIdx) {
        String tableIdStr = jedis.hget(GameConst.RedisKey.CrossArenaPlayerTable, playerIdx);
        return NumberUtils.toInt(tableIdStr);
    }

    /**
     * @param playerIdx
     * @param sceneId
     * @return 是否在某个队列
     */
    private boolean hasAtQue(String playerIdx, int sceneId) {
        if (sceneId <= 0) {
            return false;
        }
        String QueLockKey = GameConst.RedisKey.CrossArenaQueLock + "" + sceneId;
        String keyQue = GameConst.RedisKey.CrossArenaQue + "" + sceneId;
        List<String> list = jedis.lrange(keyQue, 0, -1);
        if (null != list && list.contains(playerIdx)) {
            return true;
        }
        return false;
    }

    /**
     * 战斗结束
     *
     * @param playerId
     * @param battleResult
     * @param eventId
     */
    public void battleSettle(String playerId, int battleResult, int eventId) {
        boolean victory = battleResult == 1;
        if (victory) {
            flishEvent(playerId, eventId);
        }
    }

    /**
     * @param playerIdx
     * @param eventId   玩家完成事件
     */
    public void flishEvent(String playerIdx, int eventId) {
        CrossArenaEventObject ecfg = CrossArenaEvent.getById(eventId);
        if (null == ecfg || !playerCache.getInstance().hasPlayerIdx(playerIdx)) {
            return;
        }
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        // 判断事件发是否超时
        long timeEnd = pe.getDataMsg().getEventCurOrDefault(eventId, -1L);
        if (timeEnd > 0 && GlobalTick.getInstance().getCurrentTime() > timeEnd + 300000L) {
            return;
        }
        SyncExecuteFunction.executeConsumer(pe, p -> {
            Map<Integer, Long> temp = new HashMap<>();
            temp.putAll(p.getDataMsg().getEventCurMap());
            if (ecfg.getType() != 3) {
                temp.remove(eventId);
            }
            p.getDataMsg().clearEventCur();
            p.getDataMsg().putAllEventCur(temp);
            int x = p.getDataMsg().getEventFlishNumOrDefault(eventId, 0) + 1;
            p.getDataMsg().putEventFlishNum(eventId, x);
            if (ecfg.getType() == 2) {
                savePlayerDBInfoNotLock(p, CrossArenaDBKey.LT_SCENEID, ecfg.getSceneid(), CrossArenaUtil.DbChangeRepMax);
            }
        });
        if (ecfg.getType() == 2) {
            sceneUpLV(playerIdx, ecfg.getSceneid());
        }
        sendPlayerEvents(playerIdx);
        // 闯关事件
        playerEntity pen = playerCache.getByIdx(playerIdx);
        if (ecfg.getType() == 2 && null != pen) {
            String rkey = GameConst.RedisKey.CrossArenaNoteInsMap + "" + ecfg.getSceneid();
            long len = jedis.hlen(rkey);
            boolean hasName = jedis.hexists(rkey, pen.getName());
            String yr = jedis.get(GameConst.RedisKey.CrossArenaYear);
            int yaer = 1;
            if (!StringHelper.isNull(yr)) {
                yaer = DateUtil.differDays(NumberUtils.toLong(yr)) + 1;
            }
            long yaerKey = yaer * 1000L + len;
            String yaerStr = "" + yaerKey;
            if (len < 50 && !hasName) {
                jedis.hset(rkey, yaerStr, pen.getName());
            }
        }
        if (ecfg.getMarquee() > 0) {
            playerEntity pe11 = playerCache.getByIdx(playerIdx);
            if (null != pe11) {
                GlobalData.getInstance().sendMarqueeToAllOnlinePlayer(ecfg.getMarquee(), pe11.getName());
            }
        }
    }

    /**
     * @param playerIdx
     * @param curSlv    玩家道场等级提升
     */
    public void sceneUpLV(String playerIdx, int curSlv) {
        EventUtil.triggerUpdateTargetProgress(playerIdx, TTE_CrossArena_SCENEIDReach, curSlv, 0);
        // 更新事件
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        SyncExecuteFunction.executeConsumer(pe, p -> {
            Map<Integer, Long> temp = p.getDataMsg().getEventCurMap();
            Map<Integer, Long> newMap = new HashMap<>();
            for (Entry<Integer, Long> ent : temp.entrySet()) {
                CrossArenaEventObject ecfg = CrossArenaEvent.getById(ent.getKey());
                if (null == ecfg) {
                    continue;
                }
                if (ecfg.getSceneid() == curSlv) {
                    continue;
                }
                newMap.put(ent.getKey(), ent.getValue());
            }
            p.getDataMsg().clearEventCur();
            p.getDataMsg().putAllEventCur(newMap);
        });
        // 需要判断是否在队列中，在则移除低等级队列
        if (hasAtQue(playerIdx, curSlv - 1)) {
            quitQue(playerIdx);
        }
    }

    /**
     * 下发玩家事件信息
     */
    public void sendPlayerEvents(String playerIdx) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        CrossArena.SC_CrossArenaEvents.Builder msg = CrossArena.SC_CrossArenaEvents.newBuilder();
        CrossArena.CrossArenaTenWin.Builder msg3 = CrossArena.CrossArenaTenWin.newBuilder();
        int jifen = 0;
        CrossArenaCfgObject basecfg = CrossArenaCfg.getById(GameConst.CONFIG_ID);
        long curr = GlobalTick.getInstance().getCurrentTime();
        for (Map.Entry<Integer, Long> ent : pe.getDataMsg().getEventCurMap().entrySet()) {
            CrossArenaEventObject ecfg = CrossArenaEvent.getById(ent.getKey());
            if (null == ecfg) {
                continue;
            }
            if (ent.getValue() > 0 && curr > ent.getValue()) {
                continue;
            }
            if (ecfg.getType() == 3) {
                msg3.addEventIdsCurr(ent.getKey());
                if (pe.getDataMsg().containsEventFlishNum(ent.getKey())) {
                    msg3.addEventIdsFlish(ent.getKey());
                    for (int[] ent2 : ecfg.getAward()) {
                        if (ent2[1] == basecfg.getAwardgrade()[1]) {
                            jifen += ent2[2];
                        }
                    }
                }
            } else {
                CrossArena.CrossArenaEventMsg.Builder msg2 = CrossArena.CrossArenaEventMsg.newBuilder();
                msg2.setEventId(ent.getKey());
                msg2.setEndTime(ent.getValue());
                msg.addEvents(msg2);
            }
        }
        msg3.setJifen(jifen);
        if (isOpenDub) {
            msg.setTenWIN(msg3);
        } else {
            msg.setTenWIN(CrossArena.CrossArenaTenWin.newBuilder());
        }
        GlobalData.getInstance().sendMsg(pe.getIdx(), MessageId.MsgIdEnum.SC_CrossArenaEvents_VALUE, msg);
    }


    /**
     * 过天执行，处理全部玩家得积分奖励
     */
    public void updateDailyData() {
        //清除玩家每日完成10连胜状态
        clearTenWinPlayers();
        for (BaseEntity entity : playercrossarenaCache.getInstance().getAll().values()) {
            playercrossarenaEntity pe = (playercrossarenaEntity) entity;
            awardSend(pe, false);
        } // 11 5 0 4 1
        for (String playerIdx : playerJionTime.keySet()) {
           quitCrossArenaLJTime(playerIdx,false);
        }
    }

    /**
     * @param pe 奖励结算
     */
    private void awardSend(playercrossarenaEntity pe, boolean isGm) {
        if (!playerCache.getInstance().hasPlayerIdx(pe.getBaseIdx())) {
            return;
        }
        CrossArenaCfgObject cfgData = CrossArenaCfg.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            return;
        }
        quitCrossArenaLJTime(pe.getIdx(), false);
        if (pe.getDataMsg().getIsAward() > 0) {
            return;
        }

        // 处理10连胜任务
        List<Reward> list = getWinTaskAward(pe.getIdx());
        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_CrossArena10Win);

        if (!list.isEmpty()) {
            list = RewardUtil.mergeReward(list);
            EventUtil.triggerAddMailEvent(pe.getIdx(), cfgData.getGrademailid(), list, reason);
        }

        if (!isGm) {
            SyncExecuteFunction.executeConsumer(pe, p -> {
                pe.getDataMsg().setIsAward(1);
            });
        }
    }

    public int computeExpIncr(playercrossarenaEntity pe) {
        int weekBat = getPlayerDBInfo(pe, CrossArenaDBKey.LT_BATTLENUM_WEEK);
        int weekWin = getPlayerDBInfo(pe, CrossArenaDBKey.LT_BATTLEWinNUM_WEEK);
        if (weekBat<=0){
            return 0;
        }
        int winRate = weekWin * 1000 / weekBat;
        int exp1 = CrossArenaExpCfg.getExpByTotalNum(weekBat);

        int exp2 = CrossArenaExpCfg.getExpByWinRate(winRate, weekBat);
        return exp1 + exp2;
    }

    // 处理10连胜任务
    public List<Common.Reward> getWinTaskAward(String playerIdx) {
        int sceneId = getPlayerDBInfo(playerIdx, CrossArenaDBKey.LT_SCENEID);
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        Map<Integer, TargetSystem.TargetMission> missionPro = entity.getDb_Builder().getCrossArenaInfoMap();
        List<Common.Reward> temp = new ArrayList<>();
        for (TargetSystem.TargetMission tm : missionPro.values()) {
            MissionObject missionCfg = Mission.getById(tm.getCfgId());
            if (missionCfg == null || Common.MissionStatusEnum.MSE_Finished != tm.getStatus()) {
                continue;
            }
            // 读取任务奖励
            CrossArenaTaskAwardObject jl = CrossArenaTaskAward.getInstance().getAward(missionCfg.getId(), sceneId, 0);
            if (null == jl) {
                continue;
            }
            temp.addAll(RewardUtil.parseRewardIntArrayToRewardList(jl.getAward()));
        }
        return temp;
    }

    /**
     * @param playerId
     * @param vue      增加玩家积分
     */
    public void addGrade(String playerId, int vue) {
        playercrossarenaEntity pe = getPlayerEntity(playerId);
        addGrade(pe, vue);
    }

    public void addGrade(playercrossarenaEntity pe, int vue) {

        int currLv = getPlayerDBInfo(pe, CrossArenaDBKey.LT_GRADELV);
        CrossArenaLvCfgObject lvCfg = CrossArenaLvCfg.getByLv(currLv);
        if (null == lvCfg) {
            return;
        }
        int canReachMaxLv = getScienceMaxGradeLv(getPlayerDBInfo(pe, LT_MAXSCENEID));
        int weekScore = getPlayerDBInfo(pe, CrossArenaDBKey.LT_WEEKSCORE);
        int currGrade = getPlayerDBInfo(pe, CrossArenaDBKey.LT_GRADECUR);
        Integer[] result = getUpgradeLevelsByExp(currLv, currGrade, vue, canReachMaxLv);
        int canUpLevels = result[0];
        int remainingExp = result[1];
        vue = result[2];
        int nowWeekScore =weekScore + vue;
        SyncExecuteFunction.executeConsumer(pe, p -> {
            p.getDataMsg().putDbs(CrossArenaDBKey.LT_GRADELV_VALUE, canUpLevels + currLv);
            p.getDataMsg().putDbs(CrossArenaDBKey.LT_GRADECUR_VALUE, remainingExp);
            p.getDataMsg().putDbs(CrossArenaDBKey.LT_WEEKSCORE_VALUE, nowWeekScore);
        });
        triggerGradeLvUpEvent(pe.getBaseIdx(), pe);
        sendMainPanelInfo(pe.getBaseIdx());
        sendGradeLvUpdate(pe.getIdx(),pe.getDataMsg().getDbsOrDefault(LT_GRADELV_VALUE,0));
        //更新擂台赛排行榜
        RankingManager.getInstance().updatePlayerRankingScore(pe.getIdx(), Activity.EnumRankingType.ERT_Lt_Score, findPlayerTotalExp(pe), findWeeklyWinRate(pe.getIdx()));
    }

    public int findPlayerTotalExp(playercrossarenaEntity pe) {
        int lv = pe.getDataMsg().getDbsMap().getOrDefault(LT_GRADELV_VALUE, 0);
        int nowExp = pe.getDataMsg().getDbsMap().getOrDefault(LT_GRADECUR_VALUE, 0);
        return CrossArenaLvCfg.getInstance().getLastLvTotalExp(lv) + nowExp;
    }

    private int getScienceMaxGradeLv(int scienceId) {
        CrossArenaSceneObject cfg = CrossArenaScene.getById(scienceId);
        if (cfg == null) {
            return 0;
        }
        return cfg.getHonorlv();
    }

    private void sendGradeLvUpdate(String playerIdx, int lv) {
        CrossArena.SC_CrossArenaLvUpdate.Builder msg = CrossArena.SC_CrossArenaLvUpdate.newBuilder().setHonorLv(lv);
        GlobalData.getInstance().sendMsg(playerIdx,SC_CrossArenaLvUpdate_VALUE,msg);
    }

    private void triggerGradeLvUpEvent(String playerId, playercrossarenaEntity pe) {
        int nowLv = pe.getDataMsg().getDbsMap().getOrDefault(LT_GRADELV_VALUE, 0);
        EventUtil.triggerUpdateTargetProgress(playerId, TTE_CrossArena_GRADELvReach, nowLv, 0);
    }

    /**
     * 有当前经验值计算可以升级级数
     *
     * @param currLevel
     * @param currExp
     * @param add
     * @param canReachMaxLv
     * @return Object[0:canUpLevels; 1:remaningExp 2:实际新增的經驗]
     */
    public Integer[] getUpgradeLevelsByExp(int currLevel, int currExp, int add, int canReachMaxLv) {
        Integer[] result = new Integer[3];
        int canUpLevels = 0;
        int remainExp = currExp + add;
        int tempLevel = currLevel;
        int useExp = 0;
        CrossArenaLvCfgObject lvCfg = CrossArenaLvCfg.getByLv(tempLevel);
        if (null == lvCfg) {
            result[0] = 0;
            result[1] = currExp + add;
            result[2] = 0;
            return result;
        }
        int nextExp = lvCfg.getNextlvlexp();
        while (nextExp > 0 && remainExp >= nextExp) {
            if (canReachMaxLv <= tempLevel) {
                remainExp = Math.min(remainExp, nextExp);
                break;
            }
            remainExp -= nextExp;
            useExp += nextExp;
            canUpLevels++;
            tempLevel++;
            lvCfg = CrossArenaLvCfg.getByLv(tempLevel);
            if (null == lvCfg) {
                break;
            }
            nextExp = lvCfg.getNextlvlexp();
        }
        useExp = useExp + remainExp - currExp;
        result[0] = canUpLevels;
        result[1] = remainExp;
        result[2] = useExp;
        return result;
    }

    /**
     * 全服广播擂台数据变化
     *
     * @param tableDB
     */
    public void refTableInfoToAllServer(RedisCrossArenaTableDB tableDB) {
        // 通知全服刷新该擂台数据
        ServerTransfer.BS_GS_CrossArenaRefInfo.Builder msg10 = ServerTransfer.BS_GS_CrossArenaRefInfo.newBuilder();
        msg10.setTableInfo(tableDB);
        BattleServerManager.getInstance().transferMsgGSToGSRom(MessageId.MsgIdEnum.BS_GS_CrossArenaRefInfo_VALUE, msg10.build().toByteString());
    }

    /**
     * @param tableDB 擂主退出擂台(除非战斗状态，否则都可以退出)
     * @return
     */
    private RedisCrossArenaTableDB.Builder tbRedisQuitDef(RedisCrossArenaTableDB tableDB) {
        RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
        // 超时，设置为等待状态,设置最长等待时间
        if (StringHelper.isNull(tableDB.getDefPlayer().getPlayerId())) {
            return null;
        }
        CrossArenaSceneObject casoCfg = CrossArenaScene.getById(getSceneIdByTableId(tableDB.getLeitaiId()));
        if (null == casoCfg) {
            return null;
        }
        delPlayerTableMapCache(tableDB.getDefPlayer().getPlayerId());
        if (tableDB.getState() == CrossArena.CrossArenaState.WAIT_VALUE) {
            newtableDB.setState(CrossArena.CrossArenaState.IDLE_VALUE);
            newtableDB.setStateEndTime(0);
            newtableDB.clearDefPlayer();
            newtableDB.clearAttPlayer();
            newtableDB.clearGuessAttSvrData().clearGuessDefSvrData();
            newtableDB.setSettleTime(0);
            newtableDB.setBattleId(0);
            newtableDB.setDefWinNum(0);
            newtableDB.setLastBattleTime(System.currentTimeMillis());
        } else if (tableDB.getState() == CrossArena.CrossArenaState.READY_VALUE) {
            newtableDB.clearDefPlayer();
            newtableDB.setDefPlayer(tableDB.getAttPlayer());
            newtableDB.clearAttPlayer();
            newtableDB.setState(CrossArena.CrossArenaState.WAIT_VALUE);
            newtableDB.setStateEndTime(System.currentTimeMillis() + casoCfg.getRevoketime() * 1000L);
            newtableDB.clearGuessAttSvrData().clearGuessDefSvrData();
            newtableDB.setDefWinNum(0);
            newtableDB.setBattleId(0);
            newtableDB.setLastBattleTime(System.currentTimeMillis());
        } else {
            return null;
        }
        return newtableDB;
    }

    private void delPlayerTableMapCache(String playerIdx) {
        updataPlyTableIdRedis(playerIdx, "");
    }

    /**
     * @param tableDB 战斗超时，容错处理，移除攻击者
     * @return
     */
    private RedisCrossArenaTableDB.Builder tbRedisQuitAtt(RedisCrossArenaTableDB tableDB) {
        if (tableDB.getState() != CrossArena.CrossArenaState.READY_VALUE) {
            return null;
        }
        CrossArenaSceneObject casoCfg = CrossArenaScene.getById(getSceneIdByTableId(tableDB.getLeitaiId()));
        if (null == casoCfg) {
            return null;
        }
        RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
        // 超时，设置为等待状态,设置最长等待时间
        if (StringHelper.isNull(tableDB.getAttPlayer().getPlayerId())) {
            return null;
        }
        delPlayerTableMapCache(tableDB.getAttPlayer().getPlayerId());
        newtableDB.setState(CrossArena.CrossArenaState.WAIT_VALUE);
        newtableDB.setStateEndTime(System.currentTimeMillis() + casoCfg.getRevoketime() * 1000L);
        newtableDB.clearAttPlayer();
        newtableDB.clearGuessAttSvrData().clearGuessDefSvrData();
        newtableDB.setSettleTime(0);
        newtableDB.setDefWinNum(0);
        newtableDB.setBattleId(0);
        newtableDB.setLastBattleTime(System.currentTimeMillis());
        return newtableDB;
    }

    /**
     * @param tableDB 战斗超时，容错处理，移除攻击者
     * @return
     */
    private RedisCrossArenaTableDB.Builder tbRedisQuitQue(RedisCrossArenaTableDB tableDB, String playerIdx) {
        // 在队列中则可以退出
        List<RedisCrossArenaPlayer> tempDB = new ArrayList<RedisCrossArenaPlayer>();
        List<RedisCrossArenaPlayer> newDB = new ArrayList<RedisCrossArenaPlayer>();
        tempDB.addAll(tableDB.getDuiList());
        for (RedisCrossArenaPlayer rcap : tempDB) {
            if (!Objects.equals(rcap.getPlayerId(), playerIdx)) {
                newDB.add(rcap.toBuilder().build());
            }
        }
        if (tempDB.size() == newDB.size()) {
            delPlayerTableMapCache(playerIdx);
            return null;
        }
        RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
        newtableDB.clearDui();
        newtableDB.addAllDui(newDB);
        delPlayerTableMapCache(playerIdx);
        return newtableDB;
    }

    /**
     * 刷新缓存玩家所在擂台ID得信息
     *
     * @param playerIdx
     * @param vue
     */
    public void updataPlyTableIdRedis(String playerIdx, String vue) {
        LogUtil.info("lt player:{} is del redis table map", playerIdx);
        // 清除玩家位置信息
        if (playerIdx != null && !playerIdx.contains(AINAME)) {
            jedis.hset(GameConst.RedisKey.CrossArenaPlayerTable, playerIdx, vue);
        }
    }

    public void gmSetSceneLv(String playerId, int lv) {
        savePlayerDBInfoRep(playerId, CrossArenaDBKey.LT_SCENEID, lv);
    }

    public void gmReset() {
        openInitData(true);
    }

    public void gmSetGradeLv(String playerIdx, int n) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        SyncExecuteFunction.executeConsumer(pe, p -> {
            p.getDataMsg().putDbs(CrossArenaDBKey.LT_GRADELV_VALUE, n);
            p.getDataMsg().putDbs(LT_GRADECUR_VALUE, 100);
        });
        sendMainPanelInfo(playerIdx);
    }

    public void gmTest(String playerIdx, int tableId) {
        try {
//            byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
//            if (null != oneLeiTaiDB) {
//                RedisCrossArenaTableDB tableInfo = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
//                LogUtil.error(""+tableInfo.getIsAIBattle());
//                LogUtil.error(""+tableInfo.getDefPlayer().getIsAI());
//                LogUtil.error(""+tableInfo.getAttPlayer().getIsAI());
//                LogUtil.error(""+tableInfo.getDefPlayer().getPlayerId());
//                LogUtil.error(""+tableInfo.getAttPlayer().getPlayerId());
//            }
//            if (jedis.hexists(RedisKey.CrossArenaPlOnline, "1251902175278317573")) {
//            	LogUtil.error(""+jedis.hexists(RedisKey.CrossArenaPlOnline, "1251902175278317573"));
//            }
            // 闯关事件
//        	CrossArenaEventObject ecfg = CrossArenaEvent.getById(tableId);
//            if (null == ecfg) {
//                return;
//            }
//            playerEntity pen = playerCache.getByIdx(playerIdx);
//            if (ecfg.getType() == 2 && null != pen) {
//                String rkey = GameConst.RedisKey.CrossArenaNoteInsMap + "" + ecfg.getId();
//                long len = jedis.hlen(rkey);
//                boolean hasName = jedis.hexists(rkey, pen.getName());
//                String yr = jedis.get(GameConst.RedisKey.CrossArenaYear);
//                if (len < 50 && !hasName) {
//                    jedis.hset(rkey, yr, pen.getName());
//                }
//            }
            add10WinNote(playerIdx, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 手动过天发邮件
     */
    public void gmMailUpdateDay(String playerIdx) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        awardSend(pe, true);
        SyncExecuteFunction.executeConsumer(pe, p -> {
            pe.updateDailyData();
        });
        playerJionTime.put(playerIdx, System.currentTimeMillis());
    }

    public void gmAddGrade(String playerIdx, int n) {
        addGrade(playerIdx, n);
    }

    private long getTenTime() {
        String time = jedis.get(RedisKey.CrossArenaTen);
        if (StringHelper.isNull(time)) {
            return 0;
        }
        return NumberUtils.toLong(time);
    }

    public void updateWeekTask(playercrossarenaEntity pe, int type, int addType, int value) {
        int total = pe.getDataMsg().getWeekTaskDataMap().getOrDefault(type, 0);
        switch (addType) {
            case CrossArenaUtil.DbChangeAdd:
                int max = Integer.MAX_VALUE - total;
                if (value > max) {
                    value = max;
                }
                total += value;
                break;
            case CrossArenaUtil.DbChangeRep:
                total = value;
                break;
            case CrossArenaUtil.DbChangeRepMax:
                if (value > total) {
                    total = value;
                }
                break;
            default:
                break;
        }
        CrossArenaLvCfgObject config = CrossArenaLvCfg.getByLv(pe.getDataMsg().getDbsMap().getOrDefault(CrossArenaDBKey.LT_GRADELV_VALUE, 1));
        if (config != null) {
            for (Integer taskId : config.getFix_task()) {
                if (pe.getDataMsg().getWeekTaskDataMap().containsKey(taskId)) {
                    continue;
                }
                CrossArenaLvTaskObject taskConfig = CrossArenaLvTask.getById(taskId);
                if (taskConfig == null) {
                    continue;
                }
                if (taskConfig.getType() != type) {
                    continue;
                }
                if (total >= taskConfig.getValue()) {
                   // RewardManager.getInstance().doRewardByRewardId(pe.getIdx(), taskConfig.getScore(), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossArenaWinTask), true);
                    pe.getDataMsg().putWeekTaskData(taskConfig.getType(), 1);
                }
            }
        }
        pe.getDataMsg().putWeekTaskData(type, total);
    }

    public int getWeekTaskData(String playerId, int type) {
        playercrossarenaEntity pe = getPlayerEntity(playerId);
        return pe.getDataMsg().getWeekTaskDataMap().getOrDefault(type, 0);
    }

    /**
     * 连胜奖励
     */
    public void getWinTaskReward(String playerId) {
        SC_CrossArenaWinTask.Builder builder = SC_CrossArenaWinTask.newBuilder();
        builder.setRetCode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Failure));
        playercrossarenaEntity pe = getPlayerEntity(playerId);
        if (!pe.getDataMsg().getDbsMap().containsKey(CrossArenaDBKey.LT_WINCOTDAY_VALUE)) {
            GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWinTask_VALUE, builder);
            return;
        }

        int chooseWinTaskScienceId =pe.getDataMsg().getDailyWinTaskScienceId();
        int playerNowInSceneId = findPlayerNowInSceneId(playerId);
        if (chooseWinTaskScienceId != 0 && chooseWinTaskScienceId != playerNowInSceneId) {
            builder.setRetCode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CrossArena_OnlyCanChooseOneScienceReward));
            GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWinTask_VALUE, builder);
            return;
        }

        DailyWinTaskReward result = getDailyWinTaskRewardList(playerNowInSceneId, playerId);
        if (CollectionUtils.isEmpty(result.getRewards())){
            builder.setRetCode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CrossArena_NoRewardCanClaim));
            GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWinTask_VALUE, builder);
            return;
        }

        builder.setRetCode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
        SyncExecuteFunction.executeConsumer(pe, tmp -> {
            pe.getDataMsg().addAllWinTask(result.getTaskIds());
            pe.getDataMsg().setDailyWinTaskScienceId(playerNowInSceneId);
        });
        builder.addAllIds(result.getTaskIds());

        Reason borrowReason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossArenaWinTask);
        RewardManager.getInstance().doRewardByList(playerId, result.getRewards(), borrowReason, true);
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWinTask_VALUE, builder);
    }


    public DailyWinTaskReward getDailyWinTaskRewardList(int scienceId, String playerIdx) {
        DailyWinTaskReward result = new DailyWinTaskReward();
        playercrossarenaEntity entity = getPlayerEntity(playerIdx);
        int winNum = getWinNumByScienceId(entity, scienceId, LT_WINCOTDAY);
        if (winNum <= 0) {
            return result;
        }
        List<Integer> claimedList = entity.getDataMsg().getWinTaskList();
        List<CrossArenaWinTaskObject> dailyTaskByScienceId = CrossArenaWinTask.getInstance().getDailyTaskByScienceId(scienceId);
        List<Reward> rewards = new ArrayList<>();
        List<Integer> taskIds = new ArrayList<>();
        for (CrossArenaWinTaskObject cfg : dailyTaskByScienceId) {
            if (!claimedList.contains(cfg.getId()) && cfg.getWinning() <= winNum) {
                rewards.addAll(parseTaskReward(cfg));
                taskIds.add(cfg.getId());
            }
        }
        LogUtil.info("player:{} claim daily 10WinTask reward,taskIds:{}", playerIdx, taskIds);
        if (CollectionUtils.isNotEmpty(rewards)) {
            RewardUtil.mergeRewardList(rewards);
            rewards = discountWinTaskReward(playerIdx, scienceId, rewards);
        }
        result.setRewards(rewards);
        result.setTaskIds(taskIds);
        return result;
    }

    private List<Reward> discountWinTaskReward(String playerIdx, int scienceId, List<Reward> rewards) {
        int playerMaxSceneId = findPlayerMaxSceneId(playerIdx);
        //玩家不在当前道场奖励会衰减
        if (CollectionUtils.isEmpty(rewards) || scienceId == playerMaxSceneId) {
            return rewards;
        }
        int rate = findWinTaskRewardReduceRate(playerIdx, scienceId, playerMaxSceneId);
        if (rate <= 0) {
            return rewards;
        }
        List<Reward> result = new ArrayList<>();
        for (Reward reward : rewards) {
            result.add(reward.toBuilder().setCount((int) (reward.getCount() * (rate / 1000.0))).build());
        }

        return result;
    }

    private int findWinTaskRewardReduceRate(String playerIdx, int scienceId, int playerMaxSceneId) {
        CrossArenaSceneObject cfg = CrossArenaScene.getById(scienceId);
        int base = 1000;
        if (cfg == null) {
            return base;
        }
        for (int[] ints : cfg.getWinrewardrate()) {
            if (ints.length == 2 && ints[0] == playerMaxSceneId) {
                base = ints[1];
            }
        }
        if (scienceId >= playerMaxSceneId) {
            return base;
        }
        int winNum = getWinNumByScienceId(getPlayerEntity(playerIdx), scienceId, LT_10SerialWinWeek);

        return Math.max(0, base - CrossArenaCfg.getById(GameConst.CONFIG_ID).getSerailwinrewardreduce() * winNum);
    }

    public   List<Reward> parseTaskReward(CrossArenaWinTaskObject cfg) {
        List<Reward> result = RewardUtil.getRewardsByRewardId(cfg.getReward());
        if (CollectionUtils.isEmpty(result)){
            return Collections.emptyList();
        }
        List<Reward> random ;
        for (int i = 0; i < cfg.getRandom(); i++) {
            random= RewardUtil.getRewardsByRewardId(cfg.getRandom_reward());
            if (CollectionUtils.isNotEmpty(random)){
                result.addAll(random);
            }
        }
        return result;
    }

    public void getWinTaskPanel(String playerId) {
        SC_CrossArenaWinPanel.Builder builder = SC_CrossArenaWinPanel.newBuilder();
        playercrossarenaEntity pe = getPlayerEntity(playerId);
        builder.addAllIds(pe.getDataMsg().getWinTaskList());
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWinPanel_VALUE, builder);
    }

    /**
     * 查询玩家荣耀等级
     * @param playerIdx
     * @return
     */
    public int findPlayerGradeLv(String playerIdx) {
        playercrossarenaEntity pe = playercrossarenaCache.getByIdx(playerIdx);
        if (null == pe) {
            return 1;
        }
        return getPlayerDBInfo(pe, CrossArenaDBKey.LT_GRADELV);
    }

    public void getGradePanel(String playerId) {
        playercrossarenaEntity pe = getPlayerEntity(playerId);
        SC_CrossArenaGradePanel.Builder msg = SC_CrossArenaGradePanel.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        msg.setWeekExp(pe.getDataMsg().getDbsOrDefault(LT_WEEKSCORE_VALUE,0));
        msg.setWinNumMaxWeek(findMaxCotWeekNum(pe));
        msg.setWinNumDay(pe.getDataMsg().getDbsOrDefault(LT_WINNUM_DAY_VALUE,0));
        msg.setBattleNumDay(pe.getDataMsg().getDbsOrDefault(LT_BATTLENUM_DAY_VALUE,0));
        msg.setWinNumWeek(pe.getDataMsg().getDbsOrDefault(LT_BATTLEWinNUM_WEEK_VALUE,0));
        msg.setBattleNumWeek(pe.getDataMsg().getDbsOrDefault(LT_BATTLENUM_WEEK_VALUE,0));
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaGradePanel_VALUE, msg);
    }

    /**
     * 获取各个道场中本周最大连胜数的最大值
     * @param pe
     * @return
     */
    private int findMaxCotWeekNum(playercrossarenaEntity pe) {
        return pe.getDataMsg().getSerialWinDataMap().values().stream().map(e -> e.getWinDataOrDefault(LT_WINCOTWeekly_VALUE, 0)).max(Integer::compareTo).orElse(0);
    }

    public void getWeekBoxTaskPanel(String playerId) {
        SC_CrossArenaWeekBoxPanel.Builder builder = SC_CrossArenaWeekBoxPanel.newBuilder();

        playercrossarenaEntity pe = getPlayerEntity(playerId);
        CrossArenaPlayerDB.Builder dataMsg = pe.getDataMsg();
        List<CrossArenaTaskWeekBox> showTask = new ArrayList<>();
        List<CrossArenaTaskWeekBox> preWeekTask = dataMsg.getPreWeekboxList();
        if (preWeekTask.size() > 0) {// 推送上周数据
            showTask.addAll(preWeekTask);
            builder.setType(1);
        } else {
            SyncExecuteFunction.executeConsumer(pe, temp -> {
                if (dataMsg.getWeekBoxTaskCount() <= 0) {
                    List<Integer> createWeekBoxTask = createWeekBoxTask(playerId);
                    for (Integer i : createWeekBoxTask) {
                        pe.getDataMsg().putWeekBoxTask(i, 0);
                    }
                }
            });
//			}
            for (Entry<Integer, Integer> ent : pe.getDataMsg().getWeekBoxTaskMap().entrySet()) {
                CrossArenaLvTaskObject taskConfig = CrossArenaLvTask.getById(ent.getKey());
                if (taskConfig == null) {
                    continue;
                }
                CrossArenaTaskWeekBox creatWeekBoxTask = creatWeekBoxTask(taskConfig.getId(), getWeekTaskData(playerId, taskConfig.getType()), taskConfig.getValue());
                showTask.add(creatWeekBoxTask);
            }
        }
        builder.addAllWeekbox(showTask);
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWeekBoxPanel_VALUE, builder);
    }

    public List<CrossArenaTaskWeekBox> createPreWeekBoxReward(String playerIdx, Collection<Integer> taskIds, Map<Integer, Integer> weekData) {
        int finishTaskCount = 0;
        boolean luck = false;
        List<CrossArenaTaskWeekBox> list = new ArrayList<>();
        int gradeLv = findPlayerGradeLv(playerIdx);
        for (Integer taskId : taskIds) {
            CrossArenaLvTaskObject taskConfig = CrossArenaLvTask.getById(taskId);
            if (taskConfig == null) {
                continue;
            }
            int have = weekData.getOrDefault(taskConfig.getType(), 0);

            CrossArenaTaskWeekBox task = creatWeekBoxTask(taskConfig.getId(), have, taskConfig.getValue());
            if (task.getTask().getState() != 0) {
                finishTaskCount++;
                if (finishTaskCount >= 9) {
                    luck = true;
                }
                List<Reward> rewards = findGradeLvReward(gradeLv,taskConfig);
                if (CollectionUtils.isNotEmpty(rewards)) {
                    task = task.toBuilder().addAllReward(rewards).build();
                }
                list.add(task);
            }
        }
        if (luck) {
            CrossArenaTaskWeekBox task = list.get(random.nextInt(list.size()));
            int[] cross_weekbos_luck = GameConfig.getById(GameConst.CONFIG_ID).getCross_weekbos_luck();
            if (cross_weekbos_luck.length == 3) {
                Reward luckReward = RewardUtil.parseReward(cross_weekbos_luck);
                CrossArenaTaskWeekBox newTask = task.toBuilder().clearReward().addReward(luckReward).build();
                list.remove(task);
                list.add(newTask);
            }
        }
        return list;
    }

    private List<Reward> findGradeLvReward(int gradeLv, CrossArenaLvTaskObject taskConfig) {
        Optional<int[]> any = Arrays.stream(taskConfig.getReward()).filter(e -> e.length == 2 && e[0] == gradeLv).findAny();
        if (any.isPresent()) {
            return RewardUtil.getRewardsByRewardId(any.get()[1]);
        }
        return Collections.emptyList();
    }

/*    public void doWeekGradeReward(String playerId, int lvGrade) {
        CrossArenaLvCfgObject config = CrossArenaLvCfg.getByLv(lvGrade);
        if (config == null) {
            return;
        }
        RewardManager.getInstance().doRewardByRewardId(playerId, config.getWeek_reward(), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CROSSARENA_GRADE), true);
    }*/

    public CrossArenaTaskWeekBox creatWeekBoxTask(int id, int have, int need) {
        CrossArenaTaskWeekBox.Builder builder = CrossArenaTaskWeekBox.newBuilder();
        builder.setTask(createTaskCommon(id, have, need));
        return builder.build();
    }

    public CrossArenaTaskWeekBox creatWeekBoxTask(int id, int have, int need, Reward reward) {
        CrossArenaTaskWeekBox.Builder builder = CrossArenaTaskWeekBox.newBuilder();
        builder.setTask(createTaskCommon(id, have, need));
        return builder.build();
    }

    public CrossArenaTaskCommon createTaskCommon(int id, int have, int need) {
        if (have > need) {
            have = need;
        }
        CrossArenaTaskCommon.Builder builder = CrossArenaTaskCommon.newBuilder();
        builder.setCur(have);
        builder.setMax(need);
        builder.setState(have >= need ? 1 : 0);
        builder.setId(id);
        return builder.build();
    }

    private static  final int BoxRewardClaimState = 2;

    public void getWeekBoxTaskReward(String playerId, int taskId) {
        SC_CrossArenaWeekBoxReward.Builder builder = SC_CrossArenaWeekBoxReward.newBuilder();
        builder.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Failure));
        playercrossarenaEntity pe = getPlayerEntity(playerId);
        List<CrossArenaTaskWeekBox> weekboxList = pe.getDataMsg().getPreWeekboxList();

        if (weekboxList.size() == 0) {
            builder.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CrossArena_ClaimTimeNotReach));
            GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWeekBoxReward_VALUE, builder);
            return;
        }
        int claimedNum = (int) weekboxList.stream().filter(e -> e.getTask().getState() == BoxRewardClaimState).count();
        int maxCanClaimNum = CrossArenaCfg.getById(GameConst.CONFIG_ID).getWeekboxgetnum();
        if (claimedNum >= maxCanClaimNum) {
            builder.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CrossArena_NoRewardCanClaim));
            GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWeekBoxReward_VALUE, builder);
            return;
        }
        Optional<CrossArenaTaskWeekBox> any = weekboxList.stream().filter(e -> e.getTask().getId() == taskId).findAny();
        if (!any.isPresent()){
            builder.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CrossArena_ClaimTimeNotReach));
            GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWeekBoxReward_VALUE, builder);
            return;
        }
        CrossArenaTaskWeekBox claimTask = any.get();
        if (claimTask.getTask().getState() == BoxRewardClaimState) {
            builder.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
            GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWeekBoxReward_VALUE, builder);
            return;
        }
        List<CrossArenaTaskWeekBox> newWeekBox = new ArrayList<>(weekboxList);
        SyncExecuteFunction.executeConsumer(pe, e -> {
            if (claimedNum + 1 >= maxCanClaimNum || claimedNum+1 >= weekboxList.size()) {
                pe.getDataMsg().clearPreWeekbox();
            } else {
                //设置已领取状态
                newWeekBox.remove(claimTask);
                newWeekBox.add(claimTask.toBuilder().setTask(claimTask.getTask().toBuilder().setState(BoxRewardClaimState)).build());
                pe.getDataMsg().clearPreWeekbox().addAllPreWeekbox(newWeekBox);
            }
        });
        RewardManager.getInstance().doRewardByList(playerId, claimTask.getRewardList(), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CROSSARENA_GRADE), true);
        builder.setId(taskId);
        builder.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaWeekBoxReward_VALUE, builder);
    }



    public List<Integer> createWeekBoxTask(String playerId) {
        List<Integer> list = new ArrayList<>();

        CrossArenaLvCfgObject lvConfig = CrossArenaLvCfg.getByLv(getMaxStageId(playerId));
        if (lvConfig == null) {
            return list;
        }
        Set<Integer> taskIds = new LinkedHashSet<>();
        List<Integer> ignore = new ArrayList<>();
        int[] fixWeekTask = lvConfig.getFix_week_task();
        if (fixWeekTask == null) {
            return list;
        }
        if (fixWeekTask.length <= 0) {
            return list;
        }
        for (int i : fixWeekTask) {
            ignore.add(i);
            taskIds.add(i);
        }
        int[] randomWeekTask = lvConfig.getRandom_week_task();
        int randomWeekTaskNum = lvConfig.getRandom_week_tasknum();
        Random r = new Random();
        if (randomWeekTask.length > 0 && randomWeekTaskNum > 0) {
            if (randomWeekTaskNum > randomWeekTask.length) {
                randomWeekTaskNum = randomWeekTask.length;
            }
            int maxCount = 999;
            while (randomWeekTaskNum > 0) {
                if (maxCount <= 0) {
                    break;
                }
                int luck = randomWeekTask[r.nextInt(randomWeekTask.length)];
                if (ignore.contains(luck)) {
                    maxCount--;
                    continue;
                }
                ignore.add(luck);
                taskIds.add(luck);
                randomWeekTaskNum--;
                maxCount--;
            }
        }
        list.addAll(taskIds);
        return list;
    }

    public int findPlayerMaxSceneId(String playerIdx) {
        return getPlayerDBInfo(playerIdx, CrossArenaDBKey.LT_MAXSCENEID);
    }

    public int findPlayerNowInSceneId(String playerIdx) {
        return getPlayerDBInfo(playerIdx, CrossArenaDBKey.LT_SCENEID);
    }

    public boolean checkOpenDay(String playerId, int day) {
        playercrossarenaEntity entity = getPlayerEntity(playerId);

        long firstJoinTime = entity.getDataMsg().getFirstJoinTime();

        long now = GlobalTick.getInstance().getCurrentTime();

        return now >= TimeUtil.getNextDaysStamp(firstJoinTime, day);
    }

    public void send10ActivityTime(String playerIdx) {
        CrossArena.SC_Claim10WinActivityTime.Builder msg = CrossArena.SC_Claim10WinActivityTime.newBuilder();
        ActivityData activityData = TimeRuleManager.getInstance().getActivityData(2);
        if (activityData == null) {
            return;
        }
        if (isOpenDub) {
            msg.setTenCloseTime(activityData.getCloseTime());
        } else {
            msg.setTenOpenTime(activityData.getNextOpenTime());
        }
        GlobalData.getInstance().sendMsg(playerIdx,SC_Claim10WinActivityTime_VALUE,msg);

    }

    public int getRemainScoreItemCount(String playerIdx) {
        int lv = findPlayerGradeLv(playerIdx);
        CrossArenaLvCfgObject config = CrossArenaLvCfg.getByLv(lv);
        if (config == null) {
            return 0;
        }
        int scoreLimit = config.getScore_limit();
        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBag == null) {
            return 0;
        }
        int count = itemBag.getWeeklyLimitItemGainCount(CrossArenaScoreItemId);
        return Math.max(0,scoreLimit - count);
    }

    public int findWeeklyWinRate(String playerIdx) {
        int winNum = findPlayerDbsDataByKey(playerIdx, LT_BATTLEWinNUM_WEEK);
        int total = findPlayerDbsDataByKey(playerIdx, LT_BATTLENUM_WEEK);
        if (total <= 0) {
            return 0;
        }
        return Math.min(GameConst.commonMagnification,winNum * GameConst.commonMagnification / total);
    }

    public boolean isPlayerInLt(String playerIdx) {
        return playerPosMap.containsKey(playerIdx);
    }

    /**
     * @param playerIdx 玩家登录，检测是否有闯关事件
     * @param isResume
     */
    public void onPlayerLogIn(String playerIdx, boolean isResume) {
        playerUPLv(playerIdx);
        // 玩家登录得时候，发现还在擂台玩法，说明是短期掉线，处理业务逻辑
        removeDelayClose(playerIdx);
        if (playerPosMap.containsKey(playerIdx) && !isResume) {
            closeTable(playerIdx);
        }
    }
    public void onPlayerLogout(String playerIdx) {
        delayCloseTable(playerIdx);
    }


    private void removeDelayClose(String playerIdx) {
        delayClosePlayerTimeMap.remove(playerIdx);
    }

    private void delayCloseTable(String playerIdx) {
        delayClosePlayerTimeMap.put(playerIdx, CrossArenaManager.delayCloseTime + GlobalTick.getInstance().getCurrentTime());
    }
//jedis.hset(CrossArenaProtectCard, playerIdx, lastSerialWinNum + "");
    public RetCodeEnum useProtectCard(String playerIdx) {
        playercrossarenaEntity pe = getPlayerEntity(playerIdx);
        int todayUseWinProtect = pe.getDataMsg().getTodayUseWinProtect();
        int gradeLv = getPlayerDBInfo(pe, LT_GRADELV);
        CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(gradeLv);
        if (cfg.getSerialwinprotect() <= todayUseWinProtect) {
            return RetCodeEnum.RCE_CrossArena_ProtectCardTimeUseOut;
        }
        int curWinCot = getPlayerDBInfo(pe, LT_WINCOTCUR);
        int lastSerialWinNum = getPlayerDBInfo(playerIdx, LT_LastSerialWinNum);
        if (lastSerialWinNum <= 0 || curWinCot != 0) {
            return RetCodeEnum.RCE_CrossArena_ConditionNotMatch;
        }
        SyncExecuteFunction.executeConsumer(pe, e -> {
            e.getDataMsg().setTodayUseWinProtect(e.getDataMsg().getTodayUseWinProtect() + 1);
        });

        jedis.hset(CrossArenaProtectCard, playerIdx, lastSerialWinNum + "");
        return RetCodeEnum.RCE_Success;
    }

    public void settleDelTable(int tableId) {
        Set<String> playerIds = findTableChangeNeedPushPlayerIds(tableId);
        if (CollectionUtils.isEmpty(playerIds)) {
            return;
        }
        CrossArena.SC_CrossArenaLtDel.Builder msg = CrossArena.SC_CrossArenaLtDel.newBuilder().setTableId(tableId);
        for (String playerIdx : playerIds) {
            if (!playerPosMap.containsKey(playerIdx)) {
                continue;
            }
            GlobalData.getInstance().sendMsg(playerIdx, SC_CrossArenaLtDel_VALUE, msg);
        }
    }

    public void sendCrossArenaTablePage(String playerIdx, int page) {
        CrossArenaTablesPage tablePage = findTablePage(playerIdx, page, screenPageSize);
        CrossArena.SC_CrossArenaTablePage.Builder msg = CrossArena.SC_CrossArenaTablePage.newBuilder();
        if (RetCodeEnum.RCE_Success == tablePage.getCodeEnum()) {
            msg.addAllTables(tablePage.getTables());
            msg.setTotalPage(tablePage.getTotalPage());
            addScreenPagePlayerMap(playerIdx, page);
        }
        msg.setRet(GameUtil.buildRetCode(tablePage.getCodeEnum()));
        GlobalData.getInstance().sendMsg(playerIdx, SC_CrossArenaTablePage_VALUE, msg);
    }

    private CrossArenaTablesPage findTablePage(String playerIdx, int page, int size) {
        CrossArenaTablesPage result = new CrossArenaTablesPage();
        int nowInSceneId = findPlayerNowInSceneId(playerIdx);
        int pageStart = cretaeTableIdId(nowInSceneId, page * size + 1);
        int pageEnd = cretaeTableIdId(nowInSceneId, (page + 1) * size);
        int totalPage = claPage(nowInSceneId, size);

        List<CrossArena.CrossArenaOneInfo> tables = new ArrayList<>();
        if (page >= totalPage) {
            result.setCodeEnum(RetCodeEnum.RCE_CrossArena_NoMorePageData);
            return result;
        }
        for (int i = pageStart; i <= pageEnd; i++) {
            CrossArena.CrossArenaOneInfo tableOneInfo = findTableOneInfo(playerIdx, i);
            if (tableOneInfo != null) {
                tables.add(tableOneInfo);
            }
        }
        result.setTables(tables);
        result.setTotalPage(totalPage);
        result.setCodeEnum(RetCodeEnum.RCE_Success);

        return result;
    }

    private int claPage(int nowInSceneId, int size) {
        int maxTable = findMaxTableId(nowInSceneId);
        if (size <= 0) {
            return 0;
        }
        int temp = maxTable % 1000;
        int val1 = temp / size;
        int val2 = temp % size > 0 ? 1 : 0;
        return val1 + val2;
    }

    private int findMaxTableId(int nowInSceneId) {
        List<Integer> tableByScene= findAllTableByScene(nowInSceneId);
        return tableByScene.stream().max(Integer::compareTo).orElse(0);
    }

    private static final int queuePanelSize = 10;

    private static final int screenPageSize = 3;

    /**
     * 正在查看队列面板的玩家
     * <场景id,<面板页数,<玩家id集合>>>
     */
    private static final Map<Integer,Map<Integer,Set<String>>> queuePanelPlayerMap = new HashMap<>();

    private static final Map<Integer,Map<Integer,Set<String>>> screenPagePlayers = new HashMap<>();

    public void sendCrossArenaQueuePanel(String playerIdx, int page) {
        CrossArenaTablesPage tablePage = findTablePage(playerIdx, page, queuePanelSize);
        CrossArena.SC_CrossArenaQueuePanel.Builder msg = CrossArena.SC_CrossArenaQueuePanel.newBuilder();
        msg.addAllTables(tablePage.getTables());
        msg.setTotalPage(tablePage.getTotalPage());
        msg.setRet(GameUtil.buildRetCode(tablePage.getCodeEnum()));
        GlobalData.getInstance().sendMsg(playerIdx,SC_CrossArenaQueuePanel_VALUE,msg);
        if (RetCodeEnum.RCE_Success == tablePage.getCodeEnum()) {
            addQueuePanelPlayerMap(playerIdx, page);
        }
    }

    private void addQueuePanelPlayerMap(String playerIdx, int page) {
        removeQueuePanelPlayerMap(playerIdx);
        int sceneId = findPlayerNowInSceneId(playerIdx);
        Map<Integer, Set<String>> panelPlayers = queuePanelPlayerMap.get(sceneId);
        if (panelPlayers==null){
            return;
        }
        Set<String> players = panelPlayers.computeIfAbsent(page, a -> Collections.synchronizedSet(new HashSet<>()));
        players.add(playerIdx);
    }

    private void removeQueuePanelPlayerMap(String playerIdx) {
        int sceneId = findPlayerNowInSceneId(playerIdx);
        Map<Integer, Set<String>> panelPlayers = queuePanelPlayerMap.get(sceneId);
        if (panelPlayers == null) {
            return;
        }
        for (Set<String> players : panelPlayers.values()) {
            players.remove(playerIdx);
        }
    }

    private void addScreenPagePlayerMap(String playerIdx, int page) {
        removeScreenPagePlayerMap(playerIdx);
        int sceneId = findPlayerNowInSceneId(playerIdx);
        Map<Integer, Set<String>> panelPlayers = screenPagePlayers.get(sceneId);
        if (panelPlayers == null) {
            return;
        }
        Set<String> players = panelPlayers.computeIfAbsent(page, a -> Collections.synchronizedSet(new HashSet<>()));
        players.add(playerIdx);
    }

    private void removeScreenPagePlayerMap(String playerIdx) {
        int sceneId = findPlayerNowInSceneId(playerIdx);
        Map<Integer, Set<String>> panelPlayers = screenPagePlayers.get(sceneId);
        if (panelPlayers == null) {
            return;
        }
        for (Set<String> players : panelPlayers.values()) {
            players.remove(playerIdx);
        }
    }

    public void playerLeaveQueuePanel(String playerIdx) {
        removeQueuePanelPlayerMap(playerIdx);
        CrossArena.SC_PlayerLeaveQueuePanel.Builder msg = CrossArena.SC_PlayerLeaveQueuePanel.newBuilder();
        msg.setRet(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerIdx, SC_PlayerLeaveQueuePanel_VALUE, msg);
    }

    private Set<String> findTableChangeNeedPushPlayerIds(int tableId) {
        Set<String> players1 = findNeedPushPlayersByQueuePanel(tableId);
        Set<String> players2 = findNeedPushPlayersByScreenPanel(tableId);
        if (CollectionUtils.isEmpty(players1) && CollectionUtils.isEmpty(players2)) {
            return Collections.emptySet();
        }
        if (CollectionUtils.isEmpty(players1)) {
            return players2;
        }
        if (CollectionUtils.isEmpty(players2)) {
            return players1;
        }
        HashSet<String> result = new HashSet<>(players1);
        result.addAll(players2);
        return result;
    }

    private Set<String> findNeedPushPlayersByQueuePanel(int tableId) {
        int sceneId = getSceneIdByTableId(tableId);
        Map<Integer, Set<String>> panelPlayers = queuePanelPlayerMap.get(sceneId);
        if (MapUtils.isEmpty(panelPlayers)) {
            return Collections.emptySet();
        }
        int tableNum = getTableNumByTableId(tableId);
        int page = (tableNum - 1) / queuePanelSize;
        return panelPlayers.getOrDefault(page, Collections.emptySet());
    }

    private Set<String> findNeedPushPlayersByScreenPanel(int tableId) {
        int sceneId = getSceneIdByTableId(tableId);
        Map<Integer, Set<String>> panelPlayers = screenPagePlayers.get(sceneId);
        if (MapUtils.isEmpty(panelPlayers)) {
            return Collections.emptySet();
        }
        int tableNum = getTableNumByTableId(tableId);
        int page = (tableNum - 1) / screenPageSize;
        return panelPlayers.getOrDefault(page, Collections.emptySet());
    }

    private void saveLtDataToCache(RedisCrossArenaTableDB.Builder newDb) {
        int tableId = newDb.getLeitaiId();
        String key = createRedisKeyLT(tableId);
        jedis.set(key.getBytes(), newDb.build().toByteArray());
        putTableNextCanTickTime(tableId, newDb.getStateEndTime());
        putTableNowState(tableId, newDb.getState());
    }

    private void putTableNowState(int leitaiId, int state) {
        jedis.hset(CrossArenaTableState, leitaiId + "", state + "");
    }
}
