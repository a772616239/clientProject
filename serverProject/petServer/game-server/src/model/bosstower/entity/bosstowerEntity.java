/**
 * created by tool DAOGenerate
 */
package model.bosstower.entity;

import java.util.Map.Entry;

import com.google.protobuf.InvalidProtocolBufferException;

import cfg.BossTowerConfig;
import cfg.BossTowerConfigObject;
import cfg.GameConfig;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.GameConst;
import common.GlobalData;
import model.bosstower.dbCache.bosstowerCache;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.obj.BaseObj;
import model.player.util.PlayerUtil;
import protocol.BossTower.BossTowerPassCondition;
import protocol.BossTower.SC_ClaimBossTowerInfo;
import protocol.BossTower.SC_RefreshBossTower;
import protocol.BossTowerDB.DB_BossTower;
import protocol.BossTowerDB.DB_BossTowerPassCondition;
import protocol.BossTowerDB.DB_BossTowerPassCondition.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.bossTower.ClaimBossTowerInfoHandler;
import util.GameUtil;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class bosstowerEntity extends BaseObj {

	public String getClassType() {
		return "bosstowerEntity";
	}

	@Override
	public void putToCache() {
		bosstowerCache.put(this);
	}

	@Override
	public void transformDBData() {
		this.info = getDbBuilder().build().toByteArray();
	}

	/**
	 *
	 */
	private String playeridx;

	/**
	 *
	 */
	private byte[] info;

	/**
	 * 获得
	 */
	public String getPlayeridx() {
		return playeridx;
	}

	/**
	 * 设置
	 */
	public void setPlayeridx(String playeridx) {
		this.playeridx = playeridx;
	}

	/**
	 * 获得
	 */
	public byte[] getInfo() {
		return info;
	}

	/**
	 * 设置
	 */
	public void setInfo(byte[] info) {
		this.info = info;
	}

	private bosstowerEntity() {
	}

	public bosstowerEntity(String playerIdx) {
		this.playeridx = playerIdx;
	}

	public String getBaseIdx() {
		// TODO Auto-generated method stub
		return playeridx;
	}

	private DB_BossTower.Builder DbBuilder;

	public DB_BossTower.Builder getDbBuilder() {
		if (this.DbBuilder == null) {
			try {
				if (this.info == null) {
					this.DbBuilder = DB_BossTower.newBuilder();
				} else {
					this.DbBuilder = DB_BossTower.parseFrom(this.info).toBuilder();
				}
			} catch (InvalidProtocolBufferException e) {
				LogUtil.printStackTrace(e);
				this.DbBuilder = DB_BossTower.newBuilder();
			}
		}
		return DbBuilder;
	}

	/**
	 * 根据关卡id获取通关情况, 通过此方法获取的build 修改数据后，需要调用
	 *
	 * @param cfgId
	 * @return
	 */
	public DB_BossTowerPassCondition.Builder getBossTowerPassConditionBuilder(int cfgId) {
		DB_BossTowerPassCondition condition = getDbBuilder().getPassMap().get(cfgId);
		if (condition != null) {
			return condition.toBuilder();
		} else {
			return DB_BossTowerPassCondition.newBuilder().setConfigId(cfgId);
		}
	}

	public void putBossTowerPassCondition(DB_BossTowerPassCondition newCondition) {
		if (newCondition == null) {
			return;
		}
		getDbBuilder().putPass(newCondition.getConfigId(), newCondition);

		refreshToClient(newCondition);
	}

	public void refreshToClient(DB_BossTowerPassCondition condition) {
		if (condition == null) {
			return;
		}

		BossTowerPassCondition clientCondition = ClaimBossTowerInfoHandler.parseToClientCondition(condition);
		if (condition == null) {
			return;
		}
		SC_RefreshBossTower.Builder builder = SC_RefreshBossTower.newBuilder();
		builder.setNewCondition(clientCondition);
		builder.setTodayAlreadyChallengeTimes(getDbBuilder().getTodayAlreadyChallengeTimes());
		builder.setTodayVipBuyTime(getDbBuilder().getTodayVipBuyTime());
		builder.setItemBuyTime(getDbBuilder().getTodayItemBuy());
		GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_RefreshBossTower_VALUE, builder);
	}

	public void updateDailyData(boolean sendMsg) {
		for (Entry<Integer, DB_BossTowerPassCondition> ent : getDbBuilder().getPassMap().entrySet()) {
			Builder builder = ent.getValue().toBuilder();
			for (Entry<Integer, Integer> e : builder.getPassInfoMap().entrySet()) {
				builder.putPassInfo(e.getKey(), 0);
			}
			getDbBuilder().putPass(ent.getKey(), builder.build());
		}
		getDbBuilder().clearTodayAlreadyChallengeTimes();
		getDbBuilder().clearTodayVipUseTime();
		getDbBuilder().clearTodayItemBuy();
		getDbBuilder().clearTodayVipBuyTime();
		
		// 刷新
		if (sendMsg) {
			SC_ClaimBossTowerInfo.Builder builder = SC_ClaimBossTowerInfo.newBuilder()
					.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success)).addAllPassCondition(
							ClaimBossTowerInfoHandler.parseToClientConditionList(getDbBuilder().getPassMap().values()));
			builder.setTodayVipBuyTime(getDbBuilder().getTodayVipBuyTime());
			builder.setTodayVipUseTime(getDbBuilder().getTodayVipUseTime());
			builder.setItemBuyTime(getDbBuilder().getTodayItemBuy());
			GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_ClaimBossTowerInfo_VALUE, builder);
		}
	}

	public boolean canBattle(int cfgId, int fightMakeId) {

		BossTowerConfigObject towerCfg = BossTowerConfig.getById(cfgId);
		if (towerCfg == null) {
			return false;
		}
		// 每日基础总次数
		int dayLimit = towerCfg.getPasslimit();
		// 今日挑战次数
		int totalTime = getDbBuilder().getTodayAlreadyChallengeTimes();

		boolean firstPass = isFirstPass(cfgId, fightMakeId);
		int vipOverTime = getVipOverTime();
		if (!firstPass && totalTime >= dayLimit) {
			if (vipOverTime > 0) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	public int getItemNum() {
		itembagEntity bag = itembagCache.getInstance().getItemBagByPlayerIdx(getPlayeridx());
		if (bag == null) {
			return 0;
		}
		return (int) bag.getItemCount(GameConst.ITEM_ID_BOSSTOWER_SWEEP);
	}

	public int getVipOverTime() {
		int myVipBuyTime = getDbBuilder().getTodayVipBuyTime();
		int myItemBuyTime = getDbBuilder().getTodayItemBuy();
		int myVipUseTime = getDbBuilder().getTodayVipUseTime();

		// 剩余购买未挑战次数
		int vipOverTime = myVipBuyTime + myItemBuyTime- myVipUseTime;
		if (vipOverTime <= 0) {
			vipOverTime = 0;
		}
		return vipOverTime;
	}

	public void addBattleTimes(int cfgId, int fightMakeId, int battleStar, boolean add) {

		// 修改单个难度的挑战记录
		DB_BossTowerPassCondition.Builder builder = getBossTowerPassConditionBuilder(cfgId);

		int todayAlreadyChallengeTimes = getDbBuilder().getTodayAlreadyChallengeTimes();
		int todayTotalLimit = 0;
		VIPConfigObject vipCfg = VIPConfig.getById(PlayerUtil.queryPlayerVipLv(getPlayeridx()));
		if (vipCfg != null) {
			todayTotalLimit = vipCfg.getBosstowerchallengetimes();
		}
		if (todayAlreadyChallengeTimes >= todayTotalLimit) {// 今天已经挑战次数满了,有额外次数扣额外次数
			if (add) {
				int vipOverTime = getVipOverTime();
				if (vipOverTime > 0) {
					getDbBuilder().setTodayVipUseTime(getDbBuilder().getTodayVipUseTime() + 1);
				}
			}
		}
		boolean first = isFirstPass(cfgId, fightMakeId);
		if (first) {
			builder.putPassInfo(fightMakeId, 0);
		} else {
			builder.putPassInfo(fightMakeId, builder.getPassInfoMap().getOrDefault(fightMakeId, 0) + 1);
		}

		if (!builder.getCanSweepIdList().contains(cfgId)) {
			builder.addCanSweepId(fightMakeId);
		}
		int maxCfgId = getDbBuilder().getMaxCfgId();
		if (cfgId > maxCfgId) {
			getDbBuilder().setMaxCfgId(cfgId);
		}

		if (add) {
			getDbBuilder().setTodayAlreadyChallengeTimes(getDbBuilder().getTodayAlreadyChallengeTimes() + 1);
		}
		putBossTowerPassCondition(builder.build());
	}

	public int getCanBattleTime(int cfgId) {
		DB_BossTowerPassCondition.Builder builder = getBossTowerPassConditionBuilder(cfgId);

		BossTowerConfigObject cfg = BossTowerConfig.getById(cfgId);
		if (cfg != null) {
			int passTime = builder.getPassInfoOrDefault(cfg.getUnbeatablefightmakeid(), 0);
			int otherTime = getVipOverTime();
			int totalTime = cfg.getPasslimit() + otherTime;
			if (passTime >= totalTime) {
				return 0;
			} else {
				return totalTime - passTime;
			}
		} else {
			return 0;
		}

	}

	public void addBuyTimes(int cfgId, int time,boolean item) {
		DB_BossTowerPassCondition.Builder builder = getBossTowerPassConditionBuilder(cfgId);
		if(item) {
			getDbBuilder().setTodayItemBuy(getDbBuilder().getTodayItemBuy() + time);
		}else {
			getDbBuilder().setTodayVipBuyTime(getDbBuilder().getTodayVipBuyTime() + time);
		}
		putBossTowerPassCondition(builder.build());
	}

	// 首次通关
	public boolean isFirstPass(int cfgId, int fightMakeId) {
		DB_BossTowerPassCondition.Builder builder = getBossTowerPassConditionBuilder(cfgId);

		if (builder.getPassInfoMap().containsKey(fightMakeId)) {// 曾经有记录过则胜利过
			return false;
		}
		return true;
	}

}