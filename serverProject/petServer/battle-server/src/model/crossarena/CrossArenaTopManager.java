package model.crossarena;

import datatool.StringHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.math.NumberUtils;

import cfg.CrossArenaCfg;
import cfg.CrossArenaCfgObject;
import common.GameConst;
import common.GlobalTick;
import common.IdGenerator;
import model.crossarena.bean.CrossArenaTopBat;
import model.room.cache.RoomCache;
import model.warpServer.WarpServerManager;
import protocol.Battle;
import protocol.CrossArena;
import protocol.CrossArenaDB;
import protocol.CrossArenaDB.RedisCrossArenaTopBat;
import protocol.CrossArenaDB.RedisCrossArenaTopPly;
import protocol.MessageId;
import protocol.ServerTransfer;
import server.event.EventManager;
import server.event.crossarena.CrossArenaTopBatEvent;
import util.JedisUtil;
import util.LogUtil;

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

    private Map<String, CrossArenaTopBat> groupBat = new ConcurrentHashMap<>();
    private Map<String, Long> groupTime = new ConcurrentHashMap<>();

    private CrossArenaTopManager() {
    }

    public void onTick(String groupId) {
        try {
            boolean isBattle = JedisUtil.jedis.hexists(GameConst.RedisKey.TopPlayState, ""+ CrossArena.CrossArenaTopPlayState.TOP_FIGHT_VALUE);
            boolean isBattleEnd = JedisUtil.jedis.hexists(GameConst.RedisKey.TopPlayState, ""+ CrossArena.CrossArenaTopPlayState.TOP_FIGHTEnd_VALUE);
            if (!isBattle || isBattleEnd) {
                // 战斗未开或者已经结束，则停止
                return;
            }
            String ftime = JedisUtil.jedis.hget(GameConst.RedisKey.TopPlayState, ""+ CrossArena.CrossArenaTopPlayState.TOP_FIGHT_VALUE);
            if (groupTime.getOrDefault(groupId, 0L) != Long.valueOf(ftime)) {
                groupBat.clear();
                groupTime.put(groupId, Long.valueOf(ftime));
            }
            String groupKey = GameConst.RedisKey.TopPlayGroup + groupId;
            byte[] db = JedisUtil.jedis.get(groupKey.getBytes());
            if (null == db) {
                return;
            }
            CrossArenaDB.RedisCrossArenaGrop dbmsg = CrossArenaDB.RedisCrossArenaGrop.parseFrom(db);
            if (null == dbmsg) {
                return;
            }
            // 是否有未结束得战斗
            if (dbmsg.getBatingCount() > 0) {
            	battingEndCheck(dbmsg);
            }
            // 战斗派发
            if (dbmsg.getIdleQueCount() <= 1) {
                return;
            }
            List<String> pids = dbmsg.getIdleQueList();
            CrossArenaTopBat catb = groupBat.computeIfAbsent(groupId, k -> new CrossArenaTopBat(groupId));
            Map<String, String> batPids = new ConcurrentHashMap<String, String>();
            int s =0, e=pids.size();
            for (int i=s; i<e; i++) {
                String p1 = pids.get(i);
                if (batPids.containsKey(p1) || batPids.containsValue(p1)) {
                    continue;
                }
                for (int j = i+1; j<e; j++) {
                    String p2 = pids.get(j);
                    if (batPids.containsKey(p2) || batPids.containsValue(p2)) {
                        continue;
                    }
                    // 可以战斗加入临时队列
                    if (catb.canBattlePlay(p1, p2)) {
                        batPids.put(p1, p2);
                    }
                }
            }
            List<String> idls = new ArrayList<>();
            idls.addAll(pids);
            // 开始装备战斗
            List<RedisCrossArenaTopBat> newBat = new ArrayList<RedisCrossArenaTopBat>();
            for (Map.Entry<String, String> ent : batPids.entrySet()) {
                byte[] pbyte1 = JedisUtil.jedis.hget(GameConst.RedisKey.TopPlayPlayer.getBytes(), ent.getKey().getBytes());
                byte[] pbyte2 = JedisUtil.jedis.hget(GameConst.RedisKey.TopPlayPlayer.getBytes(), ent.getValue().getBytes());
                if (null == pbyte1 || null == pbyte2) {
                    continue;
                }
                RedisCrossArenaTopPly pmsg1 = RedisCrossArenaTopPly.parseFrom(pbyte1);
                RedisCrossArenaTopPly pmsg2 = RedisCrossArenaTopPly.parseFrom(pbyte2);
                if (null == pmsg1 || null == pmsg2) {
                    continue;
                }
                idls.remove(ent.getKey());
                idls.remove(ent.getValue());
                // 是否是两个机器人
                RedisCrossArenaTopBat batdata = null;
//                if (pmsg1.getIsAI() > 0 && pmsg2.getIsAI() > 0) {
                    // 两个机器人
                    long id = IdGenerator.getInstance().generateIdNum();
                    int[] res = computeAIWin(pmsg1.getTeamInfo().getPlayerInfo().getPower(), pmsg2.getTeamInfo().getPlayerInfo().getPower());
                    batdata = catb.addBattleAI(id, res, pmsg1.getPlayerId(), pmsg2.getPlayerId());
//                } else {
//                    long bid = statBattle(pmsg1, pmsg2);
//                    if (bid > 0) {
//                    	batdata = catb.addBattle(bid, pmsg1.getPlayerId(), pmsg2.getPlayerId());
//                    } else {
//                        // 进入战斗失败走机器人逻辑
//                        long id = IdGenerator.getInstance().generateIdNum();
//                        int[] res = computeAIWin(pmsg1.getTeamInfo().getPlayerInfo().getPower(), pmsg2.getTeamInfo().getPlayerInfo().getPower());
//                        batdata = catb.addBattleAI(id, res, pmsg1.getPlayerId(), pmsg2.getPlayerId());
//                    }
//                }
                newBat.add(batdata);
                RedisCrossArenaTopPly.Builder newpmsg1 = pmsg1.toBuilder();
                RedisCrossArenaTopPly.Builder newpmsg2 = pmsg2.toBuilder();
                newpmsg1.setOtheridx(pmsg2.getPlayerId());
                newpmsg2.setOtheridx(pmsg1.getPlayerId());
                JedisUtil.jedis.hset(GameConst.RedisKey.TopPlayPlayer.getBytes(), pmsg1.getPlayerId().getBytes(), newpmsg1.build().toByteArray());
                JedisUtil.jedis.hset(GameConst.RedisKey.TopPlayPlayer.getBytes(), pmsg2.getPlayerId().getBytes(), newpmsg2.build().toByteArray());
            }
            CrossArenaDB.RedisCrossArenaGrop.Builder newdbmsg = dbmsg.toBuilder();
            newdbmsg.clearIdleQue();
            newdbmsg.addAllIdleQue(idls);
            if (!newBat.isEmpty()) {
            	newdbmsg.addAllBating(newBat);
            }
            JedisUtil.jedis.set(groupKey.getBytes(), newdbmsg.build().toByteArray());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    private void battingEndCheck(CrossArenaDB.RedisCrossArenaGrop dbmsg) {
    	try {
    		List<String> delsplayerId = new ArrayList<>();
    		List<CrossArenaDB.RedisCrossArenaTopBat> syTemp = new ArrayList<CrossArenaDB.RedisCrossArenaTopBat>();
    		for (CrossArenaDB.RedisCrossArenaTopBat room : dbmsg.getBatingList()) {
    			if (GlobalTick.getInstance().getCurrentTime() > room.getEndTime()) {
    				battleEndSaveData(dbmsg, room);
    				delsplayerId.add(room.getPy1());
    				delsplayerId.add(room.getPy2());
    			} else {
    				syTemp.add(room);
    			}
    		}
    		if (!delsplayerId.isEmpty()) {
    			String groupKey = GameConst.RedisKey.TopPlayGroup + dbmsg.getGroup();
    			CrossArenaDB.RedisCrossArenaGrop.Builder newdbmsg = dbmsg.toBuilder();
    			newdbmsg.addAllIdleQue(delsplayerId);
    			newdbmsg.clearBating();
    			newdbmsg.addAllBating(syTemp);
    			JedisUtil.jedis.set(groupKey.getBytes(), newdbmsg.build().toByteArray());
    		}
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
		}
    }
    
    public ServerTransfer.PvpBattlePlayerInfo buildPvpPlayerInfo(int camp, RedisCrossArenaTopPly playerInfo) {
        if (playerInfo == null || null == playerInfo.getTeamInfo()) {
            LogUtil.error("model.matchArena.MatchArenaPlayer.buildPvpPlayerInfo, player info is null");
            return null;
        }
        ServerTransfer.PvpBattlePlayerInfo.Builder resultBuilder = ServerTransfer.PvpBattlePlayerInfo.newBuilder();
        resultBuilder.setPlayerInfo(playerInfo.getTeamInfo().getPlayerInfo());
        int svrIndex = StringHelper.stringToInt(playerInfo.getFromSvrIndex(), 0);
        resultBuilder.setFromSvrIndex(svrIndex);
        resultBuilder.setCamp(camp);
        resultBuilder.setIsAuto(playerInfo.getTeamInfo().getIsAuto());
        resultBuilder.setPlayerExtData(playerInfo.getTeamInfo().getPlayerExtData());
        resultBuilder.addAllPetList(playerInfo.getTeamInfo().getPetListList());
        resultBuilder.addAllPlayerSkillIdList(playerInfo.getTeamInfo().getPlayerSkillIdListList());
        return resultBuilder.build();
    }

    /**
     * @param pmsg1
     * @param pmsg2
     * 开启战斗
     * @return
     */
    public long statBattle(RedisCrossArenaTopPly pmsg1, RedisCrossArenaTopPly pmsg2) {
        ServerTransfer.PvpBattlePlayerInfo firstPvpInfo = buildPvpPlayerInfo(1, pmsg1);
        ServerTransfer.PvpBattlePlayerInfo secondPvpInfo = buildPvpPlayerInfo(2, pmsg2);
        if (firstPvpInfo == null || secondPvpInfo == null) {
            return 0;
        }
        ServerTransfer.ApplyPvpBattleData.Builder applyPvpBuilder = ServerTransfer.ApplyPvpBattleData.newBuilder();
        applyPvpBuilder.setFightMakeId(6);
        applyPvpBuilder.setSubBattleType(Battle.BattleSubTypeEnum.BSTE_CrossArenaTop);
        applyPvpBuilder.addPlayerInfo(firstPvpInfo);
        applyPvpBuilder.addPlayerInfo(secondPvpInfo);

        int svrIndex1 = StringHelper.stringToInt(pmsg1.getFromSvrIndex(), 0);
        int svrIndex2 = StringHelper.stringToInt(pmsg2.getFromSvrIndex(), 0);
        int fromSvrIndex = svrIndex1;
        if (pmsg2.getIsAI() <= 0) {
            fromSvrIndex = svrIndex2;
        }
        // 创建战斗房间
        ServerTransfer.ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(applyPvpBuilder.build(),
                ServerTransfer.ServerTypeEnum.STE_GameServer, fromSvrIndex);
        // 房间创建失败
        if (!replyBuilder.getResult()) {
            // 特殊异常情况处理
            return 0;
        }
        // 创建PVP战斗创建成功消息
        ServerTransfer.BS_GS_ReplyPvpBattle.Builder builder = ServerTransfer.BS_GS_ReplyPvpBattle.newBuilder();
        builder.setReplyPvpBattleData(replyBuilder);

        // 通知战斗双方PVP战斗开始
        WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, svrIndex1,
                MessageId.MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);

        if (pmsg1.getFromSvrIndex() != pmsg2.getFromSvrIndex()) {
            WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, svrIndex2,
                    MessageId.MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
        }
        return replyBuilder.getBattleId();
    }

    public int[] computeAIWin(long power1, long power2) {
        // 高战胜率 = （高战-低战）/(高战+低战）*3+0.5
        if (power1 == 0 && power2 == 0) {
            return new int[1];
        }
        float rate = Math.abs(power1-power2)*1F/(power1+power2)*3+0.5F;
        float rom = CrossArenaManager.getRandomFloat(0, 1);
        int winCamp = 0;
        if (rom < rate) {
            winCamp = power1 > power2 ? 1 : 2;
        } else {
        	winCamp = power1 > power2 ? 2 : 1;
        }
		// 秒 = （150*(1-(高战胜率-0.5)*2)+30）*（+-10%【可调】）
        // 30>秒>180: 秒低于30=30，高于180=180
        float romTime = CrossArenaManager.getRandomFloat(0.9F, 1.1F);
        float time = (60F*(1-(rate-0.5F)*2F)+30F)*romTime;
        int timeInt = Math.round(time);
        timeInt = Math.max(timeInt, 30);
        timeInt = Math.min(timeInt, 90);
        int[] res = new int[2];
        res[0] = winCamp;
        res[1] = timeInt;
        return res;
    }

    /**
     * @param battleId
     * @param winCamp
     * 战斗结束
     */
    public void settleEvent(long battleId, int winCamp) {
        CrossArenaTopBatEvent event = new CrossArenaTopBatEvent();
        CrossArenaTopBat btemp = null;
        for (Map.Entry<String, CrossArenaTopBat> ent : groupBat.entrySet()) {
            if (ent.getValue().getBatingMap().containsKey(battleId)) {
                btemp = ent.getValue();
                break;
            }
        }
        if (null != btemp) {
        	btemp.getBatingMap().remove(battleId);
            event.setRoomId(battleId);
            event.setWinCamp(winCamp);
            EventManager.getInstance().dealCrossArenaEvent(event, NumberUtils.toInt(btemp.getGroupId()));
        }
    }

    /**
     * @param battleId
     * @param winCamp
     * 战斗结束
     */
    public void settleMatchTable(int groupId, long battleId, int winCamp) {
        try {
            String groupKey = GameConst.RedisKey.TopPlayGroup + groupId;
            byte[] db = JedisUtil.jedis.get(groupKey.getBytes());
            if (null == db) {
                return;
            }
            CrossArenaDB.RedisCrossArenaGrop dbmsg = CrossArenaDB.RedisCrossArenaGrop.parseFrom(db);
            if (null == dbmsg) {
                return;
            }
            CrossArenaDB.RedisCrossArenaTopBat.Builder room = null;
            for (CrossArenaDB.RedisCrossArenaTopBat rooms : dbmsg.getBatingList()) {
                if (rooms.getRoomid() == battleId) {
                    room = rooms.toBuilder();
                    break;
                }
            }
            if (null == room) {
                return;
            }
            room.setCampWin(winCamp);
            battleEndSaveData(dbmsg, room.build());
            CrossArenaDB.RedisCrossArenaGrop.Builder newdbmsg = dbmsg.toBuilder();
            newdbmsg.addIdleQue(room.getPy1());
            newdbmsg.addIdleQue(room.getPy2());
            String key = GameConst.RedisKey.TopPlayGroup + dbmsg.getGroup();
            JedisUtil.jedis.set(key.getBytes(), newdbmsg.build().toByteArray());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * @param dbmsg
     * @param room
     * @throws Exception
     * 战斗结算
     */
    private void battleEndSaveData(CrossArenaDB.RedisCrossArenaGrop dbmsg, CrossArenaDB.RedisCrossArenaTopBat room) throws Exception {
        CrossArenaCfgObject cfgData = CrossArenaCfg.getById(GameConst.ConfgId);
        if (null == cfgData) {
            return;
        }
        byte[] pbyte1 = JedisUtil.jedis.hget(GameConst.RedisKey.TopPlayPlayer.getBytes(), room.getPy1().getBytes());
        byte[] pbyte2 = JedisUtil.jedis.hget(GameConst.RedisKey.TopPlayPlayer.getBytes(), room.getPy2().getBytes());
        if (null != pbyte1) {
            RedisCrossArenaTopPly pmsg1 = RedisCrossArenaTopPly.parseFrom(pbyte1);
            if (null != pmsg1) {
                RedisCrossArenaTopPly.Builder newpmsg1 = pmsg1.toBuilder();
                newpmsg1.setOtheridx("");
                if (room.getCampWin() == 1) {
                    newpmsg1.setGrade(newpmsg1.getGrade() + cfgData.getTopwinjf());
                } else {
                    newpmsg1.setGrade(newpmsg1.getGrade() - Math.abs(cfgData.getTopfailjf()));
                }
                JedisUtil.jedis.hset(GameConst.RedisKey.TopPlayPlayer.getBytes(), pmsg1.getPlayerId().getBytes(), newpmsg1.build().toByteArray());
            }
        }
        if (null != pbyte2) {
            RedisCrossArenaTopPly pmsg2 = RedisCrossArenaTopPly.parseFrom(pbyte2);
            if (null != pmsg2) {
                RedisCrossArenaTopPly.Builder newpmsg2 = pmsg2.toBuilder();
                newpmsg2.setOtheridx("");
                if (room.getCampWin() == 2) {
                    newpmsg2.setGrade(newpmsg2.getGrade() + cfgData.getTopwinjf());
                } else {
                    newpmsg2.setGrade(newpmsg2.getGrade() - Math.abs(cfgData.getTopfailjf()));
                }
                JedisUtil.jedis.hset(GameConst.RedisKey.TopPlayPlayer.getBytes(), pmsg2.getPlayerId().getBytes(), newpmsg2.build().toByteArray());
            }
        }
    }

}
