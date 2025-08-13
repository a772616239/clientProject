package model.crossarena;

import static common.JedisUtil.jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.alibaba.fastjson.JSON;

import cfg.CrossArenaCfg;
import cfg.CrossArenaCfgObject;
import cfg.CrossArenaRobot;
import cfg.CrossArenaRobotObject;
import cfg.Head;
import cfg.RankRewardRangeConfig;
import cfg.RankRewardRangeConfigObject;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import common.JedisUtil;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import datatool.StringHelper;
import model.activity.ActivityData;
import model.activity.TimeRuleManager;
import model.battle.BattleUtil;
import model.crossarena.bean.CrossArenaTopHis;
import model.crossarena.bean.CrossArenaTopHisSub;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.settle.RankingRewards;
import model.ranking.settle.RankingRewardsImpl;
import model.reward.RewardUtil;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import model.warpServer.battleServer.BattleServerManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import protocol.*;
import protocol.CrossArena.CrossArenaTopPlayState;
import protocol.CrossArenaDB.RedisCrossArenaTopPly;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.ObjUtil;
import util.RandomUtil;

/**
 * 巅峰赛总控制器
 */
public class CrossArenaTopManager {

    private static CrossArenaTopManager instance;

    public static CrossArenaTopManager getInstance() {
        if (instance == null) {
            synchronized (CrossArenaTopManager.class) {
                if (instance == null) {
                    instance = new CrossArenaTopManager();
                }
            }
        }
        return instance;
    }

    private CrossArenaTopManager() {

    }

    private CrossArenaTopPlayState state = CrossArenaTopPlayState.TOP_TOPNOT;
    private long stateEndTime = 0;

    private boolean isOpen = false;

