package model.matcharena;

import cfg.Head;
import cfg.MatchArenaDanConfig;
import cfg.MatchArenaLT;
import cfg.MatchArenaLTObject;
import cfg.MatchArenaLTRobot;
import cfg.MatchArenaLTRobotObject;
import cfg.PetBaseProperties;
import common.GameConst;
import common.GlobalData;
import common.JedisUtil;
import static common.JedisUtil.jedis;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.Tickable;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import model.battle.BattleManager;
import model.battle.BattleUtil;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.team.dbCache.teamCache;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Battle;
import protocol.Common;
import protocol.MatchArena;
import protocol.MatchArena.SC_MatchArenaLTPanel;
import protocol.MatchArena.SC_MatchArenaLTStageInfo;
import protocol.MatchArenaDB;
import protocol.MatchArenaDB.RedisMatchArenaLTOneInfo;
import protocol.MessageId;
import protocol.PetMessage;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RetCodeId;
import protocol.ServerTransfer;
import protocol.ServerTransfer.GS_BS_MatchArenaLTOpen;
import protocol.TargetSystem;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.ObjUtil;
import util.RandomUtil;
import util.timerule.TimeRules;

/**
 * 竞技场擂台赛
 */
public class MatchArenaLTManager implements Tickable {

    private static MatchArenaLTManager instance;

    public static MatchArenaLTManager getInstance() {
        if (instance == null) {
            synchronized (MatchArenaLTManager.class) {
                if (instance == null) {
                    instance = new MatchArenaLTManager();
                }
            }
        }
        return instance;
    }

    private MatchArenaLTManager() {
    }

    /**
     * 活动是否开启
     */
    private boolean isOpen = false;
    private boolean isGM = false;

    private TimeRules timeRules = null;

    // 缓存打开页面的玩家
    private Map<String, Long> openPanelPlayer = new ConcurrentHashMap<String, Long>();

