package model.crossarena;

import static common.GameConst.RedisKey.CrossArenaBSSid;
import static common.GameConst.RedisKey.CrossArenaCreateTableLock;
import static common.GameConst.RedisKey.CrossArenaTableState;
import static common.GameConst.RedisKey.CrossArenaTableStateEndTime;
import static common.GameConst.RedisKey.CrossArenaPlayerSerialFail;
import static common.GameConst.RedisKey.CrossArenaProtectCard;
import static protocol.CrossArena.CrossArenaState.FIGHT_VALUE;
import static protocol.CrossArena.CrossArenaState.IDLE_VALUE;
import static protocol.CrossArena.CrossArenaState.NONENOT_VALUE;
import static protocol.CrossArena.CrossArenaState.READY_VALUE;
import static protocol.CrossArena.CrossArenaState.WAIT_VALUE;
import static protocol.MessageId.MsgIdEnum.BS_GS_CrossArenaLtDel_VALUE;
import static util.JedisUtil.jedis;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cfg.CrossArenaRobotRule;
import cfg.CrossArenaRobotRuleObject;
import common.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import cfg.CrossArenaScene;
import cfg.CrossArenaSceneObject;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalTick;
import common.IdGenerator;
import common.load.ServerConfig;
import datatool.StringHelper;
import helper.ObjectUtil;
import hyzNet.GameServerTcpChannel;
import model.room.cache.RoomCache;
import model.warpServer.WarpServerManager;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import protocol.Battle;
import protocol.Battle.ExtendProperty;
import protocol.CrossArena.CrossArenaPlyCacheRAM;
import protocol.CrossArena.CrossArenaState;
import protocol.CrossArenaDB.RedisCrossArenaPlayer;
import protocol.CrossArenaDB.RedisCrossArenaTableDB;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.ServerTransfer;
import protocol.ServerTransfer.BS_GS_CrossArenaReadyFight;
import protocol.ServerTransfer.GS_BS_CrossArenaPos;
import protocol.ServerTransfer.ServerTypeEnum;
import server.event.EventManager;
import server.event.crossarena.CrossArenaCreateProtectLeiTTableEvent;
import util.JedisUtil;
import util.LogUtil;

/**
 * 中心服擂台赛逻辑类
 */
public class CrossArenaManager {

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

	private Set<String> cantMatchProtectRobot = Collections.synchronizedSet(new HashSet<>());

	private CrossArenaManager() {
	}

	public void init() {

	}

	public static Random random = new Random();

	/**
	 * 机器人ID
	 */
	private String AINAME = "rb";
	/**
	 * 缓存擂台房间ID<擂台ID,战斗房间ID>
	 */
	private final Map<Long, Integer> battleingRoom = new ConcurrentHashMap<Long, Integer>();

	private final Map<String, Integer> cacheInfoPS = new ConcurrentHashMap<>();
	private final Map<Integer, Map<String, CrossArenaPlyCacheRAM>> cacheInfo = new ConcurrentHashMap<>();

	private final Map<Integer, Map<String, CrossArenaPlyCacheRAM>> cacheOffInfo = new ConcurrentHashMap<>();

	/**
	 * ai随机时间间隔
	 */
	private final Map<Integer, Long> aiTime = new ConcurrentHashMap<>();

	public void synOffCache(int sid, List<CrossArenaPlyCacheRAM> req) {
		Map<String, CrossArenaPlyCacheRAM> temp = new ConcurrentHashMap<>();
		for (CrossArenaPlyCacheRAM ent : req) {
			temp.put(ent.getPid(), ent);
		}
		cacheOffInfo.put(sid, temp);
		ServerTransfer.BS_GS_CrossArenaCacheSyn.Builder msg10 = ServerTransfer.BS_GS_CrossArenaCacheSyn.newBuilder();
		msg10.setSynTime(System.currentTimeMillis());
		msg10.setOffline(2);
		int i = 0;
		for (Map.Entry<Integer, Map<String, CrossArenaPlyCacheRAM>> ent : cacheOffInfo.entrySet()) {
			if (i > 200) {
				break;
			}
			ServerTransfer.CrossArenaCacheSyn.Builder msg2 = ServerTransfer.CrossArenaCacheSyn.newBuilder();
			msg2.setSceneId(ent.getKey());
			msg2.putAllAllInfo(ent.getValue());
			msg10.addSynData(msg2);
			i++;
		}
		WarpServerManager.getInstance().sendMsgToGSAll(MessageId.MsgIdEnum.BS_GS_CrossArenaCacheSyn_VALUE, msg10);
	}

	/**
	 * @param gsChn
	 * @param req   缓存信息
	 */
	public void posMoveCache(GameServerTcpChannel gsChn, GS_BS_CrossArenaPos req) {
		int oldSceneId = cacheInfoPS.getOrDefault(req.getAllInfo().getPid(), 0);
		if (oldSceneId != req.getAllInfo().getSceneId()) {
			// 移除老的
			Map<String, CrossArenaPlyCacheRAM> temp = cacheInfo.computeIfAbsent(req.getAllInfo().getSceneId(), k -> new ConcurrentHashMap<String, CrossArenaPlyCacheRAM>());
			temp.remove(req.getAllInfo().getPid());
		}
		// 添加更新
		Map<String, CrossArenaPlyCacheRAM> temp = cacheInfo.computeIfAbsent(req.getAllInfo().getSceneId(), k -> new ConcurrentHashMap<String, CrossArenaPlyCacheRAM>());
		temp.put(req.getAllInfo().getPid(), req.getAllInfo());
		// 消息同步
		if (cacheInfoPS.size() < 300) {
			synCacheInfo();
		}
	}

	/**
	 * @param gsChn
	 * @param req   玩家退出缓存
	 */
	public void quitCache(GameServerTcpChannel gsChn, GS_BS_CrossArenaPos req) {
		int oldSceneId = cacheInfoPS.getOrDefault(req.getAllInfo().getPid(), 0);
		if (oldSceneId == req.getAllInfo().getSceneId()) {
			// 移除老的
			Map<String, CrossArenaPlyCacheRAM> temp = cacheInfo.computeIfAbsent(oldSceneId, k -> new ConcurrentHashMap<String, CrossArenaPlyCacheRAM>());
			temp.remove(req.getAllInfo().getPid());
		}
		// 消息同步
		if (cacheInfoPS.size() < 300) {
			synCacheInfo();
		}
	}

	/**
	 * 给所有逻辑服同步数据
	 */
	public void synCacheInfo() {
		ServerTransfer.BS_GS_CrossArenaCacheSyn.Builder msg10 = ServerTransfer.BS_GS_CrossArenaCacheSyn.newBuilder();
		msg10.setSynTime(System.currentTimeMillis());
		for (Map.Entry<Integer, Map<String, CrossArenaPlyCacheRAM>> ent : cacheInfo.entrySet()) {
			ServerTransfer.CrossArenaCacheSyn.Builder msg2 = ServerTransfer.CrossArenaCacheSyn.newBuilder();
			msg2.setSceneId(ent.getKey());
			msg2.putAllAllInfo(ent.getValue());
			msg10.addSynData(msg2);
		}
		WarpServerManager.getInstance().sendMsgToGSAll(MessageId.MsgIdEnum.BS_GS_CrossArenaCacheSyn_VALUE, msg10);
	}

	/**
	 * @param gsChn
	 * @param playerIdx
	 * @param tableId   玩家退出擂台，下擂或者退出队列
	 */
	public void quitTable(GameServerTcpChannel gsChn, String playerIdx, int tableId) {
		ServerTransfer.BS_GS_CrossArenaQuit.Builder msg = ServerTransfer.BS_GS_CrossArenaQuit.newBuilder();
		msg.setRetCode(RetCodeId.RetCodeEnum.RCE_Success);
		msg.setPlayerId(playerIdx);
		try {
			// 判断该擂台是否工作中
			String ltaddr = findTableWorkServer(tableId);
			int ltSvrIndex = StringHelper.stringToInt(ltaddr, 0);
			if (ltSvrIndex != ServerConfig.getInstance().getServer()) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaQuit_VALUE, msg);
				return;
			}