    public boolean init() {
        CrossArenaCfgObject cfgData = CrossArenaCfg.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            return false;
        }
        return true;
    }

    public void openAndClose(boolean state) {
        isOpen = state;
        this.state = CrossArenaTopPlayState.TOP_TOPNOT;
    }

    /**
     * 开启一轮新活动
     */
    public void openInitJion(long time, boolean isGM) {
        this.stateEndTime=time;
        isOpen = true;
        state = CrossArenaTopPlayState.TOP_JION;
        try {
            // 数据加锁失败
            if (!JedisUtil.lockRedisKey(GameConst.RedisKey.TopPlayLock, 60000l)) {
                return;
            }
            String openJionTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_JION_VALUE);
            if (!isGM && !StringHelper.isNull(openJionTime)) {
                // 判断存储数据是否是同一条数据
                if (Long.parseLong(openJionTime) == time&& CrossArenaTopPlayState.TOP_JION==state) {
                    // 数据是同一天则表示活动已经开启
                    return;
                }
            }
            // 设置活动时间
            long startTime = GlobalTick.getInstance().getCurrentTime();
            jedis.set(GameConst.RedisKey.TopPlayTime, String.valueOf(startTime));
            jedis.del(GameConst.RedisKey.TopPlayState);
            jedis.del(GameConst.RedisKey.TopPlayPlayer);
            delBattleServerInfo();
            jedis.hset(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_JION_VALUE, ""+time);
            JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    private void delTopPlayers() {
        JedisUtil.hdelAllByKey(GameConst.RedisKey.TopPlayPlayer);
    }

    public void closeFight(long time) {
        try {
            if (!JedisUtil.lockRedisKey(GameConst.RedisKey.TopPlayLock, 200000l)) {
                return;
            }
            String fightTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_FIGHTEnd_VALUE);
            if (StringHelper.isNull(fightTime)) {
                // 没有加入阶段，活动开启失败
                JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                return;
            }
            String endTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_END_VALUE);
            if (!StringHelper.isNull(endTime)) {
                // 判断存储数据是否是同一条数据
                if (Long.valueOf(endTime) == time) {
                    // 数据是同一天则表示活动已经开启
                    JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                    return;
                }
            }
            jedis.hset(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_END_VALUE, ""+time);
            JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param isGM
     * 开启展示阶段
     */
    public void openView(long stateEndTime, boolean isGM) {
        try {
            this.stateEndTime = stateEndTime;
            state = CrossArenaTopPlayState.TOP_VIEW;
            if (!JedisUtil.lockRedisKey(GameConst.RedisKey.TopPlayLock, 200000l)) {
                return;
            }
            String fightTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_FIGHTEnd_VALUE);
            if (StringHelper.isNull(fightTime)) {
                // 没有加入阶段，活动开启失败
                JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                return;
            }
            String vieTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_VIEW_VALUE);
            if (!isGM && !StringHelper.isNull(vieTime)) {
                // 判断存储数据是否是同一条数据
                if (Long.valueOf(vieTime) == stateEndTime) {
                    // 数据是同一天则表示活动已经开启
                    JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                    return;
                }
            }
            jedis.hset(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_VIEW_VALUE, ""+stateEndTime);
            JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param isGM
     * 开启分组阶段
     */
    public void openGroup(long time, boolean isGM) {
        // 数据加锁失败
        this.stateEndTime = time;
        // 开始分组
        state = CrossArenaTopPlayState.TOP_MATCHING;
        CrossArenaCfgObject cfgData = CrossArenaCfg.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            return;
        }
        try {
            if (!JedisUtil.lockRedisKey(GameConst.RedisKey.TopPlayLock, 200000l)) {
                return;
            }
            String openJionTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_JION_VALUE);
            if (StringHelper.isNull(openJionTime)) {
                // 没有加入阶段，活动开启失败
                JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                return;
            }
            String groupTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_MATCHING_VALUE);
            if (!isGM && !StringHelper.isNull(groupTime)) {
                // 判断存储数据是否是同一条数据
                if (Long.valueOf(groupTime) == time) {
                    // 数据是同一天则表示活动已经开启
                    JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                    return;
                }
            }

            List<RedisCrossArenaTopPly.Builder> tempPl = new ArrayList<>();
            Map<byte[], byte[]> alls = jedis.hgetAll(GameConst.RedisKey.TopPlayPlayer.getBytes());
            for (byte[] ent : alls.values()) {
                tempPl.add(RedisCrossArenaTopPly.parseFrom(ent).toBuilder());
            }
            int num = alls.size();
            int groupPeo = cfgData.getTopgroupnum();
            int zs = num/groupPeo;
            int ys = num%groupPeo;
            int groupNum = zs + ys > 0 ? 1 : 0;
            if (groupNum <= 0) {
                JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                return;
            }
            List<Integer> allBS = new ArrayList<>();
            allBS.addAll(BattleServerManager.getInstance().getBatServerIndexAddrMap().keySet());
            if (allBS.isEmpty()) {
                LogUtil.error("擂台赛数据初始化时，没有获取到可执行逻辑的跨服服务器!");
                // 没有战斗服务器，则默认给予一个初始值，方便初始化数据
                allBS.add(0);
            }
            int pos;
            for (int i = 1; i <= groupNum; i++) {
                pos = allBS.size() % i;
                jedis.hset(GameConst.RedisKey.TopPlayBSSid, "" + i, StringHelper.IntTostring(allBS.get(pos), "0"));
            }
            // 填充机器人
            int needAiNum = groupNum * groupPeo - num;
            List<RedisCrossArenaTopPly.Builder> tempAI = new ArrayList<>();
            for (int i = 1; i<=needAiNum; i++) {
                String id = IdGenerator.getInstance().generateId();
                tempAI.add(createAttDataRobot(id, 1, 1));
            }
            List<RedisCrossArenaTopPly.Builder> tempAll = new ArrayList<>();
            tempAll.addAll(tempAI);
            tempAll.addAll(tempPl);
            // 打乱顺序，开始分组
            Collections.shuffle(tempAll);
            int initJF = cfgData.getTopinitjf();

            int groupId = 1;
            Map<Integer, List<String>> groupPlayers = new ConcurrentHashMap<>();
            for (RedisCrossArenaTopPly.Builder pldb : tempAll) {
                pldb.setGrade(initJF);
                pldb.clearBattleIds();
                pldb.setGroup(""+groupId);
                groupPlayers.computeIfAbsent(groupId, k -> new ArrayList<>()).add(pldb.getPlayerId());
                groupId += 1;
                if (groupId > groupNum) {
                    groupId = 1;
                }
                jedis.hset(GameConst.RedisKey.TopPlayPlayer.getBytes(), pldb.getPlayerId().getBytes(), pldb.build().toByteArray());
            }
            for (Map.Entry<Integer, List<String>> ent : groupPlayers.entrySet()) {
                CrossArenaDB.RedisCrossArenaGrop.Builder groupMsg = CrossArenaDB.RedisCrossArenaGrop.newBuilder();
                groupMsg.setGroup("" + ent.getKey());
                groupMsg.addAllPlayers(ent.getValue());
                groupMsg.addAllIdleQue(ent.getValue());
                jedis.set(createGroupKey(ent.getKey()).getBytes(), groupMsg.build().toByteArray());
            }
            jedis.hset(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_MATCHING_VALUE, ""+time);
            JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void openBattle(long time, boolean isGM) {
        this.stateEndTime = time;
        state = CrossArenaTopPlayState.TOP_FIGHT;
        CrossArenaCfgObject cfgData = CrossArenaCfg.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            return;
        }
        try {
            if (!JedisUtil.lockRedisKey(GameConst.RedisKey.TopPlayLock, 200000l)) {
                return;
            }
            String groupTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_MATCHING_VALUE);
            if (StringHelper.isNull(groupTime)) {
                // 没有加入阶段，活动开启失败
                JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                return;
            }
            String fightTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_FIGHT_VALUE);
            if (!isGM && !StringHelper.isNull(fightTime)) {
                // 判断存储数据是否是同一条数据
                if (Long.valueOf(fightTime) == time) {
                    // 数据是同一天则表示活动已经开启
                    JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                    return;
                }
            }

            jedis.hset(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_FIGHT_VALUE, ""+time);
            JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param time
     * 开始结算
     */
    public void openSettle(long time, boolean isGM) {
        CrossArenaCfgObject cfgData = CrossArenaCfg.getById(GameConst.CONFIG_ID);
        state = CrossArenaTopPlayState.TOP_FIGHTEnd;
        if (null == cfgData) {
            return;
        }
        try {
            if (!JedisUtil.lockRedisKey(GameConst.RedisKey.TopPlayLock, 200000l)) {
                return;
            }
            String fightTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_FIGHT_VALUE);
            if (StringHelper.isNull(fightTime)) {
                // 没有加入阶段，活动开启失败
                JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                return;
            }
            String endTime = jedis.hget(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_FIGHTEnd_VALUE);
            if (!isGM && !StringHelper.isNull(endTime)) {
                // 判断存储数据是否是同一条数据
                if (Long.valueOf(endTime) == time) {
                    // 数据是同一天则表示活动已经开启
                    JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
                    return;
                }
            }
            String loop = jedis.get(GameConst.RedisKey.CrossArenaTopLoop);
            int nextloop = NumberUtils.toInt(loop) + 1;
            jedis.set(GameConst.RedisKey.CrossArenaTopLoop, ""+nextloop);

            // 开始结算
            Map<String, String> alls = jedis.hgetAll(GameConst.RedisKey.TopPlayBSSid);
            CrossArenaTopHis topHis = new CrossArenaTopHis();
            for (Map.Entry<String, String> ent : alls.entrySet()) {
                byte[] gbyte = jedis.get(createGroupKey(ent.getKey()).getBytes());
                if (null == gbyte) {
                    continue;
                }
                CrossArenaDB.RedisCrossArenaGrop gmsg = CrossArenaDB.RedisCrossArenaGrop.parseFrom(gbyte);
                if (null == gmsg) {
                    continue;
                }
                CrossArenaTopHisSub sub = new CrossArenaTopHisSub();
                sub.setGroup(NumberUtils.toInt(ent.getKey()));
                List<RedisCrossArenaTopPly> sortPlay = new LinkedList<>();
                // 获取所有数据开始排序
                for (String pid : gmsg.getPlayersList()) {
                    byte[] pbtyef = jedis.hget(GameConst.RedisKey.TopPlayPlayer.getBytes(), pid.getBytes());
                    if (null == pbtyef) {
                        continue;
                    }
                    RedisCrossArenaTopPly pmsgf = RedisCrossArenaTopPly.parseFrom(pbtyef);
                    if (null == pmsgf) {
                        continue;
                    }
                    sortPlay.add(pmsgf);
                }
                sort(sortPlay);
                // 根据排序给与编号
                updataPlayerDBAndAward(sortPlay);
                int i = 1;
                StringBuilder othname = new StringBuilder();
                for (RedisCrossArenaTopPly rcat : sortPlay) {
                    if (i > 8) {
                        break;
                    }
                    if (i == 1) {
                        sub.setName(rcat.getTeamInfo().getPlayerInfo().getPlayerName());
                    } else {
                    	othname.append(rcat.getTeamInfo().getPlayerInfo().getPlayerName() + ",");
                    }
                    i++;
                }
                sub.setOtherName(othname.toString());
                topHis.getHis().put(sub.getGroup(), sub);
            }

            jedis.hset(GameConst.RedisKey.TopPlayState, ""+CrossArenaTopPlayState.TOP_END_VALUE, ""+time);
            String json = JSON.toJSONString(topHis);
            jedis.hset(GameConst.RedisKey.CrossArenaNoteTopMap, ""+nextloop, json);
            JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlayLock);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    private void delBattleServerInfo() {
        JedisUtil.jedis.del(GameConst.RedisKey.TopPlayBSSid);
    }

    /**
     * @param sortPlay
     * 更新玩家数据，并给予奖励
     */
    private void updataPlayerDBAndAward(List<RedisCrossArenaTopPly> sortPlay) {
        // 给予奖励
        CrossArenaCfgObject cfgData = CrossArenaCfg.getById(GameConst.CONFIG_ID);
        if (null == cfgData) {
            return;
        }
        // 获取初始数据
        List<RankingRewardsImpl> rewards = new ArrayList<>();
        for (int id : cfgData.getTopaward()) {
            RankRewardRangeConfigObject rankRewardsConfig = RankRewardRangeConfig.getById(id);
            if (rankRewardsConfig == null) {
                continue;
            }
            RankingRewardsImpl aimpl = new RankingRewardsImpl(rankRewardsConfig.getRangemin(), rankRewardsConfig.getRangemax(), RewardUtil.parseRewardIntArrayToRewardList(rankRewardsConfig.getReward()));
            rewards.add(aimpl);
        }
        if (CollectionUtils.isEmpty(rewards)) {
            LogUtil.error("没有获取到副本排行榜奖励");
            return;
        }
        int i = 1;
        for (RedisCrossArenaTopPly rcat : sortPlay) {
            int mymc = i;
            RedisCrossArenaTopPly.Builder pldb = rcat.toBuilder();
            pldb.setSort(mymc);
            jedis.hset(GameConst.RedisKey.TopPlayPlayer.getBytes(), pldb.getPlayerId().getBytes(), pldb.build().toByteArray());
            i+=1;
            CrossArenaManager.getInstance().savePlayerDBInfo(pldb.getPlayerId(), CrossArena.CrossArenaDBKey.DFS_MaxRank,mymc, CrossArenaUtil.DbChangeRepMax);

            List<Common.Reward> all = null;
            // 根据名次获取奖励
            for (RankingRewards rankingReward : rewards) {
                if (GameUtil.inScope(rankingReward.getStartRanking(), rankingReward.getEndRanking(), mymc)) {
                    all = rankingReward.getRankingRewards();
                    break;
                }
            }
            if (null == all || rcat.getIsAI() > 0) {
                continue;
            }
            // 需要判断是否是本服，否则转发至原服逻辑
            ServerTransfer.BS_GS_CrossArenaTopAward.Builder tomsg = ServerTransfer.BS_GS_CrossArenaTopAward.newBuilder();
            tomsg.setPlayerId(rcat.getPlayerId());
            int svrIndex = StringHelper.stringToInt(rcat.getFromSvrIndex(), 0);
            tomsg.setSvrIndex(svrIndex);
            tomsg.setRankId(mymc);
            tomsg.addAllAward(all);
            tomsg.setMailId(cfgData.getTopmailid());
            if (null == playerCache.getByIdx(rcat.getPlayerId())) {
                BattleServerManager.getInstance().transferMsgGSToGS(MessageId.MsgIdEnum.BS_GS_CrossArenaTopAward_VALUE, tomsg.build().toByteString(),svrIndex);
            } else {
                sendPlayerAward(tomsg.build());
            }
        }
    }

    /**
     * @param award
     * 发送排名奖励
     */
    public void sendPlayerAward(ServerTransfer.BS_GS_CrossArenaTopAward award) {
        if (null == playerCache.getByIdx(award.getPlayerId())) {
            return;
        }
        if (award.getRankId() == 1) {
            CrossArenaManager.getInstance().savePlayerDBInfoAdd(award.getPlayerId(), CrossArena.CrossArenaDBKey.DFS_FIRSTNUM, 1);
            CrossArenaHonorManager.getInstance().honorVueByKeyAdd(award.getPlayerId(), CrossArenaUtil.HR_DFHZ_1NUM, 1);
            CrossArenaHonorManager.getInstance().honorVueFirst(award.getPlayerId(), CrossArenaUtil.HR_FIRST_DFHZ);
        }
        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_TOPPAYRANK);
        EventUtil.triggerAddMailEvent(award.getPlayerId(), award.getMailId(), award.getAwardList(), reason, String.valueOf(award.getRankId()));
    }

    /**
     * @param sortPlay
     * 积分排序
     */
    private void sort(List<RedisCrossArenaTopPly> sortPlay) {
        Collections.sort(sortPlay, new Comparator<RedisCrossArenaTopPly>() {
            public int compare(RedisCrossArenaTopPly arg0, RedisCrossArenaTopPly arg1) {
                if (arg0.getGrade() > arg1.getGrade()) {
                    return -1;
                } else if (arg0.getGrade() == arg1.getGrade()) {
                    if (arg0.getJftime() < arg1.getJftime()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else {
                    return 1;
                }
            }
        });
    }

    private String createGroupKey(String groupId) {
        return GameConst.RedisKey.TopPlayGroup + groupId;
    }

    private String createGroupKey(int groupId) {
        return GameConst.RedisKey.TopPlayGroup + groupId;
    }

    /**
     * @param playerIdx
     * 创建玩家得战斗信息
     * @return
     */
    public RedisCrossArenaTopPly createPlayerJionInfo(String playerIdx) {
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teamEntity == null) {
            return null;
        }
        Team dbTeam = teamEntity.getDBTeam(PrepareWar.TeamNumEnum.TNE_TopPlay_1);
        if (dbTeam == null) {
            return null;
        }
        List<String> petIds = new ArrayList<>();
        petIds.addAll(dbTeam.getLinkPetMap().values());
        List<Battle.BattlePetData> petDataList = petCache.getInstance().getPetBattleData(playerIdx, petIds, Battle.BattleSubTypeEnum.BSTE_CrossArenaEvent);
        if (GameUtil.collectionIsEmpty(petDataList)) {
            return null;
        }
        //检查玩家信息是否正确
        Battle.PlayerBaseInfo.Builder playerInfo = BattleUtil.buildPlayerBattleBaseInfo(playerIdx);
        if (playerInfo == null) {
            return null;
        }
        CrossArenaDB.RedisCrossArenaTopPly.Builder msgply = CrossArenaDB.RedisCrossArenaTopPly.newBuilder();
        msgply.setPlayerId(playerIdx);
        String svrIndex = StringHelper.IntTostring(ServerConfig.getInstance().getServer(), "0");
        msgply.setFromSvrIndex(svrIndex);
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
        playerEntity player = playerCache.getByIdx(playerIdx);
        for (int skillId : dbTeam.getLinkSkillMap().values()) {
            battlePlayerInfo.addPlayerSkillIdList(Battle.SkillBattleDict.newBuilder()
                    .setSkillId(skillId).setSkillLv(player.getSkillLv(skillId)).build());
        }
        msgply.setTeamInfo(battlePlayerInfo);
        msgply.setTeamdb(teamEntity.buildTeamInfo(dbTeam));
        return msgply.build();
    }

    public void updataTeamRef(String playerIdx) {
    	if (!isOpen) {
            return;
        }
    	if (this.state != CrossArenaTopPlayState.TOP_JION) {
    		 return;
    	}
    	CrossArenaDB.RedisCrossArenaTopPly msgply = createPlayerJionInfo(playerIdx);
        if (null == msgply) {
        	return;
        }
        try {
        	boolean isAt = jedis.hexists(GameConst.RedisKey.TopPlayPlayer.getBytes(), playerIdx.getBytes());
        	if (isAt) {
        		jedis.hset(GameConst.RedisKey.TopPlayPlayer.getBytes(), playerIdx.getBytes(), msgply.toByteArray());
        	}
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
    
    /**
     * @param playerIdx
     * 玩家加入擂台
     */
    public void jionTop(String playerIdx, CrossArena.CS_CrossArenaTopPlayJion req) {
        CrossArena.SC_CrossArenaTopPlayJion.Builder msg = CrossArena.SC_CrossArenaTopPlayJion.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        if (!isOpen) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlayJion_VALUE, msg);
            return;
        }
        if (this.state != CrossArenaTopPlayState.TOP_JION) {
        	 msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
             GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlayJion_VALUE, msg);
             return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (null == player) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlayJion_VALUE, msg);
            return;
        }