    public boolean init() {
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            return false;
        }
        try {
            timeRules = new TimeRules(cfgData.getOpentime());
        } catch (Exception e) {
            LogUtil.error("擂台赛活动数据异常，区间时间配置异常。活动ID=" + cfgData.getOpentime(), e);
        }
        if (null == timeRules) {
            isOpen = true;
        }
        if (!isBetweenOpenTime()) {
            // 开服检查如果时关闭状态，检查数据
            closeLT();
        }
        return true;
    }

    @Override
    public void onTick() {
        if (null == timeRules) {
            return;
        }
        if (isNeedOpen()) {
            openLT("", false);
        }
        if (isNeedClose()) {
            closeLT();
        }
    }

    /**
     * 是否需要开启
     *
     * @return
     */
    private boolean isNeedOpen() {
        if (isOpen) {// 已经开启了
            return false;
        }
        return isBetweenOpenTime();
    }

    /**
     * 是否该关闭了
     *
     * @return
     */
    private boolean isNeedClose() {
        if (!isOpen) {// 还没有开启就不需要关闭
            return false;
        }
        return !isBetweenOpenTime();
    }

    /**
     * 该活动是否是开启时间
     *
     * @return
     */
    public boolean isBetweenOpenTime() {
        long time = System.currentTimeMillis();
        return timeRules.isRuleTime(System.currentTimeMillis());
    }

    public void openPanelPlayer(String playerIdx, int oper) {
        if (oper == 1) {
            openPanelPlayer.put(playerIdx, System.currentTimeMillis());
        } else {
            openPanelPlayer.remove(playerIdx);
        }
    }

    public void closeLT() {
        if (isGM) {
            return;
        }
        isOpen = false;
        String openTime = jedis.get(GameConst.RedisKey.MatchArenaLTTime);
        if (!StringHelper.isNull(openTime)) {
            if (Long.valueOf(openTime) <= 0) {
                return;
            }
        }
        // 数据加锁失败
        if (!JedisUtil.lockRedisKey(GameConst.RedisKey.MatchArenaLTUpdateLock,5000l)) {
            return;
        }
        // 设置活动时间
        long startTime = System.currentTimeMillis();
        jedis.set(GameConst.RedisKey.MatchArenaLTTime, String.valueOf(0));
        jedis.del(GameConst.RedisKey.MatchArenaLTSyncData); // 清楚数据
        jedis.del(GameConst.RedisKey.MatchArenaLTSyncDataId);
        // 数据解锁
        JedisUtil.unlockRedisKey(GameConst.RedisKey.MatchArenaLTUpdateLock);
    }

    /**
     * 开启擂台赛(获取最优的战场服作为主服)
     */
    public void openLT(String tempidx, boolean isGM) {
        this.isGM = isGM;
        // 获取redis数据判断活动是否开启
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            LogUtil.error("擂台赛数据初始化时，没有获取到擂台配置数据");
            return;
        }
        isOpen = true;
        String openTime = jedis.get(GameConst.RedisKey.MatchArenaLTTime);
        if (!isGM && !StringHelper.isNull(openTime)) {
            // 判断存储数据是否是同一条数据
            if (Long.valueOf(openTime) > 0) {
                // 数据是同一天则表示活动已经开启
                return;
            }
        }
        // 数据加锁失败
        if (!JedisUtil.lockRedisKey(GameConst.RedisKey.MatchArenaLTUpdateLock,5000l)) {
            return;
        }
        // 设置活动时间
        long startTime = System.currentTimeMillis();
        jedis.set(GameConst.RedisKey.MatchArenaLTTime, String.valueOf(startTime));
        jedis.del(GameConst.RedisKey.MatchArenaLTSyncData);
        jedis.del(GameConst.RedisKey.MatchArenaLTSyncDataId);
        // 根据配置数据初始化所有的擂台数据
        long currTime = System.currentTimeMillis();
        for (int[] ent : cfgData.getStageltnum()) {
            if (ent.length != 2) {
                continue;
            }
            for (int i=1; i<= ent[1]; i++) {
                int leitaiId = cretaeLeitaiId(ent[0], i);

                // 初始化擂台数据
                RedisMatchArenaLTOneInfo.Builder leitaimsg = RedisMatchArenaLTOneInfo.newBuilder();
                leitaimsg.setLeitaiId(leitaiId);
                leitaimsg.setState(MatchArena.MatchArenaLTState.WAIT_VALUE);
                leitaimsg.setDefTime(currTime);
                leitaimsg.setDefWinNum(0);
                leitaimsg.setBattleId(0);
                // 初始化生成机器人守擂 leitaimsg.setDefPlayer();
                MatchArenaDB.RedisMatchArenaLTPlayer fightinfo = createInitDefInfo(tempidx, ent[0]);
                leitaimsg.setDefPlayer(fightinfo);
                // 存储单个擂台数据至redis
                jedis.set(createRedisKeyLT(leitaiId).getBytes(), leitaimsg.build().toByteArray());
                // 存储所有擂台
                // 获取一个最优战斗服务器作为该擂台的管理服务器
                BaseNettyClient coreserver = BattleServerManager.getInstance().getAvailableBattleServer();
                int ltSvrIndex = 0;
                String ltKey = ""+leitaiId;
                if (null != coreserver) {
                    ltSvrIndex = coreserver.getServerIndex();
                    // 发送消息到跨服开启活动
                    GS_BS_MatchArenaLTOpen.Builder msg1 = GS_BS_MatchArenaLTOpen.newBuilder();
                    msg1.setOper(1);
                    msg1.setOperTime(startTime);
                    coreserver.send(MessageId.MsgIdEnum.GS_BS_LoginBattleServer_VALUE, msg1);
                }
                jedis.hset(GameConst.RedisKey.MatchArenaLTSyncData, ltKey, StringHelper.IntTostring(ltSvrIndex, "0"));
            }
        }
        // 数据解锁
        JedisUtil.unlockRedisKey(GameConst.RedisKey.MatchArenaLTUpdateLock);
    }

    /**
     * @param playerIdx
     * @param stageId
     * @return
     * 初始化擂台守擂信息
     */
    private MatchArenaDB.RedisMatchArenaLTPlayer createInitDefInfo(String playerIdx, int stageId) {
        MatchArenaDB.RedisMatchArenaLTPlayer.Builder fightinfo = MatchArenaDB.RedisMatchArenaLTPlayer.newBuilder();
        Battle.BattlePlayerInfo attinfo;
        if (stageId == 0) {
            attinfo = createAttData(playerIdx, TeamNumEnum.TNE_MatchArenaLeiTai_1, Battle.BattleSubTypeEnum.BSTE_MatchArenaLeitai);
            fightinfo.setPlayerId(playerIdx);
            fightinfo.setSvrIndex(ServerConfig.getInstance().getServer());
        } else {
            attinfo = createAttDataRobot(stageId, 0);
            fightinfo.setPlayerId(attinfo.getPlayerInfo().getPlayerId());
            fightinfo.setShowPetId(getRobotShowPetId(attinfo));
            fightinfo.setSvrIndex(ServerConfig.getInstance().getServer());
        }
        if (null == attinfo) {
            return null;
        }
        fightinfo.setTeamInfo(attinfo);
        return fightinfo.build();
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
     * 获取主面板信息
     */
    public void getMainPanelInfo(String playerIdx) {
        SC_MatchArenaLTPanel.Builder msg = SC_MatchArenaLTPanel.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTPanel_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        //TODO 组装排位赛数据返回
        msg.addAllStageData(createStageInfo(playerIdx, 0));
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTPanel_VALUE, msg);
    }

    /**
     * @param playerIdx
     * 刷新玩家显示的擂台数据
     */
    public void refStageInfo(String playerIdx) {
        // 刷新一次擂台数据
        getStageInfo(playerIdx, 0);
    }

    public void refStageInfo(String playerIdx, int leitaiId) {
        // 刷新一次擂台数据
        getStageInfo(playerIdx, getStageIdByLTId(leitaiId));
    }

    /**
     * 擂台数据变动
     */
    public void refStageInfoAllOnline(int leitaiId) {
        int stageId = getStageIdByLTId(leitaiId);
        // 刷新一次擂台数据
        List<RedisMatchArenaLTOneInfo> dbData = getLeitaiDBInfo(stageId);
        if (dbData.isEmpty()) {
            return;
        }
        for (String playerIdx : openPanelPlayer.keySet()) {
            SC_MatchArenaLTStageInfo.Builder msg = SC_MatchArenaLTStageInfo.newBuilder();
            RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
            msg.setRetCode(retCode);
            msg.setStageId(stageId);
            msg.addAllStageData(createStageInfo(dbData, playerIdx, stageId));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTStageInfo_VALUE, msg);
        }
    }

    /**
     * @param playerIdx
     * @param stageId
     * 获取阶段数据
     */
    public void getStageInfo(String playerIdx, int stageId) {
        SC_MatchArenaLTStageInfo.Builder msg = SC_MatchArenaLTStageInfo.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTStageInfo_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        msg.setStageId(stageId);
        msg.addAllStageData(createStageInfo(playerIdx, stageId));
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTStageInfo_VALUE, msg);
    }

    public List<RedisMatchArenaLTOneInfo> getLeitaiDBInfo(int stageId) {
        List<RedisMatchArenaLTOneInfo> result = new ArrayList<RedisMatchArenaLTOneInfo>();
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            return result;
        }
        List<Integer> leitaiIds = getAllLeitaiIdByLimit(cfgData, stageId);
        if (leitaiIds.isEmpty()) {
            return result;
        }
        try {
            Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
            if (null == leitaiIp) {
                return result;
            }
            for (int ltId : leitaiIds) {
                byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(ltId).getBytes());
                if (null == oneLeiTaiDB) {
                    continue;
                }
                // 数据转换为可操作数据
                RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
                result.add(oneLeiTaiProtoDB.toBuilder().build());
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return result;
    }

    public List<MatchArena.MatchArenaLTOneInfo> createStageInfo(List<RedisMatchArenaLTOneInfo> dbData, String playerIdx, int stageId) {
        List<MatchArena.MatchArenaLTOneInfo> result = new ArrayList<MatchArena.MatchArenaLTOneInfo>();
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            return result;
        }
        for (RedisMatchArenaLTOneInfo oneLeiTaiProtoDB : dbData) {
            MatchArena.MatchArenaLTOneInfo.Builder msgOne = MatchArena.MatchArenaLTOneInfo.newBuilder();
            msgOne.setLeitaiId(oneLeiTaiProtoDB.getLeitaiId());
            msgOne.setStageId(getStageIdByLTId(oneLeiTaiProtoDB.getLeitaiId()));
            msgOne.setState(MatchArena.MatchArenaLTState.forNumber(oneLeiTaiProtoDB.getState()));
            if (null != oneLeiTaiProtoDB.getDefPlayer()) {
                msgOne.setPlayerinfo(oneLeiTaiProtoDB.getDefPlayer().getTeamInfo().getPlayerInfo());
            }
            msgOne.setDefTime(oneLeiTaiProtoDB.getDefTime());
            msgOne.setDefWinNum(oneLeiTaiProtoDB.getDefWinNum());
            int idx = oneLeiTaiProtoDB.getDefWinNum()-1;
            if (cfgData.getWinbuff().length <= idx) {
                idx = cfgData.getWinbuff().length - 1;
            }
            if (idx >= 0) {
                msgOne.setAttBuff(cfgData.getWinbuff()[idx]);
            }
            int isGuess = 0;
            if (oneLeiTaiProtoDB.containsGuessDefSvrData(playerIdx)) {
                isGuess = 1;
            } else if (oneLeiTaiProtoDB.containsGuessAttSvrData(playerIdx)) {
                isGuess = 2;
            }
            msgOne.setShowPetId(oneLeiTaiProtoDB.getDefPlayer().getShowPetId());
            msgOne.setIsGuess(isGuess);
            result.add(msgOne.build());
        }
        return result;
    }

    public List<MatchArena.MatchArenaLTOneInfo> createStageInfo(String playerIdx, int stageId) {
        List<MatchArena.MatchArenaLTOneInfo> result = new ArrayList<MatchArena.MatchArenaLTOneInfo>();
        List<RedisMatchArenaLTOneInfo> dbData = getLeitaiDBInfo(stageId);
        if (dbData.isEmpty()) {
            return result;
        }
        return createStageInfo(dbData, playerIdx, stageId);
    }

    /**
     * @param playerIdx
     * @param leitaiId
     * 请求观战
     */
    public void reqViewFight(String playerIdx, int leitaiId) {
        MatchArena.SC_MatchArenaLTViewFight.Builder msg = MatchArena.SC_MatchArenaLTViewFight.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTViewFight_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
        if (null == leitaiIp) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTViewFight_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        // 判断该擂台是否工作中
        String ltaSvrIndexStr = leitaiIp.getOrDefault(""+leitaiId, "0");
        int ltSvrIndex = StringHelper.stringToInt(ltaSvrIndexStr,0);
        BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(ltSvrIndex);
        if (null == bnc) {
            LogUtil.error("该服务器没有查询到战场服地址=" + ltaSvrIndexStr);
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTViewFight_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }

        byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
        if (null == oneLeiTaiDB) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTViewFight_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        try {
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != MatchArena.MatchArenaLTState.FIGHT_VALUE) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_BattleEnd);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTViewFight_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            // 执行观战逻辑，根据战斗ID处理
            BattleManager.getInstance().sendBattleServerBattleWatch(bnc, String.valueOf(oneLeiTaiProtoDB.getBattleId()), playerIdx);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTViewFight_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx
     * @param leitaiId
     * 挑战擂台(逻辑服收到客户端消息处理逻辑，上行至中心服，等待中心服返回结果)
     */
    public void attLeiTai(String playerIdx, int leitaiId) {
        MatchArena.SC_MatchArenaLTAtt.Builder msg = MatchArena.SC_MatchArenaLTAtt.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
            return;
        }
        if (MatchArenaManager.getInstance().isMatching(playerIdx)){
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatchArena_PlayerIsMatching));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        int myStageId = getStageIdByPlayer(playerIdx);
        myStageId = myStageId == 0 ? 1 : myStageId;
        int attLTID = getStageIdByLTId(leitaiId);
        if (myStageId != attLTID) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_STAGE_NOTEQ);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
        if (null == leitaiIp) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
        if (null == oneLeiTaiDB) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        try {
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != MatchArena.MatchArenaLTState.WAIT_VALUE) {
                refStageInfo(playerIdx);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOATTMY);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer().getPlayerId() || oneLeiTaiProtoDB.getDefPlayer().getPlayerId().equals(playerIdx)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOATTMY);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            // 判断该擂台是否工作中
            // 获取连接
            String ltaSvrIndexStr = leitaiIp.getOrDefault(""+leitaiId, "0");
            int ltSvrIndex = StringHelper.stringToInt(ltaSvrIndexStr,0);
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(ltSvrIndex);
            if (null == bnc) {
                LogUtil.error("该服务器没有查询到战场服地址=" + ltaSvrIndexStr);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.GS_BS_MatchArenaLTAtt_VALUE, msg);
                return;
            }
            ServerTransfer.GS_BS_MatchArenaLTAtt.Builder msgatt = ServerTransfer.GS_BS_MatchArenaLTAtt.newBuilder();
            msgatt.setLeitaiId(leitaiId);
            msgatt.setPlayerId(playerIdx);
            int serverIdx = ServerConfig.getInstance().getServer();
            msgatt.setSvrIndex(serverIdx);
            Battle.BattlePlayerInfo attinfo = createAttData(playerIdx, TeamNumEnum.TNE_MatchArenaLeiTai_1, Battle.BattleSubTypeEnum.BSTE_MatchArenaLeitai);
            if (null == attinfo) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_TheWar_EmptyPetTeam);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            Map<String, String> leitaiID = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncDataId);
            if (null != leitaiID && leitaiID.containsValue(playerIdx)) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_RPT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            msgatt.setTeamInfo(attinfo);
            msgatt.setShowPetId(getDisPetId(playerIdx));
            bnc.send(MessageId.MsgIdEnum.GS_BS_MatchArenaLTAtt_VALUE, msgatt);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

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

    /**
     * @param oper
     * @param leitaiId
     * @param defPlayerIdx
     * @param defWinNum
     * 挑战擂台(逻辑服收到客户端消息处理逻辑，上行至中心服，等待中心服返回结果)
     */
    public void attLeiTaiAI(int oper, int leitaiId, String defPlayerIdx, int defWinNum) {
        if (!isOpen) {
            return;
        }
        int attLTID = getStageIdByLTId(leitaiId);
        Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
        if (null == leitaiIp) {
            return;
        }
        byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
        if (null == oneLeiTaiDB) {
            return;
        }
        try {
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != MatchArena.MatchArenaLTState.WAIT_VALUE) {
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer()) {
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer().getPlayerId() || !oneLeiTaiProtoDB.getDefPlayer().getPlayerId().equals(defPlayerIdx)) {
                return;
            }
            // 判断该擂台是否工作中
            // 获取连接
            String ltaSvrIndexStr = leitaiIp.getOrDefault(""+leitaiId, "0");
            int ltSvrIndex = StringHelper.stringToInt(ltaSvrIndexStr,0);
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(ltSvrIndex);
            if (null == bnc) {
                LogUtil.error("该服务器没有查询到战场服地址=" + ltSvrIndex);
                return;
            }
            ServerTransfer.GS_BS_MatchArenaLTAtt.Builder msgatt = ServerTransfer.GS_BS_MatchArenaLTAtt.newBuilder();
            if (oper == 2) {
                defWinNum = 0;
            }
            Battle.BattlePlayerInfo attinfo = createAttDataRobot(getStageIdByLTId(leitaiId), defWinNum);
            if (null == attinfo) {
                return;
            }
            msgatt.setLeitaiId(leitaiId);
            msgatt.setPlayerId(defPlayerIdx);
            int serverIdx = ServerConfig.getInstance().getServer();
            msgatt.setSvrIndex(serverIdx);
            msgatt.setTeamInfo(attinfo);
            msgatt.setOper(oper);
            msgatt.setShowPetId(getRobotShowPetId(attinfo));
            bnc.send(MessageId.MsgIdEnum.GS_BS_MatchArenaLTAtt_VALUE, msgatt);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx
     * @param code
     * 攻打擂台中心服返回数据
     */
    public void attLeiTaiBSBack(String playerIdx, RetCodeId.RetCodeEnum code) {
        MatchArena.SC_MatchArenaLTAtt.Builder msg = MatchArena.SC_MatchArenaLTAtt.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        if (code == RetCodeId.RetCodeEnum.RCE_Success) {
            msg.setRetCode(retCode);
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_MatchArenaLT_Jion, 1, 0);
        } else {
            retCode.setRetCode(code);
            msg.setRetCode(retCode);
            // 刷新一次擂台数据
            refStageInfo(playerIdx);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTAtt_VALUE, msg);
    }

    /**
     * @param leitaiId
     * @param longtime
     */
    public void checkAIAtt(int leitaiId, long longtime, int defWinNum, String defPid) {
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            return;
        }
        if (defWinNum <= cfgData.getAilimit()) {
            if (longtime > cfgData.getAimintime() * 1000L) {
                // 请求执行替换操作
                attLeiTaiAI(1, leitaiId, defPid, defWinNum);
            }
        } else {
            if (longtime > cfgData.getAimaxtime() * 1000L) {
                // 请求执行AI战斗操作
                attLeiTaiAI(2, leitaiId, defPid, defWinNum);
            }
        }
    }

    public void battleWin(ServerTransfer.BS_GS_MatchArenaLTWinResult msg) {
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.CONFIG_ID);
        if (null == cfgData || !isOpen) {
            return;
        }
        if (msg.getIsWin() == 2) {
            // 防守胜利
            String win = msg.getPlayerIdDef();
            playerEntity pe = playerCache.getByIdx(win);
            if (null != pe) {
                EventUtil.triggerUpdateTargetProgress(win, TargetSystem.TargetTypeEnum.TTE_MatchArenaLT_Win, 1, 0);
                int cnum = msg.getWinNumDef()+1;
                if (cnum == 3) {
                    EventUtil.triggerUpdateTargetProgress(win, TargetSystem.TargetTypeEnum.TTE_MatchArenaLT_3Win, 1, 0);
                }
                // 计算积分
                int tarScoreAtt = msg.getScoreAtt();
                if (Objects.equals(msg.getScoreAtt(), MatchArenaUtil.ROOBOTID)) {
                    tarScoreAtt = MatchArenaDanConfig.getRobotScore(getStageIdByPlayer(msg.getPlayerIdAtt()));
                }
                int playerScoreChange = MatchArenaUtil.calculateScore(msg.getScoreDef(), tarScoreAtt, 1);
                playerAddGrade(win, playerScoreChange);
            }
            playerEntity peFail = playerCache.getByIdx(msg.getPlayerIdAtt());
            if (null != peFail) {
                // 计算积分
                int tarScoreDef = msg.getScoreDef();
                if (Objects.equals(msg.getScoreDef(), MatchArenaUtil.ROOBOTID)) {
                    tarScoreDef = MatchArenaDanConfig.getRobotScore(getStageIdByPlayer(msg.getPlayerIdDef()));
                }
                int playerScoreChange = MatchArenaUtil.calculateScore(msg.getScoreAtt(), tarScoreDef, 2);
                playerAddGrade(msg.getPlayerIdAtt(), playerScoreChange);
            }
        } else {
            // 攻击胜利
            String win = msg.getPlayerIdAtt();
            playerEntity pe = playerCache.getByIdx(win);
            if (null != pe) {
                EventUtil.triggerUpdateTargetProgress(win, TargetSystem.TargetTypeEnum.TTE_MatchArenaLT_Win, 1, 0);
                // 计算积分
                int tarScoreDef = msg.getScoreDef();
                if (Objects.equals(msg.getScoreDef(), MatchArenaUtil.ROOBOTID)) {
                    tarScoreDef = MatchArenaDanConfig.getRobotScore(getStageIdByPlayer(msg.getPlayerIdDef()));
                }
                int playerScoreChange = MatchArenaUtil.calculateScore(msg.getScoreAtt(), tarScoreDef, 1);
                playerAddGrade(win, playerScoreChange);
            }
            playerEntity peFail = playerCache.getByIdx(msg.getPlayerIdDef());
            if (null != peFail) {
                // 计算积分
                int tarScoreAtt = msg.getScoreAtt();
                if (Objects.equals(msg.getScoreAtt(), MatchArenaUtil.ROOBOTID)) {
                    tarScoreAtt = MatchArenaDanConfig.getRobotScore(getStageIdByPlayer(msg.getPlayerIdAtt()));
                }
                int playerScoreChange = MatchArenaUtil.calculateScore(msg.getScoreDef(), tarScoreAtt, 2);
                playerAddGrade(msg.getPlayerIdDef(), playerScoreChange);
            }
        }
    }

    private TreeMap<Integer, Integer> createTreeMap(int[][] src) {
        TreeMap<Integer, Integer> temp = new TreeMap<>();
        for (int[] ent : src) {
            if (ent.length != 2) {
                continue;
            }
            temp.put(ent[0], ent[1]);
        }
        return temp;
    }

    public void playerAddGrade(String playerIdx, int grade) {
        if (grade == 0 || !isOpen) {
            return;
        }
        // 玩家增加排位积分，对接竞技场排位系统
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(playerIdx);
        if (entity == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> entity.incrRankScoreByAdd(grade));
        entity.sendInfo();
    }

    /**
     * @param playerIdx
     * @param leitaiId
     * 主动退出XX擂台
     */
    public void quitLeiTai(String playerIdx, int leitaiId) {
        MatchArena.SC_MatchArenaLTQuit.Builder msg = MatchArena.SC_MatchArenaLTQuit.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTQuit_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        // 原服先判断下逻辑
        Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
        if (null == leitaiIp) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTQuit_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }

        // 获取擂台数据
        byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
        if (null == oneLeiTaiDB) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTQuit_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        try {
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != MatchArena.MatchArenaLTState.WAIT_VALUE) {
                // 刷新一次擂台数据
                refStageInfo(playerIdx);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTQuit_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer() || null == oneLeiTaiProtoDB.getDefPlayer().getPlayerId() || !oneLeiTaiProtoDB.getDefPlayer().getPlayerId().equals(playerIdx)) {
                // 刷新一次擂台数据
                refStageInfo(playerIdx);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_MYLT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTQuit_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            // 判断该擂台是否工作中
            // 获取连接
            String ltaSvrIndexStr = leitaiIp.getOrDefault(""+leitaiId, "0");
            int ltSvrIndex = StringHelper.stringToInt(ltaSvrIndexStr,0);
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(ltSvrIndex);
            if (null == bnc) {
                LogUtil.error("该服务器没有查询到战场服地址=" + ltSvrIndex);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTStageInfo_VALUE, msg);
                return;
            }
            Battle.BattlePlayerInfo attinfo = createAttDataRobot(getStageIdByLTId(leitaiId), 0);
            if (null == attinfo) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTStageInfo_VALUE, msg);
                return;
            }
            // 发送消息至擂台逻辑管理服务器
            ServerTransfer.GS_BS_MatchArenaLTQuit.Builder msgquit = ServerTransfer.GS_BS_MatchArenaLTQuit.newBuilder();
            msgquit.setLeitaiId(leitaiId);
            msgquit.setPlayerId(playerIdx);
            int serverIdx = ServerConfig.getInstance().getServer();
            msgquit.setSvrIndex(serverIdx);
            msgquit.setTeamInfoRobot(attinfo);
            msgquit.setShowPetId(getRobotShowPetId(attinfo));
            bnc.send(MessageId.MsgIdEnum.GS_BS_MatchArenaLTQuit_VALUE, msgquit);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx
     * @param code
     * 攻打擂台中心服返回数据
     */
    public void quitLeiTaiBSBack(String playerIdx, int cnum, RetCodeId.RetCodeEnum code) {
        MatchArena.SC_MatchArenaLTQuit.Builder msg = MatchArena.SC_MatchArenaLTQuit.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        if (code == RetCodeId.RetCodeEnum.RCE_Success) {
            msg.setRetCode(retCode);
            // 根据占领时长计算排位积分
            MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.CONFIG_ID);
            if (null != cfgData) {
                int addGrade = 0;
                TreeMap<Integer, Integer> srcData = createTreeMap(cfgData.getTimegrade());
                for (Map.Entry<Integer, Integer> ent : srcData.entrySet()) {
                    if (cnum < ent.getKey()) {
                        break;
                    }
                    addGrade = ent.getValue();
                }
                playerAddGrade(playerIdx, addGrade);
            }
        } else {
            retCode.setRetCode(code);
            msg.setRetCode(retCode);
        }
        // 刷新一次擂台数据
        refStageInfo(playerIdx);
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTQuit_VALUE, msg);
    }

    /**
     * @param playerIdx
     * @param leitaiId
     * 竞猜信息查看
     */
    public void guessInfoView(String playerIdx, int leitaiId) {
        MatchArena.SC_MatchArenaLTGuessInfo.Builder msg = MatchArena.SC_MatchArenaLTGuessInfo.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuessInfo_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        MatchArenaLTObject cfgData = MatchArenaLT.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuessInfo_VALUE, msg);
            return;
        }
        Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
        if (null == leitaiIp) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuessInfo_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        // 获取擂台数据
        byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
        if (null == oneLeiTaiDB) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTStageInfo_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        try {
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != MatchArena.MatchArenaLTState.FIGHT_VALUE) {
                // 刷新一次擂台数据
                refStageInfo(playerIdx);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuessInfo_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer() || null == oneLeiTaiProtoDB.getAttPlayer()) {
                // 刷新一次擂台数据
                refStageInfo(playerIdx);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuessInfo_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer().getPlayerId()) {
                // 刷新一次擂台数据
                refStageInfo(playerIdx);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_MYLT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTStageInfo_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            Battle.BattlePlayerInfo.Builder defInfo = oneLeiTaiProtoDB.getDefPlayer().getTeamInfo().toBuilder();
            defInfo.setCamp(2);
            msg.addPlayerInfo(defInfo);
            Battle.BattlePlayerInfo.Builder attInfo = oneLeiTaiProtoDB.getAttPlayer().getTeamInfo().toBuilder();
            attInfo.setCamp(1);
            msg.addPlayerInfo(attInfo);
            playerEntity pe = playerCache.getByIdx(playerIdx);
            if (null != pe) {
                int sy = cfgData.getWinguessnum() - pe.getDb_data().getMathArenaLeiTaiGuess();
                msg.setSynum(Math.max(sy, 0));
            }
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuessInfo_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx
     * @param leitaiId
     * 竞猜
     */
    public void guess(String playerIdx, int leitaiId, int isWin) {
        MatchArena.SC_MatchArenaLTGuess.Builder msg = MatchArena.SC_MatchArenaLTGuess.newBuilder();
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuess_VALUE, msg);
            return;
        }
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        msg.setRetCode(retCode);
        // 原服先判断下逻辑
        Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncData);
        if (null == leitaiIp) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuess_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        // 获取擂台数据
        byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(leitaiId).getBytes());
        if (null == oneLeiTaiDB) {
            retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
            msg.setRetCode(retCode);
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuess_VALUE, msg);
            refStageInfo(playerIdx, leitaiId);
            return;
        }
        try {
            // 数据转换为可操作数据
            RedisMatchArenaLTOneInfo oneLeiTaiProtoDB = RedisMatchArenaLTOneInfo.parseFrom(oneLeiTaiDB);
            if (null == oneLeiTaiProtoDB || oneLeiTaiProtoDB.getState() != MatchArena.MatchArenaLTState.FIGHT_VALUE) {
                // 刷新一次擂台数据
                refStageInfo(playerIdx);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuess_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer() || null == oneLeiTaiProtoDB.getAttPlayer()) {
                // 刷新一次擂台数据
                refStageInfo(playerIdx);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_FIGHT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuess_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            if (null == oneLeiTaiProtoDB.getDefPlayer().getPlayerId()) {
                // 刷新一次擂台数据
                refStageInfo(playerIdx);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_MYLT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuess_VALUE, msg);
                refStageInfo(playerIdx, leitaiId);
                return;
            }
            // 判断该擂台是否工作中
            // 获取连接
            String ltaSvrIndexStr = leitaiIp.getOrDefault(""+leitaiId, "0");
            int ltSvrIndex = StringHelper.stringToInt(ltaSvrIndexStr,0);
            BaseNettyClient bnc = BattleServerManager.getInstance().getActiveNettyClient(ltSvrIndex);
            if (null == bnc) {
                LogUtil.error("该服务器没有查询到战场服地址=" + ltSvrIndex);
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_NOT_WAIT);
                msg.setRetCode(retCode);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTStageInfo_VALUE, msg);
                return;
            }
            ServerTransfer.GS_BS_MatchArenaLTGuess.Builder msgquit = ServerTransfer.GS_BS_MatchArenaLTGuess.newBuilder();
            msgquit.setLeitaiId(leitaiId);
            msgquit.setPlayerId(playerIdx);
            int serverIdx = ServerConfig.getInstance().getServer();
            msgquit.setSvrIndex(serverIdx);
            msgquit.setIsWin(isWin);
            bnc.send(MessageId.MsgIdEnum.GS_BS_MatchArenaLTGuess_VALUE, msgquit);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param playerIdx
     * @param code
     * 竞猜返回
     */
    public void guessBSBack(String playerIdx, RetCodeId.RetCodeEnum code) {
        MatchArena.SC_MatchArenaLTGuess.Builder msg = MatchArena.SC_MatchArenaLTGuess.newBuilder();
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success);
        if (code == RetCodeId.RetCodeEnum.RCE_Success) {
            msg.setRetCode(retCode);
            playerEntity player = playerCache.getByIdx(playerIdx);
            // 竞猜成功次数累加
            if (null != player) {
                SyncExecuteFunction.executeConsumer(player, p -> {
                    player.getDb_data().setMathArenaLeiTaiGuess(player.getDb_data().getMathArenaLeiTaiGuess()+1);
                });
            }
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_MatchArenaLT_Guess, 1, 0);
        } else {
            retCode.setRetCode(code);
            msg.setRetCode(retCode);
        }
        // 刷新一次擂台数据
        refStageInfo(playerIdx);
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_MatchArenaLTGuess_VALUE, msg);
    }

    /**
     * @param wins
     * 竞猜成功
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
     * @return
     * 创建参加擂台的玩家战斗数据
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
        //检查玩家信息是否正确
        Battle.PlayerBaseInfo.Builder playerInfo = BattleUtil.buildPlayerBattleBaseInfo(playerIdx);
        if (playerInfo == null) {
            return null;
        }
        Battle.BattlePlayerInfo.Builder battlePlayerInfo = Battle.BattlePlayerInfo.newBuilder();
        battlePlayerInfo.setCamp(0);
        battlePlayerInfo.addAllPetList(petDataList);
        battlePlayerInfo.setPlayerInfo(playerInfo);
        battlePlayerInfo.setIsAuto(false);
        teamCache.getInstance().getPlayerTeamSkillList(playerIdx, teamNum);
        List<Integer> skillList = teamCache.getInstance().getPlayerTeamSkillList(playerIdx, teamNum);
        if (!CollectionUtils.isEmpty(skillList)) {
            for (Integer skillId : skillList) {
                battlePlayerInfo.addPlayerSkillIdList(Battle.SkillBattleDict.newBuilder()
                        .setSkillId(skillId).setSkillLv(player.getSkillLv(skillId)).build());
            }
        }
        return battlePlayerInfo.build();
    }

    /**
     * @return
     * 创建参加擂台的玩家战斗数据
     */
    public Battle.BattlePlayerInfo createAttDataRobot(int stageId, int winNum) {
        // 构建玩家基础信息
        Battle.PlayerBaseInfo.Builder playerInfo = Battle.PlayerBaseInfo.newBuilder();
        playerInfo.setPlayerId(MatchArenaUtil.ROOBOTID);
        Common.LanguageEnum lag = Common.LanguageEnum.forNumber(ServerConfig.getInstance().getLanguage());
        if (null == lag) {
            lag = Common.LanguageEnum.LE_SimpleChinese;
        }
        playerInfo.setPlayerName(ObjUtil.createRandomName(lag));
        playerInfo.setLevel(50);
        playerInfo.setAvatar(Head.randomGetAvatar());
        playerInfo.setVipLevel(1);
        playerInfo.setAvatarBorder(0);
        playerInfo.setAvatarBorderRank(0);
        playerInfo.setTitleId(0);
        playerInfo.setNewTitleId(0);
        MatchArenaLTRobotObject robotCfg = getRobotInitData(stageId, winNum);
        List<PetMessage.Pet> petList = new ArrayList<>();
        for (int petBookId : robotCfg.getTeam()) {
            PetMessage.Pet.Builder petBuilder = petCache.getInstance().getPetBuilder(petBookId, 0);
            if (petBuilder == null) {
                continue;
            }
            int randomRarity = RandomUtil.getRandomValue(0, robotCfg.getRarity().length);
            petBuilder.setPetRarity(randomRarity);
            petBuilder.setPetLvl(RandomUtil.getRandomValue(robotCfg.getLevel()[0], robotCfg.getLevel()[1]));
            petCache.getInstance().refreshPetData(petBuilder, null);
            petList.add(petBuilder.build());
        }
        List<Battle.BattlePetData> petDataList = petCache.getInstance().buildPetBattleData(null, petList, Battle.BattleSubTypeEnum.BSTE_MatchArenaLeitai, true);
        if (GameUtil.collectionIsEmpty(petDataList)) {
            return null;
        }
        Battle.BattlePlayerInfo.Builder battlePlayerInfo = Battle.BattlePlayerInfo.newBuilder();
        battlePlayerInfo.setCamp(0);
        battlePlayerInfo.addAllPetList(petDataList);
        battlePlayerInfo.setPlayerInfo(playerInfo);
        battlePlayerInfo.setIsAuto(true);
        return battlePlayerInfo.build();
    }

    /**
     * @param playerIdx
     * @return
     * 检查玩家是否在擂台上
     */
    public boolean checkPlayerAtLeitai(String playerIdx) {
        Map<String, String> leitaiIp = jedis.hgetAll(GameConst.RedisKey.MatchArenaLTSyncDataId);
        if (null == leitaiIp) {
            return false;
        }
        return leitaiIp.containsValue(playerIdx);
    }

    /**
     * @param stageId
     * @param idx
     * @return
     * 生成擂台ID
     */
    public int cretaeLeitaiId(int stageId, int idx) {
        return stageId * 100 + idx;
    }

    /**
     * @param leitaiId
     * @return
     * 根据擂台ID获取段位ID
     */
    public int getStageIdByLTId(int leitaiId) {
        return leitaiId / 100;
    }

    /**
     * @param playerIdx
     * @return
     */
    public int getStageIdByPlayer(String playerIdx) {
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(playerIdx);
        if (entity == null) {
            return 1;
        }
        int stageId = entity.getDbBuilder().getRankMatchArenaBuilder().getDan();
        stageId = stageId <= 0 ? 1 : stageId;
        return stageId;
    }

    /**
     * @param leitaiId
     * @return
     * 创建单个擂台数据key
     */
    public String createRedisKeyLT(int leitaiId) {
        return GameConst.RedisKey.MatchArenaLTSyncData + leitaiId;
    }

    public List<Integer> getAllLeitaiIdByLimit(MatchArenaLTObject cfgData, int stageId) {
        List<Integer> temp = new ArrayList<>();
        for (int[] ent : cfgData.getStageltnum()) {
            if (ent.length != 2) {
                continue;
            }
            if (stageId == 0 || ent[0] == stageId) {
                for (int i = 1; i<=ent[1]; i++) {
                    int ltid = cretaeLeitaiId(ent[0], i);
                    if (!temp.contains(ltid)) {
                        temp.add(ltid);
                    }
                }
            }
        }
        return temp;
    }

    public MatchArenaLTRobotObject getRobotInitData(int stageId, int winNum) {
        List<MatchArenaLTRobotObject> temp = new ArrayList<>();
        for (MatchArenaLTRobotObject ent : MatchArenaLTRobot._ix_id.values()) {
            if (ent.getRank() == stageId) {
                temp.add(ent);
            }
        }
        if (temp.isEmpty()) {
            return null;
        }
        if (winNum > 0) {
            List<MatchArenaLTRobotObject> temp2 = new ArrayList<>();
            int x = 0;
            for (MatchArenaLTRobotObject ent2 : temp) {
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

}