			// 数据转换为可操作数据
			String tableLockKey = RedisKey.CrossArenaTableLock + "" + tableId;
			if (!JedisUtil.lockRedisKey(tableLockKey, 3000l)) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaQuit_VALUE, msg);
				return;
			}
			// 获取擂台数据
			byte[] tableDBByte = jedis.get(createRedisKeyLT(tableId).getBytes());
			if (null == tableDBByte) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaQuit_VALUE, msg);
				return;
			}
			RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(tableDBByte);
			RedisCrossArenaTableDB.Builder newTableDB = null;
			if (null != tableDB.getDefPlayer() && Objects.equals(tableDB.getDefPlayer().getPlayerId(), playerIdx)) {
				// 玩家是擂主,只有等待状态可以退出擂主
				if (tableDB.getState() != WAIT_VALUE) {
					JedisUtil.unlockRedisKey(tableLockKey);
					msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_NO_WAIT);
					gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaQuit_VALUE, msg);
					return;
				}
				// 擂主下台
				newTableDB = tbRedisQuitDef(tableDB);
			} else if (null != tableDB.getAttPlayer() && Objects.equals(tableDB.getAttPlayer().getPlayerId(), playerIdx)) {
				// 玩家是攻擂者，且已经开始战斗
				if (tableDB.getState() != CrossArenaState.READY_VALUE) {
					JedisUtil.unlockRedisKey(tableLockKey);
					msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_Fighting);
					gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaQuit_VALUE, msg);
					return;
				}
				newTableDB = tbRedisQuitAtt(tableDB);
			} else {
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
					JedisUtil.unlockRedisKey(tableLockKey);
					gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaQuit_VALUE, msg);
					return;
				}
				newTableDB = tableDB.toBuilder();
				newTableDB.clearDui();
				newTableDB.addAllDui(newDB);
				updataPlyTableIdRedis(playerIdx, "");
			}
			if (null != newTableDB) {
				saveLtDataToCache(newTableDB);
				// 通知全服刷新该擂台数据
				refTableInfoToAllServer(newTableDB.build());
			}
			JedisUtil.unlockRedisKey(tableLockKey);
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaQuit_VALUE, msg);
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaQuit_VALUE, msg);
		}
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

	private int findTableNowState(int leitaiId) {
		String hget = jedis.hget(CrossArenaTableState, leitaiId + "");
		return hget == null ? 0 : Integer.parseInt(hget);
	}

	private String findTableWorkServer(int tableId) {
		return jedis.hget(getTableServerKey(tableId), tableId + "");
	}

	private Map<Integer,String> tableServerKeyMap  = new ConcurrentHashMap();

	public String getTableServerKey(int tableId) {
		int sceneId = getSceneIdByTableId(tableId);
		return tableServerKeyMap.computeIfAbsent(tableId, a -> CrossArenaBSSid + sceneId);
	}

	/**
	 * 刷新缓存玩家所在擂台ID得信息
	 *
	 * @param playerIdx
	 * @param vue
	 */
	public void updataPlyTableIdRedis(String playerIdx, String vue) {
		// 清除玩家位置信息
		if (!isRobot(playerIdx) && !StringHelper.isNull(playerIdx)) {
			jedis.hset(GameConst.RedisKey.CrossArenaPlayerTable, playerIdx, vue);
		}
	}

	private boolean isRobot(String playerIdx) {
		return playerIdx.contains(AINAME);
	}

	/**
	 * @param gsChn
	 * @param playerIdx
	 * @param tableId
	 * @param isWin
	 * @param fromSvrIndex    竞猜擂台
	 */
	public void guess(GameServerTcpChannel gsChn, String playerIdx, int tableId, int isWin, int fromSvrIndex) {
		ServerTransfer.BS_GS_CrossArenaGuess.Builder msg = ServerTransfer.BS_GS_CrossArenaGuess.newBuilder();
		msg.setRetCode(RetCodeId.RetCodeEnum.RCE_Success);
		msg.setPlayerId(playerIdx);
		try {
			// 判断该擂台是否工作中
			String ltaddr = findTableWorkServer(tableId);
			int currSid = ServerConfig.getInstance().getServer();
			if (!Objects.equals("" + currSid, ltaddr)) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaGuess_VALUE, msg);
				return;
			}
			// 获取擂台数据
			byte[] tableDBByte = jedis.get(createRedisKeyLT(tableId).getBytes());
			if (null == tableDBByte) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaGuess_VALUE, msg);
				return;
			}
			// 数据转换为可操作数据
			RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(tableDBByte);
			if (tableDB.containsGuessAttSvrData(playerIdx) || tableDB.containsGuessDefSvrData(playerIdx)) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_RPEI_GUESS);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaGuess_VALUE, msg);
				return;
			}
			if (null == tableDB.getDefPlayer() || null == tableDB.getAttPlayer()) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_NO_Fighting);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaGuess_VALUE, msg);
				return;
			}
			if (tableDB.getState() != CrossArenaState.READY_VALUE && tableDB.getState() != CrossArenaState.FIGHT_VALUE) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_NO_Fighting);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaGuess_VALUE, msg);
				return;
			}
			RedisCrossArenaTableDB.Builder newData = tableDB.toBuilder();
			RedisCrossArenaPlayer playerIdxBe = null;
			if (isWin == 2) {
				newData.putGuessDefSvrData(playerIdx, StringHelper.IntTostring(fromSvrIndex, "0"));
				playerIdxBe = newData.getDefPlayer();
			} else {
				newData.putGuessAttSvrData(playerIdx, StringHelper.IntTostring(fromSvrIndex, "0"));
				playerIdxBe = newData.getAttPlayer();
			}
			// 判断通过更新数据
			saveLtDataToCache( newData);
			refTableInfoToAllServer(newData.build());
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaGuess_VALUE, msg);

			if (playerIdxBe.getIsAI() <= 0) {
				ServerTransfer.BS_GS_CrossArenaGuessBe.Builder msgBe = ServerTransfer.BS_GS_CrossArenaGuessBe.newBuilder();
				msgBe.setPlayerId(playerIdxBe.getPlayerId());
				int svrIndex = playerIdxBe.getSvrIndex();
				if (svrIndex <= 0) { // 兼容代码
					svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(playerIdxBe.getFormIpPort());
				}
				WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, svrIndex, MessageId.MsgIdEnum.BS_GS_CrossArenaGuessBe_VALUE, msgBe);
			}
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaGuess_VALUE, msg);
		}
	}

	/**
	 * @param gsChn
	 * @param parm  退出匹配队列
	 */
	public void quitQue(GameServerTcpChannel gsChn, ServerTransfer.GS_BS_CrossArenaAtt parm) {
		ServerTransfer.BS_GS_CrossArenaAtt.Builder msg = ServerTransfer.BS_GS_CrossArenaAtt.newBuilder();
		msg.setRetCode(RetCodeId.RetCodeEnum.RCE_Success);
		msg.setPlayerId(parm.getAttInfo().getPlayerId());
		msg.setOper(parm.getOper());
		try {
			delQueDis(parm.getLeitaiId(), parm.getAttInfo().getPlayerId());
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
		}
	}

	/**
	 * @param gsChn
	 * @param parm  玩家加入队列
	 */
	public void jionQue(GameServerTcpChannel gsChn, ServerTransfer.GS_BS_CrossArenaAtt parm) {
		ServerTransfer.BS_GS_CrossArenaAtt.Builder msg = ServerTransfer.BS_GS_CrossArenaAtt.newBuilder();
		msg.setRetCode(RetCodeId.RetCodeEnum.RCE_Success);
		msg.setPlayerId(parm.getAttInfo().getPlayerId());
		msg.setOper(parm.getOper());
		try {
			// 生成一个挑战数据
			RedisCrossArenaPlayer.Builder attPlayer = parm.getAttInfo().toBuilder();
			attPlayer.setJionTime(System.currentTimeMillis());
			String ppos = jedis.hget(GameConst.RedisKey.CrossArenaPlayerTable, parm.getAttInfo().getPlayerId());
			if (!StringHelper.isNull(ppos)) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_ATTABLE);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
				return;
			}
			if (atQueRedis(parm.getLeitaiId(), parm.getAttInfo().getPlayerId())) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_ATQUE);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
				return;
			}
			// 成功加入该队列
			addQueRedis(parm.getLeitaiId(), parm.getAttInfo().getPlayerId(),false);
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
		}
	}

	/**
	 * @param gsChn
	 * @param parm  攻击挑战擂台
	 */
	public void attTable(GameServerTcpChannel gsChn, ServerTransfer.GS_BS_CrossArenaAtt parm) {
		ServerTransfer.BS_GS_CrossArenaAtt.Builder msg = ServerTransfer.BS_GS_CrossArenaAtt.newBuilder();
		msg.setRetCode(RetCodeId.RetCodeEnum.RCE_Success);
		msg.setPlayerId(parm.getAttInfo().getPlayerId());
		msg.setOper(parm.getOper());
		int tableId = parm.getLeitaiId();
		int sceneId = getSceneIdByTableId(tableId);
		CrossArenaSceneObject casoCfg = CrossArenaScene.getById(sceneId);
		if (null == casoCfg) {
			msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
			return;
		}
		try {
			// 判断该擂台是否工作中
			String ltaddr = findTableWorkServer(tableId);
			int currSid = ServerConfig.getInstance().getServer();
			if (!Objects.equals("" + currSid, ltaddr)) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
				return;
			}
			String tableLockKey = RedisKey.CrossArenaTableLock + "" + tableId;
			if (!JedisUtil.lockRedisKey(tableLockKey, 1000l)) {
				return;
			}
			// 获取擂台数据
			byte[] tableDBByte = jedis.get(createRedisKeyLT(tableId).getBytes());
			if (null == tableDBByte) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
				JedisUtil.unlockRedisKey(tableLockKey);
				return;
			}
			// 数据转换为可操作数据
			RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(tableDBByte);
			if (null == tableDB) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_Fighting);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
				JedisUtil.unlockRedisKey(tableLockKey);
				return;
			}
			if (null != tableDB.getDefPlayer() && null != tableDB.getAttPlayer() && tableDB.getDuiCount() >= casoCfg.getQueuenum()) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_TableQueueMax);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
				JedisUtil.unlockRedisKey(tableLockKey);
				return;
			}
			if (atQueRedis(sceneId, parm.getAttInfo().getPlayerId())) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_CrossArena_ATQUE);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
				JedisUtil.unlockRedisKey(tableLockKey);
				return;
			}
			String ppos = jedis.hget(GameConst.RedisKey.CrossArenaPlayerTable, parm.getAttInfo().getPlayerId());
			if (!StringHelper.isNull(ppos)) {
				msg.setRetCode(RetCodeId.RetCodeEnum.RCE_MatchArenaLT_RPT);
				gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
				JedisUtil.unlockRedisKey(tableLockKey);
				return;
			}
			// 生成一个挑战数据
			RedisCrossArenaPlayer.Builder attPlayer = parm.getAttInfo().toBuilder();
			attPlayer.setJionTime(System.currentTimeMillis());
			// 判断通过可以挑战
			RedisCrossArenaTableDB.Builder newData = addTablePlayer(tableDB, attPlayer.build(), true);
			if (null != newData) {
				// 通知全服刷新该擂台数据
				saveLtDataToCache(newData);
				updataPlyTableIdRedis(attPlayer.getPlayerId(), "" + newData.getLeitaiId());
				refTableInfoToAllServer(newData.build());
			}
			JedisUtil.unlockRedisKey(tableLockKey);
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			msg.setRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError);
			gsChn.send(MessageId.MsgIdEnum.BS_GS_CrossArenaAtt_VALUE, msg);
		}
	}

	private RedisCrossArenaTableDB.Builder addTablePlayer(RedisCrossArenaTableDB tableDB, RedisCrossArenaPlayer attPlayer, boolean isAddQue) {
		RedisCrossArenaTableDB.Builder newData = null;
		if (tableDB.getState() == CrossArenaState.IDLE_VALUE) {
			// 等于空闲状态直接上擂主
			newData = upTableDef(tableDB, attPlayer);
		} else if (tableDB.getState() == WAIT_VALUE) {
			// 等于状态直接攻擂
			newData = upTableAtt(tableDB, attPlayer);
		} else {
			// 加入排队队列
			if (isAddQue) {
				newData = upTableQue(tableDB, attPlayer);
			}
		}
		return newData;
	}

	/**
	 * @param tableDB 清楚擂台
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder clearTable(RedisCrossArenaTableDB tableDB) {
		RedisCrossArenaTableDB.Builder newData = tableDB.toBuilder();
		newData.setState(CrossArenaState.IDLE_VALUE);
		newData.clearDefPlayer();
		newData.clearAttPlayer();
		newData.clearGuessAttSvrData();
		newData.clearGuessDefSvrData();
		newData.clearDui();
		newData.clearBattleId();
		newData.clearDefTime();
		newData.clearDefWinNum();
		newData.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newData.addAllReadyState(readyList);
		return newData;
	}

	/**
	 * @param tableDB
	 * @param attPlayer 玩家上擂 成为擂主
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder upTableDef(RedisCrossArenaTableDB tableDB, RedisCrossArenaPlayer attPlayer) {
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		RedisCrossArenaTableDB.Builder newData = tableDB.toBuilder();
		newData.setDefPlayer(attPlayer);
		newData.setState(WAIT_VALUE);
		newData.setStateEndTime(GlobalTick.getInstance().getCurrentTime() + casoCfg.getRevoketime() * 1000L);
		newData.clearAttPlayer();
		newData.clearGuessAttSvrData();
		newData.clearGuessDefSvrData();
		newData.clearDui();
		newData.clearBattleId();
		newData.clearDefTime();
		newData.clearDefWinNum();
		newData.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newData.addAllReadyState(readyList);
		return newData;
	}

	/**
	 * @param tableDB
	 * @param attPlayer 玩家上擂 成为擂主
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder upTableAtt(RedisCrossArenaTableDB tableDB, RedisCrossArenaPlayer attPlayer) {
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		RedisCrossArenaTableDB.Builder newData = tableDB.toBuilder();
		newData.setAttPlayer(attPlayer);
		newData.setState(CrossArenaState.READY_VALUE);
		newData.setStateEndTime(getReadyTime(newData,casoCfg));
		newData.clearGuessAttSvrData();
		newData.clearGuessDefSvrData();
		newData.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newData.addAllReadyState(readyList);
		noticeReadyFight(newData.build());
		return newData;
	}

	/**
	 * @param tableDB
	 * @param attPlayer 加入排队队列
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder upTableQue(RedisCrossArenaTableDB tableDB, RedisCrossArenaPlayer attPlayer) {
		RedisCrossArenaTableDB.Builder newData = tableDB.toBuilder();
		newData.addDui(attPlayer);
		return newData;
	}

	/**
	 * @param camp
	 * @param playerInfo
	 * @return 构建跨服PVP战斗数据
	 */
	public ServerTransfer.PvpBattlePlayerInfo buildPvpPlayerInfo(int camp, RedisCrossArenaPlayer playerInfo) {
		if (playerInfo == null || null == playerInfo.getTeamInfo()) {
			LogUtil.error("model.matchArena.MatchArenaPlayer.buildPvpPlayerInfo, player info is null");
			return null;
		}
		ServerTransfer.PvpBattlePlayerInfo.Builder resultBuilder = ServerTransfer.PvpBattlePlayerInfo.newBuilder();
		resultBuilder.setPlayerInfo(playerInfo.getTeamInfo().getPlayerInfo());
		int svrIndex = playerInfo.getSvrIndex();
		if (svrIndex <= 0) { // 兼容代码
			svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(playerInfo.getFormIpPort());
		}
		resultBuilder.setFromSvrIndex(svrIndex);
		resultBuilder.setCamp(camp);
		resultBuilder.setIsAuto(playerInfo.getTeamInfo().getIsAuto());
		resultBuilder.setPlayerExtData(playerInfo.getTeamInfo().getPlayerExtData());
		resultBuilder.addAllPetList(playerInfo.getTeamInfo().getPetListList());
		resultBuilder.addAllPlayerSkillIdList(playerInfo.getTeamInfo().getPlayerSkillIdListList());
		if (playerInfo.getPlayerId().contains(AINAME)) {
			resultBuilder.setIsAI(1);
		}
		for (ExtendProperty ent : playerInfo.getExtendPropList()) {
			resultBuilder.addExtendProp(ent.toBuilder().setCamp(camp).build());
		}
		return resultBuilder.build();
	}

	public CrossArenaSceneObject getSceneCfg(int tableId) {
		CrossArenaSceneObject casoCfg = CrossArenaScene.getById(getSceneIdByTableId(tableId));
		return casoCfg;
	}

	/**
	 * @param tableDB
	 * @param winCamp 战斗结束
	 */
	public void settleMatchTableAi(RedisCrossArenaTableDB tableDB, int winCamp) {
		if (tableDB.getType() == TempLeiT && tableDB.getDuiCount() <= 0) {
			LogUtil.info("after settleMatchTableAi ,del temp lt " + tableDB.getLeitaiId());
			settleMatchTableRemove(tableDB);
			return;
		}
		//TODO 战斗结束
		// 判断通过可以挑战
		// 处理竞猜
		ServerTransfer.BS_GS_CrossArenaGuessResult.Builder msg2 = ServerTransfer.BS_GS_CrossArenaGuessResult.newBuilder();
		msg2.setLeitaiId(tableDB.getLeitaiId());
		String addQueId = "";
		RedisCrossArenaTableDB.Builder newData = null;
		if (winCamp == 1) {
			// 统计竞猜信息
			msg2.addAllWinIds(tableDB.getGuessAttSvrDataMap().keySet());
			msg2.addAllFailsIds(tableDB.getGuessDefSvrDataMap().keySet());
			// 攻击方胜利，守擂者移除擂主(攻擂成功自动成为擂主)
			newData = tbRedisBatWinAtt(tableDB);
			// 守擂者失败移除擂主数据，移除守擂者信息
			addQueId = tableDB.getDefPlayer().getPlayerId();
		} else {
			// 统计竞猜信息
			msg2.addAllWinIds(tableDB.getGuessDefSvrDataMap().keySet());
			msg2.addAllFailsIds(tableDB.getGuessAttSvrDataMap().keySet());
			// 防守方胜利
			newData = tbRedisBatWinDef(tableDB);
			addQueId = tableDB.getAttPlayer().getPlayerId();
		}
		if (null != newData) {
			addQueRedis(getSceneIdByTableId(tableDB.getLeitaiId()), addQueId,false);
			// 判断在擂台上得是否离线,是否可以继续在擂台上
			if (newData.getDefWinNum() >= 10) {
				newData = clearTable(tableDB);
			}
		} else {
			newData = clearTable(tableDB);
		}
		saveLtDataToCache(newData);
		// 通知玩家战斗结果
		// 处理竞猜
		Set<Integer> fromServerSet = new HashSet<Integer>();
		// 兼容代码
		int svrIndex;
		for (String addr : newData.getGuessDefSvrDataMap().values()) {
			svrIndex = StringHelper.stringToInt(addr, 0);
			if (svrIndex <= 0) {
				svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(addr);
			}
			if (svrIndex > 0) {
				fromServerSet.add(svrIndex);
			}
		}
		for (String addr : newData.getGuessAttSvrDataMap().values()) {
			svrIndex = StringHelper.stringToInt(addr, 0);
			if (svrIndex <= 0) {
				svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(addr);
			}
			if (svrIndex > 0) {
				fromServerSet.add(svrIndex);
			}
		}
		for (Integer fromSvrIndex : fromServerSet) {
			WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, fromSvrIndex, MessageId.MsgIdEnum.BS_GS_CrossArenaGuessResult_VALUE, msg2);
		}
		// 通知全服刷新该擂台数据
		if (newData != null) {
			refTableInfoToAllServer(newData.build());
		}
	}

	private void settleMatchTableRemove(RedisCrossArenaTableDB tableDB) {
		addQueRedis(getSceneIdByTableId(tableDB.getLeitaiId()), tableDB.getAttPlayer().getPlayerId(), false);
		addQueRedis(getSceneIdByTableId(tableDB.getLeitaiId()), tableDB.getDefPlayer().getPlayerId(), false);
		delTable(tableDB.getLeitaiId());
	}

	public int settleMatchWinCot(long battleId, int winCamp) {
		int tableId = battleingRoom.getOrDefault(battleId, 0);
		if (tableId <= 0) {
			return 0;
		}
		try {
			// 判断该擂台是否工作中
			String ltaddr = findTableWorkServer(tableId);
			int currSid = ServerConfig.getInstance().getServer();
			if (!Objects.equals("" + currSid, ltaddr)) {
				return 0;
			}
			// 获取擂台数据
			byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
			if (null == oneLeiTaiDB) {
				return 0;
			}
			// 数据转换为可操作数据
			RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
			int defNum = 0;

			if (winCamp == 1) {
				if (!isRobot(tableDB.getAttPlayer().getPlayerId()) && isTenWin()) {
					Integer protectWinNum = getProtectWinNum(tableDB.getAttPlayer().getPlayerId());
					if (protectWinNum != null) {
						return protectWinNum + 1;
					}
				}
				defNum = tableDB.getAttPlayer().getDefNum() + 1;
			} else {
				defNum = tableDB.getDefPlayer().getDefNum() + 1;
			}

			return defNum;
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			return 0;
		}
	}

	/**
	 * @param battleId
	 * @param winCamp  战斗结束
	 */
	public void settleMatchTable(long battleId, int winCamp) {
		// 根据缓存查找擂台
		int tableId = battleingRoom.getOrDefault(battleId, 0);
		if (tableId <= 0) {
			return;
		}
		CrossArenaSceneObject casoCfg = getSceneCfg(tableId);
		if (null == casoCfg) {
			return;
		}
		boolean needRemoveLt =false;
		try {
			// 判断该擂台是否工作中
			String ltaddr = findTableWorkServer(tableId);
			int currSid = ServerConfig.getInstance().getServer();
			if (!Objects.equals("" + currSid, ltaddr)) {
				return;
			}
			String tableLockKey = RedisKey.CrossArenaTableLock + "" + tableId;
			if (!JedisUtil.lockRedisKey(tableLockKey, 3000l)) {
				return;
			}
			// 获取擂台数据
			byte[] oneLeiTaiDB = jedis.get(createRedisKeyLT(tableId).getBytes());
			if (null == oneLeiTaiDB) {
				JedisUtil.unlockRedisKey(tableLockKey);
				return;
			}
			// 数据转换为可操作数据
			RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(oneLeiTaiDB);
			// 判断通过可以挑战
			// 处理竞猜
			ServerTransfer.BS_GS_CrossArenaGuessResult.Builder msg2 = ServerTransfer.BS_GS_CrossArenaGuessResult.newBuilder();
			msg2.setLeitaiId(tableId);
			String addQueId = "";
			int otherWinNum = 0;
			int defWinNum = 0;
			String attackPlayer = tableDB.getAttPlayer().getPlayerId();
			String defendPlayer = tableDB.getDefPlayer().getPlayerId();
			RedisCrossArenaTableDB.Builder newData;
			boolean isAttackRobot = isRobot(attackPlayer);
			if (winCamp == 1) {
				// 统计竞猜信息
				msg2.addAllWinIds(tableDB.getGuessAttSvrDataMap().keySet());
				msg2.addAllFailsIds(tableDB.getGuessDefSvrDataMap().keySet());
				otherWinNum = tableDB.getDefPlayer().getDefNum();
				// 攻击方胜利，守擂者移除擂主(攻擂成功自动成为擂主)
				newData = tbRedisBatWinAtt(tableDB);
				// 守擂者失败移除擂主数据，移除守擂者信息
				addQueId = tableDB.getDefPlayer().getPlayerId();
				//处理连败/胜次数
				if (!isRobot(defendPlayer)) {
					jedis.hincrByFloat(CrossArenaPlayerSerialFail, defendPlayer, 1);
				}
				if (!isRobot(attackPlayer)) {
					jedis.hdel(CrossArenaPlayerSerialFail, attackPlayer);
				}

			} else {
				// 统计竞猜信息
				msg2.addAllWinIds(tableDB.getGuessDefSvrDataMap().keySet());
				msg2.addAllFailsIds(tableDB.getGuessAttSvrDataMap().keySet());
				otherWinNum = tableDB.getAttPlayer().getDefNum();
				// 防守方胜利
				newData = tbRedisBatWinDef(tableDB);
				addQueId = tableDB.getAttPlayer().getPlayerId();
				//处理连败/胜次数
				if (!isRobot(attackPlayer)) {
					jedis.hincrByFloat(CrossArenaPlayerSerialFail, attackPlayer, 1);
				}

				if (!isRobot(defendPlayer)) {
					jedis.hdel(CrossArenaPlayerSerialFail, defendPlayer);
				}
			}


			needRemoveLt = newData != null && newData.getType() == TempLeiT && isAttackRobot && winCamp == 1 && newData.getDuiCount() <= 0;
			if (winCamp == 1 && isProtectRobot(attackPlayer)) {
				putBackProtectRobot(getSceneIdByTableId(tableDB.getLeitaiId()), attackPlayer);
			}
			if (needRemoveLt) {
				delTable(tableId);
			}else if (null != newData) {
				defWinNum = newData.getDefWinNum();
				if (!addQueId.contains(AINAME)) {
					if (jedis.hexists(RedisKey.CrossArenaPlOnline, addQueId)) {
						addQueRedis(getSceneIdByTableId(tableId), addQueId,false);
					}
				} else {
					addQueRedis(getSceneIdByTableId(tableId), addQueId,false);
				}
				// 判断在擂台上得是否离线,是否可以继续在擂台上
				// 玩家在10连胜里面
				if (jedis.sismember(getCrossArenaTenPlayerKey(getSceneIdByTableId(tableId)), newData.getDefPlayer().getPlayerId())) {
					String defpid = newData.getDefPlayer().getPlayerId();
					newData = clearTable(newData.build());
					updataPlyTableIdRedis(defpid, "");
				} else {
					// 机器人不再10连里，需要单独处理
					String defpid = newData.getDefPlayer().getPlayerId();
					if (!newData.getDefPlayer().getPlayerId().contains(AINAME)) {
						// 不是机器人需要判断玩家是否在线
						if (!jedis.hexists(RedisKey.CrossArenaPlOnline, newData.getDefPlayer().getPlayerId())) {
							newData = clearTable(newData.build());
							updataPlyTableIdRedis(defpid, "");
						}
					} else {
						// 处理机器人
						if (newData.getDefWinNum() >= 10) {
							newData = clearTable(newData.build());
							updataPlyTableIdRedis(defpid, "");
						}
					}
				}
			} else {
				defWinNum = tableDB.getDefWinNum();
				newData = clearTable(tableDB);
				updataPlyTableIdRedis(tableDB.getAttPlayer().getPlayerId(), "");
				updataPlyTableIdRedis(tableDB.getDefPlayer().getPlayerId(), "");
			}
			if (!needRemoveLt) {
				saveLtDataToCache(newData);
			}
			JedisUtil.unlockRedisKey(tableLockKey);
			// 通知玩家战斗结果
			ServerTransfer.BS_GS_CrossArenaWinResult.Builder settleMsg = ServerTransfer.BS_GS_CrossArenaWinResult.newBuilder();

			//移除玩家特权保护卡
			removeWinProtectCard(attackPlayer,defendPlayer);

			settleMsg.setPlayerIdDef(tableDB.getDefPlayer().getPlayerId());
			settleMsg.setPlayerIdAtt(tableDB.getAttPlayer().getPlayerId());
			settleMsg.setIsWin(winCamp == 1 ? 1 : 2);
			settleMsg.setWinNumDef(defWinNum);
			settleMsg.setWinNumOther(otherWinNum);
			long defTime = System.currentTimeMillis() - tableDB.getDefTime();
			if (defTime > 2100000000L) {
				defTime = 0;
			}
			int defTimeInt = (int) (defTime / 1000);
			settleMsg.setDefTime(defTimeInt);
			int defSvrIndex = tableDB.getDefPlayer().getSvrIndex();
			if (defSvrIndex <= 0) { // 兼容代码
				defSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(tableDB.getDefPlayer().getFormIpPort());
			}
			int attSvrIndex = tableDB.getDefPlayer().getSvrIndex();
			if (attSvrIndex <= 0) { // 兼容代码
				attSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(tableDB.getAttPlayer().getFormIpPort());
			}
			WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, defSvrIndex, MessageId.MsgIdEnum.BS_GS_CrossArenaWinResult_VALUE, settleMsg);
			if (defSvrIndex != attSvrIndex) {
				WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, attSvrIndex, MessageId.MsgIdEnum.BS_GS_CrossArenaWinResult_VALUE, settleMsg);
			}
			// 处理竞猜
			Set<Integer> fromServerSet = new HashSet<>();
			int svrIndex;
			for (String addr : newData.getGuessDefSvrDataMap().values()) {
				svrIndex = StringHelper.stringToInt(addr, 0);
				if (svrIndex <= 0) {
					svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(addr);
				}
				if (svrIndex > 0) {
					fromServerSet.add(svrIndex);
				}
			}
			for (String addr : newData.getGuessAttSvrDataMap().values()) {
				svrIndex = StringHelper.stringToInt(addr, 0);
				if (svrIndex <= 0) {
					svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(addr);
				}
				if (svrIndex > 0) {
					fromServerSet.add(svrIndex);
				}
			}
			for (Integer fromSvrIndex : fromServerSet) {
				WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, fromSvrIndex, MessageId.MsgIdEnum.BS_GS_CrossArenaGuessResult_VALUE, msg2);
			}
			battleingRoom.remove(battleId);

			// 通知全服刷新该擂台数据
			if (!needRemoveLt&&newData != null) {
				refTableInfoToAllServer(newData.build());
			}
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
		}
	}

	private void delTable(int tableId) {
		//临时擂台移除掉
		int scienceId = getSceneIdByTableId(tableId);
		jedis.hdel(getTableServerKey(tableId), "" + tableId);
		jedis.hset(RedisKey.CrossArenaTableNum, "" + scienceId, "" + findNowTableNum(scienceId));
		jedis.del(createRedisKeyLT(tableId));
		sendTableRemove(tableId);
	}

	private void sendTableRemove(int tableId) {
		ServerTransfer.BS_GS_CrossArenaLtDel.Builder msg = ServerTransfer.BS_GS_CrossArenaLtDel.newBuilder();
		msg.setTableId(tableId);
		WarpServerManager.getInstance().sendMsgToGSAll(BS_GS_CrossArenaLtDel_VALUE, msg);
	}

	private void putBackProtectRobot(int sceneId, String attackPlayer) {
		String protectRobotQueKey = getProtectRobotQueKey(sceneId, getdifficultByRobotIdx(attackPlayer));
		jedis.rpush(protectRobotQueKey, attackPlayer);
	}

	private int getdifficultByRobotIdx(String attackPlayer) {
		String[] split = attackPlayer.split("-");
		if (split.length > 2) {
			return Integer.parseInt(split[1]) % 1000;
		}
		return 0;
	}
	private static final int protectRobot = 1;

	private boolean isProtectRobot(String attackPlayer) {
		return attackPlayer.contains(AINAME + "-" + protectRobot);
	}

	private int findNowTableNum(int scienceId) {
		return findAllTableByScene(scienceId).size();
	}

	private String getCrossArenaTenPlayerKey(int scienceId) {
		return RedisKey.CrossArenaTenPlayer + scienceId;
	}

	private void removeWinProtectCard(String attackPlayer, String defendPlayer) {
		jedis.hdel(CrossArenaProtectCard, attackPlayer, defendPlayer);
	}

	/**
	 * 检查桌子变化
	 *
	 * @param sceneId
	 * @param tableNum
	 * @throws Exception
	 */
	public void checkTableChange(int sceneId, int tableNum) throws Exception {
		CrossArenaSceneObject casoCfg = CrossArenaScene.getById(sceneId);
		if (null == casoCfg) {
			return;
		}
		String queKey = RedisKey.CrossArenaQue + "" + sceneId;
		// 每个循环分配完成后，判断是否需要增加减擂台
		long queNum = jedis.llen(queKey);
		int numSY = (int) queNum - (casoCfg.getTablenum() * casoCfg.getTableaddlimit());
		if (numSY > 0) {
			// 计算出当前人数应该有多少桌子
			if (tableNum >= casoCfg.getTablenummax()) {
				return;
			}
			int needAddTar = casoCfg.getTablenum() + Math.round(numSY * 1F / casoCfg.getTableaddlimit());
			if (tableNum < needAddTar) {
				if (!JedisUtil.lockRedisKey(CrossArenaCreateTableLock +sceneId, 3000l)) {
					return;
				}
				// 桌子不够，开始增加擂台
				for (int ta = tableNum + 1; ta <= needAddTar; ta++) {
					// 初始化擂台数据
					Integer tableId = findCanUseTableId(sceneId);
					if (tableId == null) {
						return;
					}
					RedisCrossArenaTableDB.Builder leitaimsg = RedisCrossArenaTableDB.newBuilder();
					leitaimsg.setLeitaiId(tableId);
					leitaimsg.setState(CrossArenaState.IDLE_VALUE);
					leitaimsg.setDefTime(System.currentTimeMillis());
					leitaimsg.setDefWinNum(0);
					leitaimsg.setBattleId(0);
					List<Integer> readyFightList = new ArrayList<>();
					readyFightList.add(0);
					readyFightList.add(0);
					leitaimsg.addAllReadyState(readyFightList);
					String serverIndex = StringHelper.IntTostring(ServerConfig.getInstance().getServer(), "");
					saveNewTable(sceneId,tableId,leitaimsg,serverIndex);
				}
				JedisUtil.unlockRedisKey(CrossArenaCreateTableLock + sceneId);
			}
		} else {
			//todo 改为空闲减少擂台
			// 排队人数不足，判断是否减少擂台(只能重最后一个开始缩减)
			// 获取擂台数据
		/*	int tableId = cretaeTableIdId(sceneId, tableNum);
			byte[] tableDBByte = jedis.get(createRedisKeyLT(tableId).getBytes());
			if (null == tableDBByte) {
				if (tableNum > casoCfg.getTablenum()) {
					jedis.hdel(GameConst.RedisKey.CrossArenaBSSid, "" + tableId);
					jedis.hset(GameConst.RedisKey.CrossArenaTableNum, "" + sceneId, "" + findNowTableNum(sceneId));
				}
				return;
			}
			// 数据转换为可操作数据
			RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(tableDBByte);
			if (null == tableDB) {
				if (tableNum > casoCfg.getTablenum()) {
					jedis.hdel(GameConst.RedisKey.CrossArenaBSSid, "" + tableId);
					jedis.hset(GameConst.RedisKey.CrossArenaTableNum, "" + sceneId, "" + findNowTableNum(sceneId));
				}
				return;
			}
			if (tableDB.getState() != CrossArenaState.IDLE_VALUE) {
				return;
			}
			if (tableNum <= casoCfg.getTablenum()) {
				return;
			}
			String tableLockKey = RedisKey.CrossArenaTableLock + "" + tableDB.getLeitaiId();
			// 多客户端操作同擂台，需要给擂台上锁
			if (JedisUtil.lockRedisKey(tableLockKey, 3000l)) {
				jedis.hdel(createRedisKeyLT(tableId).getBytes());
				jedis.hdel(GameConst.RedisKey.CrossArenaBSSid, "" + tableId);
				jedis.hset(GameConst.RedisKey.CrossArenaTableNum, "" + sceneId, "" + findNowTableNum(sceneId));
				JedisUtil.unlockRedisKey(tableLockKey);
			}*/
		}
	}

	public void checkAiChange(CrossArenaSceneObject casoCfg) {
		String queKey = RedisKey.CrossArenaQue + "" + casoCfg.getId();
		String queLockKey = RedisKey.CrossArenaQueLock + "" + casoCfg.getId();
		int queNum = (int) jedis.llen(queKey);
		float rate = queNum * 1F / (queNum + casoCfg.getAi());
		float raterom = getRandomFloat(0, 1);
		if (!JedisUtil.lockRedisKey(queLockKey, 3000l)) {
			return;
		}
		if (raterom > rate) {
			if (queNum < casoCfg.getAi() * 3) {
				// 增加一个机器人
				String pid = AINAME + IdGenerator.getInstance().generateId();
				addQueRedis(casoCfg.getId(), pid,true);
			}
		} else {
			// 减少一个机器人
			if (queNum > casoCfg.getAi()) {
				List<String> ques = jedis.lrange(queKey, 0, -1);
				String delKey = "";
				for (String strid : ques) {
					if (strid.contains(AINAME)) {
						delKey = strid;
						break;
					}
				}
				if (!"".equals(delKey)) {
					// 删除
					delQueDis(casoCfg.getId(), delKey);
					removeAi(delKey);
				}
			}
		}
		JedisUtil.unlockRedisKey(queLockKey);
	}

	/**
	 * @param currTime
	 * @param sceneId  场景帧(主要执行判断场景是否增加擂台)(队列分配使用该逻辑)
	 */
	public void onTickScene(long currTime, int sceneId) {
		try {
			CrossArenaSceneObject casoCfg = CrossArenaScene.getById(sceneId);
			if (null == casoCfg) {
				return;
			}
			List<Integer> tableIds =  findAllTableByScene(sceneId);
			int tableNum = tableIds.size();
			// 遍历所有擂台，分配队列中玩家战斗
			RedisCrossArenaTableDB.Builder newtableDB = null;
			for (Integer tableId : tableIds) {
				// 获取擂台数据
				if (!canTableTickScience(tableId)) {
					continue;
				}
				// 桌子枷锁
				String tableLockKey = RedisKey.CrossArenaTableLock + "" + tableId;
				if (!JedisUtil.lockRedisKey(tableLockKey, 500l)) {
					continue;
				}
				byte[] tableDBByte = jedis.get(createRedisKeyLT(tableId).getBytes());
				if (null == tableDBByte) {
					JedisUtil.unlockRedisKey(tableLockKey);
					continue;
				}
				RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(tableDBByte);
				if (tableDB.getDuiCount() > 0) {
					JedisUtil.unlockRedisKey(tableLockKey);
					continue;
				}
				newtableDB = null;
				if (tableDB.getState() == CrossArenaState.IDLE_VALUE) {
					// 空闲状态时，到大队列中拉取2个玩家
					newtableDB = queBigJionTableTwo(tableDB);
				} else if (tableDB.getState() == WAIT_VALUE) {
					// 等待状态时，到大队列中拉取1个玩家
					newtableDB = queBigJionTable(tableDB);
				}
				JedisUtil.unlockRedisKey(tableLockKey);
				if (newtableDB != null) {
					refTableInfoToAllServer(newtableDB.build());
				}
			}
			checkTableChange(sceneId, tableNum);
			if (currTime > aiTime.getOrDefault(sceneId, 0L)) {
				aiTime.put(sceneId, currTime + 10000L);
				checkAiChange(casoCfg);
			}
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
		}
	}

	private boolean canTableTickScience(int tableId) {
		int tableNowState = findTableNowState(tableId);
		return tableNowState == IDLE_VALUE || tableNowState == WAIT_VALUE ||tableNowState ==NONENOT_VALUE;
	}

	private void putTableNextCanTickTime(int tableId, long stateEndTime) {
		jedis.hset(CrossArenaTableStateEndTime, tableId+"", stateEndTime+"");
	}

	private long findTableNextStateTime(int tableId) {
		String hget = jedis.hget(CrossArenaTableStateEndTime, tableId + "");
		if (hget==null){
			return 0;
		}
		return Long.parseLong(hget);
	}

	private List<Integer> findAllTableByScene(int sceneId) {
		Set<String> allTables = jedis.hkeys(CrossArenaBSSid + sceneId);
		List<Integer> tables = new ArrayList<>();
		int tableId;
		for (String table : allTables) {
			tableId = Integer.parseInt(table);
			tables.add(tableId);
		}
		return tables;
	}

	/**
	 * @param currTime
	 * @param tableId  擂台帧(只分配自身队列)
	 */
	public void onTick(long currTime, int tableId) {
		try {
			String openTime = jedis.get(RedisKey.CrossArenaTime);
			if (StringHelper.isNull(openTime) || Long.parseLong(openTime) <= 0) {
				// 活动关闭未开启
				return;
			}
			if (!canTableTick(tableId)) {
				return;
			}
			tableLastTickTime.put(tableId, GlobalTick.getInstance().getCurrentTime());
			// 操作擂台数据得时候先给该擂台上锁
			String tableLockKey = RedisKey.CrossArenaTableLock + "" + tableId;
			if (!JedisUtil.lockRedisKey(tableLockKey, 3000l)) {
				return;
			}
			// 获取擂台数据
			byte[] tableDBByte = jedis.get(createRedisKeyLT(tableId).getBytes());
			if (null == tableDBByte) {
				delTable(tableId);
				JedisUtil.unlockRedisKey(tableLockKey);
				return;
			}
			// 数据转换为可操作数据
			RedisCrossArenaTableDB tableDB = RedisCrossArenaTableDB.parseFrom(tableDBByte);
			RedisCrossArenaTableDB.Builder newtableDB = null;
			if (tableDB.getState() == CrossArenaState.IDLE_VALUE) {
				// 空闲状态时，擂台帧逻辑不处理
			} else if (tableDB.getState() == WAIT_VALUE) {
				CrossArenaSceneObject casoCfg = getSceneCfg(tableId);
				if (null == casoCfg) {
					return;
				}
				// 攻擂者等待战斗，需要安排玩家与之战斗
				newtableDB = tbRedisQueJionAttTable(tableDB);
				if (tableDB.getLeitaiId() > casoCfg.getTablenum() && currTime > tableDB.getStateEndTime()) {
					// 回收擂台(等待超时，回收擂台)
					newtableDB = tbRedisRecoveryTable(tableDB);
				}
			} else if (tableDB.getState() == CrossArenaState.READY_VALUE) {
				if (canStableFight(currTime, tableDB)) {
					// 发起战斗
					newtableDB = tbRedisStartBattle(tableDB);
					if (null == newtableDB) {
						// 战斗发起失败，容错处理
						newtableDB = tbRedisStartBattleFail(tableDB);
					}
				}
			} else if (tableDB.getState() == CrossArenaState.FIGHT_VALUE) {
				if (currTime >= tableDB.getStateEndTime()) {
					// 容错处理，战斗超时，300秒还没有收到战斗结果消息(超时默认守擂成功)
					if (tableDB.getIsAIBattle() > 0) {
						settleMatchTableAi(tableDB, tableDB.getAiWin());
					} else {
						battleingRoom.remove(tableDB.getBattleId());
						newtableDB = tbRedisFightOverTime(tableDB);
					}
				}
			}
			if (newtableDB != null) {
				putTableNextCanTickTime(tableDB.getLeitaiId(), tableDB.getStateEndTime());
				saveLtDataToCache(newtableDB);
				// 通知全服刷新该擂台数据
				refTableInfoToAllServer(newtableDB.build());
			}
			JedisUtil.unlockRedisKey(tableLockKey);
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
		}
	}

	private static final Map<Integer,Long> tableLastTickTime = new ConcurrentHashMap<>();

	/**
	 * 这个方法提前判断一下状态是否能执行,减小redis压力
	 * @param tableId
	 * @return
	 */
	private boolean canTableTick(int tableId) {
		int tableNowState = findTableNowState(tableId);
		switch (tableNowState) {
			case WAIT_VALUE:
			case NONENOT_VALUE:
				return true;
			case FIGHT_VALUE:
			case READY_VALUE:
				return findTableNextStateTime(tableId) < GlobalTick.getInstance().getCurrentTime() || doTimeOutProtect(tableId);
		}
		return false;
	}

	private boolean doTimeOutProtect(int tableId) {
		Long lastExecuteTime = tableLastTickTime.get(tableId);
		return lastExecuteTime == null || GlobalTick.getInstance().getCurrentTime() - lastExecuteTime > TimeUtil.MS_IN_A_MIN * 3;
	}

	private boolean canStableFight(long currTime, RedisCrossArenaTableDB tableDB) {
		boolean fight = false;
		List<Integer> readyStateList = tableDB.getReadyStateList();
		int readyCount = 0;
		for (Integer i : readyStateList) {
			if (i > 0) {
				readyCount++;
			}
		}
		if (readyCount >= 2) {
			fight = true;
		} else {
			if (currTime >= tableDB.getStateEndTime()) {
				fight = true;
			}
		}
		return fight;
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
		WarpServerManager.getInstance().sendMsgToGSAll(MessageId.MsgIdEnum.BS_GS_CrossArenaRefInfo_VALUE, msg10);
	}

	private boolean delQueDis(int sceneId, String playerIdx) {
		String queLockKey = RedisKey.CrossArenaQueLock + "" + sceneId;
		if (JedisUtil.lockRedisKey(queLockKey, 2000l)) {
			String queKey = RedisKey.CrossArenaQue + "" + sceneId;
			jedis.lrem(queKey, 0, playerIdx);
			JedisUtil.unlockRedisKey(queLockKey);
			return true;
		}
		return false;
	}

	private void addTenWin(String playerIdx,int scienceId) {
		if (!playerIdx.contains(AINAME)) {
			jedis.sadd(getCrossArenaTenPlayerKey(scienceId), playerIdx);
		}
	}

	private void addQueRedis(int sceneId, String playerIdx,boolean addAi) {
		// 10连胜后不准自动匹配
		if (jedis.sismember(getCrossArenaTenPlayerKey(sceneId), playerIdx)) {
			return;
		}
		if (isProtectRobot(playerIdx)) {
			putBackProtectRobot(sceneId, playerIdx);
			return;
		}
		String queKey = RedisKey.CrossArenaQue + "" + sceneId;
		if (addAi) {
			if (playerIdx.contains(AINAME)) {
				int gameServerCount = WarpServerManager.getInstance().getGameServerCount();
				if (gameServerCount > 0) {
					seedGSNeedNormalMatchAi(sceneId, playerIdx);
					jedis.rpush(queKey, playerIdx);
				}
			}
		} else {
			jedis.rpush(queKey, playerIdx);
		}
	}

	private boolean atQueRedis(int sceneId, String playerIdx) {
		String keyQue = GameConst.RedisKey.CrossArenaQue + "" + sceneId;
		List<String> list = jedis.lrange(keyQue, 0, -1);
		if (null != list && list.contains(playerIdx)) {
			return true;
		}
		return false;
	}

	/**
	 * @param tableDB 等待时间到开始战斗
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder tbRedisStartBattleFail(RedisCrossArenaTableDB tableDB) {
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
		updataPlyTableIdRedis(tableDB.getAttPlayer().getPlayerId(), "");
		newtableDB.clearAttPlayer();
		// 设置等待状态，设置最大等待时间
		newtableDB.setState(WAIT_VALUE);
		newtableDB.setStateEndTime(System.currentTimeMillis() + casoCfg.getRevoketime() * 1000L);
		// 判断是否重新加入队列
		return newtableDB;
	}

	/**
	 * @param tableDB 等待时间到开始战斗
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder tbRedisStartBattle(RedisCrossArenaTableDB tableDB) {
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
		RedisCrossArenaPlayer.Builder playerInfoDef = newtableDB.getDefPlayer().toBuilder();
		ServerTransfer.PvpBattlePlayerInfo firstPvpInfo = buildPvpPlayerInfo(1, newtableDB.getAttPlayer());
		ServerTransfer.PvpBattlePlayerInfo secondPvpInfo = buildPvpPlayerInfo(2, playerInfoDef.build());
		if (firstPvpInfo == null || secondPvpInfo == null) {
			// 特殊异常情况处理
			return null;
		}
		// 检查是否是机器人之间战斗
		if (tableDB.getDefPlayer().getIsAI() > 0 && tableDB.getAttPlayer().getIsAI() > 0) {
			newtableDB.setIsAIBattle(1);
			newtableDB.setLastBattleTime(System.currentTimeMillis());
			newtableDB.setState(CrossArenaState.FIGHT_VALUE);
			if (tableDB.getDefPlayer().getJionTime() <= 0 || tableDB.getAttPlayer().getJionTime() <= 0) {
				return null;
			}
			int[] res = CrossArenaTopManager.getInstance().computeAIWin(tableDB.getAttPlayer().getPower(), tableDB.getDefPlayer().getPower());
			if (res.length < 2) {
				return null;
			}
			newtableDB.setAiWin(res[0]);
			newtableDB.setBattleId(0);
			newtableDB.setStateEndTime(System.currentTimeMillis() + res[1] * 1000L);
			return newtableDB;
		}
		ServerTransfer.ApplyPvpBattleData.Builder applyPvpBuilder = ServerTransfer.ApplyPvpBattleData.newBuilder();
		applyPvpBuilder.setFightMakeId(casoCfg.getFightmakeid());
		applyPvpBuilder.setSubBattleType(Battle.BattleSubTypeEnum.BSTE_MatchArenaLeitai);
		applyPvpBuilder.addPlayerInfo(firstPvpInfo);
		applyPvpBuilder.addPlayerInfo(secondPvpInfo);

		int attSvrIndex = newtableDB.getAttPlayer().getSvrIndex();
		if (attSvrIndex <= 0) { // 兼容代码
			attSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(newtableDB.getAttPlayer().getFormIpPort());
		}
		// 创建战斗房间
		ServerTransfer.ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(applyPvpBuilder.build(), ServerTransfer.ServerTypeEnum.STE_GameServer, attSvrIndex);
		// 房间创建失败
		if (!replyBuilder.getResult()) {
			// 特殊异常情况处理
			return null;
		}
		newtableDB.setIsAIBattle(0);
		newtableDB.setLastBattleTime(System.currentTimeMillis());
		newtableDB.setBattleId(replyBuilder.getBattleId());
		newtableDB.setState(CrossArenaState.FIGHT_VALUE);
		newtableDB.setStateEndTime(System.currentTimeMillis() + 300000L);
		battleingRoom.put(replyBuilder.getBattleId(), newtableDB.getLeitaiId());
		// 创建PVP战斗创建成功消息
		ServerTransfer.BS_GS_ReplyPvpBattle.Builder builder = ServerTransfer.BS_GS_ReplyPvpBattle.newBuilder();
		builder.setReplyPvpBattleData(replyBuilder);

		// 通知战斗双方PVP战斗开始
		WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, attSvrIndex, MessageId.MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);

		int defSvrIndex = newtableDB.getDefPlayer().getSvrIndex();
		if (defSvrIndex <= 0) { // 兼容代码
			defSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(newtableDB.getDefPlayer().getFormIpPort());
		}
		if (attSvrIndex != defSvrIndex) {
			WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, defSvrIndex, MessageId.MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
		}
		return newtableDB;
	}

	/**
	 * @param tableDB 大队列拉取2个玩家战斗
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder queBigJionTableTwo(RedisCrossArenaTableDB tableDB) throws Exception {
		// 首先拉取总队列数据
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		int sceneId = casoCfg.getId();
		String queKey = RedisKey.CrossArenaQue + "" + sceneId;
		String queKeyLock = RedisKey.CrossArenaQueLock + "" + sceneId;
		// 队列加锁
		if (!JedisUtil.lockRedisKey(queKeyLock, 3000l)) {
			return null;
		}
		List<String> strings = popFromQue(queKey, 2,sceneId);
		if (strings.size() < 2) {
			JedisUtil.unlockRedisKey(queKeyLock);
			return null;
		}
		String py1 = strings.get(0);
		String py2 = strings.get(1);
		// 弹出一个玩家加入战斗
		RedisCrossArenaPlayer plInfoDB1 = getPlayerInfoRedis(py1, tableDB);
		RedisCrossArenaPlayer plInfoDB2 = getPlayerInfoRedis(py2, tableDB);
		if (null == plInfoDB1 || null == plInfoDB2) {
			JedisUtil.unlockRedisKey(queKeyLock);
			return null;
		}
		if (jedis.hexists(RedisKey.CrossArenaTempWinCot, py1)) {
			String def = jedis.hget(RedisKey.CrossArenaTempWinCot, py1);
			plInfoDB1 = plInfoDB1.toBuilder().setDefNum(NumberUtils.toInt(def, 0)).build();
		}
		if (jedis.hexists(RedisKey.CrossArenaTempWinCot, py2)) {
			String def = jedis.hget(RedisKey.CrossArenaTempWinCot, py2);
			plInfoDB2 = plInfoDB2.toBuilder().setDefNum(NumberUtils.toInt(def, 0)).build();
		}

		if (plInfoDB1.getIsAI() > 0) {
			plInfoDB1 = getPlayerInfoRedis(plInfoDB1.getPlayerId(), tableDB);
		}
		if (plInfoDB2.getIsAI() > 0) {
			plInfoDB2 = getPlayerInfoRedis(plInfoDB2.getPlayerId(), tableDB);
		}
		RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
		newtableDB.setState(CrossArenaState.READY_VALUE);
		newtableDB.setDefPlayer(plInfoDB1);
		newtableDB.setAttPlayer(plInfoDB2);
		newtableDB.setStateEndTime(getReadyTime(newtableDB, casoCfg));
		newtableDB.clearGuessAttSvrData();
		newtableDB.clearGuessDefSvrData();
		newtableDB.clearDui();
		newtableDB.clearBattleId();
		newtableDB.clearDefTime();
		newtableDB.clearDefWinNum();
		newtableDB.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newtableDB.addAllReadyState(readyList);
		saveLtDataToCache(newtableDB);
		updataPlyTableIdRedis(py1, "" + tableDB.getLeitaiId());
		updataPlyTableIdRedis(py2, "" + tableDB.getLeitaiId());
		noticeReadyFight(newtableDB.build());
		JedisUtil.unlockRedisKey(queKeyLock);

		return newtableDB;
	}

	/**
	 * 调用方法前建议在外面对队列加锁
	 * @param queKey
	 * @param needNum
	 * @return
	 */
	private List<String> popFromQue(String queKey, int needNum,int scienceId) {
		List<String> players = new ArrayList<>();
		String player;
		for (int num = needNum; num > 0; num--) {
			player = lpopCanUsePlayerAndOrSerailFailProtect(queKey,scienceId);
			if (!StringUtils.isEmpty(player)) {
				players.add(player);
			} else {
				break;
			}
		}
		//个数不对还原回去

		if (players.size() != needNum) {
			for (String playerIdx : players) {
				jedis.lpush(queKey, playerIdx);
			}
			return Collections.emptyList();
		}
		return players;
	}

	private String lpopCanUsePlayerAndOrSerailFailProtect(String queKey, int scienceId) {
		while (true){
			String player = jedis.lpop(queKey);
			if (StringUtils.isEmpty(player)){
				return null;
			}
			if (isRobot(player)){
				return player;
			}
			int robotDifficult = tiggerSeailFailProtect(player, scienceId);
			if (robotDifficult >= 0) {
				CrossArenaCreateProtectLeiTTableEvent event = new CrossArenaCreateProtectLeiTTableEvent();
				event.setPlayerIdx(player);
				event.setScienceId(scienceId);
				event.setRobotDifficult(robotDifficult);
				EventManager.getInstance().dealCrossArenaEvent(event, -1);
			}else {
				return player;
			}
		}
	}

	private int tiggerSeailFailProtect(String player, int scienceId) {
		String val = jedis.hget(CrossArenaPlayerSerialFail, player);
		if (val == null) {
			return -1;
		}
		if (cantMatchProtectRobot.contains(player)){
			cantMatchProtectRobot.remove(player);
			return -1;
		}
		RedisCrossArenaPlayer dbPlayer = getPlayerInfoRedis(player, null);
		if (dbPlayer == null) {
			return -1;
		}
		return matchProtectAiLevel(scienceId, dbPlayer, Integer.parseInt(val), false);
	}

	/**
	 * @param tableDB 大队列拉取玩家战斗
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder queBigJionTable(RedisCrossArenaTableDB tableDB) throws Exception {
		// 首先拉取总队列数据
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		int sceneId = casoCfg.getId();
		int robotLeve = matchProtectAiLevel(sceneId, tableDB.getDefPlayer(), tableDB.getDefWinNum(), true);
		String py1 = null;
		if (robotLeve >= 0) {
			py1 = queProtectRobotJoinTable(sceneId, robotLeve);
			if (StringUtils.isEmpty(py1)) {
				seedGSNeedProtectMatchAi(sceneId, robotLeve);
				Thread.sleep(600);
				{
					py1 = queProtectRobotJoinTable(sceneId, robotLeve);
				}
			}
		}
		String queKey = RedisKey.CrossArenaQue + "" + sceneId;
		String queKeyLock = RedisKey.CrossArenaQueLock + "" + sceneId;
		// 队列加锁
		if (!JedisUtil.lockRedisKey(queKeyLock, 100l)) {
			return null;
		}
		if (StringUtils.isEmpty(py1)) {
			// 队列非空
			List<String> player = popFromQue(queKey, 1, sceneId);
			if (player.size() < 1) {
				JedisUtil.unlockRedisKey(queKeyLock);
				return null;
			}
			py1 = player.get(0);
		}

		// 弹出一个玩家加入战斗
		RedisCrossArenaPlayer plInfoDB = getPlayerInfoRedis(py1, tableDB);
		if (null == plInfoDB) {
			JedisUtil.unlockRedisKey(queKeyLock);
			return null;
		}
		if (jedis.hexists(RedisKey.CrossArenaTempWinCot, py1)) {
			String def = jedis.hget(RedisKey.CrossArenaTempWinCot, py1);
			plInfoDB = plInfoDB.toBuilder().setDefNum(NumberUtils.toInt(def, 0)).build();
		}
		RedisCrossArenaTableDB.Builder newtableDB = addTablePlayer(tableDB, plInfoDB, false);
		saveLtDataToCache(newtableDB);
		updataPlyTableIdRedis(py1, "" + tableDB.getLeitaiId());
		JedisUtil.unlockRedisKey(queKeyLock);

//		if (plInfoDB.getIsAI() > 0) {
//			seedGSNeedAi(tableDB.getLeitaiId());
//		}
		return newtableDB;
	}

	private String queProtectRobotJoinTable(int sceneId, int robotDifficult) {
		if (!JedisUtil.lockRedisKey(getProtectRobotLock(sceneId, robotDifficult), 100l)) {
			return null;
		}
		String queKey = getProtectRobotQueKey(sceneId, robotDifficult);
		// 队列非空
		List<String> players = popFromQue(queKey, 1, sceneId);
		if (players.size() < 1) {
			JedisUtil.unlockRedisKey(queKey);
			return null;
		}
		String playerIdx = players.get(0);
		JedisUtil.unlockRedisKey(getProtectRobotLock(sceneId, robotDifficult));
		return playerIdx;
	}

	private String getProtectRobotQueKey(int sceneId, int robotDifficult) {
		return RedisKey.CrossArenaProtectRobotQue + sceneId + "-" + robotDifficult;
	}

	private String getProtectRobotLock(int sceneId, int robotDifficult) {
		return RedisKey.CrossArenaProtectRobotQueLock + sceneId + "-" + robotDifficult;
	}

	private int matchProtectAiLevel(int sceneId, RedisCrossArenaPlayer defPlayer, int num, boolean win) {
		if (defPlayer==null||isRobot(defPlayer.getPlayerId())){
			return -1;
		}

		CrossArenaSceneObject cfg = CrossArenaScene.getById(sceneId);
		if (cfg == null) {
			return -1;
		}
		int aiAbilityBase = cfg.getAiabilitybase();

		List<CrossArenaRobotRuleObject> checkCfg = win ? CrossArenaRobotRule.getInstance().getWinList()
				: CrossArenaRobotRule.getInstance().getFailList();

		for (CrossArenaRobotRuleObject robotRuleCfg :checkCfg) {
			if (robotRuleCfgMatch(defPlayer.getPower(), aiAbilityBase, robotRuleCfg, num, win)) {
				return robotRuleCfg.getAilevel();
			}
		}
		return -1;
	}

	private boolean robotRuleCfgMatch(long playerAbility, int aiAbilityBase, CrossArenaRobotRuleObject robotRuleCfg, int num, boolean win) {
		if ((robotRuleCfg.getResult() == 1) != win || num != robotRuleCfg.getNum()) {
			return false;
		}
		if (robotRuleCfg.getPlayerabilityl() != -1 && playerAbility < (aiAbilityBase * (robotRuleCfg.getPlayerabilityl() / 1000.0))) {
			return false;
		}
		if (robotRuleCfg.getPlayerabilityh() != -1 && playerAbility >= (aiAbilityBase * (robotRuleCfg.getPlayerabilityh() / 1000.0))) {
			return false;
		}
		if (robotRuleCfg.getPro() == 1000) {
			return true;
		}
		return RandomUtils.nextInt(1000) < robotRuleCfg.getPro();
	}

	public RedisCrossArenaPlayer getPlayerInfoRedis(String playerId, RedisCrossArenaTableDB tableDB) {
		try {
			String redisMainKey = RedisKey.CrossArenaPlayerInfo;
			if (playerId.contains(AINAME)) {
//				RedisCrossArenaPlayer.Builder msg = RedisCrossArenaPlayer.newBuilder();
//				msg.setPlayerId(playerId);
//				msg.setDefNum(tableDB.getDefWinNum());
//				msg.setIsAI(1);
//				return msg.build();
				redisMainKey = RedisKey.CrossArenaRBPlayerInfo;
			}
			byte[] plInfo = jedis.hget(redisMainKey.getBytes(), playerId.getBytes());
			if (null == plInfo) {
				return null;
			}
			RedisCrossArenaPlayer plInfoDB = RedisCrossArenaPlayer.parseFrom(plInfo);
			if (null == plInfoDB) {
				return null;
			}
			return plInfoDB;
		} catch (Exception e) {
			return null;
		}
	}


	private void seedGSNeedNormalMatchAi(int sceneId, String playerId) {
		ServerTransfer.BS_GS_CrossArenaNeedAI.Builder send = ServerTransfer.BS_GS_CrossArenaNeedAI.newBuilder();
		send.setTableId(sceneId);
		send.setPlayerId(playerId);
		WarpServerManager.getInstance().sendMsgToRandomServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, MessageId.MsgIdEnum.BS_GS_CrossArenaNeedAI_VALUE, send);
	}

	private void seedGSNeedProtectMatchAi(int sceneId, int difficult) {
		String robotIdx = AINAME + "-" + protectRobot * 100 + difficult + "-" + IdGenerator.getInstance().generateId();
		ServerTransfer.BS_GS_CrossArenaNeedAI.Builder send = ServerTransfer.BS_GS_CrossArenaNeedAI.newBuilder();
		send.setTableId(sceneId);
		send.setPlayerId(robotIdx);
		send.setDifficult(difficult);
		send.setUseType(protectRobot);
		WarpServerManager.getInstance().sendMsgToRandomServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, MessageId.MsgIdEnum.BS_GS_CrossArenaNeedAI_VALUE, send);
	}

	private void removeAi(String playerId) {

		jedis.hdel(RedisKey.CrossArenaRBPlayerInfo.getBytes(), playerId.getBytes());
	}

	/**
	 * @param tableDB 攻擂战斗胜利
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder tbRedisBatWinAtt(RedisCrossArenaTableDB tableDB) {
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
		// 超时，设置为等待状态,设置最长等待时间
		updataPlyTableIdRedis(tableDB.getDefPlayer().getPlayerId(), "");
		// 攻击方强制守擂
		if (isTenWin()) {
			int lastDefNum = tableDB.getAttPlayer().getDefNum();
			Integer protectWinNum = getProtectWinNum(tableDB.getAttPlayer().getPlayerId());
			lastDefNum = protectWinNum == null ? lastDefNum : protectWinNum;
			int defNum = lastDefNum + 1;
			newtableDB.setDefPlayer(tableDB.getAttPlayer().toBuilder().setDefNum(defNum).build());
			newtableDB.setDefWinNum(defNum);
			if (defNum >= 10) {
				addTenWin(tableDB.getAttPlayer().getPlayerId(),getSceneIdByTableId(tableDB.getLeitaiId()));
			}
		} else {
			newtableDB.setDefPlayer(tableDB.getAttPlayer().toBuilder().setDefNum(0).build());
			newtableDB.setDefWinNum(0);
		}
		newtableDB.setDefTime(System.currentTimeMillis());
		newtableDB.clearAttPlayer();
		newtableDB.clearGuessAttSvrData();
		newtableDB.clearGuessDefSvrData();
		newtableDB.setSettleTime(0);
		newtableDB.setBattleId(0);
		newtableDB.setLastBattleTime(System.currentTimeMillis());
		newtableDB.setState(WAIT_VALUE);
		newtableDB.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newtableDB.addAllReadyState(readyList);
		newtableDB.setStateEndTime(System.currentTimeMillis() + casoCfg.getRevoketime() * 1000L);
		return newtableDB;
	}

	private Integer getProtectWinNum(String playerId) {
		String hget = jedis.hget(CrossArenaProtectCard, playerId);
		return hget == null ? null : Integer.parseInt(hget);
	}

	/**
	 * 是否10连胜
	 *
	 * @return
	 */
	private boolean isTenWin() {
		String time = jedis.get(RedisKey.CrossArenaTen);
		if (StringHelper.isNull(time)) {
			return false;
		}
		return GlobalTick.getInstance().getCurrentTime() < NumberUtils.toLong(time);
	}

	/**
	 * @param tableDB 守擂战斗胜利
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder tbRedisBatWinDef(RedisCrossArenaTableDB tableDB) {
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
		// 超时，设置为等待状态,设置最长等待时间
		updataPlyTableIdRedis(tableDB.getAttPlayer().getPlayerId(), "");
		// 攻击方强制守擂
		if (isTenWin()) {
			int defNum = tableDB.getDefPlayer().getDefNum() + 1;
			newtableDB.setDefPlayer(tableDB.getDefPlayer().toBuilder().setDefNum(defNum).build());
			newtableDB.setDefWinNum(defNum);
			if (defNum >= 10) {
				addTenWin(tableDB.getDefPlayer().getPlayerId(),getSceneIdByTableId(tableDB.getLeitaiId()));
			}
		} else {
			newtableDB.setDefPlayer(tableDB.getDefPlayer().toBuilder().setDefNum(0).build());
			newtableDB.setDefWinNum(0);
		}
		newtableDB.clearAttPlayer();
		newtableDB.clearGuessAttSvrData();
		newtableDB.clearGuessDefSvrData();
		newtableDB.setSettleTime(0);
		newtableDB.setBattleId(0);
		newtableDB.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newtableDB.addAllReadyState(readyList);
		newtableDB.setLastBattleTime(System.currentTimeMillis());
		newtableDB.setState(WAIT_VALUE);
		newtableDB.setStateEndTime(System.currentTimeMillis() + casoCfg.getRevoketime() * 1000L);
		return newtableDB;
	}

	/**
	 * @param tableDB 回收擂台
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder tbRedisRecoveryTable(RedisCrossArenaTableDB tableDB) {
		int sceneId = getSceneIdByTableId(tableDB.getLeitaiId());
		if (null == tableDB.getDefPlayer()) {
			return clearTable(tableDB);
		}
		// 拿到擂台锁，大队列锁，修改数据
		if (tableDB.getDefPlayer().getIsAI() <= 0) {
			if (jedis.hexists(RedisKey.CrossArenaPlOnline, tableDB.getDefPlayer().getPlayerId())) {
				addQueRedis(sceneId, tableDB.getDefPlayer().getPlayerId(),false);
				jedis.hset(RedisKey.CrossArenaTempWinCot, tableDB.getDefPlayer().getPlayerId(), "" + tableDB.getDefWinNum());
			}
		}
		RedisCrossArenaTableDB.Builder newtableDB = clearTable(tableDB);
		updataPlyTableIdRedis(tableDB.getDefPlayer().getPlayerId(), "");
		return newtableDB;
	}

	/**
	 * @param tableDB
	 * @return 将自身队列中得数据拉入战斗
	 */
	private RedisCrossArenaTableDB.Builder tbRedisQueJionAttTable(RedisCrossArenaTableDB tableDB) {
		if (tableDB.getDuiList().isEmpty()) {
			return null;
		}
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
		List<RedisCrossArenaPlayer> temp = new ArrayList<>(tableDB.getDuiList());
		temp.sort((o1, o2) -> {
			if (o1.getJionTime() > o2.getJionTime()) {
				return 1;
			} else {
				return -1;
			}
		});
		RedisCrossArenaPlayer att = temp.remove(0);
		newtableDB.setAttPlayer(att);
		newtableDB.clearDui();
		newtableDB.addAllDui(temp);
		newtableDB.setState(CrossArenaState.READY_VALUE);
		newtableDB.setStateEndTime(getReadyTime(newtableDB,casoCfg));
		newtableDB.clearGuessDefSvrData().clearGuessAttSvrData();
		newtableDB.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newtableDB.addAllReadyState(readyList);
		updataPlyTableIdRedis(att.getPlayerId(), "" + newtableDB.getLeitaiId());
		noticeReadyFight(newtableDB.build());
		return newtableDB;
	}

	private long getReadyTime(RedisCrossArenaTableDB.Builder newtableDB, CrossArenaSceneObject casoCfg) {
		boolean hasAI = newtableDB.getDefPlayer() != null && newtableDB.getDefPlayer().getIsAI() > 0 || newtableDB.getAttPlayer() != null && newtableDB.getAttPlayer().getIsAI() > 0;
		if (hasAI) {
			//机器人先写死5秒,催着打包没时间写配置了
			return GlobalTick.getInstance().getCurrentTime() + casoCfg.getAireadytime() * 1000L;
		}
		return GlobalTick.getInstance().getCurrentTime() + casoCfg.getReadytime() * 1000L;
	}

	public void noticeReadyFight(RedisCrossArenaTableDB tableDb) {
		String attPlayer = tableDb.getAttPlayer().getPlayerId();
		String defPlayer = tableDb.getDefPlayer().getPlayerId();
		int attFromSvrIndex = tableDb.getAttPlayer().getSvrIndex();
		if (attFromSvrIndex <= 0) { // 兼容代码
			attFromSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(tableDb.getAttPlayer().getFormIpPort());
		}
		int defFromSvrIndex = tableDb.getDefPlayer().getSvrIndex();
		if (defFromSvrIndex <= 0) { // 兼容代码
			defFromSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(tableDb.getDefPlayer().getFormIpPort());
		}

		long endTime = tableDb.getStateEndTime();
		BS_GS_CrossArenaReadyFight.Builder builder = BS_GS_CrossArenaReadyFight.newBuilder();
		builder.setEndtime(endTime);
		builder.setPlayerId(attPlayer);
		builder.addAllState(tableDb.getReadyStateList());
		builder.setAtt(1);
		if (!attPlayer.contains(AINAME)) {
			WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, attFromSvrIndex, MsgIdEnum.BS_GS_CrossArenaReadyFight_VALUE, builder);
		}
		builder.setPlayerId(defPlayer);
		builder.setAtt(0);
		if (!defPlayer.contains(AINAME)) {
			WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, defFromSvrIndex, MsgIdEnum.BS_GS_CrossArenaReadyFight_VALUE, builder);
		}
	}

	/**
	 * @param tableDB 战斗超时，容错处理，移除攻击者
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder tbRedisQuitAtt(RedisCrossArenaTableDB tableDB) {
		if (tableDB.getState() != CrossArenaState.READY_VALUE) {
			return null;
		}
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
		// 超时，设置为等待状态,设置最长等待时间
		if (null != tableDB.getAttPlayer()) {
			updataPlyTableIdRedis(tableDB.getAttPlayer().getPlayerId(), "");
		}
		newtableDB.setState(WAIT_VALUE);
		newtableDB.setStateEndTime(System.currentTimeMillis() + casoCfg.getRevoketime() * 1000L);
		newtableDB.clearAttPlayer();
		newtableDB.clearGuessAttSvrData().clearGuessDefSvrData();
		newtableDB.setSettleTime(0);
		newtableDB.setBattleId(0);
		newtableDB.setLastBattleTime(System.currentTimeMillis());
		newtableDB.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newtableDB.addAllReadyState(readyList);
		return newtableDB;
	}

	/**
	 * @param tableDB 擂主退出擂台
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder tbRedisQuitDef(RedisCrossArenaTableDB tableDB) {
		RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
		// 超时，设置为等待状态,设置最长等待时间
		updataPlyTableIdRedis(tableDB.getDefPlayer().getPlayerId(), "");
		newtableDB.setState(CrossArenaState.IDLE_VALUE);
		newtableDB.setStateEndTime(0);
		newtableDB.clearDefPlayer();
		newtableDB.clearAttPlayer();
		newtableDB.clearGuessAttSvrData().clearGuessDefSvrData();
		newtableDB.setSettleTime(0);
		newtableDB.setBattleId(0);
		newtableDB.setLastBattleTime(System.currentTimeMillis());
		newtableDB.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newtableDB.addAllReadyState(readyList);
		return newtableDB;
	}

	/**
	 * @param tableDB 战斗超时，容错处理，移除攻击者
	 * @return
	 */
	private RedisCrossArenaTableDB.Builder tbRedisFightOverTime(RedisCrossArenaTableDB tableDB) {
		CrossArenaSceneObject casoCfg = getSceneCfg(tableDB.getLeitaiId());
		if (null == casoCfg) {
			return null;
		}
		RedisCrossArenaTableDB.Builder newtableDB = tableDB.toBuilder();
		// 超时，设置为等待状态,设置最长等待时间
		updataPlyTableIdRedis(tableDB.getAttPlayer().getPlayerId(), "");
		newtableDB.setState(WAIT_VALUE);
		newtableDB.setStateEndTime(System.currentTimeMillis() + casoCfg.getRevoketime() * 1000L);
		newtableDB.clearAttPlayer();
		newtableDB.clearGuessAttSvrData().clearGuessDefSvrData();
		newtableDB.setSettleTime(0);
		newtableDB.setBattleId(0);
		newtableDB.setLastBattleTime(System.currentTimeMillis());
		newtableDB.clearReadyState();
		List<Integer> readyList = new ArrayList<>();
		readyList.add(0);
		readyList.add(0);
		newtableDB.addAllReadyState(readyList);
		return newtableDB;
	}

	/**
	 * @param leitaiId
	 * @return 创建单个擂台数据key
	 */
	public String createRedisKeyLT(int leitaiId) {
		return RedisKey.CrossArenaData + leitaiId;
	}

	public String getIpPort() {
		String ipPort = ServerConfig.getInstance().getIp() + ":" + ServerConfig.getInstance().getPort();
		return ipPort;
	}

	public static final int TableBase = 10000;

	/**
	 * @param sceneId
	 * @param idx
	 * @return 生成擂台ID
	 */
	public int cretaeTableIdId(int sceneId, int idx) {
		return sceneId * TableBase + idx;
	}

	public int getSceneIdByTableId(int tableId) {
		return tableId / TableBase;
	}

	public static float getRandomFloat(float min, float max) {
		if (max < min) {
			throw new IllegalArgumentException("max must greater than min");
		}
		float seed = max - min;
		float hit = random.nextFloat() * seed;
		DecimalFormat dcmFmt = new DecimalFormat("0.000");
		String r = dcmFmt.format(hit + min);
		return Float.valueOf(r);
	}

	public void createProtectLeiTTable(int scienceId, String playerIdx, int robotDifficult) {
		if (!JedisUtil.lockRedisKey(CrossArenaCreateTableLock +scienceId, 3000l)) {
			//放玩家回去
			putBackPlayerToQue(scienceId, playerIdx);
			return;
		}
		Integer tableId = findCanUseTableId(scienceId);
		if (tableId == null) {
			JedisUtil.unlockRedisKey(CrossArenaCreateTableLock +scienceId);
			//放玩家回去
			putBackPlayerToQue(scienceId, playerIdx);
			return;
		}

		String robotPlayerIdx = pullOneProtectRobot(scienceId, robotDifficult);
		if (StringUtils.isEmpty(robotPlayerIdx)) {
			JedisUtil.unlockRedisKey(CrossArenaCreateTableLock +scienceId);
			cantMatchProtectRobot.add(playerIdx);
			LogUtil.warn("player:" + playerIdx + " cant`t match protect Robot ,scienceId:" + scienceId
					+ ",robotDifficult:" + robotDifficult);
			putBackPlayerToQue(scienceId,playerIdx);

			seedGSNeedProtectMatchAi(scienceId,robotDifficult);
			return;
		}
		RedisCrossArenaPlayer defendPlayer = getPlayerInfoRedis(playerIdx, null);
		if (defendPlayer == null) {
			JedisUtil.unlockRedisKey(CrossArenaCreateTableLock +scienceId);
			return;
		}
		RedisCrossArenaPlayer robot = getPlayerInfoRedis(robotPlayerIdx, null);
		if (robot == null) {
			JedisUtil.unlockRedisKey(CrossArenaCreateTableLock +scienceId);
			putBackPlayerToQue(scienceId, playerIdx);
			return;
		}
		// 初始化擂台数据
		RedisCrossArenaTableDB.Builder tableData = RedisCrossArenaTableDB.newBuilder();
		tableData.setLeitaiId(tableId);
		tableData.setType(TempLeiT);
		tableData.setState(READY_VALUE);
		tableData.setDefPlayer(defendPlayer);
		RedisCrossArenaTableDB.Builder newtableDB = addTablePlayer(tableData.build(), robot, true);
		tableData.setStateEndTime(GlobalTick.getInstance().getCurrentTime());
		// 给擂台分配逻辑服
		String serverIndex = StringHelper.IntTostring(ServerConfig.getInstance().getServer(), "");
		saveNewTable(scienceId, tableId, newtableDB, serverIndex);
		updataPlyTableIdRedis(playerIdx, "" + tableId);
		JedisUtil.unlockRedisKey(CrossArenaCreateTableLock +scienceId);
	}

	private void saveNewTable(int scienceId, Integer tableId, RedisCrossArenaTableDB.Builder newtableDB, String serverIndex) {
		jedis.hset(getTableServerKey(tableId), "" + tableId, serverIndex);
		jedis.hset(RedisKey.CrossArenaTableNum, "" + scienceId, "" + findNowTableNum(scienceId));
		// 存储单个擂台数据至redis
		saveLtDataToCache(newtableDB);
	}

	/**
	 * 放玩家回排队队列
	 * @param scienceId
	 * @param playerIdx
	 */
	private void putBackPlayerToQue(int scienceId, String playerIdx) {
		String queKey = RedisKey.CrossArenaQue + "" + scienceId;
		jedis.lpush(queKey, playerIdx);
	}

	private String pullOneProtectRobot(int scienceId, int robotDifficult) {
		String queKey = getProtectRobotQueKey(scienceId, robotDifficult);
		return jedis.lpop(queKey);
	}

	private static final int maxTableIndex =200;

	private static final int TempLeiT = 1;

	private Integer findCanUseTableId(int scienceId) {
		Set<String> hkeys = jedis.hkeys(RedisKey.CrossArenaBSSid + scienceId);
		for (int i = 1; i < maxTableIndex; i++) {
			int newTableId = cretaeTableIdId(scienceId, i);
			if (!hkeys.contains(String.valueOf(newTableId))) {
				return newTableId;
			}
		}
		return null;
	}
}