//        if (req.getMapsList().isEmpty()) {
//            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_CP_TeamNotExists));
//            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlayJion_VALUE, msg);
//            return;
//        }
        //检查玩家信息是否正确
        CrossArenaDB.RedisCrossArenaTopPly msgply = createPlayerJionInfo(playerIdx);
        if (null == msgply) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_CP_TeamNotExists));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlayJion_VALUE, msg);
            return;
        }
        try {
            jedis.hset(GameConst.RedisKey.TopPlayPlayer.getBytes(), playerIdx.getBytes(), msgply.toByteArray());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerIdx, CrossArenaUtil.HR_DFHZ_JION, 1);
        CrossArenaManager.getInstance().savePlayerDBInfoAdd(playerIdx, CrossArena.CrossArenaDBKey.DFS_JIONNUM, 1);
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlayJion_VALUE, msg);
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_CrossArena_QFS, 1, 0);
        LogService.getInstance().submit(new GamePlayLog(playerIdx, Common.EnumFunction.PeakMelee));
    }

    public void getPanel(String playerIdx) {
        CrossArena.SC_CrossArenaTopPlay.Builder msg = CrossArena.SC_CrossArenaTopPlay.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        msg.setState(state);
        msg.setStateEndTime(stateEndTime);
        if (state == CrossArenaTopPlayState.TOP_TOPNOT) {
        	ActivityData ad = TimeRuleManager.getInstance().getActivityData(4);
        	if (null != ad) {
        		msg.setStateEndTime(ad.getNextOpenTime());
        	}
        	GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlay_VALUE, msg);
            return;
        } else if (state == CrossArenaTopPlayState.TOP_JION || state == CrossArenaTopPlayState.TOP_MATCHING) {
            try {
                byte[] pbyte = jedis.hget(GameConst.RedisKey.TopPlayPlayer.getBytes(), playerIdx.getBytes());
                if (null != pbyte) {
                    RedisCrossArenaTopPly pmsg = RedisCrossArenaTopPly.parseFrom(pbyte);
                    if (null != pmsg) {
                        msg.setTeamInfo(pmsg.getTeamdb());
                    }
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlay_VALUE, msg);
            return;
        }
        try {
            byte[] pbyte = jedis.hget(GameConst.RedisKey.TopPlayPlayer.getBytes(), playerIdx.getBytes());
            if (null == pbyte) {
                //todo 临时处理，不报名就给玩家显示报名结束，看策划后面改不改
                msg.setState(CrossArenaTopPlayState.TOP_MATCHING);
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlay_VALUE, msg);
                return;
            }
            RedisCrossArenaTopPly pmsg = RedisCrossArenaTopPly.parseFrom(pbyte);
            if (null == pmsg) {
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlay_VALUE, msg);
                return;
            }
            msg.setTeamInfo(pmsg.getTeamdb());
            byte[] gbyte = jedis.get(createGroupKey(pmsg.getGroup()).getBytes());
            if (null == gbyte) {
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlay_VALUE, msg);
                return;
            }
            CrossArenaDB.RedisCrossArenaGrop gmsg = CrossArenaDB.RedisCrossArenaGrop.parseFrom(gbyte);
            if (null == gmsg) {
                GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlay_VALUE, msg);
                return;
            }
            msg.setGroupName(gmsg.getGroup());
            for (String pid : gmsg.getPlayersList()) {
                byte[] pbtyef = jedis.hget(GameConst.RedisKey.TopPlayPlayer.getBytes(), pid.getBytes());
                if (null == pbtyef) {
                    continue;
                }
                RedisCrossArenaTopPly pmsgf = RedisCrossArenaTopPly.parseFrom(pbtyef);
                if (null == pmsgf) {
                    continue;
                }
                msg.addGroup(redisDBToMsgSend(pmsgf));
                if (Objects.equals(pid, pmsg.getOtheridx())) {
                    msg.setOtherInfo(redisDBToMsgSend(pmsgf));
                }
            }
            msg.setSelfInfo(redisDBToMsgSend(pmsg));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlay_VALUE, msg);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    private CrossArena.CrossArenaTopPlayDB redisDBToMsgSend(RedisCrossArenaTopPly pmsgf) {
        CrossArena.CrossArenaTopPlayDB.Builder msg = CrossArena.CrossArenaTopPlayDB.newBuilder();
        msg.setPlayerId(pmsgf.getPlayerId());
        msg.setName(pmsgf.getTeamInfo().getPlayerInfo().getPlayerName());
        msg.setHead(pmsgf.getTeamInfo().getPlayerInfo().getAvatar());
        msg.setPower(pmsgf.getTeamInfo().getPlayerInfo().getPower());
        msg.setGrade(pmsgf.getGrade());
        msg.setSort(pmsgf.getSort());
        msg.setBorderId(pmsgf.getTeamInfo().getPlayerInfo().getAvatarBorder());
        return msg.build();
    }

    /**
     * @return
     * 创建参加擂台的玩家战斗数据
     */
    public CrossArenaDB.RedisCrossArenaTopPly.Builder createAttDataRobot(String idx, int sceneId, int winNum) {
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
        CrossArenaRobotObject robotCfg = getRobotInitData(sceneId, winNum);
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


        CrossArenaDB.RedisCrossArenaTopPly.Builder attPlayerInfo = CrossArenaDB.RedisCrossArenaTopPly.newBuilder();
        attPlayerInfo.setPlayerId(idx);
        String svrIndex = StringHelper.IntTostring(ServerConfig.getInstance().getServer(), "0");
        attPlayerInfo.setFromSvrIndex(svrIndex);
        attPlayerInfo.setTeamInfo(battlePlayerInfo);
        attPlayerInfo.setIsAI(1);
        attPlayerInfo.setGrade(2000);
        return attPlayerInfo;
    }

    public CrossArenaRobotObject getRobotInitData(int stageId, int winNum) {
        List<CrossArenaRobotObject> temp = new ArrayList<>();
        for (CrossArenaRobotObject ent : CrossArenaRobot._ix_id.values()) {
            if (ent.getRank() == stageId) {
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

    public void openGM(int state) {
        if (state == 1) {
            openInitJion(System.currentTimeMillis(), true);
        } else if (state == 2) {
            openGroup(System.currentTimeMillis(), true);
        } else if (state == 3) {
            openBattle(System.currentTimeMillis(), true);
        } else if (state == 4) {
            openSettle(System.currentTimeMillis(), true);
        } else if (state == 5) {
            openView(System.currentTimeMillis(), true);
        }
    }

}
